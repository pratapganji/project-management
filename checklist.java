Here’s a clean, production‑ready checklist you can share with Ops/QE for the BulkRetryScheduler rollout. It’s specific to your job that retries rows from OM_NURAFLOW_AUDIT_DATA and calls the Olympus Bulk API only when the /olympus/service/health check returns 200.

Pre‑Deployment (Production)

Approvals & windows
	•	Change/ROD raised, approved, and within Prod window.
	•	Stakeholders notified (API owners, DBAs, QE, L2/L3 Ops), rollback owner named.

Artifact & branch
	•	PRs merged to release branch; CI green.
	•	Artifact/image tag to deploy recorded: olympus-nura-flow:<TAG>.

Config & secrets
	•	application-prod.yml reviewed:
	•	bulkapiconfig.scheduleFixedRate (ms) = agreed cadence (e.g., 3600000 = 1h).
	•	bulkapiconfig.rateLimit & burstTime match Bulk API rate policy.
	•	Oracle/JDBC points to Prod; secrets pulled from vault/secret (no hardcoding).
	•	AuditTableConstants.HEALTH_CHECK_ENDPOINT = "/olympus/service/health".
	•	Java truststore / corporate truststore contains certificates for the Prod Bulk API host (no PKIX errors).
	•	Outbound allow‑list/firewall rules permit calls from the scheduler pod to Bulk API and to Oracle.

Environment targeting
	•	Harness Service/Environment/Infrastructure points to Prod namespace & correct pod group (e.g., not UAT 23u/24u).
	•	Helm values (ConfigMap/Secrets/namespace/replicas) verified.
	•	Replicas set to 1 (or leader election enabled) to avoid double retries.

Observability
	•	Log level = INFO; logs flowing to Splunk/ELK.
	•	Alerts configured: pod crash/restarts, non‑2xx spikes, consecutive health‑check failures, DB connection errors.

Test data & queries ready (read‑only sanity)
	•	Verify table exists and counts look normal: