# AutoFix — Evaluación 2 (Mingeso, USACH)

Esta rama implementa la Evaluación 2 del curso Métodos y Técnicas de Ingeniería de Software. El proyecto original
de Evaluación 1 permanece en `autofix-backend/`; el código monolítico y la configuración de despliegue de esa
evaluación no se usan en el stack de microservicios. La rama `main` se mantiene intacta.

## Historias de usuario

| HU | Funcionalidad | Componente |
|---|---|---|
| HU1 | Registro de vehículos | M1 `vehicle-service` |
| HU2 | Catálogo de reparaciones y precios por motor | M2 `repair-list-service` |
| HU3 | Registro de reparaciones realizadas y cálculo del total al retiro | M3 `repair-service` |
| HU4 | Historial completo por vehículo | M3 `repair-service` |
| HU5 | Cantidad y monto por reparación y tipo de vehículo en un mes/año | M4 `report-service` |
| HU6 | Comparativo de cantidad y monto para el mes seleccionado y los dos anteriores | M4 `report-service` |

## Arquitectura

- Config Server centraliza `autofix-microservices/config-data`.
- Eureka registra instancias con puertos asignados dinámicamente.
- API Gateway es el único ingreso del frontend al backend.
- M1, M2 y M3 tienen bases PostgreSQL independientes; M4 es stateless.
- M1, M3 y M4 llaman otros microservicios con RestTemplate balanceado por Eureka.
- Cada API de microservicio se mantiene en capas de controladores, servicios, repositorios y entidades.

## Desarrollo con Docker Compose

Requiere Docker y Docker Compose. Desde la raíz del proyecto:

```bash
cd autofix-microservices
cp .env.example .env
# Asigna un valor fuerte a DB_PASSWORD en .env
docker-compose up --build
```

Frontend: `http://localhost:8088`. Config Server: `http://localhost:8081`. Eureka: `http://localhost:8761`.
No se publica el Gateway al host en Compose: el Nginx del frontend envía `/api` al Gateway interno.

## Imágenes y Kubernetes

Las imágenes independientes se publican en Docker Hub y se despliegan en el clúster descrito en
[autofix-microservices/k8s/README.md](autofix-microservices/k8s/README.md). El Gateway es el único componente
del backend expuesto por NodePort. El frontend usa otro NodePort para que el navegador pueda abrir la aplicación.
No se usa port-forward.

## Endpoints

- M1: `/api/v1/vehicles/`
- M2: `/api/v1/repair-types/`
- M3: `/api/v1/repairs/`, `/api/v1/repairs/history`, `/api/v1/repairs/report-data`
- M4: `/api/v1/reports/r1?month=4&year=2026`, `/api/v1/reports/r2?month=4&year=2026`

Todas las rutas del frontend empiezan por `/api` y llegan al backend a través del Gateway.

