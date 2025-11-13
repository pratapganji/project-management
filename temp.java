apiVersion: batch/v1
kind: CronJob
metadata:
  name: {{ .Values.app.name }}
spec:
  schedule: {{ .Values.cron.schedule | quote }}
  successfulJobsHistoryLimit: 3
  failedJobsHistoryLimit: 3
  jobTemplate:
    spec:
      backoffLimit: 1
      template:
        metadata:
          labels:
            app: {{ .Values.app.name }}
        spec:
          restartPolicy: Never
          containers:
            - name: {{ .Values.app.name }}
              image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
              imagePullPolicy: {{ .Values.image.pullPolicy }}
              env:
                - name: APP_PROFILE
                  value: {{ .Values.env.PROFILE | quote }}
              resources:
                requests:
                  {{- toYaml .Values.resources.requests | nindent 16 }}
                limits:
                  {{- toYaml .Values.resources.limits | nindent 16 }}