## Tactical-Level Domain-Driven Design

El diseño táctico de *Domain-Driven Design* (DDD Táctico), fundamentado en los patrones canónicos establecidos por Evans (2004) y profundizados por Vernon (2013), transforma las fronteras y capacidades estratégicas definidas previamente en modelos de software estructurados y listos para su implementación. Esta etapa permite modelar con precisión los conceptos, comportamientos y reglas de negocio de la plataforma Viora mediante agregados, entidades, objetos de valor (*Value Objects*), servicios de dominio, repositorios y eventos, asegurando que cada módulo mantenga responsabilidades delimitadas y una alta cohesión interna.

Para garantizar la mantenibilidad, escalabilidad y separación de responsabilidades, cada contexto delimitado (*Bounded Context*) se estructura bajo una arquitectura en capas que organiza el sistema en cuatro niveles conceptuales:

1. **Capa de Dominio (*Domain Layer*):** Constituye el núcleo de cada contexto y concentra los modelos fundamentales del negocio, sus reglas operativas y las invariantes que deben cumplirse en todo momento, garantizando que la lógica esencial del sistema permanezca independiente de detalles técnicos o herramientas externas.
2. **Capa de Aplicación (*Application Layer*):** Coordina los casos de uso y flujos de trabajo de la solución, orquestando la interacción entre los requerimientos solicitados por los usuarios y las operaciones internas del dominio. En lugar de emplear un servicio de aplicación genérico o monolítico, la arquitectura adopta una separación explícita de responsabilidades mediante servicios especializados de comando (*CommandService*) y servicios de consulta (*QueryService*). Los controladores de la capa de interfaz delegan las operaciones de mutación a sus respectivos servicios de comando y las operaciones de lectura a los servicios de consulta, los cuales se conectan directamente con los puertos de persistencia (*Repository Interfaces*) y los servicios de dominio pertinentes según los requerimientos de cada contexto delimitado.
3. **Capa de Interfaces (*Interface Layer*):** Establece los canales de comunicación y puntos de contacto a través de los cuales las aplicaciones cliente y los usuarios interactúan con el sistema, gestionando el intercambio ordenado de información y la validación de los datos de entrada y salida.
4. **Capa de Infraestructura (*Infrastructure Layer*):** Brinda el soporte operativo necesario para la persistencia de la información, la integración con servicios auxiliares y la ejecución técnica de las operaciones definidas en los niveles superiores.

En las siguientes secciones se detalla el diseño táctico de los nueve contextos delimitados que conforman Viora, presentando de forma estructurada sus modelos conceptuales, reglas de negocio, interfaces de servicio, orquestación de casos de uso, esquemas de datos y diagramas de arquitectura de software a nivel de componentes y código.

### Bounded Context: Identity and Access Management (IAM)

Propósito: Administra el ciclo de vida de las credenciales de acceso, la autenticación y la autorización en la plataforma Viora. Es responsable de salvaguardar las contraseñas bajo funciones criptográficas de derivación con sal (BCrypt), emitir y validar tokens de sesión JWT con claims de rol (`ROLE_PRODUCER`, `ROLE_TECHNICAL_MANAGER`), gestionar tokens efímeros para la recuperación de cuentas y publicar eventos de seguridad hacia el contexto downstream de perfiles y las aplicaciones clientes. Establece el perímetro de seguridad del sistema sin acoplarse a la lógica agronómica.

#### Domain Layer

##### Modelo de dominio: `UserAccount` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-1} se describe la estructura y delimitación transaccional del modelo `UserAccount`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio UserAccount (Aggregate Root) en Identity and Access Management (IAM).} \label{tab:tactical-1} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Delimita la consistencia transaccional para credenciales, autenticación y recuperación de cuenta. \\
Relaciones de dominio & Raíz autónoma. Referenciada lógicamente por ID desde \texttt{UserProfile} y \texttt{Subscription}. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-2} se presentan los atributos, tipos de datos e invariantes que rigen a `UserAccount`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo UserAccount en Identity and Access Management (IAM).} \label{tab:tactical-2} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{UserAccountId} & Identificador único universal inmutable (\texttt{UUID v4}). \\
\texttt{email} & \texttt{EmailAddress} & Correo electrónico normalizado en minúsculas y validado bajo RFC 5322. \\
\texttt{password} & \texttt{HashedPassword} & Hash criptográfico seguro con sal aleatoria derivado mediante BCrypt. \\
\texttt{role} & \texttt{Role} & Rol canónico asignado: \texttt{ROLE\_PRODUCER} o \texttt{ROLE\_TECHNICAL\_MANAGER}. \\
\texttt{passwordResetToken} & \texttt{Optional<Password} \texttt{ResetToken>} & Token efímero de un solo uso con marca temporal de expiración a 15 minutos. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-3} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `UserAccount`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de UserAccount en Identity and Access Management (IAM).} \label{tab:tactical-3} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{changePassword} & \texttt{newHash: HashedPassword} & \texttt{void} & Actualiza la credencial de forma atómica y emite evento de cambio de contraseña. La nueva clave debe diferir de la actual. \\
\texttt{request} \texttt{PasswordReset} & \texttt{generator: TokenGenerator}, \texttt{expiryMinutes: int} & \texttt{Password} \texttt{ResetToken} & Genera un token efímero de 64 caracteres criptográficos y emite evento de solicitud de restablecimiento. \\
\texttt{resetPassword} & \texttt{tokenVal: String}, \texttt{newHash: HashedPassword} & \texttt{void} & Valida vigencia del token, aplica el nuevo hash, invalida el token y emite evento de confirmación de restablecimiento. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-4} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Identity and Access Management (IAM).} \label{tab:tactical-4} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{UserAccountId} & \texttt{UUID v4} & Identificador único universal inmutable de la cuenta de acceso. \\
\texttt{EmailAddress} & \texttt{String} & Correo electrónico normalizado en minúsculas y validado bajo regex RFC 5322. \\
\texttt{Password} & \texttt{String} & Contraseña en texto plano temporal que verifica reglas de complejidad en su creación. \\
\texttt{HashedPassword} & \texttt{String} & Hash criptográfico seguro e inmutable devuelto por el servicio BCrypt. \\
\texttt{Role} & \texttt{Enum (String)} & Roles canónicos del sistema: \texttt{ROLE\_PRODUCER}, \texttt{ROLE\_TECHNICAL\_MANAGER}. \\
\texttt{PasswordResetToken} & \texttt{tokenValue: String}, \texttt{expiresAt: Instant} & Token pseudoaleatorio de 64 caracteres criptográficos con marca temporal de expiración. \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-5} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Identity and Access Management (IAM).} \label{tab:tactical-5} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{HashingService} & Domain Service & \texttt{hash(raw: Password):} \texttt{HashedPassword} & Abstracción para el hashing criptográfico de contraseñas. \\
\texttt{HashingService} & Domain Service & \texttt{matches(raw: Password,} \texttt{hashed: HashedPassword):} \texttt{boolean} & Verificación de coincidencia entre texto plano y hash criptográfico. \\
\texttt{UserAccount} \texttt{Repository} & Repository & \texttt{findById(id: UserAccountId):} \texttt{Optional<UserAccount>} & Recupera la cuenta de usuario por su identificador único. \\
\texttt{UserAccount} \texttt{Repository} & Repository & \texttt{findByEmail(email:} \texttt{EmailAddress):} \texttt{Optional<UserAccount>} & Recupera la cuenta por su dirección de correo normalizada. \\
\texttt{UserAccount} \texttt{Repository} & Repository & \texttt{existsBy} \texttt{Email(email:} \texttt{EmailAddress):} \texttt{boolean} & Verifica la no duplicidad de correo para salvaguardar la unicidad. \\
\texttt{UserAccount} \texttt{Repository} & Repository & \texttt{save(account: UserAccount):} \texttt{UserAccount} & Persiste atómicamente el estado del agregado de cuenta. \\
\texttt{UserAccount} \texttt{Registered} \texttt{Event} & Domain Event & \texttt{userId: UUID, email: String,} \texttt{role: String, occurredOn: Instant} & Notifica el alta de credenciales para inicializar el onboarding civil. \\
\texttt{User} \texttt{Authenticated} \texttt{Event} & Domain Event & \texttt{userId: UUID, email: String,} \texttt{occurredOn: Instant} & Notifica el inicio de sesión exitoso para auditoría de accesos. \\
\texttt{PasswordChanged} \texttt{Event} & Domain Event & \texttt{userId: UUID,} \texttt{occurredOn: Instant} & Señala la actualización voluntaria de contraseña. \\
\texttt{PasswordReset} \texttt{RequestedEvent} & Domain Event & \texttt{userId: UUID, email: String,} \texttt{tokenValue: String, expiresAt: Instant} & Dispara la entrega del correo con el enlace de recuperación vía Brevo. \\
\texttt{PasswordReset} \texttt{CompletedEvent} & Domain Event & \texttt{userId: UUID, email: String,} \texttt{occurredOn: Instant} & Confirma el restablecimiento exitoso de la credencial. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-6} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Identity and Access Management (IAM).} \label{tab:tactical-6} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/auth/sign-up} & \texttt{SignUpRequest} & \texttt{UserAccount} \texttt{Resource} (201 Created) & Registro inicial de credenciales de usuario. \\
\texttt{POST} & \nolinkurl{/api/v1/auth/sign-in} & \texttt{SignInRequest} & \texttt{Authenticated} \texttt{UserResource} (200 OK) & Autenticación y expedición de tokens JWT. \\
\texttt{POST} & \nolinkurl{/api/v1/auth/refresh-tokens} & \texttt{RefreshToken} \texttt{Request} & \texttt{TokenRefresh} \texttt{Resource} (200 OK) & Renovación de sesión mediante token de refresco. \\
\texttt{PATCH} & \nolinkurl{/api/v1/auth/passwords} & \texttt{ChangePassword} \texttt{Request} & \texttt{204 No Content} & Actualización de contraseña para sesión autenticada. \\
\texttt{POST} & \nolinkurl{/api/v1/auth/password-reset-tokens} & \texttt{RequestPassword} \texttt{ResetRequest} & \texttt{202 Accepted} & Solicitud de código de recuperación por correo. \\
\texttt{PUT} & \nolinkurl{/api/v1/auth/password-reset-tokens/{token}} & \texttt{ResetPassword} \texttt{Request} & \texttt{204 No Content} & Restablecimiento de contraseña con token efímero. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-7} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Identity and Access Management (IAM).} \label{tab:tactical-7} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{SignUpRequest} & Request DTO & \texttt{{ email: String, password: String, role: String }} & Payload para registro con validaciones Bean Validation (\texttt{@Email}, \texttt{@NotBlank}). \\
\texttt{SignInRequest} & Request DTO & \texttt{{ email: String, password: String }} & Credenciales para inicio de sesión seguro. \\
\texttt{UserAccount} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, email: String, role: String, createdAt: Instant }} & Datos públicos de la cuenta registrada. \\
\texttt{Authenticated} \texttt{UserResource} & Response DTO & \texttt{{ accessToken: String, refreshToken: String, tokenType: String, expiresIn: Long }} & Paquete de autenticación con Bearer token JWT. \\
\texttt{UserAccount} \texttt{ResourceAssembler} & Assembler & \texttt{toResource(} \texttt{UserAccount):} \texttt{UserAccountResource} & Convierte la entidad de dominio a su representación de salida. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-8} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Identity and Access Management (IAM).} \label{tab:tactical-8} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Register} \texttt{User} \texttt{Account} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Register} \texttt{User} \texttt{Account} \texttt{Command} & Inicia transacción (\texttt{@Transactional}), verifica no duplicidad del correo, delega hashing, guarda cuenta y publica evento de registro. \\
\texttt{Authenticate} \texttt{User} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Authenticate} \texttt{User} \texttt{Command} & Consulta repositorio, valida coincidencia de hash (\texttt{BCrypt}), genera par de tokens JWT y despacha evento de autenticación. \\
\texttt{Refresh} \texttt{User} \texttt{Session} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Refresh} \texttt{User} \texttt{Session} \texttt{Command} & Valida vigencia y firma del refresh token en base de datos, rota el token y emite un nuevo JWT. \\
\texttt{Change} \texttt{User} \texttt{Password} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Change} \texttt{User} \texttt{Password} \texttt{Command} & Carga cuenta por ID, valida clave anterior, verifica que la nueva clave difiera, persiste hash y emite evento de cambio. \\
\texttt{Request} \texttt{Password} \texttt{Reset} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Request} \texttt{Password} \texttt{Reset} \texttt{Command} & Genera token de 15 min, lo vincula a la cuenta y dispara evento para envío de correo vía Brevo. \\
\texttt{Reset} \texttt{User} \texttt{Password} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Reset} \texttt{User} \texttt{Password} \texttt{Command} & Busca cuenta por token, verifica no expiración, aplica nuevo hash, consume el token y emite evento de confirmación. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-9} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Identity and Access Management (IAM).} \label{tab:tactical-9} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{UserAccount} \texttt{JpaRepository} & Persistence & Spring Data JPA & Acceso a tabla \texttt{user\_} \texttt{accounts} sobre PostgreSQL. \\
\texttt{JpaUserAccount} \texttt{RepositoryAdapter} & Adapter & Spring Component & Implementa el puerto de dominio \texttt{UserAccount} \texttt{Repository}. \\
\texttt{JwtTokenProvider} & Security & JJWT / Nimbus & Emisión, firma criptográfica HMAC-SHA256 y parseo de tokens JWT. \\
\texttt{BCryptPassword} \texttt{Service} & Security Adapter & Spring Security Crypto & Implementa \texttt{HashingService} con costo de cómputo configurable. \\
\texttt{BrevoEmail} \texttt{DeliveryAdapter} & External Adapter & Brevo REST API & Despacho de plantillas transaccionales para recuperación de contraseña. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Gestión segura de sesión y almacenamiento de tokens:**
  * *Android Nativo (Kotlin):* Componente `SessionManager` que utiliza `EncryptedSharedPreferences` respaldado por Android KeyStore (cifrado AES-256-GCM) para la persistencia del par de tokens (access token y refresh token de 30 días) y claims de rol.
  * *Cross-Platform (Flutter/Dart):* Componente `SecureStorageSessionManager` apoyado en `flutter_secure_storage` (KeyStore en Android y Keychain en iOS).
* **Intercepción y renovación transparente de tokens:**
  * *Interceptor HTTP (OkHttp / Dio):* `AuthInterceptor` intercepta peticiones HTTP salientes inyectando la cabecera `Authorization: Bearer <access_token>`.
  * *Manejador de Renovación (Authenticator):* Ante respuestas `401 Unauthorized`, `TokenRefreshAuthenticator` bloquea momentáneamente la cola de peticiones, despacha de forma atómica la invocación a renovación de tokens, actualiza los tokens en el almacenamiento seguro y reintenta la solicitud original sin degradar la experiencia en campo.

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-10} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Identity and Access Management (IAM).} \label{tab:tactical-10} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{user\_} \texttt{accounts} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador único inmutable de la cuenta. \\
\texttt{user\_} \texttt{accounts} & \texttt{email} & \texttt{VARCHAR(255)} & \texttt{NOT NULL, UNIQUE} & Correo electrónico normalizado para inicio de sesión. \\
\texttt{user\_} \texttt{accounts} & \texttt{password\_hash} & \texttt{VARCHAR(255)} & \texttt{NOT NULL} & Hash seguro derivado con sal (BCrypt). \\
\texttt{user\_} \texttt{accounts} & \texttt{role} & \texttt{VARCHAR(50)} & \texttt{NOT NULL, CHECK} & Rol del usuario (\texttt{ROLE\_PRODUCER}, \texttt{ROLE\_TECHNICAL\_MANAGER}). \\
\texttt{user\_} \texttt{accounts} & \texttt{reset\_token} & \texttt{VARCHAR(100)} & \texttt{NULL} & Token criptográfico temporal de restablecimiento. \\
\texttt{user\_} \texttt{accounts} & \texttt{reset\_token\_} \texttt{expires\_at} & \texttt{TIMESTAMPTZ} & \texttt{NULL} & Fecha y hora límite para uso del token de recuperación. \\
\texttt{user\_} \texttt{accounts} & \texttt{created\_at} & \texttt{TIMESTAMPTZ} & \texttt{NOT NULL DEFAULT NOW()} & Marca temporal de auditoría de creación. \\
\texttt{user\_} \texttt{accounts} & \texttt{updated\_at} & \texttt{TIMESTAMPTZ} & \texttt{NOT NULL DEFAULT NOW()} & Marca temporal de última modificación. \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS iam;

CREATE TABLE iam.user_accounts (
    id                     UUID PRIMARY KEY,
    email                  VARCHAR(255) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    role                   VARCHAR(50) NOT NULL,
    reset_token            VARCHAR(100),
    reset_token_expires_at TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_accounts_email UNIQUE (email),
    CONSTRAINT chk_user_accounts_role
        CHECK (role IN ('ROLE_PRODUCER', 'ROLE_TECHNICAL_MANAGER'))
);

CREATE INDEX idx_user_accounts_reset_token
    ON iam.user_accounts(reset_token)
    WHERE reset_token IS NOT NULL;
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-11} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Identity and Access Management (IAM).} \label{tab:tactical-11} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{AuthController} & Exposición de endpoints REST para registro, login, refresh y reseteo. & Spring MVC, Jakarta Validation \\
Application & \texttt{UserAccount} \texttt{CommandService}; \texttt{UserAccount} \texttt{QueryService} & Orquestación de comandos de registro/autenticación/reseteo y consultas de sesión/credenciales. & Spring \texttt{@Transactional}, \texttt{@Service} \\
Domain & \texttt{UserAccount} \texttt{Repository}; \texttt{BCryptPasswordHasher} & Contrato de persistencia de cuentas (puerto de dominio) y servicio de derivación de claves con sal. & Java / Spring Security Crypto \\
Infrastructure & \texttt{JpaUserAccount} \texttt{RepositoryAdapter}; \texttt{JwtTokenProvider}; \texttt{BrevoEmailDeliveryAdapter} & Implementación JPA sobre PostgreSQL, emisión de JWT y entrega de correos vía Brevo. & Spring Data JPA, Nimbus, Brevo API \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El contenedor cliente (`Android Application` o `Cross-Platform Application`) envía `POST` \nolinkurl{/api/v1/auth/sign-in} con credenciales hacia `AuthController`.
2. `AuthController` valida el cuerpo de la petición y despacha el comando a `UserAccountCommandService` (mientras que las consultas de sesión o verificación de credenciales son atendidas por `UserAccountQueryService`).
3. `UserAccountCommandService` recupera la cuenta mediante el puerto `UserAccountRepository`.
4. Se valida la contraseña delegando en el servicio de dominio `BCryptPasswordHasher`.
5. Se invoca a `JwtTokenProvider` para generar los tokens JWT con claims de rol y `userId`.
6. Se dispara el evento de autenticación a través del publicador de eventos para auditoría.
7. Se retorna `AuthenticatedUserResource` (`200 OK`) a la aplicación cliente.

A continuación, en la \autoref{fig:c4-component-iam} se esquematiza el diagrama de componentes del Bounded Context Identity and Access Management:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Identity and Access Management.} \label{fig:c4-component-iam}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-iam.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Identity and Access Management. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-iam}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-iam}) para el Bounded Context Identity and Access Management:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Identity and Access Management.} \label{fig:class-diagram-iam}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-iam.png}
\caption*{\textit{Nota.} Estructura estática de clases, tipos y métodos del modelo de dominio de IAM. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Identity and Access Management.} \label{fig:database-diagram-iam}
\vspace{0.25cm}
\centering
\includegraphics[width=0.85\textwidth]{report/assets/tactical-diagrams/database-diagram-iam.png}
\caption*{\textit{Nota.} Estructura de tabla, índices y restricciones en PostgreSQL para IAM. Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: User Profiles

Propósito: Gestiona la información civil, personal y de contacto de los usuarios de la plataforma Viora. Se activa tras la creación de credenciales en IAM y vincula a cada actor con su rol agronómico específico (Productor Olivarero o Gestor Técnico Cooperativo). Es responsable de normalizar los números telefónicos bajo el estándar E.164, custodiar los nombres completos para la emisión de certificaciones oficiales y sincronizar las actualizaciones de datos de contacto hacia el padrón técnico de la cooperativa, estableciendo una base civil verificada para las operaciones agronómicas.

#### Domain Layer

##### Modelo de dominio: `UserProfile` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-12} se describe la estructura y delimitación transaccional del modelo `UserProfile`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio UserProfile (Aggregate Root) en User Profiles.} \label{tab:tactical-12} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Custodia la identidad civil, datos personales y de contacto verificados de los actores del sistema. \\
Relaciones de dominio & Vinculado 1:1 mediante referencia lógica externa (\texttt{userId}) con \texttt{UserAccount}. Referenciado por ID en predios y contratos. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-13} se presentan los atributos, tipos de datos e invariantes que rigen a `UserProfile`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo UserProfile en User Profiles.} \label{tab:tactical-13} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{ProfileId} & Identificador único universal inmutable (\texttt{UUID v4}). \\
\texttt{userId} & \texttt{UserId} & Referencia lógica externa hacia \texttt{UserAccount} en el Bounded Context de IAM. \\
\texttt{fullName} & \texttt{FullName} & Nombres y apellidos completos normalizados para expedientes oficiales. \\
\texttt{country} & \texttt{Country} & Código de país ISO 3166-1 alpha-2 para localización. \\
\texttt{phoneNumber} & \texttt{PhoneNumber} & Número telefónico normalizado bajo estándar internacional E.164. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-14} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `UserProfile`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de UserProfile en User Profiles.} \label{tab:tactical-14} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{create} & \texttt{userId: UserId}, \texttt{name: FullName}, \texttt{country: Country}, \texttt{phone: PhoneNumber} & \texttt{UserProfile} & Método fábrica que valida integridad de datos civiles y emite evento de creación de perfil. \\
\texttt{updateContact} \texttt{Info} & \texttt{name: FullName}, \texttt{country: Country}, \texttt{phone: PhoneNumber} & \texttt{void} & Actualiza datos de contacto y emite evento de actualización de contacto hacia la cooperativa. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-15} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en User Profiles.} \label{tab:tactical-15} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{ProfileId} & \texttt{UUID v4} & Identificador único universal del perfil. \\
\texttt{UserId} & \texttt{UUID v4} & Identificador del usuario en IAM (referencia lógica sin FK física). \\
\texttt{FullName} & \texttt{firstName: String}, \texttt{lastName: String} & Nombre y apellidos normalizados, sin espacios redundantes. \\
\texttt{Country} & \texttt{String (ISO 3166-1 alpha-2)} & Código de país estándar de residencia (ej. \texttt{PE}, \texttt{CL}). \\
\texttt{PhoneNumber} & \texttt{String (E.164)} & Teléfono normalizado con signo \texttt{+} y prefijo internacional. \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-16} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en User Profiles.} \label{tab:tactical-16} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{PhoneNumber} \texttt{Validator} & Domain Service & \texttt{validate(phone:} \texttt{PhoneNumber): boolean} & Validación estricta de estructura y longitud telefónica E.164. \\
\texttt{Profile} \texttt{Repository} & Repository & \texttt{findById(id: ProfileId):} \texttt{Optional<UserProfile>} & Búsqueda de perfil por su identificador único universal. \\
\texttt{Profile} \texttt{Repository} & Repository & \texttt{findByUserId(} \texttt{userId: UserId):} \texttt{Optional<} \texttt{UserProfile>} & Recupera el perfil civil asociado a una cuenta de IAM. \\
\texttt{Profile} \texttt{Repository} & Repository & \texttt{existsByUserId(} \texttt{userId:} \texttt{UserId): boolean} & Verifica si una cuenta ya posee un perfil inicializado. \\
\texttt{Profile} \texttt{Repository} & Repository & \texttt{save(profile: UserProfile):} \texttt{UserProfile} & Persiste atómicamente el perfil y sus datos de contacto. \\
\texttt{ProfileCreated} \texttt{Event} & Domain Event & \texttt{profileId: UUID, userId: UUID,} \texttt{fullName: String, occurredOn: Instant} & Notifica la creación del perfil civil para habilitar contratación. \\
\texttt{ContactProfile} \texttt{UpdatedEvent} & Domain Event & \texttt{profileId: UUID, userId: UUID,} \texttt{phone: String, email: String,} \texttt{occurredOn: Instant} & Propaga actualización de datos de contacto hacia la cooperativa. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-17} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en User Profiles.} \label{tab:tactical-17} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/profiles} & \texttt{CreateProfile} \texttt{Request} & \texttt{UserProfile} \texttt{Resource} (201 Created) & Alta inicial de perfil civil para el usuario autenticado. \\
\texttt{GET} & \nolinkurl{/api/v1/profiles/{userId}} & N/A & \texttt{UserProfile} \texttt{Resource} (200 OK) & Consulta de perfil por identificador de usuario con control de titularidad (*Owner Check*). \\
\texttt{PUT} & \nolinkurl{/api/v1/profiles/{userId}} & \texttt{UpdateProfile} \texttt{Request} & \texttt{UserProfile} \texttt{Resource} (200 OK) & Actualización completa de datos personales y teléfono con validación E.164. \\
\texttt{PATCH} & \nolinkurl{/api/v1/profiles/{userId}} & \texttt{UpdateContact} \texttt{ProfileRequest} & \texttt{UserProfile} \texttt{Resource} (200 OK) & Actualización parcial de datos de contacto y teléfono con validación E.164. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-18} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en User Profiles.} \label{tab:tactical-18} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{CreateProfile} \texttt{Request} & Request DTO & \texttt{{ fullName: String, country: String, phoneNumber: String }} & Datos de entrada para formalización del perfil. \\
\texttt{UpdateProfile} \texttt{Request} & Request DTO & \texttt{{ fullName: String, country: String, phoneNumber: String }} & Modificación completa de datos personales y contacto. \\
\texttt{UpdateContact} \texttt{ProfileRequest} & Request DTO & \texttt{{ fullName: String, country: String, phoneNumber: String }} & Modificación parcial de datos de contacto. \\
\texttt{UserProfile} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, userId: UUID, fullName: String, country: String, phoneNumber: String }} & Perfil de usuario consolidado. \\
\texttt{UserProfile} \texttt{ResourceAssembler} & Assembler & \texttt{toResource(} \texttt{UserProfile):} \texttt{UserProfileResource} & Transforma el agregado en el DTO de presentación. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-19} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en User Profiles.} \label{tab:tactical-19} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Create} \texttt{User} \texttt{Profile} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Create} \texttt{User} \texttt{Profile} \texttt{Command} & Valida que el \texttt{userId} no posea perfil, formatea teléfono, persiste y despacha evento de creación. \\
\texttt{Update} \texttt{Contact} \texttt{Profile} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Update} \texttt{Contact} \texttt{Profile} \texttt{Command} & Carga perfil por \texttt{userId}, aplica validaciones de contacto, persiste cambios y publica evento de contacto. \\
\texttt{Get} \texttt{UserProfile} \texttt{ByUserId} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{UserProfile} \texttt{ByUserId} \texttt{Query} & Recupera el perfil optimizado en solo lectura y mapea a \texttt{UserProfileResource}. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-20} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en User Profiles.} \label{tab:tactical-20} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{UserProfile} \texttt{JpaRepository} & Persistence & Spring Data JPA & Acceso a tabla \texttt{profiles} sobre PostgreSQL. \\
\texttt{JpaUserProfile} \texttt{RepositoryAdapter} & Adapter & Spring Component & Implementa el puerto de dominio \texttt{UserProfileRepository}. \\
\texttt{Libphonenumber} \texttt{Adapter} & Service Adapter & Google libphonenumber & Parsing, validación y normalización a estándar E.164. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Caché local de perfil y acceso a datos:**
  * *Android Nativo (Room / SQLite):* `ProfileDao` y entidad local `LocalProfileEntity` que almacenan en caché el nombre, país y teléfono del usuario autenticado para visualización instantánea en drawer y cabeceras de navegación sin requerir conexión continua.
  * *Cross-Platform (sqflite / SQLite):* Tabla local `local_profiles` administrada por `LocalDataAccess` con invalidación explícita ante mutaciones.
