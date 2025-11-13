app:
  name: olympus-sb-user-consumption

image:
  repository: docker-icg-dev-local.artifactory.citigroup.net/icg-isg-olympus/olympus-sb-user-consumption
  tag: "1.0.0"
  pullPolicy: IfNotPresent

cron:
  schedule: "0 3 * * 1"

resources:
  requests:
    cpu: "500m"
    memory: "1Gi"
  limits:
    cpu: "1500m"
    memory: "3Gi"

env:
  PROFILE: "dev"