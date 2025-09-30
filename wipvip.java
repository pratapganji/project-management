#!/bin/sh

# Assign the environment variable (from Jenkins parameter)
ENV_NAME="${p:environment.name}"

# Define base paths
SOURCE_BASE="common"
DESTINATION="/data/1/gfolybkb/bin/CRM_Olympus_Config/config"

# Conditional logic based on environment
if [ "$ENV_NAME" = "DEV" ]; then
    echo "Environment is DEV. Copying DEV cacert.jks..."
    cp "$SOURCE_BASE/DEV/cacert.jks" "$DESTINATION/"
elif [ "$ENV_NAME" = "UAT" ]; then
    echo "Environment is UAT. Copying UAT cacert.jks..."
    cp "$SOURCE_BASE/UAT/cacert.jks" "$DESTINATION/"
else
    echo "Unknown environment: $ENV_NAME"
    exit 1
fi