Header parsing & rate limiting
	•	Logs show headers parsed without exception.
	•	“Permit acquired” appears before each outbound POST (rate limiter active).
	•	No throttling/back‑pressure errors.

Stability & metrics (first 30–60 min)
	•	Pod restarts = 0.
	•	CPU/Memory within normal envelope.
	•	Error rate near baseline; health‑check failures = 0.

Documentation & handover
	•	Record: artifact tag, namespace, Helm values, timestamp, validation SQL/IDs, outcomes.
	•	Update runbook with:
	•	Health check URL pattern (built from API_ENDPOINT domain + /olympus/service/health).
	•	How to pause (scale replicas to 0) and resume.
	•	Rollback steps.

Rollback triggers (execute if any hit)
	•	Persistent PKIX/DNS/5xx bursts or DB pool exhaustion.
	•	Rapid growth of FAILED with identical client error across many rows.
	•	Unexpected load on Bulk API beyond agreed QPS.

Rollback steps
	•	Use Harness → Executions → select previous green → Rollback/Deploy Previous Artifact.
	•	Confirm pod healthy, repeat quick validation.

⸻

Notes tailored to this scheduler
	•	The scheduler reads API_ENDPOINT, REQUEST_PAYLOAD, and HEADERS from OM_NURAFLOW_AUDIT_DATA.
	•	It extracts the domain from API_ENDPOINT, appends HEALTH_CHECK_ENDPOINT (/olympus/service/health), and only retries when HTTP 200 is returned.
	•	Outbound POST is executed through RestTemplateService.sendRequest(...) with the parsed headers.
	•	rateLimit and burstTime enforce QPS and short spikes to remain within Bulk API SLOs.