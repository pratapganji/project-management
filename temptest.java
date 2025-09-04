1.	How JARs are versioned and released
	•	Each time you generate a new JAR, you must increment the version number and update the build date inside configuration files (build_artifactory.xml, sometimes pom.xml).
	•	This ensures every JAR is unique and traceable.
	2.	Where to make changes
	•	In Bitbucket, inside the repo citifix, branch feature/upgradeSpringOlyORAAS.
	•	The file build_artifactory.xml contains conditions and properties for different modules (OES, EQRIOIntf8Oracle, Risk, DPMRIO, UMSR).
	•	The file pom.xml may also require version bumps.
	3.	What kind of changes
	•	Change version: 3.120 → 3.121.
	•	Change date: 20250806 → 20250812.
Old: OESIntf-3.120-20250806
New: OESIntf-3.121-20250812

4.	How to commit changes
	•	After updating files, commit with a standard message format like:
OES, EQRIOIntf8OracleWithPOM, Risk, DPMRIO, UMSR prod jar generation
	•	For quick fixes (pom.xml), use:
qfix jar generation.
	5.	How builds are triggered
	•	Once changes are pushed, you go to TeamCity SWAYAM project.
	•	Select builds (e.g., EQRIOIntf8Oracle, DPMRIOIntf8Oracle) and run them.
	6.	How to check build results
	•	TeamCity shows build history and status.
	•	Green = success → JAR uploaded to Artifactory.
	•	Red = failure → check logs and errors.
	7.	Final Outcome
	•	A new JAR is generated with updated version & date.
	•	It is published to Artifactory where other teams/projects can use it
In simple terms:
He is explaining the end-to-end process of JAR release – starting from checking commits in Bitbucket, making version/date changes in configuration files, committing them with proper messages, triggering TeamCity builds, and finally verifying that new JARs are published to Artifactory.


🚀 SOP – Citify JAR Generation Workflow  

Step 1 – Open Repository
- Go to Bitbucket → Project: 176352-OLYMPUS-SWAYAM → Repo: citifix.
- Checkout branch: feature/upgradeSpringOlyORAAS.

Step 2 – Review Commits
- Go to Commits tab.
- Refer to the latest 3 commits in sequence.
- Look for commit messages like:
  OES, EQRIOIntf8OracleWithPOM, Risk, DPMRIO, UMSR prod jar generation

Step 3 – Update build_artifactory.xml
1. Open the file: build_artifactory.xml
2. Search for lines containing version & date (examples: 94, 406, 416, 422, 466, 502, 812, 815, 816, 821, 827, 832).
3. Increment the version by +1.
   Example: 3.120 → 3.121
4. Update the date to the new build date (YYYYMMDD).
   Example: 20250806 → 20250812
5. Save changes.

Example change:
Old: OESIntf-3.120-20250806
New: OESIntf-3.121-20250812

Step 4 – Commit Changes
- Commit with the same message format:
  OES, EQRIOIntf8OracleWithPOM, Risk, DPMRIO, UMSR prod jar generation

Step 5 – Update pom.xml (if required)
1. Open pom.xml
2. Update the version line.
   Example:
   <version>1.3_C54</version> → <version>1.3_C55</version>
3. Save & commit with message:
   qfix jar generation

Step 6 – Trigger Build in TeamCity
1. Go to TeamCity (SWAYAM project).
   URL: https://teamcity.isg.icgbuild.nam.nsroot.net
2. Locate the builds:
   - EQRIOIntf8Oracle
   - DPMRIOIntf8Oracle
   - OESIntf8Oracle
   - UMSRIntf
3. Run the build(s).

Step 7 – Verify Build Status
- Check status in TeamCity:
  Green = Success → JAR generated.
  Red = Failed → check logs.
- Each successful run publishes the JAR to Artifactory with the updated version/date.

✅ Quick Checklist
[ ] Open citifix repo → branch feature/upgradeSpringOlyORAAS
[ ] Review last 3 commits
[ ] Update build_artifactory.xml → bump version + date
[ ] Commit with proper message
[ ] Update pom.xml if needed → bump version
[ ] Commit with qfix jar generation
[ ] Trigger build in TeamCity (SWAYAM)
[ ] Verify success + artifact in Artifactory