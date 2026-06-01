# Documentación de Endpoints de Autenticación

Base URL: `http://localhost:8080/api/v1/auth`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

Todos los endpoints son `POST`. Los que requieren autenticación llevan el header:
```
Authorization: Bearer <accessToken>
```

---

## Tiempos y límites de seguridad

| Parámetro | Valor por defecto | Env var para cambiarlo |
|---|---|---|
| Access token | **15 minutos** | `jwt.access-token-expiration` |
| Refresh token | **7 días** | `jwt.refresh-token-expiration` |
| Password reset token | **15 minutos** | `jwt.password-reset-token-expiration` |
| OTP válido durante | **10 minutos** | `app.security.otp-expiration-minutes` |
| Intentos máximos OTP | **3** | `app.security.otp-max-attempts` |
| Intentos fallidos login antes de bloqueo | **5** | `app.security.max-failed-login-attempts` |
| Duración del bloqueo de cuenta | **30 minutos** | `app.security.account-lock-duration-minutes` |

---

## Respuesta de error estándar

Todos los errores siguen este formato:

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error",
  "path": "/api/v1/auth/login"
}
```

Los errores de validación de campos devuelven además un objeto `errors`:
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Validation failed",
  "errors": {
    "email": "Must be a valid email address",
    "password": "Password must be between 8 and 100 characters"
  }
}
```

---

## Objeto `AuthTokenDto` (respuesta de login y refresh)

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "a3f8c2d1e4b7...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "usuario@ejemplo.com",
    "firstName": "Juan",
    "lastName": "García",
    "phone": "+34600000000",
    "status": "ACTIVE",
    "lastLoginAt": "2024-01-01T12:00:00",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

- `expiresIn`: segundos que dura el access token (900 = 15 min por defecto)
- `tokenType`: siempre `"Bearer"`
- `status` del usuario puede ser: `ACTIVE`, `PENDING_ACTIVATION`, `LOCKED`, `DISABLED`

---

## Posibles estados de cuenta (`UserStatus`)

| Estado | Descripción |
|---|---|
| `PENDING_ACTIVATION` | Registrado pero sin activar el email |
| `ACTIVE` | Cuenta normal en uso |
| `LOCKED` | Bloqueada temporalmente por intentos fallidos |
| `DISABLED` | Desactivada por administrador (permanente) |

---

## Endpoints

---

### 1. Registro

```
POST /api/v1/auth/register
```

**Body:**
```json
{
  "email": "usuario@ejemplo.com",
  "password": "MiPassword1!",
  "firstName": "Juan",
  "lastName": "García",
  "phone": "+34600000000"
}
```

**Validaciones campo a campo:**

| Campo | Regla |
|---|---|
| `email` | Obligatorio. Formato email válido. Se guarda en minúsculas. |
| `password` | Obligatorio. 8–100 caracteres. Debe tener: 1 mayúscula, 1 minúscula, 1 dígito, 1 especial (`@$!%*?&`) |
| `firstName` | Obligatorio. 1–100 caracteres. |
| `lastName` | Obligatorio. 1–100 caracteres. |
| `phone` | Opcional. Formato: `+` opcional seguido de 7–15 dígitos. Ej: `+34600000000` o `600000000` |

**Lógica interna:**
- Comprueba que el email no exista ya en la BD → `409 Conflict` si existe
- Hashea la contraseña con BCrypt antes de guardar
- Crea el usuario con estado `PENDING_ACTIVATION`
- Asigna el rol `USER` automáticamente
- Genera un OTP de 6 dígitos, lo hashea (SHA-256) y lo guarda en BD
- Envía el OTP por email de forma asíncrona

**Respuesta exitosa: `201 Created`**
```json
{
  "message": "Registration successful. Please check your email for the activation code."
}
```

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | Validación de campos fallida |
| `409` | El email ya está registrado |

---

### 2. Activar cuenta

```
POST /api/v1/auth/activate
```

