#!/usr/bin/env python3
"""Purpose: Build a deterministic Markdown report of IdP metadata changes in a pull request.

For every IdP metadata XML added or modified between two git refs, the script compares
each IdP (keyed by entityID) against every metadata file of the same type already present
on the base ref (root, prod/, io-prod/, ... excluding test/). Signature-related noise
(XML signatures, ID attributes, aggregate publication info) is ignored.

Only the certificates that changed (new IdPs or new certificate values) are checked:
an IdP is reported when none of its changed certificates is currently valid.

Usage examples:
  python .github/scripts/idp_metadata_diff.py --base-ref origin/main --head-ref HEAD
  python .github/scripts/idp_metadata_diff.py --base-ref "$BASE_SHA" --head-ref "$HEAD_SHA" --output comment.md
  python .github/scripts/idp_metadata_diff.py --base-ref origin/main --head-ref HEAD --reference-date 2030-01-01
"""

from __future__ import annotations

import argparse
import base64
import binascii
import functools
import hashlib
import re
import subprocess
import sys
import textwrap
import xml.etree.ElementTree as ET
from collections import Counter
from dataclasses import dataclass, field
from datetime import datetime, timezone
from itertools import zip_longest
from pathlib import PurePosixPath

COMMENT_MARKER = "<!-- idp-metadata-diff -->"
DEFAULT_METADATA_DIR = "src/oneid/metadata"
EXCLUDED_METADATA_SUBDIRS = {"test"}
MAX_COMMENT_LENGTH = 60000

NS_DSIG = "http://www.w3.org/2000/09/xmldsig#"
NS_METADATA = "urn:oasis:names:tc:SAML:2.0:metadata"
IGNORED_ATTRIBUTES = {"ID"}
WRAPPER_TAGS = {"KeyInfo", "X509Data"}
PATH_QUALIFIER_ATTRIBUTES = {"KeyDescriptor": "use"}
DISPLAY_PREFIXES_TO_STRIP = ("urn:oasis:names:tc:SAML:2.0:bindings:",)

STATUS_ADDED = "added"
STATUS_REMOVED = "removed"
STATUS_MODIFIED = "modified"
STATUS_UNCHANGED = "unchanged"

STATUS_LABELS = {
    STATUS_ADDED: "🆕 Added",
    STATUS_REMOVED: "🗑️ Removed",
    STATUS_MODIFIED: "✏️ Modified",
    STATUS_UNCHANGED: "Unchanged",
}
STATUS_ORDER = [STATUS_MODIFIED, STATUS_ADDED, STATUS_REMOVED, STATUS_UNCHANGED]


@dataclass(frozen=True)
class MetadataFact:
    """A single comparable piece of information of an IdP entity."""

    path: str
    attributes: tuple[tuple[str, str], ...] = ()
    text: str = ""
    certificate: str | None = None

    def sort_key(self) -> tuple:
        return self.path, self.attributes, self.text


@dataclass
class IdpEntity:
    """An IdP entity extracted from a metadata file."""

    entity_id: str
    display_name: str
    facts: Counter = field(default_factory=Counter)


@dataclass
class EntityDiff:
    """The comparison result of a single IdP between baseline and candidate."""

    entity_id: str
    display_name: str
    status: str
    added_facts: list[MetadataFact] = field(default_factory=list)
    removed_facts: list[MetadataFact] = field(default_factory=list)


@dataclass
class FileComparison:
    """The comparison of a changed metadata file against one baseline file."""

    candidate_path: str
    baseline_path: str | None
    entity_diffs: list[EntityDiff]


@dataclass(frozen=True)
class CertificateInfo:
    """Details of an X.509 certificate read through openssl."""

    subject_label: str
    fingerprint: str
    not_before: datetime | None = None
    not_after: datetime | None = None
    error: str | None = None


@dataclass(frozen=True)
class CertificateFinding:
    """An IdP whose changed certificates are all invalid."""

    display_name: str
    entity_id: str
    problems: list[str]


@dataclass
class CertificateReport:
    """The validity check of the certificates changed in a candidate metadata file."""

    checked_count: int
    findings: list[CertificateFinding]


def log(message: str) -> None:
    print(message, file=sys.stderr)


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def normalize_text(text: str | None) -> str:
    if not text:
        return ""
    return " ".join(text.split())


