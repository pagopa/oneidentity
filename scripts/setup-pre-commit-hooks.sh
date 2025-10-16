#!/usr/bin/env bash

# Run ./scripts/setup-pre-commit-hooks.sh from the root of the repo to setup pre-commit hooks

# Install required tools
brew install pre-commit yarn node

# Install frontend dependencies
cd src/oneid/oneid-fe
yarn install
cd ../oneid-control-panel
yarn install

# Install pre-commit hooks
cd ../../..
pre-commit install

echo "Pre-commit hooks are now active, try launching: pre-commit run --hook-stage push -a"
