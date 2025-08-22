	•	Dry‑run IDs (if needed) identified with STATUS=‘Failed’ and valid Prod endpoints.

Rollback plan
	•	Previous stable artifact tag recorded.
	•	Harness “Redeploy previous” or “Rollback” path tested in non‑prod.
	•	DB changes: none required for rollback (stateless deploy).

⸻

Deployment (Production)

Harness steps
	•	In Pipeline, confirm Service/Environment/Infra & artifact tag.
	•	Review Helm diff/dry‑run (config/secret/name/namespace).
	•	Execute Rollout.

Kubernetes health
	•	Deployment Ready; no CrashLoopBackOff.
	•	Liveness/readiness probes pass.

Startup logs (first minute)
	•	“Starting Bulk Retry Scheduler…”
	•	“RateLimiter initialized with rate = X requests/second with burst time Y”
	•	Hikari/Oracle pool up; no auth/network errors.

Connectivity smoke
	•	On first tick (or manual trigger if you temporarily lowered schedule in a canary):
	•	GET to …/olympus/service/health returns HTTP 200 (visible in logs).
	•	No PKIX path building failed, DNS, or timeout errors.

Change communication
	•	Post “Deployed v to Prod; monitoring for N minutes” in team channel.

⸻

Post‑Deployment (Production Validation)

Functional checks (use a tiny sample)
	•	For 1–3 existing FAILED records (or those that naturally occur):
	•	When Bulk API returns 2xx → row status changes to SUCCESS, response stored.
	•	When 4xx → “Client error…” logged, row remains FAILED (will not flip to success).
	•	When 5xx → “Server error…” logged, row remains FAILED (will retry next run).
	•	Verify with: