#!/usr/bin/env bash

# Run ./scripts/setup-pre-commit-hooks.sh from the root of the repo to setup pre-commit hooks

# Install required tools
brew install pre-commit yarn node

# Install all workspace dependencies from root
yarn install

# Install pre-commit hooks
pre-commit install

echo "Pre-commit hooks are now active, try launching: pre-commit run --hook-stage push -a"
