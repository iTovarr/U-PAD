# Arquitectura de U-Pad

Este documento describe cómo está organizado el código, qué datos guarda la app y cómo se comunican el dispositivo del tutor y el del niño. Está pensado para quien llega nuevo al proyecto.

## Visión general

U-Pad es una app Android de un solo módulo (`:app`) escrita en Kotlin con Jetpack Compose. La misma app sirve para dos roles, que se eligen al abrirla:

- **Tutor**: inicia sesión, crea las rutinas, vincula el dispositivo del niño y sigue su progreso.
- **Niño**: no inicia sesión. Su dispositivo se vincula al tutor con un código de 6 dígitos y muestra las rutinas del día.

Todo el estado compartido vive en Firebase (Firestore, Storage y Authentication). No hay servidor propio, salvo dos Cloud Functions opcionales.

```
┌──────────────────────────────────────────────────────────────┐
│ Interfaz (Jetpack Compose)                                   │
│ auth/ setup/ dashboard/ routines/ child/ premium/ components/│
└───────────────┬──────────────────────────────────────────────┘
                │ estado (StateFlow) y acciones
┌───────────────▼──────────────────────────────────────────────┐
│ ViewModels                                                   │
│ RoutineViewModel · AiViewModel · SettingsViewModel ·         │
│ TrackingViewModel                                            │
└───────────────┬──────────────────────────────────────────────┘
                │ funciones suspend y Flow
┌───────────────▼──────────────────────────────────────────────┐
│ Datos (data/)                                                │
│ SessionRepository · FirebaseRepository · DeviceRepository ·  │
│ ParentProfileRepository · ArasaacRepository · AiRepository · │
│ DataStoreManager · LanguageDataStore · ThemePreferences      │
└───────────────┬──────────────────────────────────────────────┘
                │
   Firebase (Auth, Firestore, Storage, FCM) · ARASAAC · Groq
```

La regla que sostiene esta estructura: **solo el paquete `data/` (y los widgets) usa el SDK de Firebase**. Las pantallas y los ViewModels hablan con repositorios.

## Paquetes

| Paquete | Contenido |
|---|---|
| `auth/` | Selección de rol, bienvenida, inicio de sesión, registro y recuperación de contraseña. |
| `setup/` | Configuración inicial del tutor: plan, perfil del hijo, perfil sensorial, vinculación y tutoriales. |
| `dashboard/` | Panel del tutor: analítica, calendario, emociones, avisos, perfil, ajustes, dispositivos y mapa. |
| `routines/` | Panel de rutinas, creación de rutinas y buscador de pictogramas. |
| `child/` | Modo niño: inicio, ejecución de la tarea, registro de emoción y pantalla de logro, con su propio tema (`ChildTheme`), voz y sonido (`ChildAudio`) según el perfil sensorial. |
| `premium/` | Pantalla de pago (simulado). |
| `components/` | Piezas de interfaz compartidas: barras, cajón lateral, fondo y aviso de conexión. |
| `viewmodel/` | ViewModels y la fábrica que los crea (`AppViewModelFactory`). |
| `data/` | Repositorios, almacenes de preferencias y modelos. |
| `di/` | `AppContainer` (contenedor de dependencias) y `LocalAppContainer` (acceso desde Compose). |
| `navigation/` | `Routes` (todas las rutas) y las entradas de la barra inferior. |
| `notifications/` | Notificaciones locales, token push y servicio de Firebase Messaging. |
| `location/` | Servicio en primer plano que envía la ubicación del dispositivo del niño. |
| `kiosk/` | Modo kiosco (fijación de la app en pantalla). |
| `widget/` | Tres widgets de escritorio: progreso del día, progreso premium y sesión del niño. |
| `utils/` | Lógica pura y utilidades: cálculo de progreso, validadores, código de vinculación, huella, idioma y red. |
| `ui/theme/` | Tema de Material 3: paletas Calma y Tierra en claro y oscuro (`Palettes.kt`), colores extra del tema (`MaterialTheme.extendedColors`) y tipografía Atkinson Hyperlegible (`Type.kt`). Ninguna pantalla define colores propios. |

## Inyección de dependencias

No se usa Hilt ni Koin. `UPadApplication` crea un `AppContainer` con una instancia de cada repositorio y servicio:

- Los **ViewModels** las reciben por constructor a través de `AppViewModelFactory`.
- Las **pantallas** que necesitan un repositorio lo obtienen con `LocalAppContainer.current`.
- Los **servicios** de Android lo obtienen con `(application as UPadApplication).container`.

Los cuatro ViewModels principales se crean en `MainActivity` con ámbito de Activity y se pasan a las pantallas por parámetro, de modo que su estado se conserva al navegar.

## Navegación

Una sola Activity (`MainActivity`) con un `NavHost` de Navigation Compose. Todas las rutas y sus argumentos están en `navigation/Routes.kt`; las rutas con parámetros se construyen con funciones (`Routes.createRoutine(turno, dia)`) que codifican cada argumento.

Las rutas del modo niño empiezan todas por `child_`. `MainActivity` usa ese prefijo para decidir cuándo aplicar el modo kiosco y cuándo ocultar el aviso de conexión.

## Modelo de datos en Firestore

