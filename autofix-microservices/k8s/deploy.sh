#!/usr/bin/env bash
# Despliega AutoFix (Evaluación 2) en el clúster de Kubernetes actual (minikube).
# Uso: DOCKERHUB_USER=<usuario> ./deploy.sh
set -euo pipefail
: "${DOCKERHUB_USER:?Define DOCKERHUB_USER (usuario de Docker Hub con las imágenes)}"
DIR="$(cd "$(dirname "$0")" && pwd)"
for manifest in 00-namespace.yaml 01-secrets.yaml 01-configmap.yaml 02-databases.yaml 03-platform.yaml 04-microservices.yaml 05-frontend.yaml; do
  sed "s/__DOCKERHUB_USER__/$DOCKERHUB_USER/g" "$DIR/$manifest" | kubectl apply -f -
done
IP="$(minikube -p "${MINIKUBE_PROFILE:-minikube}" ip 2>/dev/null | tail -1)"
echo
echo "Frontend:    http://${IP:-<minikube ip>}:30081"
echo "API Gateway: http://${IP:-<minikube ip>}:30080"
