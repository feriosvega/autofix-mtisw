# AutoFix: Sistema de Gestión de Reparaciones

Evaluación 1, Métodos y Técnicas de Ingeniería de Software (USACH).

Aplicación web monolítica organizada por capas para la cadena de talleres **AutoFix**. Permite registrar
vehículos y reparaciones, calcular el costo total de cada reparación y generar los reportes R1 a R4.

| Componente | Tecnología |
|---|---|
| Frontend | React 18 + Vite + Material UI (`autofix-frontend/`) |
| Backend | Spring Boot 3.3 / Java 17: Spring Web, Spring Data JPA, PostgreSQL Driver, Lombok (`autofix-backend/`) |
| Base de datos | PostgreSQL 16 |
| IAM | Keycloak 26 (realm `autofix-realm`, roles `ADMIN` y `USER`) |
| Despliegue | Docker Compose: 3 réplicas del backend, 3 del frontend y Nginx como balanceador (`deployment/`) |
| CI/CD | GitHub Actions: tests, imágenes Docker y publicación en Docker Hub (`.github/workflows/`) |

## Despliegue actual

- App: http://138.197.52.16:8088 (DigitalOcean)
- API / Swagger: http://138.197.52.16:8090/swagger-ui/index.html
- Imágenes: [`fernandorios1/autofix-backend`](https://hub.docker.com/r/fernandorios1/autofix-backend) · [`fernandorios1/autofix-frontend`](https://hub.docker.com/r/fernandorios1/autofix-frontend)

## Historias de usuario

| HU | Funcionalidad | Backend | Frontend | Rol |
|---|---|---|---|---|
| HU1 | Registro de vehículos | `VehicleController` → `VehicleService` | `VehicleList`, `AddEditVehicle` | USER, ADMIN |
| HU2 | Registro de reparaciones (historial) | `RepairController` → `RepairService.saveRepair/updateRepair` | `RepairList`, `AddEditRepair` | USER, ADMIN |
| HU3 | Cálculo del costo total | `RepairService.calculateCost` + `RepairCostService` | `RepairDetail`, botón *Calcular* | USER, ADMIN |
| HU4 | Reporte R1 | `ReportService.getReportR1` | `ReportR1` | USER, ADMIN |
| HU5 | Reporte R2 | `ReportService.getReportR2` | `MatrixReport report="r2"` | ADMIN |
| HU6 | Reporte R3 | `ReportService.getReportR3` | `ReportR3` | ADMIN |
| HU7 | Reporte R4 | `ReportService.getReportR4` | `MatrixReport report="r4"` | ADMIN |

Costo Total = [Suma(Reparaciones) + Recargos − Descuentos] + IVA (19 %). Los porcentajes se aplican sobre la
suma de las reparaciones y el bono se descuenta como monto fijo.

## Usuarios (Keycloak)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN (HU1 a HU7) |
| `usuario` | `usuario123` | USER (HU1 a HU4) |

Consola de administración de Keycloak: `http://<host>/auth/admin` (usuario `admin`, contraseña `KEYCLOAK_ADMIN_PASSWORD` del `.env`).

## Desarrollo local

```bash
# Backend (requiere PostgreSQL y Keycloak)
cd autofix-backend
./mvnw clean test             # pruebas + reporte JaCoCo en target/site/jacoco/index.html
./mvnw spring-boot:run        # http://localhost:8090/swagger-ui/index.html

# Frontend
cd autofix-frontend
npm install
npm run dev                   # http://localhost:5173 (proxy /api -> :8090, /auth -> :9090)
```

## Pipeline (GitHub Actions)

Al hacer push a `main`, cada workflow ejecuta checkout, pruebas unitarias con verificación de cobertura (solo
el backend), build, `docker build` y `docker push` a Docker Hub.

Configura estos secrets en GitHub (Settings → Secrets and variables → Actions):

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`: es un *access token* que se crea en Docker Hub → Account settings → Personal access tokens.

## Despliegue manual en la nube

En una VM (AWS EC2, Azure, GCP o DigitalOcean) con Docker instalado y el puerto 80 abierto:

```bash
git clone <repo> && cd <repo>/deployment
cp .env.example .env           # DOCKERHUB_USER, PUBLIC_HOST=<IP pública>, contraseñas
docker compose pull
docker compose up -d
python3 scripts/seed_data.py http://<IP pública>   # opcional: datos de ejemplo
```

Después abre `http://<IP pública>` desde cualquier navegador.

Para comprobar el balanceo de carga, el pie de página muestra qué réplica del backend respondió. También se
puede consultar directamente:

```bash
for i in 1 2 3 4 5 6; do curl -s http://<IP>/api/v1/public/instance; echo; done
```