def idp_type_of(path: str) -> str:
    """Return the IdP type of a metadata file (e.g. 'spid' for 'prod/spid-1764087002.xml')."""
    name = PurePosixPath(path).name
    return re.split(r"[-.]", name, maxsplit=1)[0]


def is_managed_metadata_file(path: str, metadata_dir: str) -> bool:
    """Tell whether a repository path is a OneIdentity-managed IdP metadata XML."""
    pure_path = PurePosixPath(path)
    if pure_path.suffix != ".xml":
        return False
    try:
        relative = pure_path.relative_to(metadata_dir)
    except ValueError:
        return False
    if len(relative.parts) > 1 and relative.parts[0] in EXCLUDED_METADATA_SUBDIRS:
        return False
    return True


def find_baseline_paths(candidate_path: str, base_paths: list[str], metadata_dir: str) -> list[str]:
    """Return every managed metadata file on the base ref sharing the candidate IdP type."""
    idp_type = idp_type_of(candidate_path)
    baselines = [
        path
        for path in base_paths
        if is_managed_metadata_file(path, metadata_dir) and idp_type_of(path) == idp_type
    ]
    return sorted(baselines)


@functools.lru_cache(maxsize=None)
def inspect_certificate(certificate_b64: str) -> CertificateInfo:
    """Read subject, validity window and SHA-256 fingerprint of a base64 DER certificate."""
    try:
        der = base64.b64decode(certificate_b64, validate=True)
    except (binascii.Error, ValueError):
        fingerprint = hashlib.sha256(certificate_b64.encode()).hexdigest()[:16]
        return CertificateInfo("<invalid certificate>", fingerprint, error="not valid base64")

    fingerprint = hashlib.sha256(der).hexdigest()[:16]
    pem_body = "\n".join(textwrap.wrap(certificate_b64, 64))
    pem = f"-----BEGIN CERTIFICATE-----\n{pem_body}\n-----END CERTIFICATE-----\n"
    try:
        result = subprocess.run(
            ["openssl", "x509", "-noout", "-subject", "-startdate", "-enddate", "-nameopt", "RFC2253"],
            input=pem,
            capture_output=True,
            text=True,
            check=True,
        )
    except OSError:
        return CertificateInfo("<unknown subject>", fingerprint, error="openssl not available")
    except subprocess.CalledProcessError:
        return CertificateInfo("<invalid certificate>", fingerprint, error="not a parsable X.509 certificate")

    fields: dict[str, str] = {}
    for line in result.stdout.splitlines():
        key, _, value = line.partition("=")
        fields[key.strip()] = value.strip()

    subject = fields.get("subject", "")
    common_name_match = re.search(r"(?:^|,)CN=((?:[^,\\]|\\.)*)", subject)
    subject_label = f"CN={common_name_match.group(1)}" if common_name_match else f"subject={subject or 'unknown'}"
    return CertificateInfo(
        subject_label=subject_label,
        fingerprint=fingerprint,
        not_before=parse_openssl_date(fields.get("notBefore", "")),
        not_after=parse_openssl_date(fields.get("notAfter", "")),
    )


def parse_openssl_date(value: str) -> datetime | None:
    try:
        return datetime.strptime(" ".join(value.split()), "%b %d %H:%M:%S %Y %Z").replace(tzinfo=timezone.utc)
    except ValueError:
        return None


def format_date(value: datetime | None) -> str:
    return value.strftime("%Y-%m-%d") if value else "unknown"


def describe_certificate(certificate_b64: str) -> str:
    """Summarize an X.509 certificate as subject, expiry date and short SHA-256 fingerprint."""
    info = inspect_certificate(certificate_b64)
    if info.error:
        return f"{info.subject_label}, sha256={info.fingerprint}"
    return f"{info.subject_label}, expires={format_date(info.not_after)}, sha256={info.fingerprint}"


def certificate_problem(info: CertificateInfo, reference_time: datetime) -> str | None:
    """Return why a certificate is not valid at the reference time, or None when it is valid."""
    if info.error:
        return f"Unreadable certificate ({info.error})"
    if info.not_before is None or info.not_after is None:
        return "Validity dates not readable"
    if reference_time < info.not_before:
        return f"Not valid before {format_date(info.not_before)}"
    if reference_time > info.not_after:
        return f"Expired on {format_date(info.not_after)}"
    return None


