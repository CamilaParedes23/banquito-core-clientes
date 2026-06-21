# Comparativo final de versiones — core-customer-service

## Decisión

La versión final híbrida usa como base la versión estructurada del proyecto.

## Elementos conservados

- Gestión de clientes persona natural y jurídica.
- Consulta de clientes por UUID e identificación.
- Listado filtrado y paginado de clientes.
- Subtipos de cliente.
- Cambio de estado del cliente.
- Cambio de habilitación para pagos masivos.
- Relaciones entre clientes, incluyendo representantes o vínculos jurídicos.
- gRPC interno para consulta de clientes desde otros microservicios.
- Seguridad JWT, trazabilidad, auditoría y outbox.
- Dockerfile, pom.xml, configuración y scripts de base.

## Elementos revisados de la versión del desarrollador

La versión del desarrollador tenía los mismos endpoints principales de Customer, pero incluía archivos ajenos al bounded context:

- `com.banquito.switchpagos.routing.SwitchRoutingServiceApplication`
- `com.banquito.switchpagos.routing.controller.HealthController`

## Elementos no migrados

No se migró `GET /api/v1/health` del paquete `switchpagos.routing`, porque no pertenece a Customer y ya existe healthcheck vía Actuator.

## Conclusión

No se pierden endpoints de negocio relevantes. Esta versión conserva el contexto Customer limpio y alineado a DDD, RF/RNF y arquitectura de microservicios.
