Below is the exact wording you should put in Short Description and Description for the Autosys issue shown in your screenshots.

Your job:
1676960_OM_CRM_OLYMPUS_API_NEW_SERVER1_START on DA5
is repeatedly failing with exit code 13, even when triggered manually.

⸻

✅ Short Description (copy-paste this)

Autosys job 1676960_OM_CRM_OLYMPUS_API_NEW_SERVER1_START is failing with Exit Code 13 on DA5

⸻

✅ Description (copy-paste this)

The Autosys job 1676960_OM_CRM_OLYMPUS_API_NEW_SERVER1_START in DA5 environment is failing repeatedly.
The job fails within a few seconds after starting and always returns Exit Code 13.
No recent code changes were made from application side.
The job is supposed to start the CRM Olympus API New Server1 instance, but the script does not execute successfully.
Please investigate Autosys logs, machine agent, permissions, and underlying script bin/start_sp.sh.
We need assistance to identify the root cause and fix the failure.

Additional details:
	•	Environment: DA5
	•	Machine: sd-olb5-oob01.nam.nsroot.net
	•	Job Type: CMD job
	•	Issue started on: Nov 16, 2025
	•	Behavior: Shows “Event was scheduled based on job definition”, then fails immediately
	•	Requesting SA/Autosys team to validate node health, script path, job owner, permissions, and autosyslog**
