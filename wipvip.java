CRM Data Ingest to Olympus – QE Team Documentation

1. Purpose

The CrmDataIngestToOlympusApplication is a Spring Boot application that replicates and publishes CRM (Customer Relationship Management) data from the primary system (main CRM where data is generated) to a secondary system (backup/reporting system).

This ensures:
	•	Redundancy – a backup copy of CRM data exists.
	•	Reporting – secondary system can be used for analytics, dashboards, or auditing.
	•	Reliability – failed messages are retried or persisted locally.

⸻

2. Key Functionalities
	•	Protobuf Object Creation
	•	The application creates Protobuf objects such as:
	•	CallReport
	•	ActionItem
	•	ClientMeeting
	•	ProductivityMetrics
	•	These are serialized into binary format before transmission.
	•	WebClient for API Calls
	•	Uses Spring WebClient to send HTTP POST requests.
	•	Adds authentication headers (X-IBM-Client-Id, X-IBM-Client-Secret) and Content-Type.
	•	Sends the binary payloads to API endpoints.
	•	Logging
	•	Logs start/end of Protobuf object creation.
	•	Logs every API call and its response (success/failure).
	•	Persistence / Retry
	•	If transmission fails, data is stored locally (CrmDatasetsPersistService) and retried later.

⸻

3. API Categories

GET APIs (Retrieve existing templates)
	•	ProtoTemplates: for managing data structures.
	•	/crm_olympus/actionitemprototemplate
	•	/crm_olympus/callreportprototemplate
	•	/crm_olympus/meetingprototemplate
	•	/crm_olympus/metricsprototemplates

POST APIs (Send actual CRM data)
	•	Data APIs: handle replication of CRM data.
	•	/crm_olympus/publishCallReportData
	•	/crm_olympus/publishClientActionItemData
	•	/crm_olympus/publishClientMeetingData
	•	/crm_olympus/PublishClientProductivityMetricsDetails

⸻

4. API Endpoints (UAT – via Apigee)

Example UAT URLs for QE to test:
	•	GET Requests
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/client/actionitemprototemplate
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/call/reportprototemplate
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/client/meetingprototemplate
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/client/metricsprototemplates
	•	POST Requests (require Protobuf client to send binary payloads)
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/publishCallReportData
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/publishClientActionItemData
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/publishClientMeetingData
	•	https://uat.isq.icgservices.citigroup.net/isg/internal/crm/olympus/PublishClientProductivityMetricsDetails

⚠️ Note: QE does not need to provide payloads manually – the Client Application generates the Protobuf payloads and calls these POST APIs.

⸻

5. Application Flow
	1.	Data Preparation
	•	Data is built into Protobuf objects (binary format).
	2.	Message Production
	•	CrmDatasetsProducer prepares the data.
	3.	Message Publishing
	•	CrmDatasetsMessagePublisher sends it via WebClient.
	4.	Transmission
	•	Sent over HTTPS POST → API endpoint (Apigee → Backend).
	5.	Persistence
	•	If transmission fails, it is stored locally and retried.

⸻

6. What QE Should Focus On
	•	Integration Testing
	•	Ensure POST calls reach secondary system successfully.
	•	Validate data matches expected structure.
	•	Error Handling
	•	Simulate network errors, invalid payloads.
	•	Verify retries and local persistence.
	•	Authentication
	•	Test that X-IBM-Client-Id and X-IBM-Client-Secret are required and working.
	•	Performance
	•	High load tests to ensure application scales.

⸻

7. Key Talking Points for QE Meeting
	•	Primary → Secondary system = replication of CRM data for backup and reporting.
	•	GET = templates (structure). POST = data replication (actual CRM data).
	•	Client application auto-generates POST payloads (QE doesn’t need to prepare them).
	•	QE role is mainly testing API calls via Apigee, validating responses, checking error handling, and ensuring retries work.