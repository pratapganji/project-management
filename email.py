import requests
import json


def send_email_notification(params):

    # REST endpoint URL
    url = "http://localhost:8080/notification"

    # Request payload
    payload = {
        "to": params.get("to"),
        "cc": params.get("cc"),
        "subject": params.get("subject"),
        "body": params.get("body"),
        "attachments": params.get("attach", [])
    }

    # Optional headers
    headers = {
        "Content-Type": "application/json"
    }

    try:
        response = requests.post(
            url,
            headers=headers,
            data=json.dumps(payload),
            timeout=30
        )

        # Success check
        if response.status_code in [200, 201, 202]:
            msg = "Notification email sent successfully"
            return True, msg

        else:
            msg = f"Failed to send notification. Status Code: {response.status_code}, Response: {response.text}"
            return False, msg

    except Exception as e:
        msg = "Exception while calling notification API: " + str(e)
        return False, msg
