#!/usr/bin/env bash
set -euo pipefail
: "${DOCKERHUB_USER:?Define DOCKERHUB_USER}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SERVICES=(config-service eureka-service gateway-service vehicle-service repair-list-service repair-service report-service)
for service in "${SERVICES[@]}"; do
  image="${DOCKERHUB_USER}/autofix-${service}:eval2"
  docker build -t "$image" "$ROOT/$service"
  docker push "$image"
done
image="${DOCKERHUB_USER}/autofix-frontend:eval2"
docker build -t "$image" "$ROOT/../autofix-frontend"
docker push "$image"
