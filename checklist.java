set -e
echo "Deleting the pod olympus-sb-user-consumption"
oc delete pod -l app=olympus-sb-user-consumption
echo "Waiting for the pod to be recreated"
sleep 60
echo "Pod restart completed successfully"