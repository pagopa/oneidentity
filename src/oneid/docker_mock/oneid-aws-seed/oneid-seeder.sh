#!/usr/bin/env bash
#
# Purpose: Initialize local development dependencies for OneIdentity.
#
# Usage examples:
#   /home/aws/oneid-seeder.sh
#
#   AWS_REGION=eu-south-1 /home/aws/oneid-seeder.sh

set -euo pipefail

readonly AWS_REGION="${AWS_REGION:-eu-south-1}"
readonly DYNAMODB_ENDPOINT="http://oneid-aws-local:4566"
readonly SSM_ENDPOINT="http://oneid-aws-local:4566"
readonly KMS_ENDPOINT="http://oneid-aws-local:4566"
readonly SEED_ROOT="/home/aws"
readonly DYNAMODB_SEED_ROOT="$SEED_ROOT/dynamodb"
readonly DUMMY_CLIENT_ENV_TEMPLATE_FILE="$SEED_ROOT/dummy-client.env.template"
readonly KMS_SEED_ROOT="$SEED_ROOT/kms"
readonly RUNTIME_SHARED_ROOT="${RUNTIME_SHARED_ROOT:-/runtime}"
readonly RUNTIME_SEED_ROOT="$(mktemp -d /tmp/oneid-seeder.XXXXXX)"
readonly RUNTIME_DYNAMODB_SEED_FILE="$RUNTIME_SEED_ROOT/batchDynamo.runtime.json"
readonly RUNTIME_CERT_FILE="$RUNTIME_SEED_ROOT/cert.pem"
readonly RUNTIME_KEY_FILE="$RUNTIME_SEED_ROOT/key.pem"
readonly RUNTIME_IDP_INTERNAL_CERT_FILE="$RUNTIME_SEED_ROOT/idp_internal_cert.pem"
readonly RUNTIME_IDP_INTERNAL_KEY_FILE="$RUNTIME_SEED_ROOT/idp_internal_key.pem"
readonly RUNTIME_DUMMY_CLIENT_ENV_FILE="$RUNTIME_SHARED_ROOT/dummy-client.env"
readonly IDP_INTERNAL_CERT_PLACEHOLDER="__IDP_INTERNAL_CERTIFICATE_BASE64__"

cleanup() {
  rm -rf "$RUNTIME_SEED_ROOT"
}

trap cleanup EXIT

require_seed_file() {
  local file_path="$1"

  if [[ -f "$file_path" ]]; then
    return
  fi

  echo "❌ [oneid-seeder] missing seed file: $file_path"
  exit 1
}

ensure_runtime_dependencies() {
  if command -v openssl >/dev/null 2>&1; then
    :
  else
    echo "❌ [oneid-seeder] openssl is required but not available in the seeder environment"
    echo "ℹ️ [oneid-seeder] install openssl in the image or provide it in the runtime environment before running the seeder"
    exit 1
  fi

  if command -v python3 >/dev/null 2>&1; then
    return
  fi

  echo "❌ [oneid-seeder] python3 is required but not available in the seeder environment"
  echo "ℹ️ [oneid-seeder] install python3 in the image or provide it in the runtime environment before running the seeder"
  exit 1
}

generate_certificate_pair() {
  local cert_name="$1"
  local cert_path="$2"
  local key_path="$3"
  local subject="$4"

  echo "ℹ️ [oneid-seeder] generating $cert_name certificate pair..."

  openssl req \
    -x509 \
    -newkey rsa:2048 \
    -keyout "$key_path" \
    -out "$cert_path" \
    -days 30 \
    -nodes \
    -subj "$subject" \
    >/dev/null 2>&1

  require_seed_file "$cert_path"
  require_seed_file "$key_path"
  echo "✅ [oneid-seeder] $cert_name certificate pair generated"
}

generate_default_certificate_pair() {
  generate_certificate_pair \
    "default" \
    "$RUNTIME_CERT_FILE" \
    "$RUNTIME_KEY_FILE" \
    "/CN=oneid-core.local/O=OneIdentity/C=IT"
}

generate_internal_idp_certificate_pair() {
  generate_certificate_pair \
    "internal IDP" \
    "$RUNTIME_IDP_INTERNAL_CERT_FILE" \
    "$RUNTIME_IDP_INTERNAL_KEY_FILE" \
    "/CN=oneid-internal-idp.local/O=OneIdentity/C=IT"
}

