Target server URL vs VIP vs WIP
	•	Each backend server (dev/qa/uat/prod) has its own target server URL (hostnames differ per node).
	•	In PROD, there are two clusters: PROD and COB (COB = Continuity Of Business). Example: 3 PROD nodes + 3 COB nodes ⇒ 6 target URLs.
	•	Create a VIP (Virtual IP) over the 3 PROD nodes, and another VIP over the 3 COB nodes. VIPs do load balancing (e.g., round-robin) across their 3 servers.
	•	Create a WIP (Wide IP) on top of the two VIPs. Flow becomes: WIP → (VIP-PROD or VIP-COB) → one of 3 servers. This distributes traffic across all 6 servers.
	•	Apigee sits in front: Consumer → Apigee (authn/authz) → WIP → VIPs → servers.
	•	Current pain
	•	Today, target server URLs are HTTP, so WIP and VIP are HTTP, while Apigee is HTTPS.
	•	Because the underlying hop is HTTP, Apigee keeps generating alerts. We need to flip the whole chain under Apigee to HTTPS: target servers → VIPs → WIP → (then Apigee stays HTTPS).
	•	Why not a single “big bang” change in PROD
	•	During Green Zone (Sat evening → Sun; ~16–24h) you could:
	1.	change target URLs to HTTPS, 2) update VIPs, 3) update WIP, 4) update Apigee.
	•	But doing all live, in sequence, is risky and hard to roll back (no quick revert if you mutate the only live chain).
	•	Preferred approach (safer)
	•	Make a replica deployment in PROD (a second API deployment with a new name, no consumers initially).
	•	Build new HTTPS chain for it end-to-end: target URLs (HTTPS) → VIPs (HTTPS) → WIP (HTTPS) and wire that under a new Apigee proxy (or same proxy with new target, but isolated).
	•	Test in DEV/UAT with users, then switch Apigee routing in PROD from old WIP to new WIP.
	•	Rollback is simple: switch Apigee back to old WIP if something breaks.
	•	ECS would solve HTTPS more cleanly, but capacity isn’t available yet. Until ECS arrives, proceed with the VM-based replica.
	•	Testing
	•	There are no Postman collections (protobuf). Use the in-house client code (“standalone client”) to hit endpoints and validate end-to-end (request → Kafka).
	•	Sachin will provide the client and prior APIM→Apigee migration notes (useful context).
	•	Ownership & tools
	•	VIP/WIP are managed in the CAS portal (Rupesh/APIs team can help).
	•	Timeline target: UAT by early Nov, PROD later via switch-over.
	•	Immediate asks: coordinate with Kumar (ECS capacity), Shan & networking team for VIP/WIP, and Rohit for testing; Aparna for subscription follow-up.