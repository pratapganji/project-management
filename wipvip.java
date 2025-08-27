“I’m working on the HTTPS migration for CRM Olympus APIs. Specifically, I’m helping to update the target servers, VIPs, and WIP from HTTP to HTTPS, and validating the flow through Apigee to Kafka with the standalone client.”


“Right now, my task is to prepare and test the replica deployment of CRM Olympus APIs with HTTPS enabled end-to-end — updating target server URLs, configuring new VIP/WIP in the CAS portal, and then validating with our protobuf client before we switch routing in PROD.”


“I’m working on the CRM Olympus HTTPS cutover — setting up and testing the new HTTPS chain (target servers → VIPs → WIP → Apigee).”


	“Kafka here is the downstream message bus where our CRM Olympus APIs publish events. My job is to test that when we hit the API via Apigee, the expected protobuf message gets published to Kafka correctly.”
