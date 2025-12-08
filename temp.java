DEBUG = True

try:
    from .settings import *
except ImportError as e:
    raise Exception(f"Failed to import shared settings!! with error: {str(e)}")

import django.conf as conf
import os
from subprocess import PIPE, run

# ------------------------------------------------------------
# 1. CyberArk / NGC Secret Configuration
# ------------------------------------------------------------

# CSI ID from CMP (Olympus application CSI)
CSI_ID = "167969"

# Secret nickname injected by Helm (from dev-namicggtd41d-values.yaml)
SECRET_NICKNAME = os.environ.get("SecretNickName")
if not SECRET_NICKNAME:
    raise Exception("Environment variable 'SecretNickName' is not set. "
                    "Ensure Helm values have SecretNickName: olympus_sbuc_db_password")

# ------------------------------------------------------------
# 2. Fetch secret from CyberArk (FID DB password)
# ------------------------------------------------------------

cmd = f"fngp getSecret --SecretNickName={SECRET_NICKNAME} --csiid={CSI_ID}"
process = run(cmd, stdout=PIPE, stderr=PIPE, shell=True, text=True)

stdout_msg = process.stdout.strip()
stderr_msg = process.stderr.strip()

print(f"[NGC] stdout: {stdout_msg!r}")
print(f"[NGC] stderr: {stderr_msg!r}")

if process.returncode != 0 or "failed" in stderr_msg.lower():
    raise Exception(f"Failed to fetch DB password from CyberArk. Details: {stderr_msg}")

DB_PASSWORD = stdout_msg  # final password from CyberArk

# ------------------------------------------------------------
# 3. Put values into Django CONFIG so other modules can access
# ------------------------------------------------------------

conf.settings.CONFIG.update({
    "env": "dev",
    "db_pass": DB_PASSWORD,   # this is what gen_user_consumption.py will use
})

print("[NGC] Secret successfully loaded into CONFIG['db_pass']")