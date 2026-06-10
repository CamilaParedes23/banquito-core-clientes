# Revisión cloud-ready - core-customer-service

## Cambios aplicados

- Parametrización completa de `application.yml` y `application-docker.yml`.
- `SERVER_PORT`, `CUSTOMER_DB_URL`, `CUSTOMER_DB_USER`, `CUSTOMER_DB_PASSWORD`, `JWT_ISSUER`, `JWT_SECRET` y `DEMO_DATA_ENABLED` ahora se manejan por entorno.
- Dockerfile preparado para ejecución en contenedor.
- `.env.example`, `.gitignore` y `.dockerignore` agregados/actualizados.
- `SecurityConfig` actualizado con `RestAuthenticationEntryPoint` y `RestAccessDeniedHandler`.
- Manejo de errores homologado con `BusinessException` + `@RestControllerAdvice`.
- Respuestas 401/403 ahora devuelven JSON estructurado.
- Se agregó documentación de despliegue Docker/nube.
- Se incorporó el SQL base en `docs/database/00_core_customer_db.sql`.

## Cobertura funcional revisada

Customer mantiene el bounded context de datos maestros de clientes:

- Cliente natural.
- Cliente jurídico.
- Relaciones entre clientes.
- Representante legal.
- Subtipos de cliente.
- Estado del cliente.
- Habilitación de pagos masivos para empresas.

## Comunicación

- REST/OpenAPI: exposición externa vía Kong.
- gRPC: comunicación interna prevista para `core-account-service -> core-customer-service`.
- RabbitMQ: no es flujo crítico inicial para Customer; puede publicar eventos de cliente en fase de integración asíncrona.