* **Validación de formatos en cliente:**
  * Componentes de interfaz móvil (`Account and Profile UI`) que incorporan validación reactiva de formato E.164 previa al envío de formularios de onboarding y edición de contacto, sincronizando el feedback de error en tiempo real.

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-21} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en User Profiles.} \label{tab:tactical-21} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{profiles} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador único del perfil. \\
\texttt{profiles} & \texttt{user\_id} & \texttt{UUID} & \texttt{NOT NULL, UNIQUE} & Referencia externa a la cuenta en \texttt{iam.user\_accounts}. \\
\texttt{profiles} & \texttt{full\_name} & \texttt{VARCHAR(150)} & \texttt{NOT NULL, CHECK} & Nombre y apellidos completos del usuario. \\
\texttt{profiles} & \texttt{country} & \texttt{VARCHAR(2)} & \texttt{NOT NULL} & Código de país ISO 3166-1 alpha-2. \\
\texttt{profiles} & \texttt{phone\_number} & \texttt{VARCHAR(25)} & \texttt{NOT NULL} & Teléfono normalizado bajo formato E.164. \\
\texttt{profiles} & \texttt{created\_at} & \texttt{TIMESTAMPTZ} & \texttt{NOT NULL DEFAULT NOW()} & Marca temporal de registro civil. \\
\texttt{profiles} & \texttt{updated\_at} & \texttt{TIMESTAMPTZ} & \texttt{NOT NULL DEFAULT NOW()} & Marca temporal de última modificación. \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS profiles;

CREATE TABLE profiles.profiles (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL,
    full_name    VARCHAR(150) NOT NULL,
    country      VARCHAR(2) NOT NULL,
    phone_number VARCHAR(25) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_profiles_user_id UNIQUE (user_id),
    CONSTRAINT chk_profiles_full_name
        CHECK (length(trim(full_name)) >= 2)
);

CREATE INDEX idx_profiles_phone
    ON profiles.profiles(phone_number);
