# Peluquería - Frontend (Angular)

Frontend en Angular 17 (standalone components) para la app de turnos de la peluquería,
armado sobre el backend Spring Boot ya existente (auth con JWT, servicios, turnos,
disponibilidad, comercio y horarios).

## Cómo levantarlo

1. Instalar dependencias:
   ```
   npm install
   ```

2. Verificar la URL del backend en `src/environments/environment.ts`
   (por defecto apunta a `http://localhost:8080/api`).

3. Levantar el backend Spring Boot (puerto 8080).

4. Levantar el frontend:
   ```
   npm start
   ```
   Se abre en `http://localhost:4200`.

## Flujo implementado

1. El usuario se registra (`/register`) o inicia sesión (`/login`).
2. Una vez logueado, ve el listado de **servicios** (`/servicios`).
3. Elige un servicio → elige **fecha** → el sistema consulta
   `GET /api/turnos/disponibilidad` y muestra los **horarios libres**.
4. Confirma el turno (`POST /api/turnos`).
5. Puede ver y cancelar sus turnos en **"Mis turnos"** (`/turnos/mis-turnos`).

## Nota importante sobre el backend

Actualmente `GET /api/servicios`, `/api/comercios` y `/api/horarios` requieren estar
autenticado (ver `SecurityConfig.java`). Por eso el frontend pide login/registro
**antes** de mostrar los servicios. Si en el futuro cambian esos endpoints a
`permitAll()`, se puede quitar el `authGuard` de la ruta `/servicios` para que
cualquier visitante pueda ver el catálogo sin cuenta, y pedir login recién al
confirmar el turno.

## Estructura

```
src/app/
├── core/
│   ├── models/         # Interfaces que calcan los DTOs del backend
│   ├── services/        # AuthService, ServicioService, ComercioService, TurnoService
│   ├── interceptors/    # Adjunta el JWT a cada request
│   └── guards/          # authGuard, protege rutas privadas
├── features/
│   ├── auth/            # Login y registro
│   ├── servicios/       # Listado de servicios
│   └── turnos/          # Reserva de turno y "Mis turnos"
└── layout/
    └── navbar/          # Barra de navegación superior
```