def element_attributes(element: ET.Element) -> tuple[tuple[str, str], ...]:
    path_qualifier = PATH_QUALIFIER_ATTRIBUTES.get(local_name(element.tag))
    attributes = {
        local_name(name): value
        for name, value in element.attrib.items()
        if local_name(name) not in IGNORED_ATTRIBUTES and local_name(name) != path_qualifier
    }
    return tuple(sorted(attributes.items()))


def path_segment(element: ET.Element) -> str:
    name = local_name(element.tag)
    qualifier = PATH_QUALIFIER_ATTRIBUTES.get(name)
    if qualifier and element.get(qualifier):
        return f"{name}[{qualifier}={element.get(qualifier)}]"
    return name


def extract_facts(entity: ET.Element) -> Counter:
    """Flatten an EntityDescriptor into comparable facts, skipping signature noise."""
    facts: Counter = Counter()
    root_attributes = element_attributes(entity)
    if root_attributes:
        facts[MetadataFact("EntityDescriptor", root_attributes)] += 1

    def walk(element: ET.Element, parent_path: str) -> None:
        for child in element:
            if not isinstance(child.tag, str):
                continue
            if child.tag == f"{{{NS_DSIG}}}Signature":
                continue

            name = local_name(child.tag)
            if name in WRAPPER_TAGS:
                walk(child, parent_path)
                continue

            child_path = f"{parent_path}/{path_segment(child)}" if parent_path else path_segment(child)
            attributes = element_attributes(child)
            if name == "X509Certificate":
                certificate = "".join((child.text or "").split())
                facts[MetadataFact(child_path, attributes, describe_certificate(certificate), certificate)] += 1
                continue

            text = normalize_text(child.text)
            has_children = any(isinstance(grandchild.tag, str) for grandchild in child)
            # Pure containers (e.g. Extensions, Organization) are represented by their children only.
            if attributes or text or not has_children:
                facts[MetadataFact(child_path, attributes, text)] += 1
            walk(child, child_path)

    walk(entity, "")
    return facts


def language_of(element: ET.Element) -> str:
    for name, value in element.attrib.items():
        if local_name(name) == "lang":
            return value
    return ""


def resolve_display_name(entity: ET.Element) -> str:
    for tag in ("OrganizationDisplayName", "OrganizationName"):
        candidates = entity.findall(f"{{{NS_METADATA}}}Organization/{{{NS_METADATA}}}{tag}")
        if not candidates:
            continue
        preferred = sorted(candidates, key=lambda item: language_of(item) != "it")[0]
        name = normalize_text(preferred.text)
        if name and not name.startswith("http"):
            return name
    return entity.get("entityID", "unknown")


def parse_entities(xml_content: bytes) -> dict[str, IdpEntity]:
    """Parse a metadata document (EntitiesDescriptor or EntityDescriptor) into IdP entities."""
    root = ET.fromstring(xml_content)
    if local_name(root.tag) == "EntityDescriptor":
        descriptors = [root]
    else:
        descriptors = [
            child for child in root if isinstance(child.tag, str) and local_name(child.tag) == "EntityDescriptor"
        ]

    entities: dict[str, IdpEntity] = {}
    for descriptor in descriptors:
        entity_id = descriptor.get("entityID", "unknown")
        entities[entity_id] = IdpEntity(
            entity_id=entity_id,
            display_name=resolve_display_name(descriptor),
            facts=extract_facts(descriptor),
        )
    return entities


def sorted_facts(facts: Counter) -> list[MetadataFact]:
    return sorted(facts.elements(), key=MetadataFact.sort_key)


