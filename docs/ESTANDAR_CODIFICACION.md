# Estándar de codificación aplicado

- Paquete base: `com.banquito.core.customer`.
- Entidades JPA con `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column` explícito.
- Imports Jakarta Persistence, no `javax.persistence`.
- Lombok permitido: `@Getter`, `@Setter`, `@RequiredArgsConstructor` en servicios/controladores.
- Lombok no permitido en entidades: `@Data`, `@EqualsAndHashCode`.
- `equals()` y `hashCode()` manuales por clave primaria.
- `toString()` manual y seguro.
- Enums con sufijo `Enum` y `@Enumerated(EnumType.STRING)`.
- `@Version` en entidades mutables.
- DTOs separados de entidades.
- Servicios contienen reglas de negocio; controladores solo reciben/delegan/responden.
- No hay relaciones JPA con otros microservicios.
- Integración futura por REST/gRPC, UUIDs, correlationId y eventos.