```
users/{uid}                         Perfil del tutor
  name, isPremium, language, fcmTokens[]
  perfilSensorial                   paleta, tema, intensidadColor, animaciones, sonido,
                                    voz, volumen, temporizador, celebracion
  notifications/{id}                Avisos que deja el dispositivo del niño
    title, description, type, timestamp

usuarios/{uid}                      Plan y datos de configuración del tutor
  planSuscripcion, estadoSuscripcion, fechaSeleccionPlan
  hijos/perfil                      nombre, edad, intereses, fotoUrl, fechaRegistro

routines/{uid}/turns/{turno}        turno: MAÑANA | TARDE | NOCHE
  tasks[], mensajesRefuerzo[]
  days/{dia}                        dia: LUNES … DOMINGO
    tasks[]                         Lista de tareas de ese turno y día

dispositivos_niños/{ANDROID_ID}     Dispositivo del niño
  padreId, modelo, latitud, longitud, ultimaActualizacion, kioscoActivo

codigos_vinculacion/{codigo}        Código de 6 dígitos, válido 15 minutos
  deviceId, estado, padreId, creadoEn
```

Cada tarea (`TaskItem`) tiene `actividad`, `palabraClave`, `imageUrl`, `dias`, `duration`, `estadosPorDia` y `emocionesPorDia`. Los dos últimos son mapas cuya clave es el prefijo del día (`LUN`, `MAR`…).

En Storage se guardan los pictogramas propios (`pictograms/{uid}/…`) y la foto del hijo (`usuarios/{uid}/perfil_hijo.jpg`).

Las reglas de acceso están en `firestore.rules` y `storage.rules`.

## Flujos principales

### Vinculación de dispositivos

1. El dispositivo del niño reserva un código libre en `codigos_vinculacion` (transacción en `FirebaseRepository.createUniquePairingCode`) y lo muestra en pantalla.
2. El tutor introduce el código. `DeviceRepository.redeemPairingCode` comprueba que siga en espera y no haya expirado, y lo marca como `enlazado` con su UID.
3. El dispositivo del niño, que escucha ese documento, escribe `padreId` en `dispositivos_niños/{ANDROID_ID}` y borra el código.
4. A partir de ahí el dispositivo del niño lee las rutinas de `routines/{padreId}`.

### Una tarea completada

1. El niño termina la tarea y elige una emoción.
2. `RoutineViewModel` actualiza `estadosPorDia` y `emocionesPorDia` de la tarea y guarda la lista del turno.
3. Deja un aviso en `users/{padreId}/notifications`.
4. El tutor lo recibe por dos caminos: el listener de `MainActivity` si tiene la app abierta, y una notificación push enviada por la Cloud Function `notifyParent` si la tiene cerrada. Ambos usan el ID del aviso como etiqueta para que no se muestre dos veces.

### Rastreo de ubicación (premium)

`ChildStartScreen` inicia `LocationTrackingService` cuando el dispositivo está vinculado, el tutor es premium y hay permiso de ubicación. El servicio escribe `latitud` y `longitud` en el documento del dispositivo; el mapa del tutor (`HijoTrackingScreen`) los observa a través de `TrackingViewModel`.

### Inteligencia artificial

`AiViewModel` llama a `AiService`. Su implementación, `AiRepository`, usa la Cloud Function `aiChat` si `AI_PROXY_URL` está configurada; si no, llama a Groq directamente con la clave del build (solo para desarrollo). Las respuestas se piden en el idioma de la app.

## Servicios en la nube

| Pieza | Para qué sirve | Requisito |
|---|---|---|
| `firestore.rules`, `storage.rules` | Control de acceso a los datos. | `firebase deploy --only firestore:rules,storage` |
| `functions/aiChat` | Proxy de IA: la clave de Groq no viaja en el APK. | Plan Blaze |
| `functions/notifyParent` | Notificación push al tutor por cada aviso. | Plan Blaze |

## Pruebas y calidad

- Los tests unitarios (`app/src/test`) cubren la lógica pura: cálculo de progreso, mapeo de tareas, validadores, código de vinculación, parseo de la IA y `AiViewModel` con un doble de `AiService`.
- Android Lint se ejecuta sin errores; `app/lint.xml` documenta la única excepción.
- GitHub Actions (`.github/workflows/android.yml`) ejecuta tests, lint y build en cada push y pull request.

## Deuda técnica conocida

- **El dispositivo del niño no inicia sesión.** Las reglas de Firestore no pueden atribuirle sus escrituras; se protegen prohibiendo listados y validando lo que se escribe. La solución es un inicio de sesión anónimo en el modo niño.
- **Las tareas se guardan como una lista dentro de un documento** y se identifican por nombre. Dos escrituras simultáneas (tutor y niño) pueden pisarse. Lo adecuado sería un documento por tarea con ID propio.
- **Dos colecciones de usuario** (`users` y `usuarios`) con datos repartidos.
- **`RoutineViewModel` sigue siendo grande** (rutinas más vinculación del niño) y varias pantallas contienen lógica que debería estar en un ViewModel propio.
- **Los widgets consultan Firestore directamente** en lugar de usar los repositorios.
- **El plan premium se activa desde la propia app** (pago simulado). Con un cobro real debe hacerlo solo el servidor.