def compare_entities(baseline: dict[str, IdpEntity], candidate: dict[str, IdpEntity]) -> list[EntityDiff]:
    """Compare IdP entities by entityID and return a deterministically sorted list of diffs."""
    diffs: list[EntityDiff] = []
    for entity_id in sorted(set(baseline) | set(candidate)):
        old_entity = baseline.get(entity_id)
        new_entity = candidate.get(entity_id)

        if old_entity is None:
            diffs.append(EntityDiff(entity_id, new_entity.display_name, STATUS_ADDED, sorted_facts(new_entity.facts)))
            continue
        if new_entity is None:
            diffs.append(
                EntityDiff(entity_id, old_entity.display_name, STATUS_REMOVED, [], sorted_facts(old_entity.facts))
            )
            continue

        added = sorted_facts(new_entity.facts - old_entity.facts)
        removed = sorted_facts(old_entity.facts - new_entity.facts)
        status = STATUS_MODIFIED if added or removed else STATUS_UNCHANGED
        diffs.append(EntityDiff(entity_id, new_entity.display_name, status, added, removed))

    return sorted(diffs, key=lambda diff: (STATUS_ORDER.index(diff.status), diff.display_name.lower(), diff.entity_id))


def change_rows(diff: EntityDiff) -> list[tuple[str, MetadataFact | None, MetadataFact | None]]:
    """Pair removed and added facts of the same path into (path, before, after) rows."""
    paths = sorted({fact.path for fact in diff.added_facts} | {fact.path for fact in diff.removed_facts})
    rows: list[tuple[str, MetadataFact | None, MetadataFact | None]] = []
    for path in paths:
        before = [fact for fact in diff.removed_facts if fact.path == path]
        after = [fact for fact in diff.added_facts if fact.path == path]
        rows += [(path, old, new) for old, new in zip_longest(before, after)]
    return rows


def check_changed_certificates(
    entity_diffs_per_baseline: list[list[EntityDiff]],
    reference_time: datetime,
) -> CertificateReport:
    """Validate only new certificates and report IdPs that have no valid one among them."""
    changed: dict[str, tuple[str, set[str]]] = {}
    for entity_diffs in entity_diffs_per_baseline:
        for diff in entity_diffs:
            if diff.status not in (STATUS_ADDED, STATUS_MODIFIED):
                continue
            certificates = {fact.certificate for fact in diff.added_facts if fact.certificate is not None}
            if not certificates:
                continue
            changed.setdefault(diff.entity_id, (diff.display_name, set()))[1].update(certificates)

    findings: list[CertificateFinding] = []
    for entity_id, (display_name, certificates) in sorted(changed.items(), key=lambda item: item[1][0].lower()):
        problems = [
            certificate_problem(inspect_certificate(certificate), reference_time)
            for certificate in sorted(certificates)
        ]
        if any(problem is None for problem in problems):
            continue
        findings.append(CertificateFinding(display_name, entity_id, sorted(set(problems))))

    checked_count = sum(len(certificates) for _, certificates in changed.values())
    return CertificateReport(checked_count, findings)


def pluralize(count: int, singular: str, plural: str) -> str:
    return f"{count} {singular if count == 1 else plural}"


def escape_table_cell(value: str) -> str:
    return value.replace("|", "\\|").replace("\n", " ").replace("`", "'")


def strip_known_prefixes(value: str) -> str:
    for prefix in DISPLAY_PREFIXES_TO_STRIP:
        if value.startswith(prefix):
            return value.removeprefix(prefix)
    return value


def display_value(fact: MetadataFact | None, include_values: bool) -> str:
    if fact is None:
        return "—"
    if not include_values:
        return "…"
    if fact.certificate is not None:
        return "<br>".join(f"`{escape_table_cell(part)}`" for part in fact.text.split(", "))

    parts = [f"`{escape_table_cell(f'{name}={strip_known_prefixes(value)}')}`" for name, value in fact.attributes]
    if fact.text:
        parts.append(f"`{escape_table_cell(fact.text)}`")
    return "<br>".join(parts) if parts else "present"


def relative_display_path(path: str | None, metadata_dir: str) -> str:
    if path is None:
        return "none"
    try:
        return str(PurePosixPath(path).relative_to(metadata_dir))
    except ValueError:
        return path


ComparisonGroup = tuple[list[str | None], list[EntityDiff]]


def group_comparisons(comparisons: list[FileComparison]) -> dict[str, list[ComparisonGroup]]:
    """Group comparisons by candidate file, merging baselines that produced identical IdP diffs."""
    groups: dict[str, list[ComparisonGroup]] = {}
    for comparison in comparisons:
        candidate_groups = groups.setdefault(comparison.candidate_path, [])
        matching_group = next((group for group in candidate_groups if group[1] == comparison.entity_diffs), None)
        if matching_group is None:
            candidate_groups.append(([comparison.baseline_path], comparison.entity_diffs))
            continue
        matching_group[0].append(comparison.baseline_path)
    return groups


