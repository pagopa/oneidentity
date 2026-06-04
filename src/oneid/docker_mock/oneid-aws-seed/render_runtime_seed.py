#!/usr/bin/env python3
"""Purpose: Render runtime seed artifacts for local OneID development.

Usage examples:
  python render_runtime_seed.py \
    --dynamodb-template ./dynamodb/batchDynamo.json \
    --dummy-client-template ./dummy-client.env.template \
    --output-dynamodb /tmp/batchDynamo.runtime.json \
    --output-dummy-client-env /tmp/dummy-client.env \
    --certificate-base64 "<base64>"

    python render_runtime_seed.py --self-check
"""

from __future__ import annotations

import argparse
import base64
from dataclasses import dataclass
from pathlib import Path
import secrets
import sys
from typing import Mapping

IDP_INTERNAL_CERT_PLACEHOLDER = "__IDP_INTERNAL_CERTIFICATE_BASE64__"
CLIENT_PLACEHOLDERS = {
    "client_1": {
        "secret": "__CLIENT_1_SECRET__",
        "hashed_secret": "__CLIENT_1_HASHED_CLIENT_SECRET__",
        "salt": "__CLIENT_1_SALT__",
    },
    "client_2": {
        "secret": "__CLIENT_2_SECRET__",
        "hashed_secret": "__CLIENT_2_HASHED_CLIENT_SECRET__",
        "salt": "__CLIENT_2_SALT__",
    },
    "client_3": {
        "secret": "__CLIENT_3_SECRET__",
        "hashed_secret": "__CLIENT_3_HASHED_CLIENT_SECRET__",
        "salt": "__CLIENT_3_SALT__",
    },
}


@dataclass(frozen=True)
class ClientSecretMaterial:
    secret: str
    salt: str
    hashed_secret: str


def log_info(message: str) -> None:
    print(f"ℹ️ [render-runtime-seed] {message}")



def log_error(message: str) -> None:
    print(f"❌ [render-runtime-seed] {message}", file=sys.stderr)



def encode_without_padding(raw_bytes: bytes) -> str:
    return base64.b64encode(raw_bytes).decode("ascii").rstrip("=")



def build_client_secret_material() -> ClientSecretMaterial:
    from argon2.low_level import Type, hash_secret_raw

    secret_bytes = secrets.token_bytes(32)
    salt_bytes = secrets.token_bytes(16)
    hashed_secret_bytes = hash_secret_raw(
        secret=secret_bytes,
        salt=salt_bytes,
        time_cost=2,
        memory_cost=19000,
        parallelism=4,
        hash_len=32,
        type=Type.ID,
        version=19,
    )
    return ClientSecretMaterial(
        secret=encode_without_padding(secret_bytes),
        salt=encode_without_padding(salt_bytes),
        hashed_secret=encode_without_padding(hashed_secret_bytes),
    )



def build_client_materials() -> dict[str, ClientSecretMaterial]:
    return {
        client_name: build_client_secret_material()
        for client_name in CLIENT_PLACEHOLDERS
    }



def replace_known_placeholders(template_text: str, replacements: Mapping[str, str]) -> str:
    rendered_text = template_text
    for placeholder, value in replacements.items():
        if placeholder in rendered_text:
            rendered_text = rendered_text.replace(placeholder, value)
    return rendered_text



def render_runtime_artifacts(
    dynamodb_template_text: str,
    env_template_text: str,
    certificate_base64: str,
    client_materials: Mapping[str, ClientSecretMaterial],
) -> tuple[str, str]:
    replacements: dict[str, str] = {
        IDP_INTERNAL_CERT_PLACEHOLDER: certificate_base64,
    }

    if IDP_INTERNAL_CERT_PLACEHOLDER not in dynamodb_template_text:
        raise ValueError("missing certificate placeholder in batchDynamo.json")

    for client_name, placeholders in CLIENT_PLACEHOLDERS.items():
        material = client_materials.get(client_name)
        if material is None:
            raise ValueError(f"missing client secret material for {client_name}")

        required_placeholders = (
            placeholders["hashed_secret"],
            placeholders["salt"],
        )
        missing_placeholders = [
            placeholder
            for placeholder in required_placeholders
            if placeholder not in dynamodb_template_text
        ]
        if missing_placeholders:
            missing_placeholders_text = ", ".join(missing_placeholders)
            raise ValueError(
                f"missing DynamoDB placeholders for {client_name}: {missing_placeholders_text}"
            )

        replacements[placeholders["secret"]] = material.secret
        replacements[placeholders["salt"]] = material.salt
        replacements[placeholders["hashed_secret"]] = material.hashed_secret

    rendered_dynamodb_template = replace_known_placeholders(
        dynamodb_template_text,
        replacements,
    )
    rendered_env_template = replace_known_placeholders(
        env_template_text,
        replacements,
    )
    return rendered_dynamodb_template, rendered_env_template



