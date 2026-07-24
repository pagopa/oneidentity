#!/usr/bin/env python3
"""Purpose: Render runtime seed artifacts for local OneID development.

Usage examples:
  python render_runtime_seed.py \
    --dynamodb-template ./dynamodb/batchDynamo.json \
    --dummy-client-template ./dummy-client.env.template \
    --output-dynamodb /tmp/batchDynamo.runtime.json \
    --output-dummy-client-env /tmp/dummy-client.env \
        --output-public-assets /tmp/public-assets \
    --certificate-base64 "<base64>"

    python render_runtime_seed.py --self-check
"""

from __future__ import annotations

import argparse
import base64
from dataclasses import dataclass
import json
from pathlib import Path
import secrets
import shutil
import sys
from typing import Any, Mapping

IDP_INTERNAL_CERT_PLACEHOLDER = "__IDP_INTERNAL_CERTIFICATE_BASE64__"
DEFAULT_SAML_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
PUBLIC_CLIENT_FIELDS = (
    "clientID",
    "friendlyName",
    "logoUri",
    "policyUri",
    "tosUri",
    "a11yUri",
    "backButtonEnabled",
    "samlBinding",
    "callbackURI",
    "eidasIndex",
    "localizedContentMap",
)
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


def deserialize_dynamodb_attribute(attribute: Mapping[str, Any]) -> Any:
    result: dict[str, Any] = {}
    pending_attributes: list[tuple[Mapping[str, Any], dict[Any, Any] | list[Any], Any]] = [
        (attribute, result, "value")
    ]

    while pending_attributes:
        current_attribute, container, key = pending_attributes.pop()

        if "S" in current_attribute:
            container[key] = current_attribute["S"]
            continue
        if "N" in current_attribute:
            number = current_attribute["N"]
            container[key] = int(number) if number.lstrip("-").isdigit() else float(number)
            continue
        if "BOOL" in current_attribute:
            container[key] = current_attribute["BOOL"]
            continue
        if "NULL" in current_attribute:
            container[key] = None
            continue
        if "SS" in current_attribute:
            container[key] = sorted(current_attribute["SS"])
            continue
        if "L" in current_attribute:
            list_values: list[Any] = [None] * len(current_attribute["L"])
            container[key] = list_values
            pending_attributes.extend(
                (value, list_values, index)
                for index, value in enumerate(current_attribute["L"])
            )
            continue
        if "M" in current_attribute:
            map_values: dict[str, Any] = {}
            container[key] = map_values
            pending_attributes.extend(
                (value, map_values, field_name)
                for field_name, value in current_attribute["M"].items()
            )
            continue
        raise ValueError(f"unsupported DynamoDB attribute: {current_attribute}")

    return result["value"]


def read_seed_items(seed_data: Mapping[str, Any], table_name: str) -> list[dict[str, Any]]:
    requests = seed_data.get(table_name, [])
    items: list[dict[str, Any]] = []
    for request in requests:
        item = request.get("PutRequest", {}).get("Item")
        if item is None:
            continue
        items.append(
            {
                field_name: deserialize_dynamodb_attribute(attribute)
                for field_name, attribute in item.items()
            }
        )
    return items


def build_public_client(client: Mapping[str, Any]) -> dict[str, Any]:
    public_client = {
        "clientID": client.get("clientId", ""),
        "friendlyName": client.get("friendlyName", ""),
        "logoUri": client.get("logoUri", ""),
        "policyUri": client.get("policyUri", ""),
        "tosUri": client.get("tosUri", ""),
        "a11yUri": client.get("a11yUri", ""),
        "backButtonEnabled": client.get("backButtonEnabled", False),
        "samlBinding": client.get("samlBinding", DEFAULT_SAML_BINDING),
        "callbackURI": client.get("callbackURI", []),
        "eidasIndex": client.get("eidasIndex"),
        "localizedContentMap": client.get("localizedContentMap", {}),
    }
    return {field: public_client[field] for field in PUBLIC_CLIENT_FIELDS}


def build_public_idp(idp: Mapping[str, Any]) -> dict[str, Any]:
    return {
        "entityID": idp.get("entityID", ""),
        "pointer": idp.get("pointer", ""),
        "active": idp.get("active", False),
        "status": idp.get("status", ""),
        "idpSSOEndpoints": idp.get("idpSSOEndpoints", {}),
        "certificates": idp.get("certificates", []),
        "friendlyName": idp.get("friendlyName", ""),
    }


def write_json_file(path: Path, payload: Any) -> None:
    write_text_file(path, json.dumps(payload, ensure_ascii=True, separators=(",", ":")))


def render_public_assets(dynamodb_seed_text: str, output_directory: Path) -> None:
    seed_data = json.loads(dynamodb_seed_text)
    shutil.rmtree(output_directory, ignore_errors=True)
    clients = [
        build_public_client(client)
        for client in read_seed_items(seed_data, "ClientRegistrations")
        if client.get("active", False)
    ]
    idps = [
        build_public_idp(idp)
        for idp in read_seed_items(seed_data, "IDPMetadata")
        if idp.get("pointer") == "LATEST_SPID"
    ]

    write_json_file(output_directory / "clients.json", clients)
    write_json_file(output_directory / "idps.json", idps)
    for client in clients:
        write_json_file(
            output_directory / "clients-publisher" / f"{client['clientID']}.json",
            client,
        )


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

    public_assets_directory = Path("/tmp/render-runtime-seed-self-check")
    render_public_assets(
        json.dumps(
            {
                "ClientRegistrations": [
                    {
                        "PutRequest": {
                            "Item": {
                                "clientId": {"S": "active-client"},
                                "active": {"BOOL": True},
                                "callbackURI": {"SS": ["https://callback.example"]},
                                "localizedContentMap": {"M": {}},
                            }
                        }
                    },
                    {
                        "PutRequest": {
                            "Item": {
                                "clientId": {"S": "inactive-client"},
                                "active": {"BOOL": False},
                            }
                        }
                    },
                ],
                "IDPMetadata": [
                    {
                        "PutRequest": {
                            "Item": {
                                "entityID": {"S": "https://idp.example"},
                                "pointer": {"S": "LATEST_SPID"},
                                "active": {"BOOL": True},
                            }
                        }
                    }
                ],
            }
        ),
        public_assets_directory,
    )
    published_clients = json.loads((public_assets_directory / "clients.json").read_text())
    published_idps = json.loads((public_assets_directory / "idps.json").read_text())
    assert [client["clientID"] for client in published_clients] == ["active-client"]
    assert (public_assets_directory / "clients-publisher" / "active-client.json").is_file()
    assert not (public_assets_directory / "clients-publisher" / "inactive-client.json").exists()
    assert published_idps[0]["entityID"] == "https://idp.example"

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
        "--output-public-assets",
        type=Path,
        help="Directory for public IDP and client JSON assets.",
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
        "--output-public-assets": args.output_public_assets,
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
    render_public_assets(rendered_dynamodb_template, args.output_public_assets)

    log_info("runtime seed artifacts rendered")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:  # pragma: no cover - top-level guard
        log_error(str(error))
        raise SystemExit(1) from error
