#!/bin/bash

set -e

# Function to display script usage
usage() {
    echo "Usage: $0 <action> <environment> [<region>] [other terrafrm allowed paramenter]"
    exit 1
}

# Default values
ACTION=""
ENVIRONMENT=""
REGION=""
OTHERS=""

# Check if AWS_REGION environment variable is set
if [ -n "$TF_REGION" ]; then
    REGION="$TF_REGION"
fi

# Parse command line options
while [ "$#" -gt 0 ]; do
    case "$1" in
        init|apply|plan|import|output|console|refresh|destroy)
            ACTION="$1"
            ;;
        dev|uat|prod) 
            ENVIRONMENT="$1"
            ;;
        eu-south-1|eu-central-1) 
            REGION="$1"
            ;;
        \?)
            usage
            ;;
        *)
            OTHERS=$@
            ;;
    esac
    shift
done

# Check if mandatory options are provided
if [ -z "$ACTION" ] || [ -z "$ENVIRONMENT" ]; then
    echo "Error: Action and environment are mandatory."
    usage
fi

# Build the path
path="env/$ENVIRONMENT"

if [ -n "$REGION" ]; then
    path="$path/$REGION"
fi


# Display the provided options
echo "Action: $ACTION"
echo "Environment: $ENVIRONMENT"
echo "Region: $REGION"
echo "Others: $OTHERS"
echo "Path: $path"

if [ $ACTION = "init" ]; then
  terraform $ACTION -backend-config="./$path/backend.tfvars" $OTHERS
elif [ $ACTION = "output" ] || [ $ACTION = "state" ] || [ $ACTION = "taint" ]; then
  terraform $ACTION $OTHERS
else
  terraform $ACTION -var-file="./$path/terraform.tfvars" $OTHERS
fi