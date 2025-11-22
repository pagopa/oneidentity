#!/usr/bin/env bash

# Run ./scripts/setup-pre-commit-hooks.sh from the root of the repo to setup pre-commit hooks

# Check if required tools are installed
if ! command -v node &> /dev/null; then
    echo "Error: Node.js is not installed. Please install from https://nodejs.org/"
    exit 1
fi

if ! command -v yarn &> /dev/null; then
    echo "Error: yarn is not installed. Run: npm install -g yarn"
    exit 1
fi

if ! command -v pre-commit &> /dev/null; then
    echo "Error: pre-commit is not installed. Run: pip install pre-commit"
    exit 1
fi

if ! command -v terraform &> /dev/null; then
    echo "Error: terraform is not installed. Please install from https://www.terraform.io/downloads"
    exit 1
fi

if ! command -v terraform-docs &> /dev/null; then
    echo "Error: terraform-docs is not installed. Please install from https://terraform-docs.io/user-guide/installation/"
    exit 1
fi

# Install frontend dependencies
cd src/oneid/oneid-fe
yarn install
cd ../oneid-control-panel
yarn install

# Install pre-commit hooks
cd ../../..
pre-commit install

echo "Pre-commit hooks are now active, try launching: pre-commit run --hook-stage push -a"
