# proyecto_mingeso

AutoFix: Evaluación 1 de Métodos y Técnicas de Ingeniería de Software (USACH, curso 13166). La visión general y
el mapeo de cada HU al código están en `README.md`.

## Estructura
- `autofix-backend/`: Spring Boot 3.3, Java 17, Maven wrapper. Paquete `edu.mtisw.autofixbackend`, organizado en
  capas: `controllers → services → repositories → entities`, más `dtos/` y `config/`. Sigue el estilo del ejemplo
  `payroll-backend` del profesor: `@Autowired` por campo, `ResponseEntity`, `@PreAuthorize`.
- `autofix-frontend/`: React + Vite + MUI + keycloak-js. Sigue el estilo de `payroll-frontend`:
  `services/*.service.js` sobre `http-common.js` con un interceptor que agrega el Bearer token.
- `deployment/`: docker-compose (postgres, keycloak, backend×3, frontend×3, nginx-backend, nginx-frontend), los
  `.conf` de nginx, el realm de Keycloak y `scripts/seed_data.py`.
- `.github/workflows/`: backend.yml y frontend.yml (test → build → push a Docker Hub).
- `material_curso/`: material descargado de uVirtual (clases, código del profesor, enunciado, rúbrica,
  transcripciones de los videos). Está en `.gitignore`.
- `docs/`: guía de presentación en PDF y su fuente. Está en `.gitignore`.

## Comandos
- Tests y cobertura: `cd autofix-backend && ./mvnw clean test`. JaCoCo hace fallar el build si la cobertura de
  `services` baja de 90 %.
- Jar: `./mvnw clean package -DskipTests`
- Frontend: `npm run dev` / `npm run build`
- Stack local: en este equipo solo existe `docker-compose` v1 (no `docker compose`) y el puerto 80 está ocupado.
  Usar `deployment/.env` con `PUBLIC_PORT=8088` y `PUBLIC_HOST=localhost:8088`, con las imágenes
  `local/autofix-*`. En compose v1, recrear un servicio falla con `KeyError: 'ContainerConfig'`; hay que hacer
  `rm -sf` antes de `up`.

## Convenciones
- Comunicación en español.
- Las reglas de negocio puras (tablas del enunciado) van en `RepairCostService`; la orquestación con la BD va en
  `RepairService`.