def write_text_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def run_self_check() -> int:
    log_info("running self-check...")

    assert encode_without_padding(b"test") == "dGVzdA"

    dynamodb_template = "\n".join(
        [
            '{"cert":"__IDP_INTERNAL_CERTIFICATE_BASE64__",',
            '"client1Hash":"__CLIENT_1_HASHED_CLIENT_SECRET__",',
            '"client1Salt":"__CLIENT_1_SALT__",',
            '"client2Hash":"__CLIENT_2_HASHED_CLIENT_SECRET__",',
            '"client2Salt":"__CLIENT_2_SALT__",',
            '"client3Hash":"__CLIENT_3_HASHED_CLIENT_SECRET__",',
            '"client3Salt":"__CLIENT_3_SALT__"}',
        ]
    )
    env_template = "client_id=client_1\nclient_secret=__CLIENT_1_SECRET__\n"
    client_materials = {
        "client_1": ClientSecretMaterial(
            secret="secret-1",
            salt="salt-1",
            hashed_secret="hashed-1",
        ),
        "client_2": ClientSecretMaterial(
            secret="secret-2",
            salt="salt-2",
            hashed_secret="hashed-2",
        ),
        "client_3": ClientSecretMaterial(
            secret="secret-3",
            salt="salt-3",
            hashed_secret="hashed-3",
        ),
    }

    rendered_dynamodb, rendered_env = render_runtime_artifacts(
        dynamodb_template_text=dynamodb_template,
        env_template_text=env_template,
        certificate_base64="certificate-value",
        client_materials=client_materials,
    )

    assert "__IDP_INTERNAL_CERTIFICATE_BASE64__" not in rendered_dynamodb
    assert "__CLIENT_1_SECRET__" not in rendered_env
    assert "certificate-value" in rendered_dynamodb
    assert "hashed-1" in rendered_dynamodb
    assert "salt-2" in rendered_dynamodb
    assert "secret-1" in rendered_env

    log_info("self-check passed")
    return 0



def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Render runtime DynamoDB and dummy client seed artifacts."
    )
    parser.add_argument(
        "--dynamodb-template",
        type=Path,
        help="Path to the batchDynamo.json template.",
    )
    parser.add_argument(
        "--dummy-client-template",
        type=Path,
        help="Path to the dummy client env template.",
    )
    parser.add_argument(
        "--output-dynamodb",
        type=Path,
        help="Path for the rendered DynamoDB seed file.",
    )
    parser.add_argument(
        "--output-dummy-client-env",
        type=Path,
        help="Path for the rendered dummy client env file.",
    )
    parser.add_argument(
        "--certificate-base64",
        help="Base64-encoded certificate value for DynamoDB seeding.",
    )
    parser.add_argument(
        "--self-check",
        action="store_true",
        help="Run built-in zero-dependency checks and exit.",
    )
    return parser.parse_args()



def main() -> int:
    args = parse_args()

    if args.self_check:
        return run_self_check()

    required_args = {
        "--dynamodb-template": args.dynamodb_template,
        "--dummy-client-template": args.dummy_client_template,
        "--output-dynamodb": args.output_dynamodb,
        "--output-dummy-client-env": args.output_dummy_client_env,
        "--certificate-base64": args.certificate_base64,
    }
    missing_args = [flag for flag, value in required_args.items() if value is None]
    if missing_args:
        missing_args_text = ", ".join(missing_args)
        raise ValueError(f"missing required arguments: {missing_args_text}")

    if args.certificate_base64 is None:
        raise ValueError("--certificate-base64 is required unless --self-check is used")
    log_info("rendering runtime seed artifacts...")

    dynamodb_template_text = args.dynamodb_template.read_text(encoding="utf-8")
    env_template_text = args.dummy_client_template.read_text(encoding="utf-8")

    rendered_dynamodb_template, rendered_env_template = render_runtime_artifacts(
        dynamodb_template_text=dynamodb_template_text,
        env_template_text=env_template_text,
        certificate_base64=args.certificate_base64,
        client_materials=build_client_materials(),
    )

    write_text_file(args.output_dynamodb, rendered_dynamodb_template)
    write_text_file(args.output_dummy_client_env, rendered_env_template)

    log_info("runtime seed artifacts rendered")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:  # pragma: no cover - top-level guard
        log_error(str(error))
        raise SystemExit(1) from error
