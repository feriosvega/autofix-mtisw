#!/bin/bash
# Se ejecuta solo la primera vez que se crea el volumen de PostgreSQL.
# Buena práctica (clase 1): la aplicación NO usa el superusuario de la BD, sino usuarios propios
# dueños únicamente de su base de datos.
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-SQL
    CREATE USER autofix WITH PASSWORD '${APP_DB_PASSWORD}';
    CREATE DATABASE autofix OWNER autofix;
    CREATE USER keycloak WITH PASSWORD '${KEYCLOAK_DB_PASSWORD}';
    CREATE DATABASE keycloak OWNER keycloak;
SQL