**Body:**
```json
{
  "email": "usuario@ejemplo.com",
  "otp": "123456"
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `email` | Obligatorio. Formato email válido. |
| `otp` | Obligatorio. Exactamente 6 caracteres. |

**Lógica interna:**
- Busca el usuario por email → `404` si no existe
- Si el usuario ya está `ACTIVE` → `400 Bad Request`
- Busca el OTP activo más reciente de tipo `ACCOUNT_ACTIVATION`
- Si el OTP no existe (nunca se generó o fue consumido) → `400 Invalid OTP`
- Si el OTP expiró (pasaron más de 10 min) → `400 OTP has expired`
- Si ya se superaron 3 intentos fallidos → `400 Too many failed attempts. Please request a new OTP`
- Si el OTP es incorrecto → incrementa el contador de intentos y devuelve cuántos intentos quedan
- Si el OTP es correcto → marca como usado, cambia estado del usuario a `ACTIVE`

**Respuesta exitosa: `200 OK`**
```json
{
  "message": "Account activated successfully. You can now log in."
}
```

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | OTP inválido, expirado, ya usado, demasiados intentos, o cuenta ya activa |
| `404` | Email no encontrado |

---

### 3. Reenviar OTP de activación

```
POST /api/v1/auth/resend-activation
```

**Body:**
```json
{
  "email": "usuario@ejemplo.com"
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `email` | Obligatorio. Formato email válido. |

**Lógica interna:**
- Busca el usuario por email → `404` si no existe
- Si el usuario ya está `ACTIVE` → `400 Bad Request`
- **Invalida todos los OTPs anteriores** de tipo `ACCOUNT_ACTIVATION` para ese usuario
- Genera un OTP nuevo de 6 dígitos y lo envía por email
- El nuevo OTP tiene vigencia de 10 minutos desde este momento

**Respuesta exitosa: `200 OK`**
```json
{
  "message": "Activation code resent. Please check your email."
}
```

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | Cuenta ya activa |
| `404` | Email no encontrado |

---

### 4. Login

```
POST /api/v1/auth/login
```

**Body:**
```json
{
  "email": "usuario@ejemplo.com",
  "password": "MiPassword1!"
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `email` | Obligatorio. Formato email válido. |
| `password` | Obligatorio. No puede estar vacío. |

**Lógica interna (en orden):**
1. Busca el usuario por email → `401 Invalid credentials` si no existe (sin revelar si el email existe)
2. Si estado es `PENDING_ACTIVATION` → `403 Account is not activated`
3. Si estado es `DISABLED` → `403 Account is disabled`
4. Si la cuenta está bloqueada (`isAccountLocked()`) → `403 Account is temporarily locked`
5. Autentica con Spring Security (verifica contraseña BCrypt)
6. Si contraseña incorrecta:
   - Incrementa `failedLoginAttempts`
   - Si `failedLoginAttempts >= 5` → cambia estado a `LOCKED`, guarda `lockedUntil = ahora + 30 min`
   - Devuelve `401 Invalid credentials`
7. Si credenciales correctas:
   - Resetea `failedLoginAttempts = 0`, limpia `lockedUntil`
   - Actualiza `lastLoginAt = ahora`
   - Si estaba `LOCKED` (y el bloqueo ya expiró) → vuelve a `ACTIVE`
8. Genera access token JWT (RS256, 15 min)
9. Crea refresh token (64 bytes random, SHA-256 en BD, 7 días), guarda IP y User-Agent

**Respuesta exitosa: `200 OK`** → [`AuthTokenDto`](#objeto-authtokendto-respuesta-de-login-y-refresh)

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | Validación de campos |
| `401` | Credenciales incorrectas |
| `403` | Cuenta no activada, desactivada o bloqueada |

> **Nota para el front:** si recibes `403` con mensaje `"Account is temporarily locked"`, puedes mostrar al usuario que espere hasta que expire el bloqueo. No hay endpoint para saber cuánto tiempo queda.

---

### 5. Refresh token (renovar tokens)

```
POST /api/v1/auth/refresh
```

**Body:**
```json
{
  "refreshToken": "a3f8c2d1e4b7..."
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `refreshToken` | Obligatorio. No puede estar vacío. |

**Lógica interna (rotación de tokens):**
1. Hashea el token recibido (SHA-256) y lo busca en BD
2. Si no existe → `401 Refresh token not found`
3. Si existe pero está revocado o expiró → `401 Refresh token is expired or revoked`
4. **Revoca inmediatamente el token antiguo** (`revoked = true`, `revokedAt = ahora`)
5. Genera un nuevo access token JWT
6. Crea un nuevo refresh token (nuevo valor random, nueva entrada en BD con nueva expiración de 7 días)
7. Devuelve ambos tokens nuevos

**Respuesta exitosa: `200 OK`** → [`AuthTokenDto`](#objeto-authtokendto-respuesta-de-login-y-refresh)

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | Campo vacío |
| `401` | Token no encontrado, expirado o ya revocado |

> **Cómo debe manejarlo el front:**
> 1. Guardar `accessToken` y `refreshToken` (localStorage, memory, cookie httpOnly...)
> 2. Cuando una petición devuelve `401`, llamar a `/refresh` con el `refreshToken` guardado
> 3. Si `/refresh` tiene éxito → guardar los **dos** tokens nuevos (el antiguo ya no sirve nunca más) y reintentar la petición original
> 4. Si `/refresh` también falla → redirigir al login y limpiar tokens locales

---

### 6. Logout

```
POST /api/v1/auth/logout
```

**Body:**
```json
{
  "refreshToken": "a3f8c2d1e4b7..."
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `refreshToken` | Obligatorio. No puede estar vacío. |

**Lógica interna:**
- Hashea el token y lo busca en BD
- Si no existe → no hace nada (operación idempotente, no da error)
- Si existe → lo marca como revocado
- El access token sigue siendo válido hasta que expire (15 min). No hay blacklist de access tokens.

**Respuesta exitosa: `200 OK`**
```json
{
  "message": "Logged out successfully."
}
```

> **Nota para el front:** siempre limpiar los tokens del lado cliente al hacer logout, independientemente de si la llamada tuvo éxito.

---

### 7. Olvidé mi contraseña

```
POST /api/v1/auth/forgot-password
```

**Body:**
```json
{
  "email": "usuario@ejemplo.com"
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `email` | Obligatorio. Formato email válido. |

**Lógica interna:**
- Busca el usuario por email
- **Si el email NO existe → responde igualmente con éxito** (para no revelar si el email está registrado)
- Si el email existe y el usuario está `ACTIVE` → genera OTP, lo envía por email
- Si el usuario no está activo → no envía nada (pero tampoco da error)
- Invalida cualquier OTP anterior de tipo `PASSWORD_RESET` antes de generar el nuevo

**Respuesta exitosa: `200 OK`** (siempre, aunque el email no exista)
```json
{
  "message": "If that email is registered, you will receive a reset code shortly."
}
```

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | Validación de campo |

---

### 8. Verificar OTP de reset de contraseña

```
POST /api/v1/auth/verify-password-reset-otp
```

**Body:**
```json
{
  "email": "usuario@ejemplo.com",
  "otp": "654321"
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `email` | Obligatorio. Formato email válido. |
| `otp` | Obligatorio. Exactamente 6 caracteres. |

**Lógica interna:**
- Busca el usuario por email → `400 Invalid OTP` si no existe
- Valida el OTP de tipo `PASSWORD_RESET` (mismas reglas que en activación: expiración, intentos máximos)
- Si el OTP es correcto → lo marca como usado
- Genera un **password reset token** (JWT especial con `tokenType: "password_reset"`, válido 15 min)

**Respuesta exitosa: `200 OK`**
```json
{
  "resetToken": "eyJhbGciOiJSUzI1NiJ9..."
}
```

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | OTP inválido, expirado o demasiados intentos |

---

### 9. Resetear contraseña

```
POST /api/v1/auth/reset-password
```

**Body:**
```json
{
  "resetToken": "eyJhbGciOiJSUzI1NiJ9...",
  "newPassword": "NuevaPassword1!",
  "confirmPassword": "NuevaPassword1!"
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `resetToken` | Obligatorio. No puede estar vacío. |
| `newPassword` | Obligatorio. 8–100 caracteres. Mismas reglas de complejidad (mayúscula, minúscula, dígito, especial). |
| `confirmPassword` | Obligatorio. No puede estar vacío. |

**Lógica interna:**
1. Comprueba que `newPassword == confirmPassword` → `400` si no coinciden
2. Valida el `resetToken` JWT: firma RS256, no expirado y que `tokenType == "password_reset"`
3. Extrae el email del claim `sub` del JWT
4. Busca el usuario por email → `404` si no existe
5. Hashea la nueva contraseña con BCrypt y la guarda
6. **Revoca TODOS los refresh tokens del usuario** (sesiones cerradas en todos los dispositivos)

**Respuesta exitosa: `200 OK`**
```json
{
  "message": "Password reset successfully. Please log in with your new password."
}
```

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | Contraseñas no coinciden, token inválido o expirado |
| `404` | Usuario no encontrado |

---

### 10. Cambiar contraseña (autenticado)

```
POST /api/v1/auth/change-password
Authorization: Bearer <accessToken>
```

**Body:**
```json
{
  "currentPassword": "MiPassword1!",
  "newPassword": "NuevaPassword2@",
  "confirmPassword": "NuevaPassword2@"
}
```

**Validaciones:**

| Campo | Regla |
|---|---|
| `currentPassword` | Obligatorio. No puede estar vacío. |
| `newPassword` | Obligatorio. 8–100 caracteres. Mismas reglas de complejidad. |
| `confirmPassword` | Obligatorio. No puede estar vacío. |

**Lógica interna:**
1. Extrae el `userId` del JWT en el header `Authorization`
2. Comprueba que `newPassword == confirmPassword` → `400` si no coinciden
3. Busca el usuario por ID
4. Verifica que `currentPassword` coincide con el hash en BD → `400` si es incorrecta
5. Hashea la nueva contraseña y la guarda
6. **Revoca TODOS los refresh tokens del usuario** (sesiones cerradas en todos los dispositivos)

**Respuesta exitosa: `200 OK`**
```json
{
  "message": "Password changed successfully."
}
```

**Errores posibles:**

| HTTP | Cuándo |
|---|---|
| `400` | Contraseñas no coinciden, contraseña actual incorrecta |
| `401` | Access token ausente, inválido o expirado |
| `404` | Usuario no encontrado |

---

## Cómo funciona el Refresh Token en detalle

### Ciclo de vida completo

```
REGISTER ──► ACTIVATE ──► LOGIN
                             │
                      ┌──────┴──────┐
                 accessToken    refreshToken
                 (15 min)       (7 días, en BD hashed)
                      │
                 [petición API]
                      │
                 si 401 Expired
                      │
                      ▼
                   REFRESH ──► nuevo accessToken + nuevo refreshToken
                                │         (token viejo ya no sirve)
                                │
                          [petición API reintentada]
```

### Reglas de invalidación

| Evento | Qué tokens se revocan |
|---|---|
| Logout | Solo el refresh token enviado en el body |
| Cambiar contraseña | **Todos** los refresh tokens del usuario |
| Resetear contraseña | **Todos** los refresh tokens del usuario |
| Usar un refresh token | El token usado queda revocado, se crea uno nuevo |

### Lo que guarda el servidor por cada refresh token

- Hash SHA-256 del token (nunca el valor en claro)
- `expiresAt`: fecha exacta de expiración
- `revoked` + `revokedAt`: si fue invalidado y cuándo
- `ipAddress`: IP de quien creó la sesión
- `deviceInfo`: User-Agent (máx 255 caracteres)

### Lo que incluye el Access Token (JWT payload)

```json
{
  "sub": "usuario@ejemplo.com",
  "userId": 1,
  "roles": ["ROLE_USER"],
  "tokenType": "access",
  "iat": 1700000000,
  "exp": 1700000900
}
```

---

## Flujo completo de registro → uso

```
1. POST /register          → Crea cuenta, envía OTP por email
2. POST /activate          → Activa la cuenta con el OTP
3. POST /login             → Obtiene accessToken + refreshToken
4. [Peticiones autenticadas con Bearer accessToken]
5. [Cuando accessToken expira (401)] → POST /refresh con refreshToken
6. [Guardar nuevos tokens, reintentar petición]
7. POST /logout            → Revoca el refreshToken
```

## Flujo de recuperación de contraseña

```
1. POST /forgot-password              → Envía OTP al email
2. POST /verify-password-reset-otp    → Valida OTP, devuelve resetToken (JWT, 15 min)
3. POST /reset-password               → Cambia contraseña con el resetToken
                                         (cierra sesión en todos los dispositivos)
4. POST /login                         → Nueva sesión con la nueva contraseña
```

---

## Reglas de contraseña

La contraseña debe cumplir **todas** estas reglas simultáneamente:

- Mínimo 8 caracteres, máximo 100
- Al menos 1 letra **mayúscula** (A-Z)
- Al menos 1 letra **minúscula** (a-z)
- Al menos 1 **dígito** (0-9)
- Al menos 1 **carácter especial** de este conjunto exacto: `@ $ ! % * ? &`

Aplica en: registro, cambiar contraseña y resetear contraseña.