def render_diff_table(
    baseline_paths: list[str | None],
    entity_diffs: list[EntityDiff],
    metadata_dir: str,
    include_values: bool,
) -> list[str]:
    counts = Counter(diff.status for diff in entity_diffs)
    summary = " · ".join(f"{counts.get(status, 0)} {status}" for status in STATUS_ORDER)
    if baseline_paths == [None]:
        compared_with = "_no metadata of the same IdP type on the base branch_"
    else:
        compared_with = ", ".join(f"`{relative_display_path(path, metadata_dir)}`" for path in baseline_paths)

    lines = [f"Compared with {compared_with} — **{summary}**", ""]
    changed_diffs = [diff for diff in entity_diffs if diff.status != STATUS_UNCHANGED]
    if not changed_diffs:
        return lines + ["No IdP changes detected.", ""]

    lines += ["| IdP | Status | Field | Before | After |", "|---|---|---|---|---|"]
    for diff in changed_diffs:
        idp_cell = f"**{escape_table_cell(diff.display_name)}**<br>`{escape_table_cell(diff.entity_id)}`"
        status_cell = STATUS_LABELS[diff.status]
        if diff.status != STATUS_MODIFIED:
            lines.append(f"| {idp_cell} | {status_cell} | | | |")
            continue
        for index, (path, before, after) in enumerate(change_rows(diff)):
            first_row = index == 0
            lines.append(
                f"| {idp_cell if first_row else ''} | {status_cell if first_row else ''} | `{path}` "
                f"| {display_value(before, include_values)} | {display_value(after, include_values)} |"
            )
    return lines + [""]


def render_certificate_section(report: CertificateReport) -> list[str]:
    lines = ["#### 🔐 Changed certificates", ""]
    if report.checked_count == 0:
        return lines + ["No certificate was added or changed.", ""]
    if not report.findings:
        if report.checked_count == 1:
            return lines + ["The changed certificate is valid.", ""]
        return lines + [f"All {report.checked_count} changed certificates are valid.", ""]

    lines += [
        f"{pluralize(len(report.findings), 'IdP has', 'IdPs have')} no valid certificate among the "
        f"{pluralize(report.checked_count, 'changed certificate', 'changed certificates')} checked:",
        "",
        "| IdP | Issue |",
        "|---|---|",
    ]
    for finding in report.findings:
        issues = "<br>".join(f"❌ {escape_table_cell(problem)}" for problem in finding.problems)
        lines.append(f"| **{escape_table_cell(finding.display_name)}** | {issues} |")
    return lines + [""]


def render_report(
    comparisons: list[FileComparison],
    certificate_reports: dict[str, CertificateReport],
    metadata_dir: str,
    head_ref: str,
    include_values: bool = True,
) -> str:
    """Render the full Markdown comment, hiding before/after values if it would be too long."""
    lines = [COMMENT_MARKER, "## 🔎 IdP metadata diff", ""]
    if not comparisons:
        lines += ["No managed IdP metadata XML was added or modified in this pull request.", ""]

    for candidate_path, candidate_groups in group_comparisons(comparisons).items():
        lines += [f"### `{relative_display_path(candidate_path, metadata_dir)}`", ""]
        for baseline_paths, entity_diffs in candidate_groups:
            lines += render_diff_table(baseline_paths, entity_diffs, metadata_dir, include_values)
        if candidate_path in certificate_reports:
            lines += render_certificate_section(certificate_reports[candidate_path])

    if not include_values:
        lines += ["> Before/after values omitted because the comment exceeded GitHub's size limit.", ""]
    lines.append(
        "<sub>IdPs are matched by `entityID`; signatures, `ID` attributes and publication info are ignored. "
        f"Generated by `.github/scripts/idp_metadata_diff.py` for commit `{head_ref[:12]}`.</sub>"
    )
    report = "\n".join(lines)

    if include_values and len(report) > MAX_COMMENT_LENGTH:
        log("⚠️ Report too long, omitting before/after values")
        return render_report(comparisons, certificate_reports, metadata_dir, head_ref, include_values=False)
    return report


