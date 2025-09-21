Subject: JIRA Details & API List for CRM Olympus HTTPS Migration

Hi Param,

As requested, please find below the details of the APIs impacted by the CRM Olympus HTTPS migration, along with the updated endpoints for QE testing.

JIRA References:
	•	[C167969S-104428] Target Server URL to HTTPS for CRM Olympus API
	•	[C167969S-117341] Improve Code Coverage for CRM Olympus API
	•	[C167969S-117342] Fix Vulnerabilities in CRM Olympus API

API Endpoints Updated to HTTPS:

DEV (VIP/WIP):
	•	https://olympus.api.crmtoolympusnew.dev.cloudgsl.nam.nsroot.net/crm_olympus/health
	•	https://olympus.api.crmtoolympusnew.dev.cloudgsl.nam.nsroot.net/crm_olympus/publishCallReportData
	•	https://olympus.api.crmtoolympusnew.dev.cloudgsl.nam.nsroot.net/crm_olympus/publishClientActionItemData
	•	https://olympus.api.crmtoolympusnew.dev.cloudgsl.nam.nsroot.net/crm_olympus/publishClientMeetingData
	•	https://olympus.api.crmtoolympusnew.dev.cloudgsl.nam.nsroot.net/crm_olympus/publishClientProductivityMetricsDetails
	•	https://olympus.api.crmtoolympusnew.dev.cloudgsl.nam.nsroot.net/crm_olympus/clientMeetingProtoTemplate

UAT (VIP/WIP):
	•	https://olympus.api.crmtoolympusnew.uat.cloudgsl.nam.nsroot.net:2023/crm_olympus/health
	•	https://olympus.api.crmtoolympusnew.uat.cloudgsl.nam.nsroot.net:2023/crm_olympus/publishCallReportData
	•	https://olympus.api.crmtoolympusnew.uat.cloudgsl.nam.nsroot.net:2023/crm_olympus/publishClientActionItemData
	•	https://olympus.api.crmtoolympusnew.uat.cloudgsl.nam.nsroot.net:2023/crm_olympus/publishClientMeetingData
	•	https://olympus.api.crmtoolympusnew.uat.cloudgsl.nam.nsroot.net:2023/crm_olympus/publishClientProductivityMetricsDetails
	•	https://olympus.api.crmtoolympusnew.uat.cloudgsl.nam.nsroot.net:2023/crm_olympus/clientMeetingProtoTemplate

Additional Notes:
	•	VIP and WIP URLs for both DEV and UAT have been created and updated.
	•	Currently, we are resolving Jenkins build pipeline issues; once resolved, these changes will be deployed to UAT.
	•	We will notify QE as soon as the changes are available in UAT for validation.