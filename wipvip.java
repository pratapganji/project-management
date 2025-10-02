• Keep the JKS only on the server and point the JVM or client libraries to it.

• Do NOT use Spring Boot server.ssl.* with cacerts.jks (it is a truststore, not a keystore with a private key).

Where the truststore lives
/data/1/gfolybkb/bin/CRM_Olympus_Config/config/cacerts.jks

Current environment properties (already working)
These are set in crm-olympus-application.properties and point Kafka to the truststore:

KAFKA_TRUST_STORE=/data/1/gfolybkb/bin/CRM_Olympus_Config/config/cacerts.jks

KRB5_CONFIG=/data/1/gfolybkb/bin/CRM_Olympus_Config/config/krb5.conf

KAFKA_LOGIN_CONF=/data/1/gfolybkb/bin/CRM_Olympus_Config/config/gfolybkb.jaas.client.conf

application.properties – what to keep / remove
Keep Spring external config import (if you need it):

spring.config.import=file:/data/1/gfolybkb/bin/CRM_Olympus_Config/config/crm-olympus-application.properties

Remove any lines that try to use cacerts.jks as a Spring Boot keystore (these cause the “Alias does not identify a key entry” error):

server.ssl.enabled=true

server.ssl.key-store=file:/data/1/gfolybkb/bin/CRM_Olympus_Config/config/cacerts.jks   # WRONG for truststore

server.ssl.key-store-password=changeit

server.ssl.key-store-type=JKS

server.ssl.key-alias=development (or springboot)

Explanation: server.ssl.* config is only for hosting HTTPS on the embedded Tomcat and requires a keystore with a PRIVATE KEY (not a truststore).

Outbound HTTPS (Apigee, etc.) – set the JVM truststore
Add the following to start_app.sh (or the uDeploy “Start Service” step) so all outbound HTTPS uses the server truststore:

JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.trustStore=/data/1/gfolybkb/bin/CRM_Olympus_Config/config/cacerts.jks"

JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.trustStorePassword=changeit"

If DEV and UAT have different truststores, set the path conditionally based on ${p:environment.name} in your uDeploy shell step (no need to copy if the file already exists).

uDeploy script notes
• You can leave the copy lines commented if the truststore already exists in the server config folder.

• The “Start Service” step should only launch the app and pass the JVM trustStore flags (no copy needed).

• The “Symlink update” step should not copy the truststore; it only updates the app symlink.

Quick checklist
• Verify the truststore contains ONLY trusted certs (no key entry):

 keytool -list -keystore /data/1/gfolybkb/bin/CRM_Olympus_Config/config/cacerts.jks -storepass changeit

• Ensure file permissions allow the app user to read it:  chmod 640 cacerts.jks; chown <appuser>:<appgroup> cacerts.jks

• Remove any server.ssl.key-store=...cacerts.jks lines from application.properties.

• Confirm Kafka/APIs work from the server with the truststore in place.

FAQ
Q: Do we ever need keystore.p12?
A: Only if the application must HOST HTTPS itself (embedded Tomcat). That is not required for calling Apigee/Kafka; for outbound TLS only the truststore is needed.






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