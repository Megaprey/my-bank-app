{{- define "zipkin.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "zipkin.fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "zipkin.labels" -}}
app.kubernetes.io/name: {{ include "zipkin.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