```

#### Bounded Context Software Architecture Component Level Diagrams 
&nbsp;

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-22} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en User Profiles.} \label{tab:tactical-22} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{ProfileController} & Endpoints REST para alta, consulta y actualización de perfiles de usuario. & Spring MVC, Jakarta Validation \\
Application & \texttt{Profile} \texttt{CommandService}; \texttt{Profile} \texttt{QueryService} & Orquestación de comandos de alta y actualización de contacto y consultas de perfil civil. & Spring \texttt{@Transactional}, \texttt{@Service} \\
Domain & \texttt{Profile} \texttt{Repository}; \texttt{PhoneNumberValidator} & Contrato de persistencia de perfil (puerto de dominio) y servicio de validación de formato internacional E.164. & Java puro / libphonenumber \\
Infrastructure & \texttt{JpaProfile} \texttt{RepositoryAdapter}; \texttt{DomainEventPublisher} & Persistencia JPA sobre PostgreSQL (\texttt{profiles.profiles}) y despacho de eventos de dominio. & Spring Data JPA, Spring Events \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El contenedor cliente móvil (`Android Application` o `Cross-Platform Application`) despacha `PUT` \nolinkurl{/api/v1/profiles/{userId}} con datos de contacto hacia `ProfileController`.
2. `ProfileController` extrae el `userId` del claim JWT, valida la correspondencia de titularidad (*Owner Check*) con el recurso de la ruta y delega el comando en `ProfileCommandService` (mientras que las consultas de perfil civil son atendidas por `ProfileQueryService`).
3. `ProfileCommandService` carga el registro mediante el puerto de dominio `ProfileRepository`.
4. Invoca el servicio de dominio `PhoneNumberValidator` para normalizar el número telefónico al estándar E.164.
5. Se persisten las modificaciones atómicamente en PostgreSQL a través de `ProfileRepository` (implementado por `JpaProfileRepositoryAdapter`).
6. Se despacha el evento de actualización de contacto vía `DomainEventPublisher`, el cual es consumido asíncronamente por *Cooperative Operations* para actualizar el padrón de socios.

A continuación, en la \autoref{fig:c4-component-profiles} se esquematiza el diagrama de componentes del Bounded Context User Profiles:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de User Profiles.} \label{fig:c4-component-profiles}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-profiles.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context User Profiles. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-profiles}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-profiles}) para el Bounded Context User Profiles:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de User Profiles.} \label{fig:class-diagram-profiles}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-profiles.png}
\caption*{\textit{Nota.} Estructura estática de clases y objetos de valor del modelo de dominio de User Profiles. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de User Profiles.} \label{fig:database-diagram-profiles}
\vspace{0.25cm}
\centering
\includegraphics[width=0.60\textwidth]{report/assets/tactical-diagrams/database-diagram-profiles.png}
\caption*{\textit{Nota.} Estructura de la tabla profiles, índices y restricciones en PostgreSQL. Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: Subscription and Cooperative Membership

Propósito: Gobierna los contratos comerciales, planes SaaS y cupos institucionales del ecosistema Viora. Administra dos modalidades de activación: suscripciones individuales de productores mediante pasarela de pago digital (Mercado Pago con webhooks seguros) y suscripciones patrocinadas por organizaciones agrarias mediante canje de códigos de activación corporativos. Controla los agregados `Subscription` (contrato individual y transiciones de pago), `Cooperative` `License` (acuerdo corporativo que custodia los acumuladores de plazas y superficie autorizada) y `InvitationCode` `Batch` (emisión, expiración y ajuste de vigencia de códigos).

#### Domain Layer

##### Modelo de dominio: `Subscription` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-23} se describe la estructura y delimitación transaccional del modelo `Subscription`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio Subscription (Aggregate Root) en Subscription and Cooperative Membership.} \label{tab:tactical-23} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Delimita la consistencia de los derechos comerciales contratados, cálculo de vigencias y balance de superficie. \\
Relaciones de dominio & Referencia por ID a \texttt{ProducerId},\texttt{CooperativeId }y opcionalmente a \texttt{InvitationCodeId}. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-24} se presentan los atributos, tipos de datos e invariantes que rigen a `Subscription`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo Subscription en Subscription and Cooperative Membership.} \label{tab:tactical-24} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{SubscriptionId} & Identificador único universal inmutable (\texttt{UUID v4}). \\
\texttt{producerId} & \texttt{UserId} & Identificador del productor olivarero titular. \\
\texttt{plan} & \texttt{SubscriptionPlan} & Modalidad comercial: individual o patrocinada cooperativa. \\
\texttt{quota} & \texttt{HectaresQuota} & Límite máximo contratado de superficie predial en hectáreas. \\
\texttt{status} & \texttt{Subscription} \texttt{Status} & Estado del contrato:\texttt{PENDING\_} \texttt{PAYMENT}, \texttt{ACTIVE}, \texttt{EXPIRED}, \texttt{CANCELLED}. \\
\texttt{period} & \texttt{Optional<} \texttt{SubscriptionPeriod>} & Período de vigencia con marcas temporales de inicio y fin. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-25} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `Subscription`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de Subscription en Subscription and Cooperative Membership.} \label{tab:tactical-25} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{requestPayment} & \texttt{terms: PaymentTerms} & \texttt{PaymentIntent} & Inicia intento de pago congelando cotización y cuota de hectáreas. \\
\texttt{activateFrom} \texttt{Payment} & \texttt{receipt: PaymentReceipt}, \texttt{period: SubscriptionPeriod} & \texttt{void} & Transiciona a activo tras verificación y emite evento de activación. \\
\texttt{activate} \texttt{FromCode} & \texttt{code: RedeemedCode}, \texttt{period: SubscriptionPeriod} & \texttt{void} & Activa contrato patrocinado por cooperativa y emite evento de canje. \\
\texttt{expire} & \texttt{currentTime: Instant} & \texttt{void} & Invalida derechos de uso al vencer el plazo del ciclo contratado. \\
\texttt{hasActive} \texttt{Entitlement} & \texttt{currentTime: Instant} & \texttt{boolean} & Verifica si el productor dispone de cobertura vigente para sus parcelas. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `CooperativeLicense` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-26} se describe la estructura y delimitación transaccional del modelo `CooperativeLicense`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio CooperativeLicense (Aggregate Root) en Subscription and Cooperative Membership.} \label{tab:tactical-26} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Administra el saldo corporativo global de plazas de agricultores y superficie de hectáreas para una cooperativa. \\
Relaciones de dominio & Referencia externa a \texttt{CooperativeId}. Gobierna lotes de códigos vinculados por \texttt{licenseId}. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-27} se presentan los atributos, tipos de datos e invariantes que rigen a `CooperativeLicense`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo CooperativeLicense en Subscription and Cooperative Membership.} \label{tab:tactical-27} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{LicenseId} & Identificador único universal de la licencia corporativa. \\
\texttt{cooperativeId} & \texttt{CooperativeId} & Identificador de la cooperativa patrocinadora. \\
\texttt{tier} & \texttt{LicenseTier} & Nivel institucional contratado con sus límites asignados. \\
\texttt{totalSeats} & \texttt{Int} & Cantidad total de plazas de socios contratadas. \\
\texttt{issuedSeats} & \texttt{Int} & Plazas actualmente comprometidas en lotes emitidos. \\
\texttt{totalAreaHa} & \texttt{Double} & Superficie máxima consolidada autorizada en hectáreas. \\
\texttt{issuedAreaHa} & \texttt{Double} & Hectáreas actualmente comprometidas en lotes emitidos. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-28} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `CooperativeLicense`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de CooperativeLicense en Subscription and Cooperative Membership.} \label{tab:tactical-28} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{reserveQuota} & \texttt{seats: Int}, \texttt{area: Double} & \texttt{void} & Compromete plazas y superficie verificando disponibilidad; rechaza sobreemisión. \\
\texttt{releaseQuota} & \texttt{seats: Int}, \texttt{area: Double} & \texttt{void} & Restaura plazas y hectáreas liberadas por caducidad de códigos. \\
\texttt{hasAvailable} \texttt{Capacity} & \texttt{seats: Int}, \texttt{area: Double} & \texttt{boolean} & Consulta si la cooperativa cuenta con cupo libre para un nuevo lote. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `InvitationCodeBatch` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-29} se describe la estructura y delimitación transaccional del modelo `InvitationCodeBatch`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio InvitationCodeBatch (Aggregate Root) en Subscription and Cooperative Membership.} \label{tab:tactical-29} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Gestiona la generación criptográfica, vigencia y canje de un lote de códigos de invitación. \\
Relaciones de dominio & Referencia a \texttt{LicenseId} y contiene una colección de entidades subordinadas \texttt{Invitation} \texttt{Code}. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-30} se presentan los atributos, tipos de datos e invariantes que rigen a `InvitationCodeBatch`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo InvitationCodeBatch en Subscription and Cooperative Membership.} \label{tab:tactical-30} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{BatchId} & Identificador único universal del lote de códigos. \\
\texttt{licenseId} & \texttt{LicenseId} & Identificador de la licencia corporativa de origen. \\
\texttt{codes} & \texttt{List<InvitationCode>} & Colección de códigos individuales de invitación emitidos. \\
\texttt{status} & \texttt{BatchStatus} & Estado operativo del lote: \texttt{ACTIVE}, \texttt{EXHAUSTED}, \texttt{EXPIRED}. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-31} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `InvitationCodeBatch`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de InvitationCodeBatch en Subscription and Cooperative Membership.} \label{tab:tactical-31} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{generateCodes} & \texttt{count: Int}, \texttt{quotaHa: Double}, \texttt{days: Int} & \texttt{void} & Genera códigos criptográficos no secuenciales y emite evento de lote generado. \\
\texttt{redeemCode} & \texttt{codeId: CodeId}, \texttt{producerId: UserId} & \texttt{Invitation} \texttt{Code} & Consume atómicamente un código disponible y retorna evidencia de canje. \\
\texttt{shorten} \texttt{CodeExpiry} & \texttt{codeId: CodeId}, \texttt{newExpiry: Instant} & \texttt{void} & Anticipa la fecha límite de canje para un código no consumido. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `InvitationCode` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-32} se describe la estructura y delimitación transaccional del modelo `InvitationCode`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio InvitationCode (Internal Entity) en Subscription and Cooperative Membership.} \label{tab:tactical-32} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Representa un vale digital unívoco e intransferible que otorga derecho de suscripción a un socio. \\
Relaciones de dominio & Subordinado estrictamente a \texttt{Invitation} \texttt{Code} \texttt{Batch} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-33} se presentan los atributos, tipos de datos e invariantes que rigen a `InvitationCode`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo InvitationCode en Subscription and Cooperative Membership.} \label{tab:tactical-33} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{CodeId} & Identificador único del código de invitación. \\
\texttt{codeHash} & \texttt{String} & Huella criptográfica segura del código para validación. \\
\texttt{quotaHa} & \texttt{Double} & Hectáreas autorizadas para el socio que lo canjee. \\
\texttt{status} & \texttt{CodeStatus} & Estado: \texttt{AVAILABLE}, \texttt{REDEEMED}, \texttt{EXPIRED}. \\
\texttt{expiresAt} & \texttt{Instant} & Fecha y hora límite improrrogable para su consumo. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-34} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `InvitationCode`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de InvitationCode en Subscription and Cooperative Membership.} \label{tab:tactical-34} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{redeem} & \texttt{producerId: UserId} & \texttt{void} & Asocia el código al productor beneficiario y transiciona a estado redimido. \\
\texttt{adjustExpiry} & \texttt{newExpiry: Instant} & \texttt{void} & Actualiza la marca temporal de caducidad si el código permanece disponible. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-35} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Subscription and Cooperative Membership.} \label{tab:tactical-35} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{SubscriptionId}, \texttt{LicenseId}, \texttt{BatchId} & \texttt{UUID v4} & Identificadores únicos universales inmutables. \\
\texttt{HectaresQuota} & \texttt{Double} & Superficie máxima permitida bajo suscripción ($\ge 0.5\text{ ha}$). \\
\texttt{SubscriptionPeriod} & \texttt{startsAt: Instant}, \texttt{endsAt: Instant} & Ventana temporal de vigencia activa del servicio. \\
\texttt{Subscription} \texttt{Status} & \texttt{Enum} & Estados del contrato: \texttt{PENDING\_} \texttt{PAYMENT}, \texttt{ACTIVE}, \texttt{EXPIRED}, \texttt{CANCELLED}. \\
\texttt{CodeStatus} & \texttt{Enum} & Estados de la invitación: \texttt{AVAILABLE}, \texttt{REDEEMED}, \texttt{EXPIRED}, \texttt{REVOKED}. \\
\texttt{Money} & \texttt{amount: BigDecimal}, \texttt{currency: String} & Monto dinerario exacto con divisa ISO 4217 (\texttt{PEN}). \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-36} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Subscription and Cooperative Membership.} \label{tab:tactical-36} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{HectareQuota} \texttt{Policy} & Domain Service & \texttt{requireWithin} \texttt{Quota(quota:} \texttt{HectaresQuota,} \texttt{...): void} & Valida que la superficie predial no exceda el límite contratado. \\
\texttt{Subscription} \texttt{ActivationPolicy} & Domain Service & \texttt{annualPeriod(} \texttt{approvedAt:} \texttt{Instant):} \texttt{SubscriptionPeriod} & Computa período anual estándar de 365 días desde aprobación. \\
\texttt{Subscription} \texttt{ActivationPolicy} & Domain Service & \texttt{sponsoredPeriod(} \texttt{at: Instant,} \texttt{lic:} \texttt{SubscriptionPeriod)} & Alinea la vigencia del socio con la ventana temporal de la licencia. \\
\texttt{Subscription} \texttt{Repository} & Repository & \texttt{findById(id: SubscriptionId):} \texttt{Optional<Subscription>} & Recupera suscripción por identificador unívoco. \\
\texttt{Subscription} \texttt{Repository} & Repository & \texttt{findCurrentBy} \texttt{Producer(id:} \texttt{ProducerId):} \texttt{Optional<} \texttt{Subscription>} & Obtiene la suscripción vigente del productor olivarero. \\
\texttt{Subscription} \texttt{Repository} & Repository & \texttt{save(sub: Subscription):} \texttt{Subscription} & Persiste atómicamente el estado del contrato. \\
\texttt{Cooperative} \texttt{License} \texttt{Repository} & Repository & \texttt{findById(} \texttt{id: LicenseId):} \texttt{Optional<} \texttt{CooperativeLicense>} & Carga la licencia institucional de la cooperativa. \\
\texttt{Cooperative} \texttt{License} \texttt{Repository} & Repository & \texttt{findCurrentBy} \texttt{Cooperative(id):} \texttt{Optional<} \texttt{CooperativeLicense>} & Obtiene la licencia corporativa activa de la cooperativa. \\
\texttt{Cooperative} \texttt{License} \texttt{Repository} & Repository & \texttt{save(lic: CooperativeLicense):} \texttt{Cooperative} \texttt{License} & Actualiza plazas y superficie disponible de la licencia. \\
\texttt{Invitation} \texttt{Code} \texttt{Batch} \texttt{Repository} & Repository & \texttt{findById(id: BatchId):} \texttt{Optional<} \texttt{InvitationCodeBatch>} & Carga el lote de códigos para emisión o auditoría. \\
\texttt{Invitation} \texttt{Code} \texttt{Batch} \texttt{Repository} & Repository & \texttt{findByFingerprint(fp):} \texttt{Optional<} \texttt{InvitationCodeBatch>} & Localiza el lote contenedor de un código específico presentado. \\
\texttt{Invitation} \texttt{Code} \texttt{Batch} \texttt{Repository} & Repository & \texttt{save(batch: InvitationCodeBatch):} \texttt{Invitation} \texttt{Code} \texttt{Batch} & Persiste lote y estado de códigos individuales. \\
\texttt{Subscription} \texttt{Payment} \texttt{ApprovedEvent} & Domain Event & \texttt{subscriptionId: UUID,} \texttt{producerId: UUID, receiptId: UUID} & Confirma cobro exitoso por pasarela de pagos. \\
\texttt{Subscription} \texttt{ActivatedEvent} & Domain Event & \texttt{subscriptionId: UUID,} \texttt{producerId: UUID, quotaHa: Decimal} & Notifica vigencia activa para habilitar registro predial. \\
\texttt{Subscription} \texttt{Payment} \texttt{FailedEvent} & Domain Event & \texttt{subscriptionId: UUID,} \texttt{intentId: UUID, reasonCode: String} & Informa rechazo de transacción comercial. \\
\texttt{CooperativeCode} \texttt{RedeemedEvent} & Domain Event & \texttt{subscriptionId: UUID,} \texttt{producerId: UUID, cooperativeId: UUID} & Notifica canje de código patrocinado para afiliación. \\
\texttt{Invitation} \texttt{CodesBatch} \texttt{GeneratedEvent} & Domain Event & \texttt{batchId: UUID, licenseId: UUID,} \texttt{quantity: int, reservedArea: Decimal} & Registra reserva de cupos corporativos. \\
\texttt{Invitation} \texttt{Code} \texttt{ExpiredEvent} & Domain Event & \texttt{batchId: UUID, licenseId: UUID,} \texttt{codeId: UUID, releasedQuota: Decimal} & Notifica liberación de cupo por código vencido. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-37} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Subscription and Cooperative Membership.} \label{tab:tactical-37} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/subscriptions} & \texttt{Create} \texttt{Subscription} \texttt{Request} & \texttt{Subscription} \texttt{Resource} (201 Created) & Creación de intención contractual de suscripción individual. \\
\texttt{POST} & \nolinkurl{/api/v1/subscriptions/{id}/checkouts} & \texttt{CreateCheckout} \texttt{Request} & \texttt{Checkout} \texttt{Resource} (201 Created) & Generación de preferencia de pago y URL de checkout en pasarela. \\
\texttt{GET} & \nolinkurl{/api/v1/subscriptions} & N/A & \texttt{Subscription} \texttt{Resource} (200 OK) & Consulta de suscripción activa del titular autenticado. \\
\texttt{GET} & \nolinkurl{/api/v1/subscriptions/{id}} & N/A & \texttt{Subscription} \texttt{Resource} (200 OK) & Consulta detallada del contrato de suscripción. \\
\texttt{GET} & \nolinkurl{/api/v1/subscription-plans} & N/A & \texttt{List<PlanOffer>} (200 OK) & Catálogo comercial de planes y tarifas vigentes. \\
\texttt{POST} & \nolinkurl{/api/v1/payment-notifications/mercado-pago} & \texttt{Payment} \texttt{Notification} \texttt{Request} & \texttt{200 OK} & Recepción asíncrona y reconciliación de pago externo. \\
\texttt{POST} & \nolinkurl{/api/v1/cooperatives/{id}/invitation-code-batches} & \texttt{Generate} \texttt{Invitation} \texttt{CodesBatchRequest} & \texttt{InvitationBatch} \texttt{Resource} (201 Created) & Emisión de lote de códigos por gestor contra cupo institucional. \\
\texttt{GET} & \nolinkurl{/api/v1/cooperatives/{id}/invitation-code-batches} & N/A (\texttt{?page=0} \texttt{\&size=20}) & \texttt{Page<Invitation} \texttt{BatchSummary>} (200 OK) & Consulta paginada y auditoría de códigos generados. \\
\texttt{POST} & \nolinkurl{/api/v1/cooperative-code-redemptions} & \texttt{Redeem} \texttt{Cooperative} \texttt{CodeRequest} & \texttt{Subscription} \texttt{Resource} (201 Created) & Canje de código corporativo por productor autenticado. \\
\texttt{POST} & \nolinkurl{/api/v1/cooperatives/{id}/invitation-codes/{codeId}/expiry-adjustments} & \texttt{Shorten} \texttt{Invitation} \texttt{CodeExpiryRequest} & \texttt{200 OK} & Adelanto de vigencia para expiración anticipada. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-38} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Subscription and Cooperative Membership.} \label{tab:tactical-38} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Create} \texttt{Subscription} \texttt{Request} & Request DTO & \texttt{{ planCode: String, requestedQuotaHa: Double }} & Selección comercial contrastada con el catálogo del servidor. \\
\texttt{CreateCheckout} \texttt{Request} & Request DTO & \texttt{{ returnUrl?: String }} & Solicitud de preferencia de cobro en pasarela externa. \\
\texttt{Generate} \texttt{Invitation} \texttt{CodesBatchRequest} & Request DTO & \texttt{{ quantity: Int, hectaresCapPerCode: Double, expiresAt: Instant }} & Parámetros para emisión de lote de códigos. \\
\texttt{Shorten} \texttt{Invitation} \texttt{CodeExpiryRequest} & Request DTO & \texttt{{ newExpiresAt: Instant }} & Acortamiento de vigencia de código disponible. \\
\texttt{Redeem} \texttt{Cooperative} \texttt{CodeRequest} & Request DTO & \texttt{{ code: String }} & Código de activación ingresado por el productor. \\
\texttt{Payment} \texttt{Notification} \texttt{Request} & Request DTO & \texttt{{ externalNotificationId: String, externalPaymentId: String }} & Payload webhook de notificación de pasarela. \\
\texttt{Subscription} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, mode: String, status: String, quotaHa: Double, startsAt, endsAt, entitlementActive }} & Representación pública de suscripción vigente. \\
\texttt{Checkout} \texttt{Resource} & Response DTO & \texttt{{ intentId: UUID, checkoutUrl: String, expiresAt: Instant }} & URL segura de checkout emitida por la pasarela. \\
\texttt{InvitationBatch} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, quantity: Int, availableSeats: Int, availableAreaHa: Double, codes: List<String> }} & Lote de códigos entregado al gestor cooperativo. \\
\texttt{Subscription} \texttt{ResourceAssembler} & Assembler & \texttt{toResource(} \texttt{Subscription):} \texttt{SubscriptionResource} & Mapeador del agregado a DTO público de presentación. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-39} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Subscription and Cooperative Membership.} \label{tab:tactical-39} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Create} \texttt{Subscription} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Create} \texttt{Subscription} \texttt{Command} & Comprueba perfil, serializa productor, verifica contrato vigente, crea suscripción pendiente. \\
\texttt{Create} \texttt{Checkout} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Create} \texttt{Checkout} \texttt{Command} & Persiste PaymentIntent, invoca pasarela y retorna CheckoutResource con URL segura. \\
\texttt{Process} \texttt{Payment} \texttt{Confirmation} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Process} \texttt{Payment} \texttt{Confirmation} \texttt{Command} & Valida firma webhook, reconcilia pago, activa suscripción y emite evento de activación. \\
\texttt{Generate} \texttt{Invitation} \texttt{CodesBatch} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Generate} \texttt{Invitation} \texttt{CodesBatch} \texttt{Command} & Valida gestor y cupo en licencia, descuenta plazas/área, genera lote y emite evento. \\
\texttt{Redeem} \texttt{Cooperative} \texttt{Code} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Redeem} \texttt{Cooperative} \texttt{Code} \texttt{Command} & Localiza código, valida vigencia, marca como redimido, activa patrocinio y emite evento. \\
\texttt{Shorten} \texttt{Invitation} \texttt{CodeExpiry} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Shorten} \texttt{Invitation} \texttt{CodeExpiry} \texttt{Command} & Adelanta expiración de código disponible, transiciona a expirado y emite evento. \\
\texttt{OnInvitation} \texttt{CodeExpired} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Invitation} \texttt{Code} \texttt{ExpiredEvent} & Escucha expiración y restituye plazas y hectáreas a la licencia cooperativa. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-40} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Subscription and Cooperative Membership.} \label{tab:tactical-40} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Subscription} \texttt{JpaRepository} & Persistence & Spring Data JPA & Operaciones sobre esquemas \texttt{subscription} en PostgreSQL. \\
\texttt{MercadoPago} \texttt{Gateway} \texttt{Adapter} & External Adapter & Mercado Pago SDK & Creación de preferencias de pago y consulta de órdenes de cobro. \\
\texttt{SpringEventBus} \texttt{Adapter} & Integration & Spring ApplicationEvent & Publicación y enrutamiento interno de eventos transaccionales. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Coordinación de pago seguro (Hosted Checkout):**
  * *Android Nativo (Kotlin):* Componente `AndroidHostedCheckoutCoordinator` que lanza Chrome Custom Tabs hacia la pasarela de Mercado Pago tras obtener `checkoutUrl` (`Checkout` `Resource`), reconsultando el estado autoritativo al regresar a la aplicación sin confiar en callbacks locales.
  * *Cross-Platform (Flutter/Dart):* Componente `FlutterHostedCheckoutCoordinator` implementado con `url_launcher` para apertura controlada de checkout y refresco asíncrono del contrato.
* **Caché local de derechos y cupos (`entitlement_cache`):**
  * *Android Nativo (Room / SQLite):* `EntitlementCacheDao` y entidad `LocalEntitlementEntity` que custodian el estado del contrato (`status`), vigencia (`starts_at`, `ends_at`) y hectáreas autorizadas (`quota_ha`) asociadas al `account_id` para validación inmediata previa a la edición de polígonos.
  * *Cross-Platform (sqflite / SQLite):* Tabla local `entitlement_cache` gestionada por `LocalDataAccess`. No autoriza operaciones de alta comercial offline, operando como proyección de solo lectura.

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-41} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Subscription and Cooperative Membership.} \label{tab:tactical-41} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{subscrip-} \texttt{tions} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador de la suscripción. \\
\texttt{subscrip-} \texttt{tions} & \texttt{producer\_id} & \texttt{UUID} & \texttt{NOT NULL, UNIQUE} & Productor titular de los derechos. \\
\texttt{subscrip-} \texttt{tions} & \texttt{plan\_type} & \texttt{VARCHAR(50)} & \texttt{NOT NULL} & Modalidad (\texttt{INDIVIDUAL\_PAID}, \texttt{COOPERATIVE\_SPONSORED}). \\
\texttt{subscrip-} \texttt{tions} & \texttt{quota\_ha} & \texttt{NUMERIC(8,2)} & \texttt{NOT NULL, CHECK} & Hectáreas autorizadas para parcelas. \\
\texttt{subscrip-} \texttt{tions} & \texttt{status} & \texttt{VARCHAR(30)} & \texttt{NOT NULL} & Estado (\texttt{ACTIVE}, \texttt{PENDING\_PAYMENT}, \texttt{EXPIRED}). \\
\texttt{subscrip-} \texttt{tions} & \texttt{starts\_at} & \texttt{TIMESTAMPTZ} & \texttt{NULL} & Inicio de vigencia activa. \\
\texttt{subscrip-} \texttt{tions} & \texttt{ends\_at} & \texttt{TIMESTAMPTZ} & \texttt{NULL} & Término de vigencia activa. \\
\texttt{coop-} \texttt{erative\_} \texttt{licenses} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Licencia corporativa institucional. \\
\texttt{coop-} \texttt{erative\_} \texttt{licenses} & \texttt{coop-} \texttt{erative\_} \texttt{id} & \texttt{UUID} & \texttt{NOT NULL, UNIQUE} & Cooperativa propietaria del convenio. \\
\texttt{coop-} \texttt{erative\_} \texttt{licenses} & \texttt{total\_seats} & \texttt{INT} & \texttt{NOT NULL, CHECK} & Plazas máximas autorizadas. \\
\texttt{coop-} \texttt{erative\_} \texttt{licenses} & \texttt{issued\_seats} & \texttt{INT} & \texttt{NOT NULL, CHECK} & Plazas comprometidas en códigos vigentes. \\
\texttt{coop-} \texttt{erative\_} \texttt{licenses} & \texttt{total\_area\_ha} & \texttt{NUMERIC(10,2)} & \texttt{NOT NULL} & Superficie máxima del convenio. \\
\texttt{coop-} \texttt{erative\_} \texttt{licenses} & \texttt{issued\_area\_ha} & \texttt{NUMERIC(10,2)} & \texttt{NOT NULL} & Superficie comprometida en códigos vigentes. \\
\texttt{invita-} \texttt{tion\_} \texttt{codes} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador único del código. \\
\texttt{invita-} \texttt{tion\_} \texttt{codes} & \texttt{batch\_id} & \texttt{UUID} & \texttt{NOT NULL, FK} & Lote de procedencia. \\
\texttt{invita-} \texttt{tion\_} \texttt{codes} & \texttt{code\_hash} & \texttt{VARCHAR(64)} & \texttt{NOT NULL, UNIQUE} & Hash SHA-256 del código alfanumérico. \\
\texttt{invita-} \texttt{tion\_} \texttt{codes} & \texttt{quota\_ha} & \texttt{NUMERIC(8,2)} & \texttt{NOT NULL} & Cobertura en hectáreas que confiere el código. \\
\texttt{invita-} \texttt{tion\_} \texttt{codes} & \texttt{status} & \texttt{VARCHAR(30)} & \texttt{NOT NULL} & Estado (\texttt{AVAILABLE}, \texttt{REDEEMED}, \texttt{EXPIRED}). \\
\texttt{invita-} \texttt{tion\_} \texttt{codes} & \texttt{expires\_at} & \texttt{TIMESTAMPTZ} & \texttt{NOT NULL} & Marca temporal límite para canje. \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS subscription;

CREATE TABLE subscription.subscriptions (
    id          UUID PRIMARY KEY,
    producer_id UUID NOT NULL,
    plan_type   VARCHAR(50) NOT NULL,
    quota_ha    NUMERIC(8,2) NOT NULL CHECK (quota_ha > 0),
    status      VARCHAR(30) NOT NULL,
    starts_at   TIMESTAMPTZ,
    ends_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_subscriptions_producer
        UNIQUE (producer_id)
);

CREATE TABLE subscription.cooperative_licenses (
    id             UUID PRIMARY KEY,
    cooperative_id UUID NOT NULL,
    total_seats    INT NOT NULL CHECK (total_seats > 0),
    issued_seats   INT NOT NULL DEFAULT 0
        CHECK (issued_seats <= total_seats),
    total_area_ha  NUMERIC(10,2) NOT NULL
        CHECK (total_area_ha > 0),
    issued_area_ha NUMERIC(10,2) NOT NULL DEFAULT 0
        CHECK (issued_area_ha <= total_area_ha),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_licenses_cooperative
        UNIQUE (cooperative_id)
);

CREATE TABLE subscription.invitation_codes (
    id         UUID PRIMARY KEY,
    batch_id   UUID NOT NULL,
    code_hash  VARCHAR(64) NOT NULL UNIQUE,
    quota_ha   NUMERIC(8,2) NOT NULL CHECK (quota_ha > 0),
    status     VARCHAR(30) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-42} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Subscription and Cooperative Membership.} \label{tab:tactical-42} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{Subscription} \texttt{Controller}; \texttt{PaymentWebhookController}; \texttt{CooperativeInvitationController}; \texttt{CodeRedemptionController} & Endpoints REST para suscripciones, checkout, webhooks IPN, canje y lotes de códigos. & Spring MVC, Webhook Filter \\
Application & \texttt{Subscription} \texttt{CommandService}; \texttt{Subscription} \texttt{QueryService}; \texttt{PaymentReconciliation} \texttt{CommandService}; \texttt{CooperativeInvitation} \texttt{CommandService}; \texttt{CooperativeInvitation} \texttt{QueryService} & Orquestacinnn de comandos comerciales/pagos, consultas de planes/cuotas, conciliación IPN y canjes. & Spring \texttt{@Transactional}, \texttt{@Service} \\
Domain & \texttt{Subscription} \texttt{Repository}; \texttt{CooperativeInvitation} \texttt{BatchRepository}; \texttt{CooperativeLicense} \texttt{Repository}; \texttt{InvitationCodeGenerator}; \texttt{HectareQuotaPolicy}; \texttt{SubscriptionActivationPolicy} & Puertos de repositorio y servicios de dominio para cuotas de hectáreas, códigos y períodos de vigencia. & Java Security / SecureRandom \\
Infrastructure & \texttt{JpaSubscription} \texttt{RepositoryAdapter}; \texttt{JpaInvitationBatch} \texttt{RepositoryAdapter}; \texttt{JpaCooperativeLicense} \texttt{RepositoryAdapter}; \texttt{MercadoPagoPayment} \texttt{Adapter}; \texttt{DomainEventPublisher} & Adaptadores de persistencia JPA sobre PostgreSQL, cliente HTTP de Mercado Pago y publicador de eventos. & Spring Data JPA, HTTP Client \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El productor formaliza la intención de alta enviando `POST` \nolinkurl{/api/v1/subscriptions} hacia `SubscriptionController`, el cual delega en `SubscriptionCommandService`; este valida el cupo mediante `HectareQuotaPolicy` y persiste la suscripción en estado pendiente vía `SubscriptionRepository`.
2. Seguidamente, despacha `POST` \nolinkurl{/api/v1/subscriptions/{id}/checkouts}; `SubscriptionCommandService` registra el `PaymentIntent`, se comunica con `MercadoPagoPaymentAdapter` y retorna el `checkoutUrl` seguro de Mercado Pago (`Checkout` `Resource`). Las consultas de suscripción y cuotas activas se resuelven a través de `SubscriptionQueryService`.
3. El usuario completa la transacción en el gateway; Mercado Pago notifica asíncronamente a `POST` \nolinkurl{/api/v1/payment-notifications/mercado-pago}.
4. `PaymentWebhookController` autentica la firma criptográfica HMAC y delega el procesamiento en `PaymentReconciliationCommandService`.
5. `PaymentReconciliationCommandService` actualiza el estado a activo mediante `SubscriptionRepository` y publica el evento de activación de suscripción vía `DomainEventPublisher`.
6. En el modelo corporativo, el socio canjea su cupo con `POST` \nolinkurl{/api/v1/cooperative-code-redemptions}; `CodeRedemptionController` delega en `CooperativeInvitationCommandService`, el cual valida el código contra `CooperativeInvitationBatchRepository`, marca el código como redimido y emite evento de canje hacia *Cooperative Operations*.
7. El gestor cooperativo puede consultar el estado de lotes y cupos mediante `CooperativeInvitationQueryService`, o bien acortar la vigencia de un código disponible despachando `POST` \nolinkurl{/api/v1/cooperatives/{id}/invitation-codes/{codeId}/expiry-adjustments} hacia `CooperativeInvitationCommandService`, lo cual dispara evento de expiración y restituye cupos a la licencia en `CooperativeLicenseRepository`.

A continuación, en la \autoref{fig:c4-component-subscription} se esquematiza el diagrama de componentes del Bounded Context Subscription and Cooperative Membership:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Subscription and Cooperative Membership.} \label{fig:c4-component-subscription}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-subscription.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Subscription and Cooperative Membership. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-subscription}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-subscription-part1} y \autoref{fig:database-diagram-subscription-part2}) para el Bounded Context Subscription and Cooperative Membership:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Subscription and Cooperative Membership.} \label{fig:class-diagram-subscription}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-subscription.png}
\caption*{\textit{Nota.} Clases, entidades internas, acumuladores de cupo y objetos de valor de Subscription. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Subscription and Cooperative Membership (Parte 1).} \label{fig:database-diagram-subscription-part1}
\vspace{0.25cm}
\centering
\includegraphics[width=0.25\textwidth]{report/assets/tactical-diagrams/database-diagram-subscription-part1.png}
\caption*{\textit{Nota.} Tablas relacionales principales y acumuladores de cuotas (Parte 1). Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Subscription and Cooperative Membership (Parte 2).} \label{fig:database-diagram-subscription-part2}
\vspace{0.25cm}
\centering
\includegraphics[width=0.25\textwidth]{report/assets/tactical-diagrams/database-diagram-subscription-part2.png}
\caption*{\textit{Nota.} Tablas relacionales de lotes y códigos de canje (Parte 2). Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: Olive Orchard and Plot Management

Propósito: Administra el catastro territorial y la caracterización dendrométrica del olivar. Constituye la base física sobre la cual operan los demás módulos de Viora. Es responsable del Aggregate Root `Plot`, custodiando la delimitación geográfica poligonal (GeoJSON), el marco de plantación, la densidad de árboles por hectárea, la variedad cultivada (*Criolla*, *Sevillana*, *Manzanilla*, *Arbequina*) y la fecha de última poda. Valida que el área predial no exceda la cuota suscrita y publica eventos soberanos del ciclo de vida predial (`PlotRegisteredEvent`, `PlotRemovedEvent`), habilitando la compensación asíncrona de prescripciones activas en contextos downstream.

#### Domain Layer

##### Modelo de dominio: `Plot` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-43} se describe la estructura y delimitación transaccional del modelo `Plot`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio Plot (Aggregate Root) en Olive Orchard and Plot Management.} \label{tab:tactical-43} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Delimita la identidad geográfica, catastral y dendrométrica del cuartel olivarero y salvaguarda el historial de linderos. \\
Relaciones de dominio & Referencia externa por ID a \texttt{OwnerId}. Raíz espacial referenciada por telemetría, fenología y muestreos. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-44} se presentan los atributos, tipos de datos e invariantes que rigen a `Plot`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo Plot en Olive Orchard and Plot Management.} \label{tab:tactical-44} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{PlotId} & Identificador único universal inmutable del predio. \\
\texttt{producerId} & \texttt{UserId} & Identificador del agricultor propietario o arrendatario. \\
\texttt{name} & \texttt{PlotName} & Denominación agronómica descriptiva del cuartel. \\
\texttt{variety} & \texttt{OliveVariety} & Variedad botánica: \texttt{CRIOLLA}, \texttt{SEVILLANA}, \texttt{MANZANILLA}, \texttt{ARBEQUINA}. \\
\texttt{geometry} & \texttt{PlotGeometry} & Polígono catastral cerrado en formato WGS84 / GeoJSON. \\
\texttt{plantationFrame} & \texttt{PlantationFrame} & Marco de plantación (distancia entre hileras y entre árboles). \\
\texttt{treeDensity} & \texttt{TreeDensity} & Densidad efectiva calculada (árboles por hectárea teóricos u observados). \\
\texttt{lastPruningDate} & \texttt{Optional<LocalDate>} & Fecha de última labor de poda registrada. \\
\texttt{status} & \texttt{PlotStatus} & Estado del predio: \texttt{ACTIVE}, \texttt{REMOVED\_SOFT\_DELETE}. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-45} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `Plot`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de Plot en Olive Orchard and Plot Management.} \label{tab:tactical-45} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{create} & \texttt{producerId: UserId}, \texttt{name: PlotName}, \texttt{variety: OliveVariety}, \texttt{geom: PlotGeometry}, \texttt{frame: PlantationFrame} & \texttt{Plot} & Fábrica que valida topología poligonal, calcula área neta y emite \texttt{PlotDelimited} \texttt{Event}. \\
\texttt{update} & \texttt{newName: PlotName}, \texttt{newGeometry: PlotGeometry}, \texttt{newFrame: PlantationFrame}, \texttt{newPruningDate: LocalDate}, \texttt{expectedRevision: long} & \texttt{void} & Actualiza de forma atómica e integral denominación, límites espaciales, marco silvícola y labor de poda, recalibra densidad arbórea, valida concurrencia optimista incrementando revisión y emite \texttt{PlotUpdatedEvent}. \\
\texttt{remove} & \texttt{reason: String} & \texttt{void} & Ejecuta baja lógica preservando trazabilidad histórica y emite \texttt{PlotRemovedEvent}. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-46} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Olive Orchard and Plot Management.} \label{tab:tactical-46} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{PlotId} & \texttt{UUID v4} & Identificador único universal inmutable de la parcela. \\
\texttt{PlotName} & \texttt{String} & Nombre identificador del cuartel o predio (longitud 3 a 100 caracteres). \\
\texttt{OliveVariety} & \texttt{Enum} & Variedad botánica: \texttt{CRIOLLA}, \texttt{SEVILLANA}, \texttt{MANZANILLA}, \texttt{ARBEQUINA}. \\
\texttt{PlotGeometry} & \texttt{GeoJSON (Polygon)} & Polígono geográfico que computa internamente el área en hectáreas. \\
\texttt{PlantationFrame} & \texttt{rowSpacingM: Double}, \texttt{treeSpacingM: Double} & Distancias de siembra en metros ($m \times m$). \\
\texttt{TreeDensity} & \texttt{treesPerHectare: Int} & Densidad calculada ($D = 10000 / (row \times tree)$). \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-47} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Olive Orchard and Plot Management.} \label{tab:tactical-47} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Cadastral} \texttt{Geometry} \texttt{Service} & Domain Service & \texttt{validate(geoJson: String): void} & Verifica topología cerrada sin autointersecciones ni anillos abiertos. \\
\texttt{Cadastral} \texttt{Geometry} \texttt{Service} & Domain Service & \texttt{netAreaHa(geoJson: String): Double} & Calcula superficie neta en hectáreas geodésicas. \\
\texttt{Cadastral} \texttt{Geometry} \texttt{Service} & Domain Service & \texttt{computeGeometry(geoJson: String): PlotGeometry} & Valida y construye el objeto de valor \texttt{PlotGeometry}. \\
\texttt{Dendrometry} \texttt{Service} & Domain Service & \texttt{calculate(areaHa: Decimal, grid: PlantingGrid, treeCount: int):} \texttt{DendrometricAttributes} & Deriva densidades teóricas y observadas por hectárea. \\
\texttt{Plot} \texttt{Repository} & Repository & \texttt{findById(id: PlotId): Optional<Plot>} & Carga la parcela por su identificador primario. \\
\texttt{Plot} \texttt{Repository} & Repository & \texttt{findActiveByOwner(} \texttt{ownerId: OwnerId):} \texttt{List<Plot>} & Lista parcelas activas del productor para gestión predial. \\
\texttt{Plot} \texttt{Repository} & Repository & \texttt{sumActiveAreaBy} \texttt{Owner(ownerId:} \texttt{OwnerId): Decimal} & Consolida superficie activa para auditoría de cuotas. \\
\texttt{Plot} \texttt{Repository} & Repository & \texttt{existsByNameAndProducerId(} \texttt{name: PlotName, producerId:} \texttt{ProducerId): boolean} & Verifica unicidad de nombre de cuartel por productor. \\
\texttt{Plot} \texttt{Repository} & Repository & \texttt{save(plot: Plot): Plot} & Persiste atómicamente la entidad y linderos espaciales. \\
\texttt{PlotDelimited} \texttt{Event} & Domain Event & \texttt{plotId: UUID, ownerId: UUID, polygon: String, variety: String, occurredOn: Instant} & Notifica alta de cuartel para inicializar telemetría. \\
\texttt{PlotUpdated} \texttt{Event} & Domain Event & \texttt{plotId: UUID, ownerId: UUID, name: String, polygon: String, areaHa: Decimal, revision: Long, occurredOn: Instant} & Notifica rectificación integral de parcela para recalibrar modelos de sensor y fenología. \\
\texttt{PlotRemovedEvent} & Domain Event & \texttt{plotId: UUID, ownerId: UUID, reason: String, occurredOn: Instant} & Notifica baja lógica de parcela para desvincular sensores. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-48} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Olive Orchard and Plot Management.} \label{tab:tactical-48} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/plots} & \texttt{CreatePlot} \texttt{Request} & \texttt{PlotResource} (201 Created) & Delimitación y registro georreferenciado de parcela con validación de cuota. \\
\texttt{GET} & \nolinkurl{/api/v1/plots} & N/A (\texttt{?updatedSince=}) & \texttt{List<} \texttt{PlotResource>} (200 OK) & Listado y sincronización incremental delta de parcelas activas. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}} & N/A & \texttt{PlotResource} (200 OK) & Consulta de detalle agronómico, geometría y densidad arbórea. \\
\texttt{PUT} & \nolinkurl{/api/v1/plots/{plotId}} & \texttt{UpdatePlot} \texttt{Request} (Req:\texttt{If-Match}) & \texttt{PlotResource} (200 OK) & Actualización de linderos y marco dendrométrico con control de concurrencia. \\
\texttt{DELETE} & \nolinkurl{/api/v1/plots/{plotId}} & N/A & \texttt{MessageResource} (200 OK) & Baja lógica soberana de la parcela predial. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-49} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Olive Orchard and Plot Management.} \label{tab:tactical-49} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{CreatePlotRequest} & Request DTO & \texttt{{ name: String, variety: String, geoJson: String, rowSpacingM: Double, treeSpacingM: Double }} & Entrada para registro predial. \\
\texttt{UpdatePlotRequest} & Request DTO & \texttt{{ name: String, rowSpacingM: Double, treeSpacingM: Double, lastPruningDate: LocalDate, polygonGeoJson: String }} & Modificación integral agronómica y espacial del lote (controlado con \texttt{If-Match}). \\
\texttt{PlotResource} & Response DTO & \texttt{{ id: UUID, name: String, variety: String, areaHa: Double, treeDensity: Int, geoJson: String }} & Representación pública del predio. \\
\texttt{MessageResource} & Response DTO & \texttt{{ message: String }} & Confirmación informativa para operaciones de mutación sin retorno de entidad. \\
\texttt{PlotResource} \texttt{Assembler} & Assembler & \texttt{toResource(} \texttt{Plot):} \texttt{PlotResource} & Mapeador a DTO con cálculo de métricas. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-50} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Olive Orchard and Plot Management.} \label{tab:tactical-50} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Delimit} \texttt{Plot} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Delimit} \texttt{Plot} \texttt{Command} & Valida cuota de hectáreas, instancia \texttt{Plot}, calcula densidad, persiste y emite \texttt{PlotRegisteredEvent}. \\
\texttt{Update} \texttt{Plot} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Update} \texttt{Plot} \texttt{Command} & Carga predio, valida titularidad y \texttt{If-Match}, actualiza linderos, marco, nombre y poda, persiste y emite \texttt{PlotUpdatedEvent}. \\
\texttt{Remove} \texttt{Plot} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Remove} \texttt{Plot} \texttt{Command} & Marca predio inactivo, persiste la baja y emite \texttt{PlotRemovedEvent}. \\
\texttt{Get} \texttt{Plot} \texttt{ById} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Plot} \texttt{ById} \texttt{Query} & Consulta predio por ID con optimización de lectura y retorno en DTO. \\
\texttt{Get} \texttt{All} \texttt{Active} \texttt{Plots} \texttt{By} \texttt{Producer} \texttt{Id} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{All} \texttt{Active} \texttt{Plots} \texttt{By} \texttt{Producer} \texttt{Id} \texttt{Query} & Consulta declarativa de parcelas en estado activo pertenecientes a un titular. \\
\texttt{Get} \texttt{Plots} \texttt{Delta} \texttt{Sync} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Plots} \texttt{Delta} \texttt{Sync} \texttt{By} \texttt{Producer} \texttt{Id} \texttt{And} \texttt{Updated} \texttt{Since} \texttt{Query} & Sincronización incremental delta de parcelas modificadas a partir de una marca temporal. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-51} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Olive Orchard and Plot Management.} \label{tab:tactical-51} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{PlotPersistence} \texttt{Repository} & Persistence & Spring Data JPA & Acceso a tabla \texttt{plots} en PostgreSQL con soporte PostGIS/GeoJSON. \\
\texttt{PlotRepositoryImpl} & Adapter & Spring Component & Implementa el puerto de dominio \texttt{Plot} \texttt{Repository}. \\
\texttt{MapboxSpatial} \texttt{ValidationAdapter} & Adapter & GeoTools / JTS & Validación topológica de polígonos y cálculo esferoidal de área. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Adaptador de mapas y edición poligonal (Mapbox):**
  * *Android Nativo (Kotlin):* Componente `AndroidPlotMapAdapter` integrado con Mapbox Maps SDK para Android, permitiendo digitalizar vértices georreferenciados en pantalla, calcular visualmente la geometría y convertirla a GeoJSON RFC 7946 sin persistir desplazamientos del usuario como entidades de dominio.
  * *Cross-Platform (Flutter/Dart):* Componente `FlutterPlotMapAdapter` apoyado en `mapbox_maps_flutter` con idéntico contrato de renderizado y captura vectorial.
* **Caché local de parcelas (`plot_cache`):**
  * *Android Nativo (Room / SQLite):* `PlotCacheDao` y entidad `LocalPlotCacheEntity` con clave primaria compuesta `(account_id, plot_id)`, almacenando el polígono GeoJSON como `TEXT`, caracterización varietal, densidades y marca `fetched_at` para consulta cartográfica offline en predios remotos.
  * *Cross-Platform (sqflite / SQLite):* Tabla local `plot_cache` gestionada por `LocalDataAccess` con invalidación selectiva ante modificaciones remotas (`PlotUpdatedEvent`).

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-52} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Olive Orchard and Plot Management.} \label{tab:tactical-52} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{plots} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador único de la parcela. \\
\texttt{plots} & \texttt{producer\_id} & \texttt{UUID} & \texttt{NOT NULL, INDEX} & Productor propietario de la parcela. \\
\texttt{plots} & \texttt{name} & \texttt{VARCHAR(100)} & \texttt{NOT NULL} & Denominación del cuartel o predio. \\
\texttt{plots} & \texttt{variety} & \texttt{VARCHAR(50)} & \texttt{NOT NULL} & Variedad botánica de olivo cultivada. \\
\texttt{plots} & \texttt{area\_ha} & \texttt{NUMERIC(8,2)} & \texttt{NOT NULL, CHECK} & Superficie física calculada en hectáreas. \\
\texttt{plots} & \texttt{row\_spacing\_m} & \texttt{NUMERIC(4,2)} & \texttt{NOT NULL} & Distancia entre hileras en metros. \\
\texttt{plots} & \texttt{tree\_spacing\_} \texttt{m} & \texttt{NUMERIC(4,2)} & \texttt{NOT NULL} & Distancia entre plantas en metros. \\
\texttt{plots} & \texttt{tree\_density} & \texttt{INT} & \texttt{NOT NULL} & Densidad calculada de árboles/ha. \\
\texttt{plots} & \texttt{polygon\_} \texttt{geojson} & \texttt{TEXT} & \texttt{NOT NULL} & Polígono espacial en formato GeoJSON. \\
\texttt{plots} & \texttt{last\_pruning\_} \texttt{date} & \texttt{DATE} & \texttt{NULL} & Fecha registrada de la última poda. \\
\texttt{plots} & \texttt{status} & \texttt{VARCHAR(30)} & \texttt{NOT NULL} & Estado del predio (\texttt{ACTIVE}, \texttt{REMOVED}). \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS orchard;

CREATE TABLE orchard.plots (
    id                UUID PRIMARY KEY,
    producer_id       UUID NOT NULL,
    name              VARCHAR(100) NOT NULL,
    variety           VARCHAR(50) NOT NULL,
    area_ha           NUMERIC(8,2) NOT NULL
        CHECK (area_ha > 0),
    row_spacing_m     NUMERIC(4,2) NOT NULL
        CHECK (row_spacing_m > 0),
    tree_spacing_m    NUMERIC(4,2) NOT NULL
        CHECK (tree_spacing_m > 0),
    tree_density      INT NOT NULL
        CHECK (tree_density >= 50),
    polygon_geojson   TEXT NOT NULL,
    last_pruning_date DATE,
    status            VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_plots_producer
    ON orchard.plots(producer_id);
CREATE INDEX idx_plots_status ON orchard.plots(status);
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-53} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Olive Orchard and Plot Management.} \label{tab:tactical-53} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{PlotController}; \texttt{OrchardContextFacade} & Controladores REST para gestión predial y Fachada ACL (Open Host Service) para consumo inter-contexto seguro. & Spring MVC, Jakarta Validation, Java puro \\
Application & \texttt{PlotCommandService}; \texttt{PlotQueryService}; \texttt{OrchardContextFacadeImpl} & Orquestación de comandos de predio, control de concurrencia optimista (\texttt{If-Match}), consultas e implementación de fachada ACL. & Spring \texttt{@Transactional}, \texttt{@Service} \\
Domain & \texttt{Plot} \texttt{Repository}; \texttt{GeospatialPolygonValidator}; \texttt{SubscriptionQuotaPort} & Contrato de persistencia (puerto de dominio), validación topológica JTS y verificación de cupo de ha. & Java puro / JTS Topology Suite \\
Infrastructure & \texttt{PlotRepositoryImpl}; \texttt{DomainEventPublisher} & Persistencia JPA en PostgreSQL con soporte geoespacial PostGIS y despacho de eventos de dominio. & Spring Data JPA, PostGIS, Hibernate Spatial \\
\end{longtable}
\end{center}

##### Protocolo de Anti-Corrupción (ACL) y Fachada Inter-Contexto (Open Host Service)
&nbsp;

Para preservar la pureza del dominio y dar cumplimiento a las relaciones upstream-downstream del Context Map (\texttt{e6: bc4 -> bc5}, \texttt{e7: bc4 -> bc2}, \texttt{e8: bc4 -> bc1}), \texttt{orchard} actúa como Open Host Service (OHS) proveyendo una fachada canónica en su capa de interfaces (\texttt{interfaces.acl}) sin exponer entidades persistentes ni modelos agregados:

\begin{enumerate}
    \item \textbf{Contrato público de la fachada (\texttt{OrchardContextFacade}):}
    \begin{itemize}
        \item \texttt{boolean existsActivePlot(String plotId)}: Comprueba la existencia y estado operacional \texttt{ACTIVE} de la parcela identificada por su UUID string.
        \item \texttt{Optional<PlotAgronomicProfileDto> fetchPlotAgronomicProfile(String plotId)}: Provee a contextos downstream (\textit{Phenology}, \textit{Thinning}) los atributos agronómicos esenciales (\texttt{plotId}, \texttt{producerId}, \texttt{name}, \texttt{variety}, \texttt{areaHa}, \texttt{treeDensity}, \texttt{status}) mediante un DTO agnóstico (Published Language).
    \end{itemize}
    \item \textbf{Consumo aguas abajo en contextos consumidores:}
    \begin{itemize}
        \item \textit{Agroclimatic Telemetry (\texttt{bc5})}: Implementa \texttt{ExternalOrchardService} en \texttt{application.internal.outboundservices.acl}, delegando en \texttt{OrchardContextFacade} la verificación previa al registro o activación de sondas y estaciones IoT.
        \item \textit{Phenology Analytics (\texttt{bc2})}: Implementa \texttt{ExternalOrchardService} en \texttt{application.internal.outboundservices.acl}, garantizando que el registro de cosechas anuales se asocie a cuarteles activos y cuente con la densidad y superficie requerida para métricas de vecería ($BBI$).
        \item \textit{Crop Load Regulation (\texttt{bc1})}: Implementa \texttt{ExternalOrchardService} para validar predios al prescribir aclareo de frutos y verificar la variedad botánica de olivo.
    \end{itemize}
\end{enumerate}

##### Flujo de comunicación y conectividad
&nbsp;

1. El contenedor cliente móvil (`Android Application` o `Cross-Platform Application`) captura vértices GPS y envía `POST` \nolinkurl{/api/v1/plots} hacia `PlotController`.
2. El controlador valida el cuerpo sintácticamente y delega las operaciones de escritura en `PlotCommandService` (mientras que las lecturas de predios y sincronización delta son resueltas por `PlotQueryService`).
3. `PlotCommandService` invoca `SubscriptionQuotaPort` para verificar el cupo activo disponible en la suscripción del productor.
4. Si hay cupo suficiente, delega en `GeospatialPolygonValidator` la validación de no auto-intersección del polígono GeoJSON y computa área y densidad arbórea.
5. Se persiste el predio y su registro de revisión histórica en PostgreSQL a través del puerto `PlotRepository` (implementado por `PlotRepositoryImpl`).
6. Se dispara `PlotRegisteredEvent` vía `DomainEventPublisher`, permitiendo que *Telemetry* y *Phenology* sincronicen el seguimiento agronómico.

A continuación, en la \autoref{fig:c4-component-orchard} se esquematiza el diagrama de componentes del Bounded Context Olive Orchard and Plot Management:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Olive Orchard and Plot Management.} \label{fig:c4-component-orchard}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-orchard.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Olive Orchard and Plot Management. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-orchard}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-orchard}) para el Bounded Context Olive Orchard and Plot Management:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Olive Orchard and Plot Management.} \label{fig:class-diagram-orchard}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-orchard.png}
\caption*{\textit{Nota.} Estructura estática de clases, atributos dendrométricos y métodos de Plot. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Olive Orchard and Plot Management.} \label{fig:database-diagram-orchard}
\vspace{0.25cm}
\centering
\includegraphics[width=0.60\textwidth]{report/assets/tactical-diagrams/database-diagram-orchard.png}
\caption*{\textit{Nota.} Estructura de la tabla plots, columnas espaciales e índices en PostgreSQL. Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: Agroclimatic Telemetry and Sensor Monitoring

Propósito: Custodia la memoria agroclimática y el monitoreo de microclima del olivar. Captura series temporales horarias de temperatura ambiente, humedad relativa, radiación solar y humedad edáfica mediante dos fuentes: sondas virtuales calibradas (`IoTDevice`) y pronósticos meteorológicos a 7 días obtenidos de Open-Meteo vía un programador interno (`WeatherSyncScheduler` `@Scheduled`). Evalúa en tiempo real riesgos fisiológicos de estrés hídrico y choque térmico en floración, despachando alertas in-app y proveyendo datos climáticos para la acumulación de frío en Fenología.

#### Domain Layer

##### Modelo de dominio: `VirtualSensorNode` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-54} se describe la estructura y delimitación transaccional del modelo `VirtualSensorNode`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio VirtualSensorNode (Aggregate Root) en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-54} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Gestiona el inventario, profundidad y factor de calibración de los dispositivos sensores de suelo y microclima. \\
Relaciones de dominio & Referencia lógica a \texttt{PlotId}. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-55} se presentan los atributos, tipos de datos e invariantes que rigen a `VirtualSensorNode`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo VirtualSensorNode en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-55} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{SensorNodeId} & Identificador único del nodo sensor virtual. \\
\texttt{plotId} & \texttt{PlotId} & Referencia lógica a la parcela monitoreada. \\
\texttt{name} & \texttt{SensorNodeName} & Denominación descriptiva del punto de monitoreo. \\
\texttt{type} & \texttt{SensorNodeType} & Tipo: \texttt{MICROCLIMATE} o \texttt{SOIL\_PROBE}. \\
\texttt{depthCm} & \texttt{SensorDepth} & Profundidad de instalación de sondas (30 cm o 60 cm). \\
\texttt{soilTextureType} & \texttt{SoilTextureType} & Textura edáfica: franca, franco-arenosa, arcillosa. \\
\texttt{calibration} \texttt{Multiplier} & \texttt{Calibration} \texttt{Multiplier} & Factor de ajuste empírico en rango [0.50, 2.00]. \\
\texttt{status} & \texttt{SensorNodeStatus} & Estado: \texttt{ACTIVE}, \texttt{PAUSED}, \texttt{UNLINKED}. \\
\texttt{lastReading} \texttt{Timestamp} & \texttt{Instant} & Marca temporal de la última telemetría procesada. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-56} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `VirtualSensorNode`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de VirtualSensorNode en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-56} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{register} & \texttt{id: SensorNodeId}, \texttt{plotId: PlotId}, \texttt{name: SensorNodeName}, \texttt{type: SensorNodeType}, \texttt{depth: SensorDepth}, \texttt{texture: SoilTextureType}, \texttt{mult: CalibrationMultiplier} & \texttt{Virtual} \texttt{SensorNode} & Registra el nodo en el inventario predial y emite \texttt{VirtualSensorNodeLinkedEvent}. \\
\texttt{calibrate} & \texttt{depth: SensorDepth}, \texttt{texture: SoilTextureType}, \texttt{mult: CalibrationMultiplier} & \texttt{void} & Actualiza coeficientes de cálculo de humedad volumétrica. \\
\texttt{rename} & \texttt{newName: SensorNodeName} & \texttt{void} & Actualiza la denominación del nodo garantizando unicidad en el predio. \\
\texttt{unlink} & \texttt{void} & \texttt{void} & Desvincula lógicamente el sensor de la parcela activa. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `TelemetrySeries` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-57} se describe la estructura y delimitación transaccional del modelo `TelemetrySeries`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio TelemetrySeries (Aggregate Root) en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-57} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Agrupa las lecturas temporales horarias, pronósticos y eventos de estrés agroclimático para un sensor predial. \\
Relaciones de dominio & Referencia a \texttt{SensorNodeId} y \texttt{PlotId}. Compone lecturas horarias, pronósticos e incidentes. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-58} se presentan los atributos, tipos de datos e invariantes que rigen a `TelemetrySeries`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo TelemetrySeries en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-58} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{TelemetrySeriesId} & Identificador único de la serie temporal. \\
\texttt{sensorNodeId} & \texttt{SensorNodeId} & Nodo sensor emisor de los datos. \\
\texttt{plotId} & \texttt{PlotId} & Parcela a la que pertenece la serie. \\
\texttt{readings} & \texttt{List<} \texttt{HourlyTelemetry} \texttt{Reading>} & Historial cronológico de mediciones horarias. \\
\texttt{forecastDays} & \texttt{List<} \texttt{Weather} \texttt{ForecastDay>} & Pronóstico meteorológico a 7 días vigente. \\
\texttt{incidents} & \texttt{List<} \texttt{Agroclimatic} \texttt{Incident>} & Registro de alertas activas e históricas de estrés. \\
\texttt{currentStatus} & \texttt{Telemetry} \texttt{SeriesStatus} & Estado operativo: \texttt{NORMAL}, \texttt{HYDRIC\_STRESS\_ACTIVE}, \texttt{FROST\_ALERT}. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-59} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `TelemetrySeries`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de TelemetrySeries en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-59} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{ingestHourly} \texttt{Reading} & \texttt{reading: HourlyTelemetryReading}, \texttt{evaluator: AgroclimaticThresholdEvaluator} & \texttt{void} & Incorpora lectura horaria, evalúa umbrales y emite \texttt{TelemetryDataIngestedEvent}. \\
\texttt{updateWeather} \texttt{Forecast} & \texttt{forecasts:} \texttt{List<Weather-} \texttt{ForecastDay>} & \texttt{void} & Actualiza pronóstico semanal georreferenciado y emite \texttt{WeatherForecastIngestedEvent}. \\
\texttt{getActive} \texttt{Incidents} & \texttt{void} & \texttt{List<} \texttt{Agroclimatic} \texttt{Incident>} & Retorna incidentes abiertos de estrés hídrico o choque térmico. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `HourlyTelemetryReading` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-60} se describe la estructura y delimitación transaccional del modelo `HourlyTelemetryReading`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio HourlyTelemetryReading (Internal Entity) en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-60} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Captura los parámetros físicos y edafoclimáticos registrados en una hora determinada. \\
Relaciones de dominio & Subordinada a \texttt{TelemetrySeries} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-61} se presentan los atributos, tipos de datos e invariantes que rigen a `HourlyTelemetryReading`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo HourlyTelemetryReading en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-61} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{ReadingId} & Identificador único de la lectura horaria. \\
\texttt{observedAt} & \texttt{Instant} & Marca temporal UTC de la observación. \\
\texttt{soilMoisture30cm} & \texttt{Volumetric} \texttt{WaterContent} & Humedad volumétrica de suelo a 30 cm de profundidad (\%). \\
\texttt{soilMoisture60cm} & \texttt{Volumetric} \texttt{WaterContent} & Humedad volumétrica de suelo a 60 cm de profundidad (\%). \\
\texttt{airTemperature} & \texttt{Temperature} & Temperatura ambiente registrada (°C). \\
\texttt{relativeHumidity} & \texttt{RelativeHumidity} & Humedad relativa del aire (\%). \\
\texttt{isSynthetic} & \texttt{boolean} & Indicador si la lectura proviene del simulador de contingencia. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-62} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `HourlyTelemetryReading`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de HourlyTelemetryReading en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-62} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{isStress} \texttt{Inducing} & \texttt{void} & \texttt{boolean} & Determina si los niveles hídricos caen por debajo del punto de marchitez temporal. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `WeatherForecastDay` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-63} se describe la estructura y delimitación transaccional del modelo `WeatherForecastDay`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio WeatherForecastDay (Internal Entity) en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-63} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Almacena la predicción meteorológica para una jornada específica en el predio. \\
Relaciones de dominio & Subordinada a \texttt{TelemetrySeries} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-64} se presentan los atributos, tipos de datos e invariantes que rigen a `WeatherForecastDay`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo WeatherForecastDay en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-64} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{ForecastDayId} & Identificador del registro diario de pronóstico. \\
\texttt{forecastDate} & \texttt{LocalDate} & Fecha calendario pronosticada. \\
\texttt{maxTemperature} & \texttt{Temperature} & Temperatura máxima prevista (°C). \\
\texttt{minTemperature} & \texttt{Temperature} & Temperatura mínima prevista (°C). \\
\texttt{precipitation} \texttt{Probability} & \texttt{Percentage} & Probabilidad de precipitación pluvial (0-100\%). \\
\texttt{windSpeedKmh} & \texttt{WindSpeed} & Velocidad estimada del viento en km/h. \\
\texttt{syncedAt} & \texttt{Instant} & Marca temporal de sincronización con Open-Meteo. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-65} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `WeatherForecastDay`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de WeatherForecastDay en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-65} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{isFrostRisk} & \texttt{void} & \texttt{boolean} & Detecta si la temperatura mínima proyectada desciende de 2.0 °C. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `AgroclimaticIncident` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-66} se describe la estructura y delimitación transaccional del modelo `AgroclimaticIncident`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio AgroclimaticIncident (Internal Entity) en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-66} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Modela el ciclo de vida de una anomalía agroclimática que amenaza la fisiología del olivar. \\
Relaciones de dominio & Subordinada a \texttt{TelemetrySeries} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-67} se presentan los atributos, tipos de datos e invariantes que rigen a `AgroclimaticIncident`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo AgroclimaticIncident en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-67} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{IncidentId} & Identificador único del incidente. \\
\texttt{type} & \texttt{IncidentType} & Tipo: \texttt{HYDRIC\_STRESS}, \texttt{THERMAL\_SHOCK}, \texttt{FROST\_WARNING}. \\
\texttt{severity} & \texttt{IncidentSeverity} & Severidad: \texttt{WARNING}, \texttt{CRITICAL}. \\
\texttt{status} & \texttt{IncidentStatus} & Estado: \texttt{OPEN}, \texttt{RESOLVED}. \\
\texttt{triggeredAt} & \texttt{Instant} & Marca de tiempo de activación de la alerta. \\
\texttt{resolvedAt} & \texttt{Instant} & Marca de tiempo de normalización del parámetro. \\
\texttt{triggerValue} & \texttt{Double} & Valor registrado que causó el disparo. \\
\texttt{thresholdValue} & \texttt{Double} & Umbral agronómico de referencia. \\
\texttt{stress} \texttt{DurationMinutes} & \texttt{Long} & Minutos acumulados bajo condición de estrés. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-68} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `AgroclimaticIncident`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de AgroclimaticIncident en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-68} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{resolve} & \texttt{resolutionTime: Instant} & \texttt{void} & Cierra formalmente la alerta y computa la duración del estrés fisiológico. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-69} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-69} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{SensorNodeId}, \texttt{TelemetrySeriesId} & \texttt{UUID v4} & Identificadores únicos inmutables de los agregados raíz del contexto. \\
\texttt{ReadingId}, \texttt{ForecastDayId}, \texttt{IncidentId} & \texttt{UUID v4} & Identificadores únicos de las entidades internas subordinadas a \texttt{TelemetrySeries}. \\
\texttt{SensorNodeName} & \texttt{String} & Denominación descriptiva única en el predio (longitud 3 a 100 caracteres). \\
\texttt{SensorDepth} & \texttt{Int (30 o 60 cm)} & Estrato radicular objetivo de absorción de agua del olivo. \\
\texttt{SoilTextureType} & \texttt{Enum} & \texttt{SANDY\_LOAM}, \texttt{SANDY}, \texttt{LOAM}, \texttt{CLAY\_LOAM}. \\
\texttt{Calibration} \texttt{Multiplier} & \texttt{Double} & Factor volumétrico de calibración edáfica en rango agronómico $[0.50, 2.00]$. \\
\texttt{Volumetric} \texttt{WaterContent} & Double (Porcentaje VWC / θ) & Humedad volumétrica de suelo entre $0.0\%$ y $100.0\%$. \\
\texttt{Temperature} & \texttt{Double (Celsius)} & Métrica de temperatura ambiental con precisión de décimas. \\
\texttt{RelativeHumidity} & \texttt{Double (Porcentaje)} & Humedad ambiental entre $0.0\%$ y $100.0\%$. \\
\texttt{IncidentSeverity} & \texttt{Enum} & Severidad del riesgo: \texttt{WARNING}, \texttt{CRITICAL}. \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-70} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-70} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Agroclimatic} \texttt{Threshold} \texttt{Evaluator} & Domain Service & \texttt{evaluateHydricRisk(} \texttt{moisture30cm: Double,} \texttt{texture:} \texttt{SoilTextureType):} \texttt{HydricRiskResult} & Determina severidad de estrés hídrico según umbrales de textura. \\
\texttt{Agroclimatic} \texttt{Threshold} \texttt{Evaluator} & Domain Service & \texttt{evaluateThermalRisk(} \texttt{temp: Double,} \texttt{rh: Double,} \texttt{stage:} \texttt{PhenologicalStage):} \texttt{ThermalRiskResult} & Evalúa golpe de calor o choque térmico según fenología. \\
\texttt{Agroclimatic} \texttt{Threshold} \texttt{Evaluator} & Domain Service & \texttt{evaluateFrostRisk(} \texttt{minTemp: Double):} \texttt{FrostRiskResult} & Detecta alerta temprana de heladas radiativas o advectivas. \\
\texttt{VirtualSensor} \texttt{NodeRepository} & Repository & \texttt{findById(id: SensorNodeId): Optional<VirtualSensorNode>} & Carga nodo sensor por identificador primario. \\
\texttt{VirtualSensor} \texttt{NodeRepository} & Repository & \texttt{findByPlotId(plotId: PlotId): List<VirtualSensorNode>} & Lista dispositivos vinculados a un predio. \\
\texttt{VirtualSensor} \texttt{NodeRepository} & Repository & \texttt{existsByPlotId} \texttt{AndName(} \texttt{plotId: PlotId,} \texttt{name:} \texttt{SensorNodeName):} \texttt{boolean} & Verifica unicidad de nombre de sensor en el predio. \\
\texttt{VirtualSensor} \texttt{NodeRepository} & Repository & \texttt{save(sensorNode: VirtualSensorNode): VirtualSensorNode} & Persiste configuración y calibración del nodo. \\
\texttt{TelemetrySeries} \texttt{Repository} & Repository & \texttt{findById(id: TelemetrySeriesId): Optional<TelemetrySeries>} & Recupera serie temporal de telemetría. \\
\texttt{TelemetrySeries} \texttt{Repository} & Repository & \texttt{findByPlotId(plotId: PlotId): Optional<TelemetrySeries>} & Localiza la serie asociada a una parcela. \\
\texttt{TelemetrySeries} \texttt{Repository} & Repository & \texttt{findBySensorNodeId(} \texttt{nodeId:} \texttt{SensorNodeId):} \texttt{Optional<} \texttt{TelemetrySeries>} & Recupera serie emitida por un sensor específico. \\
\texttt{TelemetrySeries} \texttt{Repository} & Repository & \texttt{save(series: TelemetrySeries): TelemetrySeries} & Guarda lecturas, pronósticos e incidentes del agregado. \\
\texttt{VirtualSensor} \texttt{NodeLinkedEvent} & Domain Event & \texttt{nodeId: UUID, plotId: UUID, name: String, type: String, occurredOn: Instant} & Notifica registro de sensor para inicializar ingesta. \\
\texttt{TelemetryData} \texttt{IngestedEvent} & Domain Event & \texttt{seriesId: UUID, nodeId: UUID, observedAt: Instant, occurredOn: Instant} & Notifica ingesta de medición horaria para modelos fenológicos. \\
\texttt{HydricStress} \texttt{AlertTriggeredEvent} & Domain Event & \texttt{plotId: UUID, severity: String, moisture: Double, occurredOn: Instant} & Alerta estrés hídrico para activar recomendaciones de riego. \\
\texttt{WeatherForecast} \texttt{IngestedEvent} & Domain Event & \texttt{plotId: UUID, forecastDate: LocalDate, minTemp: Double, occurredOn: Instant} & Notifica pronóstico sincronizado con Open-Meteo. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-71} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-71} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/iot-devices} & \texttt{CreateIoT} \texttt{DeviceRequest} & \texttt{DeviceResource} (201 Created) & Alta y vinculación de nodo sensor o sonda edáfica virtual. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/iot-devices} & N/A & \texttt{List<} \texttt{DeviceResource>} (200 OK) & Consulta de inventario de dispositivos y estado de calibración. \\
\texttt{PUT} & \nolinkurl{/api/v1/plots/{plotId}/iot-devices/{deviceId}} & \texttt{CalibrateDevice} \texttt{Request} & \texttt{DeviceResource} (200 OK) & Renombrado del nodo y calibración de offset en sonda edáfica y factor edafológico. \\
\texttt{DELETE} & \nolinkurl{/api/v1/plots/{plotId}/iot-devices/{deviceId}} & N/A & \texttt{204 No Content} & Desvinculación lógica de la sonda preservando histórico. \\
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/telemetries} & \texttt{IngestTelemetry} \texttt{Request} & \texttt{Telemetry} \texttt{Resource} (201 Created) & Ingesta individual o en lote de lecturas de sensores. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/telemetries} & N/A (\texttt{?startDate=} \texttt{\&endDate=}) & \texttt{List<} \texttt{TelemetryResource>} (200 OK) & Consulta de series climáticas para gráficas y monitoreo. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/forecasts} & N/A & \texttt{WeatherForecast} \texttt{Resource} (200 OK) & Consulta de pronóstico meteorológico a 7 días vía Open-Meteo. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/incidents} & N/A (\texttt{?status=ACTIVE}) & \texttt{List<} \texttt{IncidentResource>} (200 OK) & Consulta de alertas e incidentes de estrés hídrico o térmico. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-72} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-72} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{CreateIoT} \texttt{DeviceRequest} & Request DTO & \texttt{{ name: String, deviceType: String, depthCm: Int, soilTextureType: String }} & Registro y alta de sonda edáfica virtual. \\
\texttt{CalibrateDevice} \texttt{Request} & Request DTO & \texttt{{ name: String, depthCm: Int, calibration\-Multiplier: Double, calibrationNotes: String }} & Ajuste físico y calibración edafológica de sonda. \\
\texttt{IngestTelemetry} \texttt{Request} & Request DTO & \texttt{{ sensorNodeId: UUID, readings: List<HourlyTelemetryReadingDto> }} & Lectura horaria o lote enviado por simulador o sensor. \\
\texttt{DeviceResource} & Response DTO & \texttt{{ id: UUID, plotId: UUID, name: String, deviceType: String, status: String }} & Representación de nodo sensor vinculado. \\
\texttt{Telemetry} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, plotId: UUID, temperature: Double, humidity: Double, soilMoisture: Double, recordedAt: Instant }} & Representación pública de lectura agroclimática. \\
\texttt{WeatherForecast} \texttt{Resource} & Response DTO & \texttt{{ plotId: UUID, dailyForecasts: List<DailyForecastDto> generatedAt: Instant }} & Proyección meteorológica a 7 días. \\
\texttt{IncidentResource} & Response DTO & \texttt{{ id: UUID, plotId: UUID, incidentType: String, severity: String, triggeredAt: Instant }} & Alerta de estrés hídrico o térmico. \\
\texttt{Telemetry} \texttt{Resource} \texttt{Assembler} & Assembler & \texttt{toResource(} \texttt{TelemetryReading):} \texttt{Telemetry} \texttt{Resource} & Convierte lectura interna a DTO de visualización. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-73} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-73} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Register} \texttt{IoTDevice} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Register} \texttt{IoTDevice} \texttt{Command} & Valida titularidad, persiste sonda y emite \texttt{VirtualSensorNodeLinkedEvent}. \\
\texttt{Calibrate} \texttt{IoTDevice} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Calibrate} \texttt{IoTDevice} \texttt{Command} & Carga dispositivo, ajusta offset/factor edáfico, persiste y emite \texttt{VirtualSensorNodeCalibratedEvent}. \\
\texttt{Remove} \texttt{IoTDevice} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Remove} \texttt{IoTDevice} \texttt{Command} & Desvincula lógicamente la sonda del predio y emite \texttt{VirtualSensorNodeUnlinkedEvent}. \\
\texttt{Ingest} \texttt{PlotTelemetry} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Ingest} \texttt{PlotTelemetry} \texttt{Command} & Persiste lecturas horarias en PostgreSQL, evalúa umbrales de estrés y despacha alertas. \\
\texttt{Get} \texttt{Telemetry} \texttt{Series} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Telemetry} \texttt{Series} \texttt{Query} & Recupera serie temporal acotada por rango de fechas para graficado móvil. \\
\texttt{Get} \texttt{Weather} \texttt{Forecast} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Weather} \texttt{Forecast} \texttt{Query} & Consulta caché local de pronóstico meteorológico a 7 días para la parcela. \\
\texttt{Weather} \texttt{SyncScheduler} & Scheduled Task & \texttt{ScheduledCron} & Orquesta la sincronización automática periódica con Open-Meteo emitiendo \texttt{WeatherForecastIngestedEvent}. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-74} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-74} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Virtual} \texttt{SensorNode} \texttt{JpaRepository} & Persistence & Spring Data JPA & Almacenamiento y calibración de nodos sensores virtuales. \\
\texttt{TelemetrySeries} \texttt{JpaRepository} & Persistence & Spring Data JPA & Almacenamiento optimizado de series temporales horarias e incidentes. \\
\texttt{OpenMeteoWeather} \texttt{ClientAdapter} & External Adapter & Spring RestClient & Consumo de pronósticos horarios y datos meteorológicos de Open-Meteo con caché. \\
\texttt{InAppNotification} \texttt{Adapter} & Notification & WebSocket / FCM & Difusión push e in-app de alertas de estrés hídrico y choque térmico. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Caché local de telemetría y pronóstico offline:**
  * *Android Nativo (Room / SQLite):* `TelemetryCacheDao` y entidades `LocalTelemetrySeriesEntity`, `LocalWeatherForecastEntity` que cachean las últimas 24 lecturas horarias y el pronóstico a 7 días de la parcela activa para consulta en campo sin red.
  * *Cross-Platform (sqflite / SQLite):* Tablas `telemetry_cache` y `forecast_cache` con clave compuesta `(plot_id, fetched_at)` gestionadas por `LocalDataAccess`.
* **Visualización y alertas en dispositivo:**
  * Componentes de interfaz móvil (`Agronomy and Harvest UI`) que renderizan curvas de humedad de suelo a 30/60 cm y activan banners de alerta local inmediata ante incidentes críticos de estrés hídrico (`HydricStressAlertTriggeredEvent`).

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-75} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-75} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{telemetry\_} \texttt{readings} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador de la lectura. \\
\texttt{telemetry\_} \texttt{readings} & \texttt{plot\_id} & \texttt{UUID} & \texttt{NOT NULL, INDEX} & Parcela monitoreada. \\
\texttt{telemetry\_} \texttt{readings} & \texttt{temperature} & \texttt{NUMERIC(4,2)} & \texttt{NOT NULL} & Temperatura ambiente en grados Celsius. \\
\texttt{telemetry\_} \texttt{readings} & \texttt{humidity} & \texttt{NUMERIC(5,2)} & \texttt{NOT NULL} & Humedad relativa porcentual. \\
\texttt{telemetry\_} \texttt{readings} & \texttt{soil\_moisture} & \texttt{NUMERIC(5,2)} & \texttt{NULL} & Humedad de suelo o potencial mátrico. \\
\texttt{telemetry\_} \texttt{readings} & \texttt{recorded\_at} & \texttt{TIMESTAMPTZ} & \texttt{NOT NULL, INDEX} & Marca temporal exacta de la medición. \\
\texttt{iot\_} \texttt{devices} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador de la sonda o sensor. \\
\texttt{iot\_} \texttt{devices} & \texttt{plot\_id} & \texttt{UUID} & \texttt{NOT NULL} & Parcela asociada. \\
\texttt{iot\_} \texttt{devices} & \texttt{calibration\_} \texttt{offset} & \texttt{NUMERIC(5,2)} & \texttt{NOT NULL DEFAULT 0} & Desviación calibrada de la sonda. \\
\texttt{iot\_} \texttt{devices} & \texttt{status} & \texttt{VARCHAR(30)} & \texttt{NOT NULL} & Estado del dispositivo (\texttt{ACTIVE}, \texttt{CALIBRATING}). \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS telemetry;

CREATE TABLE telemetry.telemetry_readings (
    id            UUID PRIMARY KEY,
    plot_id       UUID NOT NULL,
    temperature   NUMERIC(4,2) NOT NULL,
    humidity      NUMERIC(5,2) NOT NULL
        CHECK (humidity BETWEEN 0 AND 100),
    soil_moisture NUMERIC(5,2),
    recorded_at   TIMESTAMPTZ NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE telemetry.iot_devices (
    id                 UUID PRIMARY KEY,
    plot_id            UUID NOT NULL,
    calibration_offset NUMERIC(5,2) NOT NULL DEFAULT 0.0,
    status             VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_telemetry_plot_time
    ON telemetry.telemetry_readings(plot_id, recorded_at DESC);
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-76} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Agroclimatic Telemetry and Sensor Monitoring.} \label{tab:tactical-76} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{PlotIot} \texttt{DeviceController}; \texttt{PlotTelemetryController}; \texttt{PlotForecastController} & Ingesta horaria, configuración de nodos sensores y consulta REST de series agroclimáticas y pronóstico. & Spring MVC, Jakarta Validation \\
Application & \texttt{TelemetryCommandService}; \texttt{TelemetryQueryService}; \texttt{ForecastSyncScheduler}; \texttt{ExternalOrchardService} & Orquestación de comandos de sensores/lecturas, consultas de series/alertas, tarea programada y servicio ACL consumidor de Orchard. & Spring \texttt{@Transactional}, \texttt{@Scheduled}, \texttt{@Service} \\
Domain & \texttt{Virtual} \texttt{SensorNode} \texttt{Repository}; \texttt{TelemetrySeries} \texttt{Repository}; \texttt{AgroclimaticThresholdEvaluator} & Contratos de persistencia (puertos de dominio) y servicio de evaluación de estrés hídrico (SWP) y heladas. & Java puro / DDD \\
Infrastructure & \texttt{JpaVirtualSensorNode} \texttt{RepositoryAdapter}; \texttt{JpaTelemetrySeries} \texttt{RepositoryAdapter}; \texttt{OpenMeteoWeatherAdapter}; \texttt{SpringDomainEventPublisher} & Persistencia JPA en PostgreSQL, consumo API Open-Meteo y publicación de eventos. & Spring Data JPA, HTTP Client, Caffeine \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El nodo sensor o simulador despacha `POST` \nolinkurl{/api/v1/plots/{plotId}/telemetries} hacia `PlotTelemetryController`.
2. `PlotTelemetryController` valida el payload y delega la ingesta en `TelemetryCommandService`. Las consultas de series temporales y pronóstico a 7 días son atendidas por `TelemetryQueryService`.
3. `TelemetryCommandService` persiste la medición en `TelemetrySeriesRepository` (implementado por `JpaTelemetrySeriesRepositoryAdapter`).
4. Se invoca el servicio de dominio `AgroclimaticThresholdEvaluator` verificando los límites de potencial hídrico en tallo (SWP), golpe de calor y heladas.
5. Si se excede el umbral crítico, `TelemetryCommandService` dispara `HydricStressAlertTriggeredEvent` vía `SpringDomainEventPublisher`, notificando in-app a la aplicación cliente.
6. En paralelo, `ForecastSyncScheduler` sincroniza periódicamente la predicción meteorológica consumiendo la API de Open-Meteo vía `OpenMeteoWeatherAdapter`.

A continuación, en la \autoref{fig:c4-component-telemetry} se esquematiza el diagrama de componentes del Bounded Context Agroclimatic Telemetry:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Agroclimatic Telemetry.} \label{fig:c4-component-telemetry}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-telemetry.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Agroclimatic Telemetry. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-telemetry}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-telemetry}) para el Bounded Context Agroclimatic Telemetry:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Agroclimatic Telemetry.} \label{fig:class-diagram-telemetry}
\vspace{0.25cm}
\centering
\includegraphics[width=0.70\textwidth]{report/assets/tactical-diagrams/class-diagram-telemetry.png}
\caption*{\textit{Nota.} Estructura estática de clases, tipos y métodos del modelo de dominio de Telemetría. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Agroclimatic Telemetry.} \label{fig:database-diagram-telemetry}
\vspace{0.25cm}
\centering
\includegraphics[width=0.65\textwidth]{report/assets/tactical-diagrams/database-diagram-telemetry.png}
\caption*{\textit{Nota.} Estructura de tablas de telemetría y dispositivos IoT en PostgreSQL. Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: Phenology and Historical Bearing Analytics

Propósito: Gobierna la memoria biológica y el análisis plurianual de vecería del olivar. Modela el seguimiento de las fases fenológicas en escala BBCH (brotación, floración, cuajado, endurecimiento del carozo y maduración), calcula la acumulación de frío invernal mediante el modelo dinámico de Erez (unidades de frío / porciones de frío acumuladas), proyecta la fecha crítica de lignificación de carozo mediante grados-día de desarrollo acumulados ($680.0^\circ\text{C}\cdot\text{día}$ post-antesis), y evalúa el Índice de Vecería Bienal de Hoblyn ($BBI$) a partir de las series plurianuales de cosecha.

#### Domain Layer

##### Modelo de dominio: `ChillAccumulationTracker` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-77} se describe la estructura y delimitación transaccional del modelo `ChillAccumulationTracker`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio ChillAccumulationTracker (Aggregate Root) en Phenology and Historical Bearing Analytics.} \label{tab:tactical-77} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Monitorea la acumulación invernal de frío (Modelo Dinámico de Erez), el tiempo térmico post-antesis y el índice de vecería de Hoblyn. \\
Relaciones de dominio & Referencia a \texttt{PlotId}. Compone bitácoras diarias de frío y registros históricos plurianuales de cosecha. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-78} se presentan los atributos, tipos de datos e invariantes que rigen a `ChillAccumulationTracker`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo ChillAccumulationTracker en Phenology and Historical Bearing Analytics.} \label{tab:tactical-78} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{TrackerId} & Identificador único del seguidor de frío y fenología. \\
\texttt{plotId} & \texttt{PlotId} & Parcela olivarera analizada. \\
\texttt{current} \texttt{CampaignYear} & \texttt{CampaignYear} & Año agrícola en curso de monitoreo. \\
\texttt{harvestHistory} & \texttt{List<} \texttt{HistoricalHarvest} \texttt{Entry>} & Serie histórica plurianual de cosechas (mínimo 2 años). \\
\texttt{calculatedBbi} & \texttt{Biennial} \texttt{BearingIndex} & Índice de vecería calculado según fórmula de Hoblyn [0.00, 1.00]. \\
\texttt{dailyChillLogs} & \texttt{List<DailyChillLog>} & Bitácora diaria de avance de frío acumulado en mayo-agosto. \\
\texttt{accumulatedGdd} \texttt{PostAnthesis} & \texttt{Double} & Grados día de desarrollo acumulados tras plena floración. \\
\texttt{pitHardening} \texttt{Reached} & \texttt{Boolean} & Indicador si se alcanzó el endurecimiento de carozo (~680 GDD). \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-79} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `ChillAccumulationTracker`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de ChillAccumulationTracker en Phenology and Historical Bearing Analytics.} \label{tab:tactical-79} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{register} \texttt{Harvest} & \texttt{entry: HistoricalHarvestEntry} & \texttt{void} & Añade cosecha histórica, recalcula el BBI de Hoblyn y emite \texttt{BiennialBearingIndexAssessedEvent}. \\
\texttt{rectifyHarvest} & \texttt{year: CampaignYear}, \texttt{yield: Double} & \texttt{void} & Corrige pesajes de cosechas previas actualizando el índice de alternancia. \\
\texttt{deleteHarvest} & \texttt{year: CampaignYear} & \texttt{void} & Elimina registro histórico manteniendo la coherencia de la serie. \\
\texttt{processDaily} \texttt{Temperatures} & \texttt{date: LocalDate}, \texttt{temps: List<Double>} & \texttt{void} & Computa porciones de frío de Erez considerando termodestrucción. \\
\texttt{processPost} \texttt{Anthesis} \texttt{ThermalTime} & \texttt{date: LocalDate}, \texttt{max: Double}, \texttt{min: Double} & \texttt{void} & Acumula GDD y detecta endurecimiento de carozo emitiendo \texttt{PitHardeningStageReachedEvent}. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `HistoricalHarvestEntry` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-80} se describe la estructura y delimitación transaccional del modelo `HistoricalHarvestEntry`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio HistoricalHarvestEntry (Internal Entity) en Phenology and Historical Bearing Analytics.} \label{tab:tactical-80} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Registra el rendimiento cuantitativo anual obtenido en una campaña previa para cálculo de alternancia. \\
Relaciones de dominio & Subordinada a \texttt{ChillAccumulationTracker} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-81} se presentan los atributos, tipos de datos e invariantes que rigen a `HistoricalHarvestEntry`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo HistoricalHarvestEntry en Phenology and Historical Bearing Analytics.} \label{tab:tactical-81} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{HarvestEntryId} & Identificador único del registro de cosecha. \\
\texttt{campaignYear} & \texttt{CampaignYear} & Año de la campaña agrícola. \\
\texttt{harvestYield} & \texttt{HarvestYield} & Rendimiento y distribución de cosecha (\texttt{totalKg}, \texttt{greenKg}, \texttt{blackKg}). \\
\texttt{bearingClassification} & \texttt{BearingClassification} & Clasificación anual: \texttt{ON\_YEAR}, \texttt{OFF\_YEAR}, \texttt{BALANCED}, \texttt{INSUFFICIENT\_DATA}. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-82} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `HistoricalHarvestEntry`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de HistoricalHarvestEntry en Phenology and Historical Bearing Analytics.} \label{tab:tactical-82} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{updateYield} & \texttt{total: Double}, \texttt{green: Double}, \texttt{black: Double} & \texttt{void} & Actualiza rendimientos verificando consistencia de pesajes. \\
\texttt{classify} & \texttt{averageYield: Double} & \texttt{void} & Asigna categoría productiva comparando contra el promedio móvil predial. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `DailyChillLog` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-83} se describe la estructura y delimitación transaccional del modelo `DailyChillLog`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio DailyChillLog (Internal Entity) en Phenology and Historical Bearing Analytics.} \label{tab:tactical-83} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Almacena el cálculo matemático de porciones de frío acumuladas en una jornada invernal. \\
Relaciones de dominio & Subordinada a \texttt{ChillAccumulationTracker} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-84} se presentan los atributos, tipos de datos e invariantes que rigen a `DailyChillLog`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo DailyChillLog en Phenology and Historical Bearing Analytics.} \label{tab:tactical-84} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{DailyChillLogId} & Identificador del registro diario de frío. \\
\texttt{logDate} & \texttt{LocalDate} & Fecha invernal evaluada. \\
\texttt{portions} \texttt{AccumulatedToday} & \texttt{Double} & Porciones de frío aportadas por el ciclo térmico diario. \\
\texttt{total} \texttt{Accumulated} \texttt{ToDate} & \texttt{Double} & Acumulado progresivo de porciones al cierre del día. \\
\texttt{maxDayTemperature} & \texttt{Double} & Temperatura máxima diurna (°C). \\
\texttt{minNight} \texttt{Temperature} & \texttt{Double} & Temperatura mínima nocturna (°C). \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-85} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `DailyChillLog`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de DailyChillLog en Phenology and Historical Bearing Analytics.} \label{tab:tactical-85} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{isDestructive} \texttt{HeatOccurred} & \texttt{void} & \texttt{boolean} & Indica si temperaturas > 24 °C destruyeron el intermediario térmico inestable. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-86} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Phenology and Historical Bearing Analytics.} \label{tab:tactical-86} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{TrackerId}, \texttt{HarvestEntryId}, \texttt{DailyChillLogId} & \texttt{UUID v4} & Identificadores únicos universales inmutables. \\
\texttt{CampaignYear} & \texttt{Int} & Año de la campaña agrícola evaluada ($1980 \le year \le 2100$). \\
\texttt{HarvestYield} & \texttt{totalKg: Double, greenKg: Double, blackKg: Double} & Rendimiento en kg. Invariantes: $totalKg > 0$, $greenKg \ge 0$, $blackKg \ge 0$, $greenKg + blackKg \le totalKg$. \\
\texttt{BearingClassification} & \texttt{Enum} & Clasificación de alternancia productiva: \texttt{ON\_YEAR}, \texttt{OFF\_YEAR}, \texttt{BALANCED}, \texttt{INSUFFICIENT\_DATA}. \\
\texttt{Biennial} \texttt{BearingIndex} & \texttt{Double [0.00, 1.00]} & Índice de vecería de Hoblyn: $0$ (regularidad) a $1$ (alternancia extrema). \\
\texttt{BBCHStage} & \texttt{stageCode: Int, description: String} & Código estandarizado BBCH (ej. 65: Plena Floración, 75: Endurecimiento de Carozo). \\
\texttt{GrowingDegreeDays} & \texttt{Double (Grados-Día)} & Acumulación térmica sobre umbral base ($T_{base} = 10^\circ\text{C}$). \\
\texttt{DynamicErezPortion} & \texttt{Double} & Porciones de frío dinámico acumuladas según cinética Erez-Fishman. \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-87} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Phenology and Historical Bearing Analytics.} \label{tab:tactical-87} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{ErezDynamic} \texttt{ModelCalculator} & Domain Service & \texttt{computePortions(temps: List<Double>): Double} & Implementa las ecuaciones diferenciales del Modelo Dinámico de Erez. \\
\texttt{GrowingDegree} \texttt{DaysCalculator} & Domain Service & \texttt{calculateGdd(max: Double, min: Double, baseTemp: Double): Double} & Computa acumulación térmica post-antesis (base 10 °C). \\
\texttt{HoblynBbi} \texttt{CalculatorService} & Domain Service & \texttt{calculateBbi(} \texttt{harvests:} \texttt{List<} \texttt{HistoricalHarvestEntry>):} \texttt{Biennial} \texttt{BearingIndex} & Evalúa la alternancia productiva interanual según Hoblyn. \\
\texttt{Chill} \texttt{Accumulation} \texttt{TrackerRepository} & Repository & \texttt{findById(} \texttt{id: TrackerId):} \texttt{Optional<} \texttt{ChillAccumulation} \texttt{Tracker>} & Carga el seguidor de frío y fenología por ID. \\
\texttt{Chill} \texttt{Accumulation} \texttt{TrackerRepository} & Repository & \texttt{findByPlotId} \texttt{AndCampaign(} \texttt{plotId: PlotId,} \texttt{year:} \texttt{CampaignYear):} \texttt{Optional<} \texttt{Chill} \texttt{Accumulation} \texttt{Tracker>} & Recupera el tracker de una campaña agrícola en el predio. \\
\texttt{Chill} \texttt{Accumulation} \texttt{TrackerRepository} & Repository & \texttt{save(tracker: ChillAccumulationTracker): ChillAccumulationTracker} & Persiste atómicamente el estado y bitácoras de frío. \\
\texttt{PitHardening} \texttt{StageReachedEvent} & Domain Event & \texttt{plotId: UUID, previousStage: String, newStage: String, gdd: Double, occurredOn: Instant} & Notifica cambio de fase fenológica (ej. carozo a 680 GDD). \\
\texttt{BiennialBearing} \texttt{IndexAssessedEvent} & Domain Event & \texttt{plotId: UUID, bbiValue: Double, classification: String, occurredOn: Instant} & Informa severidad de vecería hacia Crop Load Regulation. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-88} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Phenology and Historical Bearing Analytics.} \label{tab:tactical-88} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/harvest-records} & \texttt{RecordHarvest} \texttt{YieldRequest} & \texttt{HarvestRecord} \texttt{Resource} (201 Created) & Asienta el volumen cosechado de una campaña anual. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/harvest-records} & N/A (\texttt{?campaignYear=}) & \texttt{List<} \texttt{HarvestRecord} \texttt{Resource>} (200 OK) & Historial plurianual de cosechas con filtro opcional. \\
\texttt{PUT} & \nolinkurl{/api/v1/plots/{plotId}/harvest-records/{recordId}} & \texttt{UpdateHarvest} \texttt{YieldRequest} & \texttt{HarvestRecord} \texttt{Resource} (200 OK) & Rectificación de pesaje histórico de una campaña. \\
\texttt{DELETE} & \nolinkurl{/api/v1/plots/{plotId}/harvest-records/{recordId}} & N/A & \texttt{204 No Content} & Eliminación de registro de cosecha erróneo. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/metrics} & N/A (\texttt{?name=BBI} / \texttt{?name=CHILLING}) & \texttt{MetricResource} (200 OK) & Consulta de $BBI$ de Hoblyn y porciones de frío de Erez. \\
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/phenology-observations} & \texttt{RecordPhenology} \texttt{ObservationRequest} & \texttt{Phenology} \texttt{ObservationResource} (201 Created) & Registro visual de estadio fenológico en escala BBCH. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-89} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Phenology and Historical Bearing Analytics.} \label{tab:tactical-89} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{RecordHarvest} \texttt{YieldRequest} & Request DTO & \texttt{{ campaignYear: Int, totalTons: Double, oliveUseType: String, notes: String }} & Asiento de pesaje anual cosechado. \\
\texttt{UpdateHarvest} \texttt{YieldRequest} & Request DTO & \texttt{{ totalTons: Double, notes: String }} & Corrección auditada de volumen de cosecha. \\
\texttt{HarvestRecord} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, plotId: UUID, campaignYear: Int, totalTons: Double, recordedAt: Instant }} & Representación de cosecha histórica. \\
\texttt{MetricResource} & Response DTO & \texttt{{ metricName: String, value: Double, qualitativeCategory: String, details: Map<String, Object>, evaluatedAt: Instant }} & Métrica de vecería ($BBI$) o frío dinámico (Erez). \\
\texttt{RecordPhenology} \texttt{ObservationRequest} & Request DTO & \texttt{{ stageCode: Int, observationDate: LocalDate, notes: String }} & Inspección de estadio BBCH en campo. \\
\texttt{Phenology} \texttt{ObservationResource} & Response DTO & \texttt{{ id: UUID, plotId: UUID, currentStage: Int, accumulatedGdd: Double, isWindowClosed: Boolean }} & Estado biológico y ventana de aclareo. \\
\texttt{HarvestRecord} \texttt{ResourceAssembler} & Assembler & \texttt{toResource(} \texttt{HistoricalHarvestEntry):} \texttt{HarvestRecordResource} & Transformador a DTO desacoplado. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-90} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Phenology and Historical Bearing Analytics.} \label{tab:tactical-90} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Record} \texttt{Harvest} \texttt{Yield} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Record} \texttt{Harvest} \texttt{Yield} \texttt{Command} & Asienta pesaje de campaña, actualiza agregado y recalcula $BBI$ si $N \ge 3$. \\
\texttt{Update} \texttt{Harvest} \texttt{Yield} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Update} \texttt{Harvest} \texttt{Yield} \texttt{Command} & Rectifica pesaje de campaña, actualiza serie histórica y recalcula $BBI$. \\
\texttt{Delete} \texttt{Harvest} \texttt{Yield} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Delete} \texttt{Harvest} \texttt{Yield} \texttt{Command} & Da de baja registro de cosecha erróneo y revalúa suficiencia muestral del $BBI$. \\
\texttt{List} \texttt{Harvest} \texttt{Records} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{List} \texttt{Harvest} \texttt{Records} \texttt{Query} & Consulta cronológica de cosechas con filtro por campaña agrícola. \\
\texttt{Get} \texttt{Plot} \texttt{Metrics} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Plot} \texttt{Metrics} \texttt{Query} & Consulta índices biológicos paramétricos ($BBI$ o Porciones de Frío de Erez). \\
\texttt{Record} \texttt{Phenological} \texttt{Observation} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Record} \texttt{Phenological} \texttt{Observation} \texttt{Command} & Actualiza estadio BBCH, recalcula sumas térmicas y emite evento fenológico. \\
\texttt{Accumulate} \texttt{PostAnthesis} \texttt{ThermalTime} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Accumulate} \texttt{PostAnthesis} \texttt{ThermalTime} \texttt{Command} & Suma GDD diarios post-antesis; si supera $680^\circ\text{C}\cdot\text{día}$, emite cierre de ventana. \\
\texttt{Chill} \texttt{Computation} \texttt{Scheduler} & Scheduled Task & \texttt{ScheduledCron} & Procesa lecturas telemétricas nocturnas acumulando porciones de frío bajo modelo dinámico de Erez. \\
\texttt{OnCampaign} \texttt{Harvest} \texttt{Settled} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Campaign} \texttt{Harvest} \texttt{Settled} \texttt{Event} & Escucha cierre de cosecha en Liquidación y actualiza bitácora plurianual recalculando $BBI$. \\
\texttt{OnLate} \texttt{Thinning} \texttt{Execution} \texttt{Recorded} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Late} \texttt{Thinning} \texttt{Execution} \texttt{Recorded} \texttt{Event} & Penaliza el factor de mitigación en un $70\%$ ante aclareo extemporáneo. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-91} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Phenology and Historical Bearing Analytics.} \label{tab:tactical-91} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Phenology} \texttt{JpaRepository} & Persistence & Spring Data JPA & Acceso a tablas de fenología y frío en PostgreSQL. \\
\texttt{JpaPhenology} \texttt{Repository} \texttt{Adapter} & Adapter & Spring Component & Implementa contratos de persistencia de fenología. \\
\texttt{ErezAlgorithmNative} \texttt{Adapter} & Domain Service Impl & Java Puro & Motor matemático optimizado para porciones de frío. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Caché local de historial de cosechas y métricas fenológicas:**
  * *Android Nativo (Room / SQLite):* Entidades `LocalHarvestRecordEntity` y `LocalPhenologyMetricEntity` gestionadas por `PhenologyCacheDao` para consultar memoria de vecería e índice $BBI$ sin conexión.
  * *Cross-Platform (sqflite / SQLite):* Tabla local `phenology_cache` con par `(plot_id, campaign_year)`.
* **Visualización de semáforo de vecería:**
  * Interfaz de usuario (`Agronomy and Harvest UI`) que traduce el valor decimal del $BBI$ en rangos visuales accesibles en campo (Leve, Moderado, Severo) y renderiza el avance de porciones de frío acumuladas contra la meta varietal de 25-30 UF.

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-92} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Phenology and Historical Bearing Analytics.} \label{tab:tactical-92} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{pheno-} \texttt{logical\_} \texttt{records} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador del registro. \\
\texttt{pheno-} \texttt{logical\_} \texttt{records} & \texttt{plot\_id} & \texttt{UUID} & \texttt{NOT NULL, INDEX} & Parcela monitoreada. \\
\texttt{pheno-} \texttt{logical\_} \texttt{records} & \texttt{current\_stage} & \texttt{INT} & \texttt{NOT NULL} & Código numérico BBCH actual. \\
\texttt{pheno-} \texttt{logical\_} \texttt{records} & \texttt{accumulated\_} \texttt{gdd} & \texttt{NUMERIC(6,2)} & \texttt{NOT NULL DEFAULT 0} & Grados-día de desarrollo post-antesis. \\
\texttt{pheno-} \texttt{logical\_} \texttt{records} & \texttt{is\_window\_} \texttt{closed} & \texttt{BOOLEAN} & \texttt{NOT NULL DEFAULT FALSE} & Indicador de carozo endurecido. \\
\texttt{chill\_} \texttt{trackers} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador del seguimiento de frío. \\
\texttt{chill\_} \texttt{trackers} & \texttt{plot\_id} & \texttt{UUID} & \texttt{NOT NULL} & Parcela asociada. \\
\texttt{chill\_} \texttt{trackers} & \texttt{campaign\_year} & \texttt{INT} & \texttt{NOT NULL} & Año agrícola evaluado. \\
\texttt{chill\_} \texttt{trackers} & \texttt{erez\_portions} & \texttt{NUMERIC(6,2)} & \texttt{NOT NULL} & Porciones de frío dinámico acumuladas. \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS phenology;

CREATE TABLE phenology.phenological_records (
    id               UUID PRIMARY KEY,
    plot_id          UUID NOT NULL,
    current_stage    INT NOT NULL,
    accumulated_gdd  NUMERIC(6,2) NOT NULL DEFAULT 0.0,
    is_window_closed BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE phenology.chill_trackers (
    id            UUID PRIMARY KEY,
    plot_id       UUID NOT NULL,
    campaign_year INT NOT NULL,
    erez_portions NUMERIC(6,2) NOT NULL DEFAULT 0.0,
    CONSTRAINT uq_chill_plot_year
        UNIQUE (plot_id, campaign_year)
);
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-93} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Phenology and Historical Bearing Analytics.} \label{tab:tactical-93} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{PlotChillController}; \texttt{PlotPhenologyController}; \texttt{PlotHarvestRecordController}; \texttt{PlotBearingController} & API REST para seguimiento fenológico, acumulación de frío, cosechas históricas y vecería. & Spring MVC, Jakarta Validation \\
Application & \texttt{Phenology} \texttt{CommandService}; \texttt{PhenologyQueryService}; \texttt{DailyChillComputationJob}; \texttt{ExternalOrchardService} & Orquestación de comandos de estadios y cosechas, consultas, tarea programada y servicio ACL consumidor de Orchard. & Spring \texttt{@Transactional}, \texttt{@Scheduled}, \texttt{@Service} \\
Domain & \texttt{ChillAccumulation} \texttt{TrackerRepository}; \texttt{ErezDynamicModelCalculator}; \texttt{GrowingDegreeDaysCalculator}; \texttt{HoblynBbiCalculatorService} & Contrato de persistencia (puerto de dominio), algoritmos biológicos Erez, GDD post-antesis e índice $BBI$ de Hoblyn. & Java puro / DDD \\
Infrastructure & \texttt{JpaChillAccumulation} \texttt{TrackerRepositoryAdapter}; \texttt{SpringDomainEventPublisher} & Persistencia JPA en PostgreSQL (\texttt{phenology}) y despacho de eventos de dominio. & Spring Data JPA, Spring Events \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El contenedor cliente móvil (`Android Application` o `Cross-Platform Application`) registra un estadio visual de floración con `POST` \nolinkurl{/api/v1/plots/{plotId}/phenology-observations} (o rectifica cosechas históricas vía `PlotHarvestRecordController`).
2. `PlotPhenologyController` delega la mutación en `PhenologyCommandService`, actualizando el estadio a BBCH 65 en `ChillAccumulation` `TrackerRepository`. Las consultas de frío, GDD y vecería son atendidas por `PhenologyQueryService`.
3. Diariamente, `DailyChillComputationJob` activa `PhenologyCommandService` para ejecutar el cálculo dinámico de porciones de frío (`ErezDynamicModelCalculator`) y acumular grados-día (`GrowingDegreeDaysCalculator`).
4. Al alcanzar $680^\circ\text{C}\cdot\text{día}$ acumulados, se transiciona `isWindowClosed = true` y `PhenologyCommandService` despacha `PitHardeningStageReachedEvent` vía `SpringDomainEventPublisher`.
5. El evento es recibido reactivamente por *Crop Load Regulation*, invalidando prescripciones de aclareo pendientes.
6. Ante la incorporación de cosechas históricas, `PhenologyCommandService` persiste los rendimientos en `ChillAccumulation` `TrackerRepository`, y `PhenologyQueryService` computa el índice $BBI$ de Hoblyn mediante `HoblynBbiCalculatorService`.

A continuación, en la \autoref{fig:c4-component-phenology} se esquematiza el diagrama de componentes del Bounded Context Phenology and Historical Bearing Analytics:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Phenology and Historical Bearing Analytics.} \label{fig:c4-component-phenology}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-phenology.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Phenology. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-phenology}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-phenology}) para el Bounded Context Phenology and Historical Bearing Analytics:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Phenology and Historical Bearing Analytics.} \label{fig:class-diagram-phenology}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-phenology.png}
\caption*{\textit{Nota.} Clases biológicas, algoritmos de frío y analítica de vecería en Phenology. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Phenology.} \label{fig:database-diagram-phenology}
\vspace{0.25cm}
\centering
\includegraphics[width=0.85\textwidth]{report/assets/tactical-diagrams/database-diagram-phenology.png}
\caption*{\textit{Nota.} Estructura de tablas de fenología y acumulación térmica en PostgreSQL. Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: Crop Load Regulation and Thinning Advisory

Propósito: Núcleo agronómico prescriptivo de Viora. Regula la carga frutal del olivar para mitigar la alternancia productiva entre campañas consecutivas. Gestiona el registro de muestreos de campo con soporte *offline-first* en dispositivos móviles (SQLite Room y sqflite con sincronización `WorkManager` y claves compuestas de idempotencia), calcula la tasa sostenible de frutos por metro lineal de copa, emite prescripciones automáticas de aclareo frutal en verde cuando se alcanza la representatividad muestral, alerta sobrecarga productiva sectorial hacia la cooperativa, y valida la ejecución oportuna de la labor frente al endurecimiento de carozo.

#### Domain Layer

##### Modelo de dominio: `FruitThinningPrescription` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-94} se describe la estructura y delimitación transaccional del modelo `FruitThinningPrescription`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio FruitThinningPrescription (Aggregate Root) en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-94} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Consolida los muestreos de brotes en campo, determina la carga frutal sostenible y emite la prescripción de raleo manual. \\
Relaciones de dominio & Referencia a \texttt{PlotId}. Compone rondas de muestreo y la confirmación de ejecución de raleo. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-95} se presentan los atributos, tipos de datos e invariantes que rigen a `FruitThinningPrescription`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo FruitThinningPrescription en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-95} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{PrescriptionId} & Identificador único de la prescripción. \\
\texttt{plotId} & \texttt{PlotId} & Parcela olivarera evaluada. \\
\texttt{campaignYear} & \texttt{CampaignYear} & Año de la campaña de regulación. \\
\texttt{observed} \texttt{PlotRevision} & \texttt{Long} & Versión catastral observada durante la prescripción. \\
\texttt{samplingRounds} & \texttt{List<SamplingRound>} & Rondas de muestreo de frutos por entrenudo registradas. \\
\texttt{sustainableLoad} & \texttt{Sustainable} \texttt{CropLoad} & Carga frutal agronómicamente sostenible recomendada. \\
\texttt{status} & \texttt{PrescriptionStatus} & Estado: \texttt{SAMPLING\_IN\_PROGRESS}, \texttt{PRESCRIBED}, \texttt{EXECUTED\_OPTIMAL}, \texttt{CLOSED\_BY\_PIT\_HARDENING}. \\
\texttt{execution} & \texttt{Execution} \texttt{Confirmation} & Datos de auditoría de la labor de raleo en campo. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-96} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `FruitThinningPrescription`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de FruitThinningPrescription en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-96} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{recordTree} \texttt{Sampling} & \texttt{record:} \texttt{TreeSampling} \texttt{Record} & \texttt{void} & Incorpora conteo de brote garantizando no duplicidad de árbol. \\
\texttt{ingest} \texttt{Samplings} \texttt{Batch} & \texttt{records:} \texttt{List<} \texttt{TreeSampling} \texttt{Record>}, \texttt{evaluator:} \texttt{SamplingCoverageEvaluator} & \texttt{void} & Procesa lote móvil offline y emite \texttt{SamplingRoundCompletedEvent} al alcanzar representatividad ($N \ge 5$). \\
\texttt{determine} \texttt{Sustainable} \texttt{CropLoad} & \texttt{inputs: AgronomicInputs}, \texttt{calc: CropLoadBalancingCalculatorService} & \texttt{void} & Calcula porcentaje óptimo de remoción y emite \texttt{SustainableCropLoadDeterminedEvent}. \\
\texttt{confirm} \texttt{Execution} & \texttt{confirm:} \texttt{Execution} \texttt{Confirmation} & \texttt{void} & Registra ejecución de raleo emitiendo \texttt{ThinningExecutionConfirmedEvent}. \\
\texttt{closeWindowBy} \texttt{PitHardening} & \texttt{date: LocalDate} & \texttt{void} & Cierra la ventana de intervención oportuna por endurecimiento de carozo. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `SamplingRound` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-97} se describe la estructura y delimitación transaccional del modelo `SamplingRound`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio SamplingRound (Internal Entity) en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-97} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Agrupa un conjunto de árboles muestreados en un cuartel olivarero durante una jornada de evaluación. \\
Relaciones de dominio & Subordinada a \texttt{FruitThinning} \texttt{Prescription} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-98} se presentan los atributos, tipos de datos e invariantes que rigen a `SamplingRound`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo SamplingRound en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-98} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{RoundId} & Identificador de la ronda de muestreo. \\
\texttt{actorId} & \texttt{UserId} & Técnico o productor que recolectó las muestras. \\
\texttt{clientBatchId} & \texttt{String} & Identificador de idempotencia del cliente móvil offline. \\
\texttt{samplingRecords} & \texttt{List<} \texttt{TreeSampling} \texttt{Record>} & Muestras individuales de árboles recolectadas. \\
\texttt{isRepresentative} & \texttt{Boolean} & Indicador si cumple el tamaño muestral mínimo representativo. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-99} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `SamplingRound`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de SamplingRound en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-99} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{addRecord} & \texttt{record:} \texttt{TreeSampling} \texttt{Record} & \texttt{void} & Añade una muestra individual al lote de la ronda. \\
\texttt{evaluate} \texttt{Representativeness} & \texttt{evaluator: SamplingCoverageEvaluator} & \texttt{void} & Valida que la cobertura de muestreo sea estadísticamente sólida. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `TreeSamplingRecord` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-100} se describe la estructura y delimitación transaccional del modelo `TreeSamplingRecord`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio TreeSamplingRecord (Internal Entity) en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-100} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Captura los conteos de brotes, cuajado y vigor en un olivo individualizado. \\
Relaciones de dominio & Subordinada a \texttt{SamplingRound} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-101} se presentan los atributos, tipos de datos e invariantes que rigen a `TreeSamplingRecord`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo TreeSamplingRecord en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-101} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{SamplingRecordId} & Identificador del registro de árbol. \\
\texttt{treeTag} & \texttt{String} & Identificador físico o código de placa del árbol evaluado. \\
\texttt{shootCount} & \texttt{Int} & Número de brotes representativos contabilizados. \\
\texttt{fruitSetCount} & \texttt{Int} & Cantidad de frutos cuajados observados. \\
\texttt{trunkDiameterMm} & \texttt{Double} & Diámetro de tronco a 30 cm de altura para estimar área de sección transversal (TCSA). \\
\texttt{samplingDate} & \texttt{LocalDate} & Fecha de recolección de la muestra. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-102} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `TreeSamplingRecord`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de TreeSamplingRecord en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-102} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{getFruits} \texttt{PerMeter} & \texttt{void} & \texttt{Double} & Calcula la densidad lineal de carga en frutos por metro de brote. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `ExecutionConfirmation` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-103} se describe la estructura y delimitación transaccional del modelo `ExecutionConfirmation`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio ExecutionConfirmation (Internal Entity) en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-103} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Acredita la ejecución material de la labor de raleo manual en el cuartel. \\
Relaciones de dominio & Subordinada a \texttt{FruitThinning} \texttt{Prescription} (1 a 1). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-104} se presentan los atributos, tipos de datos e invariantes que rigen a `ExecutionConfirmation`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo ExecutionConfirmation en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-104} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{ConfirmationId} & Identificador de la confirmación de raleo. \\
\texttt{executionDate} & \texttt{LocalDate} & Fecha en la que la cuadrilla completó la labor. \\
\texttt{actual} \texttt{RemovalPercentage} & \texttt{Double} & Porcentaje real de frutos retirados del árbol. \\
\texttt{laborCrewSize} & \texttt{Int} & Número de operarios de campo participantes. \\
\texttt{timeliness} & \texttt{ExecutionTimeliness} & Calificación: \texttt{OPTIMAL} (previo a carozo) o \texttt{LATE}. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-105} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `ExecutionConfirmation`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de ExecutionConfirmation en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-105} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{isOpportune} & \texttt{void} & \texttt{Boolean} & Verifica si la intervención ocurrió antes del endurecimiento de carozo. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-106} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-106} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{PrescriptionId}, \texttt{RoundId} & \texttt{UUID v4} & Identificadores únicos universales inmutables. \\
\texttt{CropLoadDensity} & \texttt{fruitsPerMeter: Double, fruitsPerTree: Int} & Densidad óptima de carga frutal balanceada. \\
\texttt{ThinningIntensity} & \texttt{percentage\-ToRemove: Double, kgToRemovePerTree: Double} & Porcentaje y masa recomendada a defructificar en verde. \\
\texttt{PrescriptionStatus} & \texttt{Enum (7 estados)} & \texttt{SAMPLING\_IN\_PROGRESS}, \texttt{PRESCRIBED}, \texttt{CONFIRMED}, \texttt{EXECUTED}, \texttt{EXPIRED}, \texttt{VOIDED\_BY\_PLOT\_REMOVAL}, \texttt{REJECTED}. \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-107} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-107} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{CropLoad} \texttt{Balancing} \texttt{CalculatorService} & Domain Service & \texttt{calculateTarget} \texttt{Removal(} \texttt{currentLoad: Double,} \texttt{bbi: Double,} \texttt{waterStatus: Double):} \texttt{Double} & Computa la tasa agronómica de remoción recomendada. \\
\texttt{FieldSampling} \texttt{Deduplicator} & Domain Service & \texttt{deduplicate(samples: List<TreeSamplingRecord>): List<TreeSamplingRecord>} & Garantiza que no existan registros superpuestos del mismo árbol. \\
\texttt{FruitThinning} \texttt{PrescriptionRepository} & Repository & \texttt{findById(id: PrescriptionId): Optional<FruitThinningPrescription>} & Carga la prescripción por su identificador primario. \\
\texttt{FruitThinning} \texttt{PrescriptionRepository} & Repository & \texttt{findByPlotId} \texttt{AndCampaign(} \texttt{plotId: PlotId,} \texttt{year:} \texttt{CampaignYear):} \texttt{Optional<} \texttt{FruitThinning} \texttt{Prescription>} & Carga la prescripción vigente para la campaña en el predio. \\
\texttt{FruitThinning} \texttt{PrescriptionRepository} & Repository & \texttt{save(prescription: FruitThinningPrescription): FruitThinningPrescription} & Guarda estado de muestreos y prescripción. \\
\texttt{SamplingRound} \texttt{CompletedEvent} & Domain Event & \texttt{prescriptionId: UUID, plotId: UUID, evaluatedTrees: int, occurredOn: Instant} & Notifica representatividad muestral suficiente para prescribir. \\
\texttt{Sustainable} \texttt{CropLoad} \texttt{DeterminedEvent} & Domain Event & \texttt{prescriptionId: UUID, plotId: UUID, removalPercentage: Double, occurredOn: Instant} & Emite prescripción formal de raleo frutal. \\
\texttt{OverloadRisk} \texttt{DetectedEvent} & Domain Event & \texttt{prescriptionId: UUID, plotId: UUID, overloadFactor: Double, occurredOn: Instant} & Alerta riesgo de sobrecarga crítica hacia la cooperativa. \\
\texttt{Thinning} \texttt{Execution} \texttt{ConfirmedEvent} & Domain Event & \texttt{prescriptionId: UUID, plotId: UUID, removalPct: Double, timeliness: String, occurredOn: Instant} & Confirma ejecución de la labor para liquidación de cosecha. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-108} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-108} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/samplings} & \texttt{SubmitSampling} \texttt{Request} & \texttt{SamplingSummary} \texttt{Resource} (201 Created) & Ingesta de muestreos individuales o por lote con cabecera \texttt{Idempotency-Key}. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/samplings} & N/A (\texttt{?campaignYear=} \texttt{\&view=summary}) & \texttt{SamplingSummary} \texttt{Resource} (200 OK) & Consulta del avance y representatividad muestral de la campaña. \\
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/thinning-prescriptions} & N/A & \texttt{Prescription} \texttt{Resource} (201 Created) & Determinación de carga frutal sostenible y emisión de prescripción bajo demanda. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/thinning-prescriptions} & N/A (\texttt{?status=ACTIVE}) & \texttt{Prescription} \texttt{Resource} (200 OK) & Consulta de prescripción vigente o por campaña. \\
\texttt{GET} & \nolinkurl{/api/v1/thinning-prescriptions/{id}} & N/A & \texttt{Prescription} \texttt{Resource} (200 OK) & Consulta de prescripción por identificador unívoco directo. \\
\texttt{POST} & \nolinkurl{/api/v1/thinning-prescriptions/{id}/execution-confirmations} & \texttt{Confirm} \texttt{Execution} \texttt{Request} & \texttt{Execution} \texttt{Confirmation} \texttt{Resource} (201 Created) & Declaración y confirmación de labor de aclareo oportuna o tardía. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-109} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-109} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{SubmitSampling} \texttt{Request} & Request DTO & \texttt{{ clientBatchId: String, samples: List<ShootSampleDto> }} & Lote de conteo capturado en campo offline. \\
\texttt{SamplingSummary} \texttt{Resource} & Response DTO & \texttt{{ plotId: UUID, sampledTreesCount: Int, sampledShootsCount: Int, meanFruitsPerMeter: Double, isRepresentative: Boolean, treesNeeded: Int }} & Resumen de representatividad muestral. \\
\texttt{Prescription} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, plotId: UUID, targetLoad: Double, percentage\-ToRemove: Double, status: String, windowClosesOn: LocalDate }} & Asesoramiento oficial de aclareo. \\
\texttt{ConfirmExecution} \texttt{Request} & Request DTO & \texttt{{ executedDate: LocalDate, removedKg: Double, notes: String }} & Declaración de ejecución de la labor. \\
\texttt{Execution} \texttt{Confirmation} \texttt{Resource} & Response DTO & \texttt{{ prescriptionId: UUID, confirmationStatus: String, executedDate: LocalDate, isOpportune: Boolean, recordedAt: Instant }} & Constancia de ejecución y sellado biológico. \\
\texttt{Prescription} \texttt{ResourceAssembler} & Assembler & \texttt{toResource(} \texttt{FruitThinningPrescription):} \texttt{Prescription} \texttt{Resource} & Mapeo a DTO con formateo agronómico. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-110} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-110} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Ingest} \texttt{Field} \texttt{Samplings} \texttt{Batch} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Ingest} \texttt{Field} \texttt{Samplings} \texttt{Batch} \texttt{Command} & Verifica idempotencia \texttt{(actorId, plotId, clientBatchId)}, persiste muestras; si cumple representatividad, emite evento. \\
\texttt{Get} \texttt{Sampling} \texttt{Round} \texttt{Status} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Sampling} \texttt{Round} \texttt{Status} \texttt{Query} & Informa el avance muestral y suficiencia sin descargar el historial completo. \\
\texttt{Determine} \texttt{Sustainable} \texttt{CropLoad} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Determine} \texttt{Sustainable} \texttt{CropLoad} \texttt{Command} & Invoca \texttt{CropLoadBalancingCalculatorService}, fija carga admisible y alerta si hay sobrecarga. \\
\texttt{Get} \texttt{Thinning} \texttt{Prescription} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Thinning} \texttt{Prescription} \texttt{Query} & Retorna snapshot autorizado de la prescripción activa o histórica. \\
\texttt{Confirm} \texttt{Thinning} \texttt{Execution} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Confirm} \texttt{Thinning} \texttt{Execution} \texttt{Command} & Compara fecha con \texttt{windowClosesOn}; si es oportuna emite confirmación óptima, si es tardía emite extemporánea. \\
\texttt{OnThinning} \texttt{WindowClosed} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Thinning} \texttt{WindowClosed} \texttt{ByPitHardening} \texttt{Event} & Transiciona prescripciones pendientes a \texttt{EXPIRED}. \\
\texttt{OnPlot} \texttt{Removed} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Plot} \texttt{Removed} \texttt{Event} & Anula reactivamente prescripciones abiertas al darse de baja el predio. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-111} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-111} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Thinning} \texttt{Prescription} \texttt{JpaRepository} & Persistence & Spring Data JPA & Almacenamiento relacional de prescripciones en PostgreSQL. \\
\texttt{FieldSamplingRound} \texttt{JpaRepository} & Persistence & Spring Data JPA & Ingesta transaccional con índice único de lote. \\
\texttt{MobileOffline} \texttt{StorageStrategy} & Client Persistence & Room (Android) / sqflite (Flutter) & Almacenamiento local SQLite y cola durable \texttt{WorkManager}. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Almacenamiento local offline-first (Room / sqflite):**
  * *Android Nativo (Room / SQLite):* `SamplingDao` gestiona `LocalSamplingRoundEntity` y `LocalTreeSamplingEntity`, permitiendo registrar árboles evaluados en campo sin cobertura de red. La tabla `sync_queue` retiene los lotes pendientes con clave de idempotencia `(actor_id, plot_id, client_batch_id)`.
  * *Cross-Platform (sqflite / SQLite):* Tabla `offline_samplings` administrada por `LocalDataAccess` con respaldo durable para mitigar cierres forzados de la aplicación.
* **Sincronización resiliente en segundo plano (WorkManager):**
  * `SamplingSyncWorkManager` programa tareas en background con restricciones de conectividad (`NetworkType.CONNECTED`). Al recuperar señal celular, drena la cola de sincronización hacia `POST /api/v1/plots/{plotId}/samplings` aplicando reintentos exponenciales automáticos ante fallas transitorias.
* **Integración con hardware del dispositivo:**
  * *Geolocalización (FusedLocationProviderClient):* Captura las coordenadas de georreferenciación del árbol testigo al momento de registrar el muestreo en campo.

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-112} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-112} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{thinning\_} \texttt{prescriptions} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador de la prescripción. \\
\texttt{thinning\_} \texttt{prescriptions} & \texttt{plot\_id} & \texttt{UUID} & \texttt{NOT NULL, INDEX} & Parcela asociada. \\
\texttt{thinning\_} \texttt{prescriptions} & \texttt{campaign\_year} & \texttt{INT} & \texttt{NOT NULL} & Año agrícola de la labor. \\
\texttt{thinning\_} \texttt{prescriptions} & \texttt{target\_} \texttt{fruits\_m} & \texttt{NUMERIC(5,2)} & \texttt{NOT NULL} & Carga objetivo de frutos/m lineal. \\
\texttt{thinning\_} \texttt{prescriptions} & \texttt{percentage\_} \texttt{remove} & \texttt{NUMERIC(4,2)} & \texttt{NOT NULL} & Porcentaje de remoción recomendado. \\
\texttt{thinning\_} \texttt{prescriptions} & \texttt{status} & \texttt{VARCHAR(30)} & \texttt{NOT NULL, INDEX} & Estado del ciclo de vida (7 estados). \\
\texttt{thinning\_} \texttt{prescriptions} & \texttt{window\_} \texttt{closes\_on} & \texttt{DATE} & \texttt{NOT NULL} & Fecha límite biológica de aclareo. \\
\texttt{field\_} \texttt{sampling\_} \texttt{rounds} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador de la ronda de muestreo. \\
\texttt{field\_} \texttt{sampling\_} \texttt{rounds} & \texttt{plot\_id} & \texttt{UUID} & \texttt{NOT NULL} & Parcela muestreada. \\
\texttt{field\_} \texttt{sampling\_} \texttt{rounds} & \texttt{actor\_id} & \texttt{UUID} & \texttt{NOT NULL} & Usuario que ejecutó el muestreo. \\
\texttt{field\_} \texttt{sampling\_} \texttt{rounds} & \texttt{client\_batch\_} \texttt{id} & \texttt{VARCHAR(64)} & \texttt{NOT NULL} & Identificador UUID local para idempotencia. \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS crop_load;

CREATE TABLE crop_load.thinning_prescriptions (
    id                UUID PRIMARY KEY,
    plot_id           UUID NOT NULL,
    campaign_year     INT NOT NULL,
    target_fruits_m   NUMERIC(5,2) NOT NULL
        CHECK (target_fruits_m > 0),
    percentage_remove NUMERIC(4,2) NOT NULL
        CHECK (percentage_remove BETWEEN 0 AND 100),
    status            VARCHAR(30) NOT NULL,
    window_closes_on  DATE NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE crop_load.field_sampling_rounds (
    id              UUID PRIMARY KEY,
    plot_id         UUID NOT NULL,
    actor_id        UUID NOT NULL,
    client_batch_id VARCHAR(64) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_sampling_idempotency
        UNIQUE (actor_id, plot_id, client_batch_id)
);

CREATE UNIQUE INDEX uq_active_prescription_per_plot_campaign 
ON crop_load.thinning_prescriptions(plot_id, campaign_year) 
WHERE status IN ('SAMPLING_IN_PROGRESS', 'PRESCRIBED');
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-113} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Crop Load Regulation and Thinning Advisory.} \label{tab:tactical-113} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{PlotSampling} \texttt{Controller}; \texttt{PlotThinningPrescriptionController}; \texttt{ThinningExecutionController} & Endpoints REST para ingesta de muestreo de campo, consulta de prescripciones y confirmación de aclareo. & Spring MVC, Jakarta Validation \\
Application & \texttt{CropLoad} \texttt{CommandService}; \texttt{CropLoadQueryService} & Orquestación de comandos de muestreo y aclareo, consultas de prescripciones y resúmenes muestrales. & Spring \texttt{@Transactional}, \texttt{@Service} \\
Domain & \texttt{FruitThinning} \texttt{PrescriptionRepository}; \texttt{CropLoadBalancingCalculatorService}; \texttt{FieldSamplingDeduplicator} & Contrato de persistencia (puerto de dominio), cálculo de carga admisible y deduplicación de lotes offline. & Java puro / DDD \\
Infrastructure & \texttt{JpaFruitThinning} \texttt{PrescriptionRepositoryAdapter}; \texttt{SamplingSyncWorkManager}; \texttt{SpringDomainEventPublisher} & Persistencia JPA en PostgreSQL, sincronización en background en clientes móviles y publicación de eventos. & Spring Data JPA, WorkManager, SQLite \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El agricultor registra muestras de brotes sin conexión en el contenedor de la aplicación cliente móvil (persistidas en Room/sqflite).
2. Al recuperar conectividad, `SamplingSyncWorkManager` despacha `POST` \nolinkurl{/api/v1/plots/{plotId}/samplings} con cabecera `Idempotency-Key` hacia `PlotSamplingController`.
3. `PlotSamplingController` delega el procesamiento en `CropLoadCommandService`, mientras que las consultas de prescripciones y resúmenes muestrales son atendidas por `CropLoadQueryService`.
4. `CropLoadCommandService` invoca `FieldSamplingDeduplicator` para filtrar duplicados y persiste los registros en `FruitThinning` `PrescriptionRepository`.
5. Si el muestreo alcanza suficiencia estadística ($N \ge 5$), `CropLoadCommandService` invoca `CropLoadBalancingCalculatorService` y publica `ThinningPrescribedEvent` vía `SpringDomainEventPublisher`. Si detecta riesgo de sobrecarga, emite alerta hacia *Cooperative Operations*.
6. El productor confirma el aclareo mediante `POST` \nolinkurl{/api/v1/thinning-prescriptions/{id}/execution-confirmations}; `ThinningExecutionController` delega en `CropLoadCommandService`, el cual actualiza la prescripción a `EXECUTED` en el repositorio y emite `ThinningExecutedEvent`.

A continuación, en la \autoref{fig:c4-component-crop-load} se esquematiza el diagrama de componentes del Bounded Context Crop Load Regulation:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Crop Load Regulation.} \label{fig:c4-component-crop-load}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-crop-load.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Crop Load Regulation. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-crop-load}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-crop-load}) para el Bounded Context Crop Load Regulation:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Crop Load Regulation and Thinning Advisory.} \label{fig:class-diagram-crop-load}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-crop-load.png}
\caption*{\textit{Nota.} Estructura estática de clases, entidades de muestreo y prescripción en Crop Load Regulation. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Crop Load Regulation.} \label{fig:database-diagram-crop-load}
\vspace{0.25cm}
\centering
\includegraphics[width=0.85\textwidth]{report/assets/tactical-diagrams/database-diagram-crop-load.png}
\caption*{\textit{Nota.} Estructura de tablas, índices de idempotencia y restricciones en PostgreSQL para Crop Load Regulation. Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: Cooperative Operations and Territorial Intelligence

Propósito: Agrupa la inteligencia territorial, la administración gremial y las proyecciones logísticas de acopio para organizaciones agrarias y cooperativas olivareras. Gobierna el Aggregate Root `Cooperative`, delimitado bajo la autoridad de un gestor técnico institucional único (`technicalManagerUserId: UserId`, validado mediante claims de token JWT con rol `ROLE_GESTOR_COOPERATIVA`). Administra el padrón unificado de socios (`cooperative_` `members`), evalúa el semáforo territorial de riesgos fenológicos, climáticos y de sobrecarga sectorial (`GET .../territorial-risk` con parámetros GPS `?latitude=` `&longitude=`), y proyecta tempranamente el volumen agregado de cosecha en toneladas verdes y negras (`EarlyIntakeProjection`), advirtiendo penalizaciones de confianza si la representatividad muestral del padrón es inferior al $50\%$.

#### Domain Layer

##### Modelo de dominio: `Cooperative` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-114} se describe la estructura y delimitación transaccional del modelo `Cooperative`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio Cooperative (Aggregate Root) en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-114} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Administra el padrón de socios olivareros, proyecta el volumen de cosecha temprana y evalúa la matriz territorial de riesgos. \\
Relaciones de dominio & Gobierna miembros (\texttt{Cooperative} \texttt{Member}), proyecciones de acopio y evaluaciones de riesgo territorial. \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-115} se presentan los atributos, tipos de datos e invariantes que rigen a `Cooperative`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo Cooperative en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-115} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{CooperativeId} & Identificador único de la cooperativa. \\
\texttt{name} & \texttt{CooperativeName} & Razón social formal de la asociación cooperativa. \\
\texttt{taxId} & \texttt{Tax} \texttt{Identification} \texttt{Number} & Registro fiscal unívoco (RUC). \\
\texttt{licenseId} & \texttt{LicenseId} & Referencia lógica a la licencia institucional de suscripción. \\
\texttt{technicalManager} \texttt{UserId} & \texttt{UserId} & Identificador del gestor técnico autorizado. \\
\texttt{members} & \texttt{List<Cooperative} \texttt{Member>} & Padrón de productores socios adscritos. \\
\texttt{riskMatrix} & \texttt{Territorial} \texttt{RiskMatrix} & Semáforo de riesgo geográfico por sector. \\
\texttt{intakeProjection} & \texttt{Early} \texttt{IntakeProjection} & Estimación agregada de volumen de cosecha para almazara. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-116} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `Cooperative`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de Cooperative en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-116} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{authorizeCode} \texttt{Issuance} & \texttt{managerId: UserId} & \texttt{void} & Autoriza generación de lotes de códigos de patrocinio institucional. \\
\texttt{affiliate} \texttt{Producer} & \texttt{userId: UserId}, \texttt{ha: Double}, \texttt{plots: List<PlotId>} & \texttt{Cooperative} \texttt{Member} & Incorpora productor al padrón y emite \texttt{MemberAffiliated} \texttt{Event}. \\
\texttt{updateMember} \texttt{Contact} & \texttt{userId: UserId}, \texttt{name: String}, \texttt{phone: String}, \texttt{email: String} & \texttt{void} & Sincroniza datos de contacto del socio en el padrón. \\
\texttt{evaluate} \texttt{Territorial} \texttt{RiskMatrix} & \texttt{incidents:} \texttt{List<Agroclimatic} \texttt{Incident>} & \texttt{void} & Consolida alertas activas y emite \texttt{CooperativeRiskMatrixEvaluatedEvent}. \\
\texttt{projectIntake} \texttt{Volume} & \texttt{service: YieldAggregationDomainService} & \texttt{void} & Agrega proyecciones de cosecha a partir de muestras y floración. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `CooperativeMember` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-117} se describe la estructura y delimitación transaccional del modelo `CooperativeMember`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio CooperativeMember (Internal Entity) en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-117} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Representa la membresía y situación gremial de un productor olivarero en la cooperativa. \\
Relaciones de dominio & Subordinada a \texttt{Cooperative} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-118} se presentan los atributos, tipos de datos e invariantes que rigen a `CooperativeMember`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo CooperativeMember en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-118} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{MemberId} & Identificador de membresía gremial. \\
\texttt{producerUserId} & \texttt{UserId} & Identificador de cuenta del socio productor. \\
\texttt{fullName} & \texttt{String} & Nombre completo oficial del socio. \\
\texttt{contactPhone} & \texttt{String} & Teléfono de contacto registrado. \\
\texttt{totalDeclaredHa} & \texttt{Double} & Hectáreas olivareras declaradas ante la cooperativa. \\
\texttt{status} & \texttt{MemberStatus} & Estado de membresía: \texttt{ACTIVE}, \texttt{SUSPENDED}, \texttt{RESIGNED}. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-119} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `CooperativeMember`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de CooperativeMember en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-119} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{updateContact} & \texttt{name: String}, \texttt{phone: String}, \texttt{email: String} & \texttt{void} & Actualiza datos civiles de comunicación del socio. \\
\texttt{linkPlot} & \texttt{plotId: PlotId}, \texttt{ha: Double} & \texttt{void} & Registra parcela asociada a la cuota de entrega de aceituna. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-120} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-120} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{CooperativeId} & \texttt{UUID v4} & Identificador único universal de la cooperativa. \\
\texttt{CooperativeName} & \texttt{String} & Razón social de la organización agraria (longitud 3 a 150 caracteres). \\
\texttt{Tax} \texttt{Identification} \texttt{Number} & \texttt{String} & Registro tributario institucional oficial. \\
\texttt{MemberId} & \texttt{UUID v4} & Identificador único de la membresía gremial. \\
\texttt{Territorial} \texttt{RiskMatrix} & \texttt{Map<String, RiskLevel>} & Evaluación cualitativa de riesgos sectoriales (\texttt{LOW}, \texttt{MEDIUM}, \texttt{HIGH}, \texttt{CRITICAL}). \\
\texttt{Early} \texttt{IntakeProjection} & \texttt{greenTons: Double, blackTons: Double} & Estimación temprana de acopio en toneladas métricas por variedad y uso industrial. \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-121} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-121} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{TerritorialRisk} \texttt{AggregationService} & Domain Service & \texttt{evaluateSectorRisk(} \texttt{alerts:} \texttt{List<} \texttt{Agroclimatic} \texttt{Incident>):} \texttt{TerritorialRisk} \texttt{Matrix} & Consolida semáforo territorial de heladas y estrés hídrico. \\
\texttt{YieldAggregation} \texttt{DomainService} & Domain Service & \texttt{projectHarvestYield(} \texttt{samples:} \texttt{List<} \texttt{CropLoad} \texttt{Sampling>,} \texttt{factor: Double):} \texttt{IntakeProjection} \texttt{Result} & Agrega proyecciones tempranas de volumen de aceituna. \\
\texttt{Cooperative} \texttt{Repository} & Repository & \texttt{findById(id: CooperativeId): Optional<Cooperative>} & Carga la cooperativa por su identificador primario. \\
\texttt{Cooperative} \texttt{Repository} & Repository & \texttt{findByTaxId(taxId: TaxIdentificationNumber): Optional<Cooperative>} & Localiza la cooperativa por su registro fiscal único. \\
\texttt{Cooperative} \texttt{Repository} & Repository & \texttt{findByTechnical} \texttt{ManagerUserId(} \texttt{userId: UserId):} \texttt{List<} \texttt{Cooperative>} & Lista cooperativas gestionadas por un responsable técnico. \\
\texttt{Cooperative} \texttt{Repository} & Repository & \texttt{save(cooperative: Cooperative): Cooperative} & Persiste cooperativa, padrón de socios y proyecciones. \\
\texttt{CooperativeRisk} \texttt{MatrixEvaluatedEvent} & Domain Event & \texttt{cooperativeId: UUID, severity: String, frostAlertsCount: int, occurredOn: Instant} & Notifica mapa de calor territorial a gestores cooperativos. \\
\texttt{MemberAffiliated} \texttt{Event} & Domain Event & \texttt{cooperativeId: UUID, memberId: UUID, producerUserId: UUID, occurredOn: Instant} & Confirma afiliación de productor al padrón cooperativo. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-122} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-122} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{GET} & \nolinkurl{/api/v1/cooperatives/{cooperativeId}/members} & N/A (\texttt{?status=ACTIVE}) & \texttt{List<} \texttt{Cooperative} \texttt{Member} \texttt{Resource>} (200 OK) & Padrón de socios agremiados. \\
\texttt{GET} & \nolinkurl{/api/v1/cooperatives/{cooperativeId}/members/{memberId}} & N/A & \texttt{Cooperative} \texttt{Member} \texttt{Resource} (200 OK) & Ficha gremial individual de socio. \\
\texttt{GET} & \nolinkurl{/api/v1/cooperatives/{cooperativeId}/territorial-risk} & N/A (\texttt{?latitude=} \texttt{\&longitude=}) & \texttt{TerritorialRisk} \texttt{MatrixResource} (200 OK) & Semáforo territorial de riesgo fenológico y sobrecarga con geolocalización GPS. \\
\texttt{GET} & \nolinkurl{/api/v1/cooperatives/{cooperativeId}/intake-forecasts} & N/A (\texttt{?campaignYear=}) & \texttt{IntakeForecast} \texttt{Resource} (200 OK) & Proyección temprana agregada de acopio en toneladas. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-123} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-123} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Cooperative} \texttt{Member} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, producerUserId: UUID, fullName: String, contactPhone: String, totalDeclaredHa: Double, status: String }} & Datos de socio agremiado. \\
\texttt{TerritorialRisk} \texttt{MatrixResource} & Response DTO & \texttt{{ cooperativeId: UUID, highRiskSectors: List<String>, generalStatus: String, evaluatedAt: Instant }} & Semáforo de riesgo territorial. \\
\texttt{IntakeForecast} \texttt{Resource} & Response DTO & \texttt{{ cooperativeId: UUID, greenOlivesTons: Double, blackOlivesTons: Double, confidenceDegraded: Boolean }} & Proyección de acopio para salmuera y aceite. \\
\texttt{Cooperative} \texttt{Resource} \texttt{Assembler} & Assembler & \texttt{toResource(} \texttt{Cooperative):} \texttt{Cooperative} \texttt{Resource} & Transformador a DTO público de presentación. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-124} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-124} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Evaluate} \texttt{Cooperative} \texttt{RiskMatrix} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Evaluate} \texttt{Cooperative} \texttt{RiskMatrix} \texttt{Command} & Carga el agregado \texttt{Cooperative}, recopila alertas activas de telemetría y sobrecarga, evalúa semáforo y emite evento. \\
\texttt{Project} \texttt{Cooperative} \texttt{IntakeVolume} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Project} \texttt{Cooperative} \texttt{IntakeVolume} \texttt{Command} & Consulta resúmenes biométricos, computa proyección de cosecha en toneladas y advierte si la representatividad es baja. \\
\texttt{Get} \texttt{Cooperative} \texttt{Directory} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Cooperative} \texttt{Directory} \texttt{Query} & Recupera el padrón de socios ordenado por apellido y estado de afiliación. \\
\texttt{Get} \texttt{Territorial} \texttt{RiskMatrix} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Territorial} \texttt{RiskMatrix} \texttt{Query} & Entrega el semáforo sectorial consolidado, resolviendo el sector por coordenadas GPS. \\
\texttt{Get} \texttt{EarlyIntake} \texttt{Projection} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{EarlyIntake} \texttt{Projection} \texttt{Query} & Entrega las toneladas proyectadas de aceituna verde y negra filtradas por campaña agrícola. \\
\texttt{OnCooperative} \texttt{CodeRedeemed} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Cooperative} \texttt{CodeRedeemed} \texttt{Event} & Escucha canje de código institucional y da de alta al socio con la superficie concedida. \\
\texttt{OnContact} \texttt{ProfileUpdated} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Contact} \texttt{ProfileUpdated} \texttt{Event} & Sincroniza datos de contacto del socio en el padrón cooperativo. \\
\texttt{OnOverload} \texttt{RiskDetected} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Overload} \texttt{RiskDetected} \texttt{Event} & Si la parcela pertenece a un socio, actualiza la matriz de riesgo marcando alerta de sobrecarga. \\
\texttt{OnWeather} \texttt{ForecastIngested} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Weather} \texttt{ForecastIngested} \texttt{Event} & Si se proyecta helada próxima en un sector, actualiza el semáforo territorial a nivel crítico. \\
\texttt{OnSampling} \texttt{RoundCompleted} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Sampling} \texttt{RoundCompleted} \texttt{Event} & Despacha comando para recalcular y actualizar la proyección de acopio gremial. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-125} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-125} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Cooperative} \texttt{JpaRepository} & Persistence & Spring Data JPA & Acceso a tabla \texttt{cooper-} \texttt{atives} y padrón de socios en PostgreSQL. \\
\texttt{JpaCooperative} \texttt{Repository} \texttt{Adapter} & Adapter & Spring Component & Implementa el puerto de dominio \texttt{Cooperative} \texttt{Repository}. \\
\texttt{GpsSpatial} \texttt{SectoringAdapter} & GIS Adapter & GeoTools / JTS & Asocia coordenadas GPS (\texttt{lat, lon}) a sectores territoriales del valle olivarero. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Caché local de padrón y semáforo territorial:**
  * *Android Nativo (Room / SQLite):* `CooperativeCacheDao` y entidades `LocalMemberEntity`, `LocalRiskMatrixEntity` para consulta inmediata del padrón de socios y matriz de riesgo sectorial sin dependencia de conectividad permanente.
  * *Cross-Platform (sqflite / SQLite):* Tablas `cooperative_members_cache` y `territorial_risk_cache` administradas por `LocalDataAccess`.
* **Sectorización espacial GPS en dispositivo:**
  * Integración con FusedLocationProviderClient en la aplicación del Gestor Técnico Cooperativo (`Cooperative Operations UI`) para resolver automáticamente la subcuenca o sector agroecológico al recorrer predios agremiados en campo, visualizando el cuadrante de riesgo correspondiente.

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-126} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-126} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{cooperat-} \texttt{ives} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador de la cooperativa. \\
\texttt{cooperat-} \texttt{ives} & \texttt{name} & \texttt{VARCHAR(150)} & \texttt{NOT NULL} & Razón social de la organización agraria. \\
\texttt{cooperat-} \texttt{ives} & \texttt{tax\_id} & \texttt{VARCHAR(11)} & \texttt{NOT NULL, UNIQUE} & RUC institucional de 11 dígitos. \\
\texttt{cooperat-} \texttt{ives} & \texttt{license\_id} & \texttt{UUID} & \texttt{NOT NULL} & Referencia al contrato corporativo en Subscription. \\
\texttt{cooperat-} \texttt{ives} & \texttt{technical\_} \texttt{manager\_} \texttt{user\_id} & \texttt{UUID} & \texttt{NOT NULL} & Gestor técnico único autorizado de la cooperativa. \\
\texttt{coopera-} \texttt{tive\_} \texttt{members} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador del socio en padrón. \\
\texttt{coopera-} \texttt{tive\_} \texttt{members} & \texttt{coop-} \texttt{erative\_} \texttt{id} & \texttt{UUID} & \texttt{NOT NULL, FK} & Cooperativa a la que pertenece. \\
\texttt{coopera-} \texttt{tive\_} \texttt{members} & \texttt{producer\_} \texttt{user\_id} & \texttt{UUID} & \texttt{NOT NULL} & Usuario productor socio. \\
\texttt{coopera-} \texttt{tive\_} \texttt{members} & \texttt{full\_name} & \texttt{VARCHAR(150)} & \texttt{NOT NULL} & Nombre civil del socio. \\
\texttt{coopera-} \texttt{tive\_} \texttt{members} & \texttt{declared\_ha} & \texttt{NUMERIC(8,2)} & \texttt{NOT NULL} & Hectáreas aportadas al padrón. \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS cooperative;

CREATE TABLE cooperative.cooperatives (
    id                         UUID PRIMARY KEY,
    name                       VARCHAR(150) NOT NULL,
    tax_id                     VARCHAR(11) NOT NULL UNIQUE,
    license_id                 UUID NOT NULL,
    technical_manager_user_id  UUID NOT NULL,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cooperative.cooperative_members (
    id                UUID PRIMARY KEY,
    cooperative_id    UUID NOT NULL
        REFERENCES cooperative.cooperatives(id) ON DELETE CASCADE,
    producer_user_id  UUID NOT NULL,
    full_name         VARCHAR(150) NOT NULL,
    contact_phone     VARCHAR(25),
    declared_ha       NUMERIC(8,2) NOT NULL
        CHECK (declared_ha > 0),
    status            VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_coop_member
        UNIQUE (cooperative_id, producer_user_id)
);

CREATE INDEX idx_coop_manager
    ON cooperative.cooperatives(technical_manager_user_id);
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-127} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Cooperative Operations and Territorial Intelligence.} \label{tab:tactical-127} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{CooperativeMember} \texttt{Controller}; \texttt{CooperativeIntakeController}; \texttt{CooperativeRiskController} & API REST para padrón de socios, proyección de acopio y semáforo de riesgo territorial. & Spring MVC, Jakarta Validation \\
Application & \texttt{Cooperative} \texttt{CommandService}; \texttt{CooperativeQueryService} & Orquestación de comandos de afiliación y evaluación de riesgos, y consultas de padrón, acopio y semáforo. & Spring \texttt{@Transactional}, \texttt{@Service} \\
Domain & \texttt{Cooperative} \texttt{Repository}; \texttt{TerritorialRiskAggregationService}; \texttt{YieldAggregationDomainService} & Contrato de persistencia (puerto de dominio), agregación de riesgo bioclimático y proyección de rendimiento. & Java puro / DDD \\
Infrastructure & \texttt{JpaCooperative} \texttt{RepositoryAdapter}; \texttt{SpringDomainEventPublisher} & Persistencia JPA en PostgreSQL y despacho de eventos de dominio de riesgo territorial. & Spring Data JPA, Spring Events \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El gestor técnico consulta el semáforo territorial desde la aplicación móvil o portal enviando `GET .../territorial-risk?latitude=-18.05&longitude=-70.25` hacia `CooperativeRiskController`.
2. `CooperativeRiskController` delega la consulta en `CooperativeQueryService` (mientras que los comandos de afiliación, suspensión y evaluación formal de riesgo son atendidos por `CooperativeCommandService`).
3. `CooperativeQueryService` consulta `Cooperative` `Repository` e interactúa con el servicio de dominio `TerritorialRiskAggregationService` para consolidar alertas de heladas y sobrecarga por sector agroclimático (*Sector Valle Bajo*, *Sector Costa*, *Sector Litoral*).
4. Se retorna `TerritorialRiskMatrixResource` resaltando el nivel de riesgo por cuadrante operativo.
5. Para proyecciones de cosecha temprana, `CooperativeQueryService` invoca `YieldAggregationDomainService` exponiendo las toneladas estimadas a través de `CooperativeIntakeController`.

A continuación, en la \autoref{fig:c4-component-cooperative} se esquematiza el diagrama de componentes del Bounded Context Cooperative Operations:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Cooperative Operations.} \label{fig:c4-component-cooperative}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-cooperative.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Cooperative Operations. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-cooperative}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-cooperative}) para el Bounded Context Cooperative Operations:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Cooperative Operations and Territorial Intelligence.} \label{fig:class-diagram-cooperative}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-cooperative.png}
\caption*{\textit{Nota.} Estructura estática de clases, padrón de socios y modelos de acopio en Cooperative Operations. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Cooperative Operations.} \label{fig:database-diagram-cooperative}
\vspace{0.25cm}
\centering
\includegraphics[width=0.85\textwidth]{report/assets/tactical-diagrams/database-diagram-cooperative.png}
\caption*{\textit{Nota.} Tablas de cooperativas, padrón de socios y claves foráneas en PostgreSQL. Elaboración propia.}
\end{figure}

\clearpage

### Bounded Context: Harvest Settlement and Performance Reporting

Propósito: Custodia la memoria productiva consolidada y la emisión de certificaciones oficiales de cosecha de Viora. Es responsable del Aggregate Root `AgronomicReport`, administrando las liquidaciones cuantitativas de pesajes comerciales al cierre de campaña (`Harvest` `Settlement`), calculando la curva de atenuación interanual de vecería ($ARR$) y la varianza productiva mediante el servicio puro `StabilizationCurveCalculatorService`, vinculando la confirmación de aclareos en verde con el balance final, y emitiendo expedientes agronómicos certificados con firma criptográfica y hash SHA-256 inmutable, los cuales pueden ser consultados en JSON o descargados en PDF inmutable mediante negociación de contenidos HTTP (`Accept: application/pdf`).

#### Domain Layer

##### Modelo de dominio: `AgronomicReport` (Aggregate Root)
&nbsp;

En la \autoref{tab:tactical-128} se describe la estructura y delimitación transaccional del modelo `AgronomicReport`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio AgronomicReport (Aggregate Root) en Harvest Settlement and Performance Reporting.} \label{tab:tactical-128} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Aggregate Root \\
Propósito & Consolida la memoria productiva auditada de una parcela, evalúa la curva de atenuación de vecería y emite expedientes oficiales certificados. \\
Relaciones de dominio & Referencia a \texttt{PlotId} y \texttt{UserId}. Compone las liquidaciones anuales de cosecha (\texttt{Harvest} \texttt{Settlement}). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-129} se presentan los atributos, tipos de datos e invariantes que rigen a `AgronomicReport`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo AgronomicReport en Harvest Settlement and Performance Reporting.} \label{tab:tactical-129} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{ReportId} & Identificador único del expediente agronómico predial. \\
\texttt{plotId} & \texttt{PlotId} & Parcela olivarera evaluada. \\
\texttt{producerId} & \texttt{UserId} & Productor titular de la parcela. \\
\texttt{settlements} & \texttt{List<} \texttt{Harvest} \texttt{Settlement>} & Liquidaciones históricas de cosecha registradas. \\
\texttt{trendCurve} & \texttt{Stabilization} \texttt{TrendCurve} & Curva y tasa de atenuación de vecería interanual (ARR). \\
\texttt{dossierMetadata} & \texttt{DossierMetadata} & Sello criptográfico SHA-256 y firma del auditor colegiado. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-130} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `AgronomicReport`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de AgronomicReport en Harvest Settlement and Performance Reporting.} \label{tab:tactical-130} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{settleCampaign} & \texttt{year: CampaignYear}, \texttt{greenKg: Double}, \texttt{blackKg: Double}, \texttt{notes: String} & \texttt{Harvest} \texttt{Settlement} & Asienta balance de cosecha y emite \texttt{CampaignHarvestSettledEvent}. \\
\texttt{evaluate} \texttt{Stabilization} \texttt{Trend} & \texttt{calculator: StabilizationCurveCalculatorService} & \texttt{void} & Computa varianza interanual y tasa de estabilización de vecería. \\
\texttt{compileDossier} & \texttt{signature: AuditorSignature}, \texttt{pdfGen: AgronomicDossierPdfGenerator} & \texttt{byte[]} & Compila expediente binario PDF, estampa SHA-256 y emite \texttt{AgronomicDossierGeneratedEvent}. \\
\texttt{is} \texttt{Stabilization} \texttt{TargetAchieved} & \texttt{void} & \texttt{boolean} & Determina si la reducción de fluctuación interanual supera el 30\% esperado. \\
\end{longtable}
\end{center}

##### Modelo de dominio: `HarvestSettlement` (Internal Entity)
&nbsp;

En la \autoref{tab:tactical-131} se describe la estructura y delimitación transaccional del modelo `HarvestSettlement`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.67\textwidth}}
\caption{Definición táctica y relaciones del modelo de dominio HarvestSettlement (Internal Entity) en Harvest Settlement and Performance Reporting.} \label{tab:tactical-131} \\
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Propiedad} & \textbf{Definición en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{2}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Estereotipo DDD & Internal Entity \\
Propósito & Modela la liquidación formal de pesaje y destino comercial de aceituna para una campaña anual concreta. \\
Relaciones de dominio & Subordinada a \texttt{AgronomicReport} (1 a N). \\
\end{longtable}
\end{center}

En la \autoref{tab:tactical-132} se presentan los atributos, tipos de datos e invariantes que rigen a `HarvestSettlement`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.25\textwidth} p{0.42\textwidth}}
\caption{Atributos y definición de tipos del modelo HarvestSettlement en Harvest Settlement and Performance Reporting.} \label{tab:tactical-132} \\
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Atributo} & \textbf{Tipo} & \textbf{Descripción e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{id} & \texttt{SettlementId} & Identificador de la liquidación anual. \\
\texttt{campaignYear} & \texttt{CampaignYear} & Año agrícola liquidado. \\
\texttt{greenOlivesWeight} & \texttt{OliveWeight} & Kilogramos de aceituna verde entregada (conserva). \\
\texttt{blackOlivesWeight} & \texttt{OliveWeight} & Kilogramos de aceituna negra entregada (mesa/aceite). \\
\texttt{totalHarvestWeight} & \texttt{OliveWeight} & Suma consolidada de cosecha en kilogramos. \\
\texttt{status} & \texttt{SettlementStatus} & Estado: \texttt{DRAFT}, \texttt{SETTLED}, \texttt{AUDITED}. \\
\end{longtable}
\end{center}

Asimismo, en la \autoref{tab:tactical-133} se consolidan las firmas de métodos y reglas de negocio ejecutadas por `HarvestSettlement`:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.21\textwidth} p{0.13\textwidth} p{0.38\textwidth}}
\caption{Comportamientos, métodos e invariantes de HarvestSettlement en Harvest Settlement and Performance Reporting.} \label{tab:tactical-133} \\
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endfirsthead
\hline
\textbf{Método} & \textbf{Parámetros} & \textbf{Retorno} & \textbf{Comportamiento e Invariantes} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{calculateTotal} \texttt{Weight} & \texttt{void} & \texttt{OliveWeight} & Suma pesajes de verde y negra garantizando consistencia contable. \\
\texttt{markAsAudited} & \texttt{auditor: AuditorSignature} & \texttt{void} & Congela la liquidación bajo sello de auditoría técnica. \\
\end{longtable}
\end{center}

##### Objetos de valor (Value Objects)
&nbsp;

En la \autoref{tab:tactical-134} se especifican los objetos de valor inmutables (*Value Objects*) que encapsulan las reglas y tipos base del contexto:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.22\textwidth} p{0.45\textwidth}}
\caption{Objetos de valor (Value Objects) e invariantes en Harvest Settlement and Performance Reporting.} \label{tab:tactical-134} \\
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endfirsthead
\hline
\textbf{Value Object} & \textbf{Base Type / Structure} & \textbf{Purpose \& Validation Invariants} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{3}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{ReportId}, \texttt{SettlementId} & \texttt{UUID v4} & Identificadores únicos universales inmutables. \\
\texttt{CampaignYear} & \texttt{Int} & Año de la campaña agrícola (rango $2000 \le year \le 2100$). \\
\texttt{OliveWeight} & \texttt{Double (Kilogramos)} & Peso exacto en kilos con validación de no negatividad. \\
\texttt{Stabilization} \texttt{TrendCurve} & \texttt{baselineYield: Double, variance: Double, arr: Double} & Curva de estabilización interanual de vecería. \\
\texttt{DossierMetadata} & \texttt{verificationHash: String (SHA-256), certifiedAt: Instant} & Metadatos inmutables de sellado criptográfico del expediente. \\
\end{longtable}
\end{center}

##### Servicios de dominio, repositorios y eventos
&nbsp;

En la \autoref{tab:tactical-135} se definen los servicios puros de dominio, los contratos de repositorio y los eventos soberanos despachados:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.10\textwidth} p{0.43\textwidth} p{0.14\textwidth}}
\caption{Servicios de dominio, contratos de repositorio y eventos en Harvest Settlement and Performance Reporting.} \label{tab:tactical-135} \\
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endfirsthead
\hline
\textbf{Componente} & \textbf{Patrón} & \textbf{Firma / Contrato / Payload} & \textbf{Propósito en el Dominio} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Stabilization} \texttt{Curve} \texttt{CalculatorService} & Domain Service & \texttt{computeCurve(} \texttt{settlements:} \texttt{List<} \texttt{Harvest} \texttt{Settlement>):} \texttt{StabilizationTrend} \texttt{Curve} & Computa varianza interanual y tasa de atenuación de vecería ($ARR$). \\
\texttt{AgronomicDossier} \texttt{Pdf} \texttt{Generator} & Output Port & \texttt{renderPdf(report: AgronomicReport): byte[]} & Contrato agnóstico para compilar binario PDF con sello criptográfico. \\
\texttt{AgronomicReport} \texttt{Repository} & Repository & \texttt{findById(id: ReportId): Optional<AgronomicReport>} & Carga el reporte agronómico por identificador primario. \\
\texttt{AgronomicReport} \texttt{Repository} & Repository & \texttt{findByPlotId(plotId: PlotId): Optional<AgronomicReport>} & Recupera el reporte agronómico consolidado de una parcela. \\
\texttt{AgronomicReport} \texttt{Repository} & Repository & \texttt{save(report: AgronomicReport): AgronomicReport} & Persiste atómicamente el reporte y sus liquidaciones. \\
\texttt{CampaignHarvest} \texttt{SettledEvent} & Domain Event & \texttt{reportId: UUID, plotId: UUID, campaignYear: int, totalKg: Double, occurredOn: Instant} & Notifica liquidación anual de cosecha hacia Fenología. \\
\texttt{AgronomicDossier} \texttt{GeneratedEvent} & Domain Event & \texttt{reportId: UUID, plotId: UUID, verificationHash: String, certifiedAt: Instant} & Certifica emisión oficial de expediente con hash SHA-256. \\
\end{longtable}
\end{center}

#### Interface Layer

##### Controladores y endpoints REST
&nbsp;

En la \autoref{tab:tactical-136} se detallan los endpoints RESTful expuestos por los controladores de la capa de interfaz:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.08\textwidth} p{0.22\textwidth} p{0.18\textwidth} p{0.23\textwidth} p{0.18\textwidth}}
\caption{Controladores y especificación de endpoints REST en Harvest Settlement and Performance Reporting.} \label{tab:tactical-136} \\
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endfirsthead
\hline
\textbf{Method} & \textbf{Route (Endpoint)} & \textbf{Request Body (DTO)} & \textbf{Response (DTO / Code)} & \textbf{Propósito} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/harvest-settlements} & \texttt{SettleHarvest} \texttt{Request} & \texttt{Harvest} \texttt{Settlement} \texttt{Resource} (201 Created) & Asienta liquidación anual de cosecha. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/harvest-settlements} & N/A (\texttt{?campaignYear=}) & \texttt{List<} \texttt{Harvest} \texttt{Settlement} \texttt{Resource>} (200 OK) & Lista histórica de liquidaciones prediales. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/harvest-settlements/{settlementId}} & N/A & \texttt{Harvest} \texttt{Settlement} \texttt{Resource} (200 OK) & Consulta liquidación puntual por su UUID. \\
\texttt{GET} & \nolinkurl{/api/v1/plots/{plotId}/agronomic-reports} & N/A (\texttt{Accept: application/json} o \texttt{application/pdf}) & \texttt{AgronomicReport} \texttt{Resource} / \texttt{byte[]} (200 OK) & Consulta métricas (JSON) o descarga expediente oficial en PDF mediante Content Negotiation. \\
\texttt{POST} & \nolinkurl{/api/v1/plots/{plotId}/agronomic-reports/certifications} & \texttt{CertifyDossier} \texttt{Request} & \texttt{Dossier} \texttt{Certification} \texttt{Resource} (201 Created) & Emite certificación colegiada oficial y estampa hash SHA-256. \\
\end{longtable}
\end{center}

##### DTOs (Resources) y mappers (Assemblers)
&nbsp;

En la \autoref{tab:tactical-137} se presentan las estructuras de datos de transferencia (DTOs) y sus ensambladores hacia recursos de presentación:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.41\textwidth} p{0.14\textwidth}}
\caption{Estructura de DTOs y ensambladores de recursos en Harvest Settlement and Performance Reporting.} \label{tab:tactical-137} \\
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Type} & \textbf{Mapping / Structure} & \textbf{Purpose} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{SettleHarvest} \texttt{Request} & Request DTO & \texttt{{ campaignYear: Int, greenOlivesKg: Double, blackOlivesKg: Double, notes: String }} & Datos del pesaje comercial asentado. \\
\texttt{CertifyDossier} \texttt{Request} & Request DTO & \texttt{{ auditorSignature: String, notes: String }} & Solicitud de certificación formal colegiada. \\
\texttt{Harvest} \texttt{Settlement} \texttt{Resource} & Response DTO & \texttt{{ id: UUID, campaignYear: Int, totalYieldKg: Double, status: String, settledAt: Instant }} & Representación de liquidación anual. \\
\texttt{AgronomicReport} \texttt{Resource} & Response DTO & \texttt{{ reportId: UUID, interannualVariance: Double, amplitudeReductionRate: Double, isEffective: Boolean }} & Resumen de estabilización interanual. \\
\texttt{GenerateAgronomic} \texttt{DossierCommandAssembler} & Assembler & \texttt{toCommand(} \texttt{CertifyDossierRequest,} \texttt{plotId):} \texttt{Generate} \texttt{Agronomic} \texttt{DossierCommand} & Ensamblador alineado al comando canónico. \\
\end{longtable}
\end{center}

#### Application Layer

##### Orquestación de casos de uso (Handlers)
&nbsp;

En la \autoref{tab:tactical-138} se especifican los manejadores de comandos y consultas que orquestan los flujos de aplicación y sus límites transaccionales:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.24\textwidth} p{0.10\textwidth} p{0.22\textwidth} p{0.33\textwidth}}
\caption{Manejadores de comandos y consultas (Handlers) en Harvest Settlement and Performance Reporting.} \label{tab:tactical-138} \\
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endfirsthead
\hline
\textbf{Handler} & \textbf{Type} & \textbf{Input Message (Command/Query/Event)} & \textbf{Orchestration Flow \& Transactionality} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{Settle} \texttt{Campaign} \texttt{Harvest} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Settle} \texttt{Campaign} \texttt{Harvest} \texttt{Command} & Inicia \texttt{@Transactional}, valida año único, calcula totales, recalcula curva y emite eventos. \\
\texttt{Generate} \texttt{Agronomic} \texttt{Dossier} \texttt{Command} \texttt{Handler} & Command Handler & \texttt{Generate} \texttt{Agronomic} \texttt{Dossier} \texttt{Command} & Carga reporte, compila PDF con \texttt{AgronomicDossierPdfGenerator}, estampa hash SHA-256 y emite evento. \\
\texttt{Get} \texttt{Agronomic} \texttt{Dossier} \texttt{Query} \texttt{Handler} & Query Handler & \texttt{Get} \texttt{Agronomic} \texttt{Dossier} \texttt{Query} & Retorna flujo binario inmutable del PDF o metadatos JSON según encabezado \texttt{Accept} negociado. \\
\texttt{OnThinning} \texttt{Execution} \texttt{Confirmed} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Thinning} \texttt{Execution} \texttt{Confirmed} \texttt{Event} & Vincula remoción en verde con el balance final cosechado en fin de campaña. \\
\texttt{OnHistorical} \texttt{Bearing} \texttt{IndexAssessed} \texttt{Event} \texttt{Handler} & Event Handler & \texttt{Biennial} \texttt{Bearing} \texttt{IndexAssessed} \texttt{Event} & Recibe $BBI$ de Fenología y actualiza índices de contraste para la curva de atenuación. \\
\end{longtable}
\end{center}

#### Infrastructure Layer

##### Componentes y adaptadores técnicos
&nbsp;

En la \autoref{tab:tactical-139} se detallan los adaptadores técnicos y componentes de infraestructura que dan soporte a las operaciones:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.22\textwidth} p{0.16\textwidth} p{0.18\textwidth} p{0.33\textwidth}}
\caption{Componentes técnicos y adaptadores de infraestructura en Harvest Settlement and Performance Reporting.} \label{tab:tactical-139} \\
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endfirsthead
\hline
\textbf{Component} & \textbf{Package / Role} & \textbf{Technology} & \textbf{Technical Responsibility} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{AgronomicReport} \texttt{JpaRepository} & Persistence & Spring Data JPA & Acceso a tablas de reporte y liquidaciones en PostgreSQL. \\
\texttt{JpaAgronomicReport} \texttt{RepositoryAdapter} & Adapter & Spring Component & Implementa el puerto de dominio \texttt{AgronomicReport} \texttt{Repository}. \\
\texttt{OpenPdfAgronomic} \texttt{DossierAdapter} & PDF Adapter & OpenPDF / iText & Renderizado en memoria del expediente técnico inmutable en PDF. \\
\end{longtable}
\end{center}

##### Perspectiva táctica de la aplicación móvil (Android / Flutter)
&nbsp;

* **Caché local y consultas offline (`HarvestSettlementCacheDao` / `LocalDataAccess`):**
  * *Android Nativo (Room / SQLite):* `HarvestSettlementCacheDao` y entidades `CachedHarvestSettlementEntity`, `CachedAgronomicReportEntity` para consultar balances de campañas anteriores y métricas de mitigación de vecería ($ARR$) en campo sin conexión.
  * *Cross-Platform (sqflite / SQLite):* Tablas `cached_harvest_settlements` y `cached_agronomic_reports` administradas por `LocalDataAccess`.
* **Sincronización resiliente de liquidaciones (`SettlementSyncWorker` / WorkManager):**
  * Ante la formalización del pesaje en zonas desconectadas, el comando se encola en una tabla local `pending_settlements` gestionada por Android Jetpack WorkManager (`CoroutineWorker`) o un servicio en segundo plano en Flutter. Al restablecerse la conectividad HTTPS, el worker despacha la solicitud `POST /api/v1/plots/{plotId}/harvest-settlements` con cabecera de idempotencia para evitar duplicación.
* **Descarga segura y verificación criptográfica de expedientes PDF:**
  * Descarga en streaming mediante `DownloadManager` de Android o `dio` en Flutter al almacenamiento privado del dispositivo, contrastando el hash SHA-256 computado localmente contra el campo `verification_` `hash` para certificar la autenticidad e inmutabilidad del expediente oficial.

##### Diccionario de datos relacional (PostgreSQL)
&nbsp;

En la \autoref{tab:tactical-140} se expone el diccionario de datos relacional con las tablas, columnas, restricciones e índices implementados en PostgreSQL:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.17\textwidth} p{0.14\textwidth} p{0.14\textwidth} p{0.17\textwidth} p{0.27\textwidth}}
\caption{Diccionario de datos relacional (PostgreSQL) en Harvest Settlement and Performance Reporting.} \label{tab:tactical-140} \\
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endfirsthead
\hline
\textbf{Table} & \textbf{Column} & \textbf{SQL Type (PostgreSQL)} & \textbf{Constraints / Indexes} & \textbf{Description \& Domain Meaning} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
\texttt{agronomic\_} \texttt{reports} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador del expediente agronómico. \\
\texttt{agronomic\_} \texttt{reports} & \texttt{plot\_id} & \texttt{UUID} & \texttt{NOT NULL, UNIQUE} & Parcela asociada. \\
\texttt{agronomic\_} \texttt{reports} & \texttt{producer\_id} & \texttt{UUID} & \texttt{NOT NULL} & Productor titular. \\
\texttt{agronomic\_} \texttt{reports} & \texttt{interannual\_} \texttt{variance} & \texttt{NUMERIC(8,2)} & \texttt{NOT NULL DEFAULT 0} & Varianza de rendimiento entre campañas. \\
\texttt{agronomic\_} \texttt{reports} & \texttt{amplitude\_} \texttt{reduction\_rate} & \texttt{NUMERIC(5,2)} & \texttt{NOT NULL DEFAULT 0} & Tasa de reducción de alternancia ($ARR$). \\
\texttt{agronomic\_} \texttt{reports} & \texttt{verification\_} \texttt{hash} & \texttt{VARCHAR(64)} & \texttt{NULL} & Hash SHA-256 del expediente certificado. \\
\texttt{harvest\_} \texttt{settlements} & \texttt{id} & \texttt{UUID} & \texttt{PRIMARY KEY} & Identificador único de la liquidación anual. \\
\texttt{harvest\_} \texttt{settlements} & \texttt{report\_id} & \texttt{UUID} & \texttt{NOT NULL, FK} & Reporte agronómico al que pertenece. \\
\texttt{harvest\_} \texttt{settlements} & \texttt{campaign\_year} & \texttt{INT} & \texttt{NOT NULL} & Año agrícola liquidado. \\
\texttt{harvest\_} \texttt{settlements} & \texttt{green\_olives\_} \texttt{kg} & \texttt{NUMERIC(10,2)} & \texttt{NOT NULL CHECK (green\_olives\_kg >= 0)} & Kilos cosechados de aceituna verde. \\
\texttt{harvest\_} \texttt{settlements} & \texttt{black\_olives\_} \texttt{kg} & \texttt{NUMERIC(10,2)} & \texttt{NOT NULL CHECK (black\_olives\_kg >= 0)} & Kilos cosechados de aceituna negra. \\
\texttt{harvest\_} \texttt{settlements} & \texttt{total\_yield\_} \texttt{kg} & \texttt{NUMERIC(10,2)} & \texttt{NOT NULL CHECK (total\_yield\_kg > 0)} & Kilos totales consolidados de la campaña. \\
\texttt{harvest\_} \texttt{settlements} & \texttt{status} & \texttt{VARCHAR(30)} & \texttt{NOT NULL} & Estado (\texttt{AUDITED}, \texttt{SETTLED}). \\
\end{longtable}
\end{center}

##### Script DDL de base de datos
&nbsp;

```sql
CREATE SCHEMA IF NOT EXISTS settlement;

