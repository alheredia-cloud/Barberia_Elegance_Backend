# 💈 Barbería Elegance — Backend (API REST)

API REST en **Spring Boot** para la tienda y agenda de Barbería Elegance (DSY1104 – Evaluación Parcial 3).
Frontend: https://github.com/alheredia-cloud/Barberia_Elegance

## Stack
- Java 21 · Spring Boot 4.2 (Web MVC, Data JPA, Security, Validation)
- MySQL (producción/local) · H2 en memoria (pruebas)
- JWT (jjwt 0.13) · BCrypt
- Swagger / OpenAPI (springdoc 3.2)
- JUnit 5 + Mockito + MockMvc

## Arquitectura
```
controller  -> Endpoints REST versionados (/api/v1/...) + documentación Swagger
service     -> Lógica de negocio (stock, total de boletas, agenda, RUT, roles)
repository  -> Spring Data JPA
model       -> Entidades JPA
dto         -> Objetos de entrada/salida (records con Bean Validation)
security    -> JwtService, JwtAuthFilter, SecurityConfig
exception   -> Manejo global de errores (formato JSON único)
config      -> Swagger, reloj, datos iniciales
```

## Modelo de datos
`USUARIO 1─N BOLETA 1─N DETALLE_BOLETA N─1 PRODUCTO N─1 CATEGORIA`
y además `CITA` (agenda, opcionalmente asociada a usuario y boleta) y `MENSAJE` (contacto).

## Roles
| Rol | Permisos |
|---|---|
| ADMIN | Acceso total (productos, categorías, usuarios, boletas, citas, mensajes, dashboard) |
| VENDEDOR | Ver productos y su detalle, ver boletas (órdenes) y su detalle |
| CLIENTE | Tienda: comprar, agendar, ver sus boletas y citas |

## Ejecución

### Opción A — MySQL (XAMPP)
1. Iniciar MySQL desde el panel de XAMPP (usuario `root`, sin clave).
2. Ejecutar (la base `barberia_db` se crea sola):
   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```
   Credenciales distintas: variables `DB_URL`, `DB_USER`, `DB_PASSWORD`.

### Opción B — H2 en memoria (sin instalar nada)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html

### Pruebas
```bash
./mvnw test
```

## Usuarios de prueba
| Rol | Email | Clave |
|---|---|---|
| ADMIN | admin@barberia.cl | admin123 |
| VENDEDOR | vendedor@barberia.cl | vendedor123 |
| CLIENTE | cliente@barberia.cl | cliente123 |

## Autenticación JWT
1. `POST /api/v1/auth/login` → `accessToken` (30 min) + `refreshToken` (7 días).
2. Enviar `Authorization: Bearer <accessToken>` en cada petición protegida.
3. Al recibir 401 por expiración: `POST /api/v1/auth/refresh` con el `refreshToken` para obtener tokens nuevos.

## Integrantes
- Bastian Canales
- Ian Muñoz
- Alexander Heredia