def run_git(*args: str) -> bytes:
    return subprocess.run(["git", *args], capture_output=True, check=True).stdout


def list_changed_metadata_files(base_ref: str, head_ref: str, metadata_dir: str) -> list[str]:
    output = run_git("diff", "--name-only", "--diff-filter=AMR", f"{base_ref}...{head_ref}", "--", metadata_dir)
    return sorted(path for path in output.decode().splitlines() if is_managed_metadata_file(path, metadata_dir))


def list_files_at_ref(ref: str, metadata_dir: str) -> list[str]:
    return run_git("ls-tree", "-r", "--name-only", ref, "--", metadata_dir).decode().splitlines()


def read_file_at_ref(ref: str, path: str) -> bytes:
    return run_git("show", f"{ref}:{path}")


def build_comparisons(
    base_ref: str,
    head_ref: str,
    metadata_dir: str,
    reference_time: datetime,
) -> tuple[list[FileComparison], dict[str, CertificateReport]]:
    changed_paths = list_changed_metadata_files(base_ref, head_ref, metadata_dir)
    if not changed_paths:
        log("ℹ️ No managed metadata files changed")
        return [], {}

    base_paths = list_files_at_ref(base_ref, metadata_dir)
    comparisons: list[FileComparison] = []
    certificate_reports: dict[str, CertificateReport] = {}
    for candidate_path in changed_paths:
        log(f"🔍 Analyzing {candidate_path}")
        candidate_entities = parse_entities(read_file_at_ref(head_ref, candidate_path))

        candidate_comparisons: list[FileComparison] = []
        baseline_paths = find_baseline_paths(candidate_path, base_paths, metadata_dir)
        if not baseline_paths:
            log(f"⚠️ No baseline found for {candidate_path}")
            candidate_comparisons.append(FileComparison(candidate_path, None, compare_entities({}, candidate_entities)))

        for baseline_path in baseline_paths:
            log(f"↔️ Comparing against {baseline_path}")
            baseline_entities = parse_entities(read_file_at_ref(base_ref, baseline_path))
            candidate_comparisons.append(
                FileComparison(candidate_path, baseline_path, compare_entities(baseline_entities, candidate_entities))
            )

        certificate_report = check_changed_certificates(
            [comparison.entity_diffs for comparison in candidate_comparisons], reference_time
        )
        checked_label = pluralize(certificate_report.checked_count, "changed certificate", "changed certificates")
        log(f"🔐 Checked {checked_label}, {len(certificate_report.findings)} IdPs without a valid one")
        comparisons += candidate_comparisons
        certificate_reports[candidate_path] = certificate_report
    return comparisons, certificate_reports


def parse_arguments(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate a Markdown diff report of IdP metadata changes.")
    parser.add_argument("--base-ref", required=True, help="Git ref of the pull request base (e.g. base SHA).")
    parser.add_argument("--head-ref", required=True, help="Git ref of the pull request head (e.g. head SHA).")
    parser.add_argument("--metadata-dir", default=DEFAULT_METADATA_DIR, help="Repository-relative metadata folder.")
    parser.add_argument("--output", help="Write the report to this file instead of stdout.")
    parser.add_argument(
        "--reference-date",
        type=lambda value: datetime.strptime(value, "%Y-%m-%d").replace(tzinfo=timezone.utc),
        help="Date (YYYY-MM-DD, UTC) used for certificate validity checks; defaults to now.",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    arguments = parse_arguments(argv)
    log(f"🚀 Comparing IdP metadata {arguments.base_ref}...{arguments.head_ref}")

    reference_time = arguments.reference_date or datetime.now(timezone.utc)
    comparisons, certificate_reports = build_comparisons(
        arguments.base_ref, arguments.head_ref, arguments.metadata_dir, reference_time
    )
    report = render_report(comparisons, certificate_reports, arguments.metadata_dir, arguments.head_ref)

    if not arguments.output:
        print(report)
        log("✅ Report printed")
        return 0

    with open(arguments.output, "w", encoding="utf-8") as output_file:
        output_file.write(report + "\n")
    log(f"✅ Report written to {arguments.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
