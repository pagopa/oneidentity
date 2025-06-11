""" 
Import users from JSON files into DynamoDB.
This script reads user data from JSON files named in the format `users_<namespace>.json`
and imports them into a DynamoDB table named `InternalIDPUsers`.
The namespace is determined from the filename, and the user data is expected to contain
fields like `username`, `password`, and other SAML attributes.
"""

import os
import json
import boto3

# Define a mapping of namespaces to their values.
# The keys should match the namespace part of the filename and the value should be the corresponding clientId
# in the ClientRegistrations table.

# Uncomment and fill in the namespace_dict with actual values as needed.
namespace_dict = {
    # "arc_dev": "",
    # "checkout_dev": "",
    # "selfcare_dev": ""
}

dynamodb = boto3.client("dynamodb")
table_name = "InternalIDPUsers"


def get_namespace_from_filename(filename):
    # Extracts the namespace key from the filename
    # e.g. users_arc_dev.json -> arc_dev
    basename = os.path.basename(filename)
    if basename.startswith("users_") and basename.endswith(".json"):
        return basename[len("users_") : -len(".json")]
    return None


def main():
    for filename in os.listdir("."):
        if filename.startswith("users_") and filename.endswith(".json"):
            namespace_key = get_namespace_from_filename(filename)
            if not namespace_key or namespace_key not in namespace_dict:
                print(f"Skipping {filename}: namespace not found in namespace_dict")
                continue
            namespace_value = namespace_dict[namespace_key]
            with open(filename, "r") as f:
                users = json.load(f)
            for user in users:
                username = user.get("username")
                password = user.get("password")
                if not username or not password:
                    print(f"Skipping user in {filename}: missing username or password")
                    continue
                # Remove fields that are not samlAttributes
                saml_attributes = {
                    k: v for k, v in user.items() if k not in ("username", "password")
                }
                # These are tests data for Internal IDP used for testing so we can store the password in plaintext
                item = {
                    "username": {"S": username},
                    "namespace": {"S": namespace_value},
                    "password": {"S": password},
                    "samlAttributes": {
                        "M": {
                            k: {"N": str(v)} if k == "spidLevel" else {"S": str(v)}
                            for k, v in saml_attributes.items()
                        }
                    },
                }
                try:
                    dynamodb.put_item(TableName=table_name, Item=item)
                    print(f"Imported user {username} in namespace {namespace_key}")
                except Exception as e:
                    print(f"Error importing user {username} in {filename}: {e}")


if __name__ == "__main__":
    main()
