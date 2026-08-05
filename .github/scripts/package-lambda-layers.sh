#!/usr/bin/env bash
#
# Purpose: Build deterministic ZIPs for the PyJWT and cryptography Lambda layers.
# Usage examples:
#   ./.github/scripts/package-lambda-layers.sh
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

DIST_DIR="${ROOT_DIR}/src/infra/dist/layers"
CLIENT_MANAGER_REQUIREMENTS="${ROOT_DIR}/src/oneid/oneid-lambda-client-manager/requirements.txt"
CERT_EXP_CHECKER_REQUIREMENTS="${ROOT_DIR}/src/oneid/oneid-lambda-cert-exp-checker/requirements.txt"

package_layer() {
  local requirements_file="$1"
  local python_version="$2"
  local layer_directory="$3"
  local archive_path="$4"

  rm -rf -- "$layer_directory"
  rm -f -- "$archive_path"
  mkdir -p "$layer_directory/python"

  python3 -m pip install \
    --platform manylinux2014_x86_64 \
    --target "$layer_directory/python" \
    --implementation cp \
    --python-version "$python_version" \
    --only-binary=:all: \
    --no-compile \
    --upgrade \
    -r "$requirements_file"

  find "$layer_directory" -exec touch -t 198001010000 {} +
  (
    cd "$layer_directory"
    LC_ALL=C find python -print | LC_ALL=C sort | zip -Xq "$archive_path" -@
  )
}

mkdir -p "$DIST_DIR"

echo "ℹ️  Packaging PyJWT layer"
package_layer "$CLIENT_MANAGER_REQUIREMENTS" "3.12" "${DIST_DIR}/pyjwt-layer" "${DIST_DIR}/pyjwt-layer.zip"

echo "ℹ️  Packaging cryptography layer"
package_layer "$CERT_EXP_CHECKER_REQUIREMENTS" "3.10" "${DIST_DIR}/cryptography-layer" "${DIST_DIR}/cryptography-layer.zip"

echo "✅ Lambda layer archives are ready in ${DIST_DIR}"