We are migrating CRM Olympus API endpoints from HTTP to HTTPS for VIP and WIP to align with enterprise security standards.

For this deployment, we are introducing a new WIP target server URL using a new uDeploy component. The existing PROD setup remains unchanged.

Apigee consumer URLs are still pointing to the current PROD target server, so there is no impact to live consumers.

This change is isolated and additive. Apigee will be updated in a separate controlled PROD deployment once validation is complete.

The change has been successfully validated in DEV and UAT.
Low risk, no business logic impact.
Rollback is available by disabling the new component and reverting to the existing target server configuration.




We are deploying a new HTTPS WIP target server for CRM Olympus API using a new uDeploy component. The existing PROD setup remains unchanged, and Apigee consumers are still connected to the current PROD target server, so there is no production impact. This change has been validated in DEV and UAT. Low risk, with rollback available if needed.