render_runtime_seed_artifacts() {
  local generated_certificate_base64

  generated_certificate_base64="$({ openssl x509 -in "$RUNTIME_IDP_INTERNAL_CERT_FILE" -outform DER | base64; } | tr -d '\n')"

  if [[ -z "$generated_certificate_base64" ]]; then
    echo "❌ [oneid-seeder] failed to encode generated certificate for DynamoDB seeding"
    exit 1
  fi

  require_seed_file "$DUMMY_CLIENT_ENV_TEMPLATE_FILE"

    python3 "$SEED_ROOT/render_runtime_seed.py" \
    --dynamodb-template "$DYNAMODB_SEED_ROOT/batchDynamo.json" \
    --dummy-client-template "$DUMMY_CLIENT_ENV_TEMPLATE_FILE" \
    --output-dynamodb "$RUNTIME_DYNAMODB_SEED_FILE" \
    --output-dummy-client-env "$RUNTIME_DUMMY_CLIENT_ENV_FILE" \
    --certificate-base64 "$generated_certificate_base64"

  require_seed_file "$RUNTIME_DYNAMODB_SEED_FILE"
  require_seed_file "$RUNTIME_DUMMY_CLIENT_ENV_FILE"
  echo "✅ [oneid-seeder] runtime seed artifacts rendered"
}

ensure_dynamodb_table() {
  local table_name="$1"
  shift

  if aws dynamodb describe-table --table-name "$table_name" --endpoint-url "$DYNAMODB_ENDPOINT" --region "$AWS_REGION" >/dev/null 2>&1; then
    echo "✅ [oneid-seeder] DynamoDB table already present: $table_name"
    return
  fi

  aws dynamodb create-table --table-name "$table_name" "$@" --endpoint-url "$DYNAMODB_ENDPOINT" --region "$AWS_REGION"
}

wait_for_dynamodb() {
  echo "ℹ️ [oneid-seeder] waiting for DynamoDB on MiniStack..."
  for i in $(seq 1 60); do
    if aws dynamodb list-tables --endpoint-url "$DYNAMODB_ENDPOINT" --region "$AWS_REGION" >/dev/null 2>&1; then
      echo "✅ [oneid-seeder] DynamoDB ready"
      return
    fi
    sleep 1
  done
  echo "❌ [oneid-seeder] DynamoDB did not become ready"
  exit 1
}

wait_for_ssm() {
  echo "ℹ️ [oneid-seeder] waiting for oneid-aws-local..."
  for i in $(seq 1 60); do
    if aws ssm describe-parameters --endpoint "$SSM_ENDPOINT" --region "$AWS_REGION" >/dev/null 2>&1; then
      echo "✅ [oneid-seeder] oneid-aws-local ready"
      return
    fi
    sleep 1
  done
  echo "❌ [oneid-seeder] oneid-aws-local did not become ready"
  exit 1
}

wait_for_kms() {
  echo "ℹ️ [oneid-seeder] waiting for KMS on MiniStack..."
  for i in $(seq 1 60); do
    if aws kms list-keys --endpoint-url "$KMS_ENDPOINT" --region "$AWS_REGION" >/dev/null 2>&1; then
      echo "✅ [oneid-seeder] KMS ready"
      return
    fi
    sleep 1
  done
  echo "❌ [oneid-seeder] KMS did not become ready"
  exit 1
}

ensure_sign_jwt_alias() {
  local sign_key_id

  require_seed_file "$KMS_SEED_ROOT/kms_seeding_file.yaml"
  sign_key_id="$(aws kms list-keys --endpoint-url "$KMS_ENDPOINT" --region "$AWS_REGION" --query 'Keys[0].KeyId' --output text)"

  if [[ -z "$sign_key_id" || "$sign_key_id" == "None" ]]; then
    echo "⚠️ [oneid-seeder] no KMS keys found, creating a local signing key"
    sign_key_id="$(aws kms create-key --description "oneid local sign-jwt key" --key-usage SIGN_VERIFY --key-spec RSA_2048 --origin AWS_KMS --endpoint-url "$KMS_ENDPOINT" --region "$AWS_REGION" --query 'KeyMetadata.KeyId' --output text)"
  fi

  if [[ -z "$sign_key_id" || "$sign_key_id" == "None" ]]; then
    echo "❌ [oneid-seeder] failed to create a KMS key for alias/sign-jwt"
    exit 1
  fi

  if aws kms create-alias --alias-name alias/sign-jwt --target-key-id "$sign_key_id" --endpoint-url "$KMS_ENDPOINT" --region "$AWS_REGION" >/dev/null 2>&1; then
    echo "✅ [oneid-seeder] alias/sign-jwt created"
    return
  fi

  aws kms update-alias --alias-name alias/sign-jwt --target-key-id "$sign_key_id" --endpoint-url "$KMS_ENDPOINT" --region "$AWS_REGION"
  echo "✅ [oneid-seeder] alias/sign-jwt updated"
}