CREATE TABLE settlement.agronomic_reports (
    id                       UUID PRIMARY KEY,
    plot_id                  UUID NOT NULL UNIQUE,
    producer_id              UUID NOT NULL,
    interannual_variance     NUMERIC(8,2) NOT NULL DEFAULT 0.0,
    amplitude_reduction_rate NUMERIC(5,2) NOT NULL DEFAULT 0.0,
    verification_hash        VARCHAR(64),
    certified_at             TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE settlement.harvest_settlements (
    id              UUID PRIMARY KEY,
    report_id       UUID NOT NULL
        REFERENCES settlement.agronomic_reports(id) ON DELETE CASCADE,
    campaign_year   INT NOT NULL,
    green_olives_kg NUMERIC(10,2) NOT NULL
        CHECK (green_olives_kg >= 0),
    black_olives_kg NUMERIC(10,2) NOT NULL
        CHECK (black_olives_kg >= 0),
    total_yield_kg  NUMERIC(10,2) NOT NULL
        CHECK (total_yield_kg > 0),
    status          VARCHAR(30) NOT NULL DEFAULT 'SETTLED',
    settled_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_report_campaign
        UNIQUE (report_id, campaign_year)
);
```

#### Bounded Context Software Architecture Component Level Diagrams 

##### Descomposición de componentes por capa
&nbsp;

En la \autoref{tab:tactical-141} se esquematiza la distribución arquitectónica de componentes internos y tecnologías empleadas por cada nivel conceptual:


\begin{center}
\small
\renewcommand{\arraystretch}{1.2}
\begin{longtable}{p{0.11\textwidth} p{0.24\textwidth} p{0.44\textwidth} p{0.10\textwidth}}
\caption{Descomposición de componentes arquitectónicos por capa en Harvest Settlement and Performance Reporting.} \label{tab:tactical-141} \\
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endfirsthead
\hline
\textbf{Architectural Layer} & \textbf{Main Component(s)} & \textbf{Architectural Responsibility} & \textbf{Key Technologies} \\
\hline
\endhead
\hline
\endfoot
\hline
\multicolumn{4}{l}{\footnotesize\textit{Nota.} Elaboración propia.} \\
\endlastfoot
Interface & \texttt{PlotHarvest} \texttt{Settlement} \texttt{Controller}; \texttt{PlotAgronomicReportController} & API REST para liquidación anual, métricas y descarga oficial de informe colegiado vía Content Negotiation. & Spring MVC, Content Negotiation \\
Application & \texttt{HarvestSettlement} \texttt{CommandService}; \texttt{AgronomicReportQueryService} & Orquestación de comandos de liquidación, certificación colegiada y consultas con streaming de PDF. & Spring \texttt{@Transactional}, \texttt{@Service} \\
Domain & \texttt{AgronomicReport} \texttt{Repository}; \texttt{StabilizationCurveCalculatorService} & Contrato de persistencia (puerto de dominio) y servicio de cálculo de curva de atenuación de vecería ($ARR$). & Java puro / DDD \\
Infrastructure & \texttt{JpaAgronomicReport} \texttt{RepositoryAdapter}; \texttt{OpenPdfAgronomicDossierAdapter}; \texttt{SpringDomainEventPublisher} & Persistencia en PostgreSQL, compilación binaria OpenPDF con hash SHA-256 y publicación de eventos. & Spring Data JPA, OpenPDF \\
\end{longtable}
\end{center}

##### Flujo de comunicación y conectividad
&nbsp;

1. El productor asienta la cosecha anual enviando `POST` \nolinkurl{/api/v1/plots/{plotId}/harvest-settlements} desde la aplicación cliente móvil hacia `PlotHarvestSettlementController`.
2. `PlotHarvestSettlementController` delega el comando `SettleCampaignHarvestCommand` en `HarvestSettlement` `CommandService`.
3. `HarvestSettlement` `CommandService` carga el reporte desde `AgronomicReportRepository`, valida las reglas y delega en `StabilizationCurveCalculatorService` la actualización de la varianza interanual y la tasa de atenuación de vecería ($ARR$).
4. Se persiste el cierre mediante `AgronomicReportRepository` y se despacha `CampaignHarvestSettledEvent` vía `SpringDomainEventPublisher` hacia *Phenology*.
5. Ante la solicitud `POST` \nolinkurl{/api/v1/plots/{plotId}/agronomic-reports/certification}, `HarvestSettlement` `CommandService` compila el expediente colegiado, estampa la firma con `OpenPdfAgronomicDossierAdapter`, genera el hash SHA-256 inmutable y emite `AgronomicDossierGeneratedEvent`.
6. Ante `GET` \nolinkurl{/api/v1/plots/{plotId}/agronomic-reports} con cabecera `Accept: application/pdf`, `AgronomicReportQueryService` consulta `AgronomicReportRepository` y delega en `OpenPdfAgronomicDossierAdapter` transmitiendo el binario inmutable del informe oficial en streaming directo con hash SHA-256.

A continuación, en la \autoref{fig:c4-component-settlement} se esquematiza el diagrama de componentes del Bounded Context Harvest Settlement and Performance Reporting:

\begin{figure}[H]
\caption{C4 Model - Component Level: Diagrama de Componentes de Harvest Settlement and Performance Reporting.} \label{fig:c4-component-settlement}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/tactical-diagrams/component-diagram-settlement.png}
\caption*{\textit{Nota.} Descomposición de componentes de la arquitectura del Bounded Context Harvest Settlement. Elaboración propia.}
\end{figure}

#### Bounded Context Software Architecture Code Level Diagrams 
&nbsp;

A continuación se presentan los diagramas de clases UML (ver \autoref{fig:class-diagram-settlement}) y de diseño de base de datos relacional (ver \autoref{fig:database-diagram-settlement}) para el Bounded Context Harvest Settlement and Performance Reporting:

##### Bounded Context Domain Layer Class Diagrams
&nbsp;

\begin{figure}[H]
\caption{Diagrama de Clases UML: Domain Layer de Harvest Settlement and Performance Reporting.} \label{fig:class-diagram-settlement}
\vspace{0.25cm}
\centering
\includegraphics[width=0.90\textwidth]{report/assets/tactical-diagrams/class-diagram-settlement.png}
\caption*{\textit{Nota.} Estructura estática de clases, liquidaciones y servicio de estabilización en Harvest Settlement. Elaboración propia.}
\end{figure}

##### Bounded Context Database Design Diagram 
&nbsp;

\begin{figure}[H]
\caption{Modelo Relacional Físico: Esquema de Base de Datos de Harvest Settlement.} \label{fig:database-diagram-settlement}
\vspace{0.25cm}
\centering
\includegraphics[width=0.65\textwidth]{report/assets/tactical-diagrams/database-diagram-settlement.png}
\caption*{\textit{Nota.} Tablas de reporte agronómico y liquidaciones anuales en PostgreSQL. Elaboración propia.}
\end{figure}

\clearpage