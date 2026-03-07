{{- define "elk.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "elk.fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "elk.labels" -}}
app.kubernetes.io/name: {{ include "elk.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