seed_dynamodb() {
  require_seed_file "$DYNAMODB_SEED_ROOT/gsi_code.json"
  require_seed_file "$DYNAMODB_SEED_ROOT/gsi_pointer.json"
  require_seed_file "$DYNAMODB_SEED_ROOT/gsi_namespace.json"
  require_seed_file "$DYNAMODB_SEED_ROOT/batchDynamo.json"
  require_seed_file "$RUNTIME_DYNAMODB_SEED_FILE"

  echo "ℹ️ [oneid-seeder] creating DynamoDB tables and seed data..."

  ensure_dynamodb_table ClientRegistrations --attribute-definitions AttributeName=clientId,AttributeType=S --key-schema AttributeName=clientId,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
  ensure_dynamodb_table Sessions --attribute-definitions AttributeName=samlRequestID,AttributeType=S AttributeName=recordType,AttributeType=S AttributeName=code,AttributeType=S --key-schema AttributeName=samlRequestID,KeyType=HASH AttributeName=recordType,KeyType=RANGE --global-secondary-indexes file://$DYNAMODB_SEED_ROOT/gsi_code.json --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
  ensure_dynamodb_table IDPMetadata --attribute-definitions AttributeName=entityID,AttributeType=S AttributeName=pointer,AttributeType=S --key-schema AttributeName=entityID,KeyType=HASH AttributeName=pointer,KeyType=RANGE --global-secondary-indexes file://$DYNAMODB_SEED_ROOT/gsi_pointer.json --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
  ensure_dynamodb_table InternalIDPUsers --attribute-definitions AttributeName=username,AttributeType=S AttributeName=namespace,AttributeType=S --key-schema AttributeName=username,KeyType=HASH AttributeName=namespace,KeyType=RANGE --global-secondary-indexes file://$DYNAMODB_SEED_ROOT/gsi_namespace.json --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
  ensure_dynamodb_table InternalIDPSessions --attribute-definitions AttributeName=authnRequestId,AttributeType=S AttributeName=clientId,AttributeType=S --key-schema AttributeName=authnRequestId,KeyType=HASH AttributeName=clientId,KeyType=RANGE --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
  aws dynamodb batch-write-item --request-items file://$RUNTIME_DYNAMODB_SEED_FILE --endpoint-url "$DYNAMODB_ENDPOINT" --region "$AWS_REGION"
  aws dynamodb list-tables --endpoint-url "$DYNAMODB_ENDPOINT" --region "$AWS_REGION"
}

seed_ssm() {
  require_seed_file "$RUNTIME_CERT_FILE"
  require_seed_file "$RUNTIME_KEY_FILE"
  require_seed_file "$RUNTIME_IDP_INTERNAL_CERT_FILE"
  require_seed_file "$RUNTIME_IDP_INTERNAL_KEY_FILE"

  echo "ℹ️ [oneid-seeder] storing SSM parameters..."

  aws ssm put-parameter --region "$AWS_REGION" --endpoint "$SSM_ENDPOINT" --name cert.pem --type String --value file://$RUNTIME_CERT_FILE --overwrite
  aws ssm put-parameter --region "$AWS_REGION" --endpoint "$SSM_ENDPOINT" --name key.pem --type SecureString --value file://$RUNTIME_KEY_FILE --overwrite
  aws ssm put-parameter --region "$AWS_REGION" --endpoint "$SSM_ENDPOINT" --name idp_internal_cert.pem --type String --value file://$RUNTIME_IDP_INTERNAL_CERT_FILE --overwrite
  aws ssm put-parameter --region "$AWS_REGION" --endpoint "$SSM_ENDPOINT" --name idp_internal_key.pem --type SecureString --value file://$RUNTIME_IDP_INTERNAL_KEY_FILE --overwrite
}

main() {
  ensure_runtime_dependencies
  wait_for_dynamodb
  wait_for_ssm
  wait_for_kms
  generate_default_certificate_pair
  generate_internal_idp_certificate_pair
  render_runtime_seed_artifacts
  ensure_sign_jwt_alias
  seed_dynamodb
  seed_ssm
  echo "✅ [oneid-seeder] completed"
}

main "$@"
