#!/bin/bash
# Keycloak exige HTTPS para IPs públicas (sslRequired=external). Para el proyecto se usa HTTP (clase 9),
# así que se desactiva en el realm "master" (consola de administración) y en "autofix-realm".
# Uso (en el servidor, dentro de deployment/):  ./scripts/keycloak_http.sh
set -e
P=$(grep ^KEYCLOAK_ADMIN_PASSWORD .env | cut -d= -f2)
docker exec autofix-keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080/auth --realm master --user admin --password "$P"
for realm in master autofix-realm; do
  docker exec autofix-keycloak /opt/keycloak/bin/kcadm.sh update realms/$realm -s sslRequired=NONE
  echo "sslRequired=NONE en $realm"
done
