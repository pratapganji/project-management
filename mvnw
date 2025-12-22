# SB User Consumption – OpenShift (OCP) Migration & Technical Implementation

## 1. Overview
**Application Name:** 176352.olympus-data.olympus-sb-user-consumption  
**Purpose:** Batch job to read API usage data and persist consumption metrics into OM_CATALOGUE_USER_CONSUMPTION table.  
**Migration Goal:** Move from on-prem Linux execution to Dockerized OpenShift (OCP) CronJob for scalability, security, and operational consistency.

**Environments:** Dev / QA / Prod  
**Technology:** Python 3.8+, Docker, OpenShift (OCP), Tekton, Oracle, Impala  
**Repository:** https://github.com/CitiInternal/176352.olympus-data.olympus-sb-user-consumption

---

## 2. Existing (Pre-OCP) Architecture
- Application executed on on-prem Linux servers via shell scripts
- Python virtual environment (venv) tied to host
- Environment variables exported via shell
- Database credentials decrypted using ICP/CPP on server
- Logging written to local files
- Manual operational dependency on host access

---

## 3. Target Architecture (OpenShift)
- Application runs as **OpenShift CronJob**
- Dockerized Python runtime
- Configurations externalized using **ConfigMap**
- Secrets injected using **NGC / CyberArk backed OCP Secrets**
- Logs written to stdout → OCP logging
- No host dependency; fully platform managed

**Execution Flow:**
CronJob → Pod → Load ConfigMap JSON → Read Secrets (env) → Connect Impala & Oracle → Insert consumption records → Exit

---

## 4. Repository & Runtime Structure
gen_user_consumption.py
gen_user_consumption.sh
constants.json
gen_user_consumption.json
Dockerfile
manifests/
cronjob.yaml
configmap.yaml
secret-ref.yaml
serviceaccount.yaml
---

## 5. Containerization Details
**Base Image:** UBI Python  
**Security:** Runs as non-root user  
**Dependency Install:** pip install via requirements  
**Entrypoint:** gen_user_consumption.sh → gen_user_consumption.py  

**Key Decisions:**
- Non-root container for security compliance
- No file-based logging; stdout only
- JSON-driven configuration to avoid rebuilds

---

## 6. Configuration Management
### Environment Variables
| Variable | Source |
|-------|-------|
| ENV | Deployment YAML |
| DB_HOST | ConfigMap |
| DB_USER | ConfigMap |
| DB_PASSWORD | Secret (NGC) |
| IMPALA_USER | ConfigMap |
| IMPALA_PASSWORD | Secret (NGC) |

### ConfigMaps
- Application configuration JSON
- Environment-specific DB endpoints
- Feature toggles and thresholds

### Secrets
- Stored in **NGC / CyberArk**
- Mounted as OCP Secrets
- Injected as environment variables
- No plaintext secrets in repo or image

---

## 7. OpenShift Objects Used
- Namespace: `icg-isg-olympus-data-176352`
- CronJob
- ConfigMap
- Secret (NGC reference)
- ServiceAccount
- Role / RoleBinding

---

## 8. Build & Deployment Process
1. Code commit to GitHub
2. Tekton pipeline builds Docker image
3. Image pushed to internal OCP registry
4. CronJob YAML applied
5. Pod triggered by schedule
6. Logs verified via `oc logs`

---

## 9. Resource & Capacity Model
**Observed Quotas:**
- limits.cpu: 10
- limits.memory: 80Gi
- pods: 20

**Explanation (10 CPU ≈ 60 comparison):**
- OCP CPU limits represent **core-equivalent quota**, not static reservation
- Batch jobs utilize burstable CPU within quota
- Actual SB usage observed ~550m CPU
- NuraFlow comparison refers to workload concurrency and node sizing, not direct 1:1 CPU core mapping

---

## 10. Validation & Testing
**Commands Used:**
oc get cronjob -n icg-isg-olympus-data-176352
oc get pods -n icg-isg-olympus-data-176352
oc logs
**Validation Performed:**
- Pod starts successfully
- Impala connection established
- Oracle connection established
- Records inserted into OM_CATALOGUE_USER_CONSUMPTION
- Job exits cleanly
- Logs visible in OCP

---

## 11. Issues Faced & Resolutions
| Issue | Root Cause | Resolution |
|----|----|----|
| Env vars not visible | Missing envFrom | Used ConfigMap envFrom |
| Script logs missing | File logging | Switched to stdout |
| DB auth failure | Incorrect secret mapping | Corrected NGC secret reference |
| JSON parse error | Payload formatting | Sanitized JSON via ObjectMapper |

---

## 12. Security Considerations
- Non-root container execution
- Secrets never stored in code or image
- RBAC-controlled service account
- NGC / CyberArk compliance
- Image scanning enabled

---

## 13. Rollback Strategy
oc rollout undo cronjob/
- Revert to previous image tag
- No data impact due to idempotent design

---

## 14. Lessons Learned
- JSON-based config simplifies environment portability
- Stdout logging is mandatory for container observability
- Secrets must be planned before DB connectivity
- ResourceQuota ≠ performance bottleneck

---

## 15. References
- OpenShift Documentation
- Internal Tekton Guidelines
- NGC / CyberArk Secret Management
- SB User Consumption Repository
