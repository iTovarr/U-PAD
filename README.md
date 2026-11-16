# U-Pad 📱✨

[![Android CI](https://github.com/iTovarr/U-PAD/actions/workflows/android.yml/badge.svg)](https://github.com/iTovarr/U-PAD/actions/workflows/android.yml)
### Apoyo Visual, Rutinas Inteligentes y Seguimiento en Tiempo Real para Niños con TEA

U-Pad es una aplicación Android nativa diseñada para mejorar la autonomía diaria de niños con Trastorno del Espectro Autista (TEA) mediante agendas visuales basadas en pictogramas. Facilita la tranquilidad de los padres y tutores al permitirles supervisar en tiempo real el progreso de las rutinas y realizar un rastreo GPS satelital de la ubicación del menor. Asimismo, integra un asistente inteligente de IA que sugiere actividades personalizadas adaptadas a las necesidades específicas de cada niño.

---

## 🛠️ Stack Tecnológico

El proyecto está desarrollado nativamente para la plataforma Android utilizando las siguientes tecnologías:

* **Lenguaje principal**: Kotlin
* **Diseño y UI**: Jetpack Compose (Material Design 3) con soporte para temas dinámicos y persistencia de modo oscuro
* **Consumo de Servicios REST (Pictogramas)**: Retrofit (para búsqueda y consulta de imágenes desde la API oficial de ARASAAC)
* **Cliente HTTP (IA)**: OkHttp (para llamadas asíncronas estructuradas)
* **Persistencia y Backend**: Firebase Firestore y Firebase Storage
* **Autenticación**: Firebase Authentication con soporte de Google Sign-In integrado
* **Servicio de Ubicación y Mapas**: Google Maps SDK, Fused Location Provider Client
* **Persistencia Local**: Preferences DataStore (gestión de idioma y suscripción)

---

## 🤖 Componente de IA y Funcionamiento

U-Pad integra de forma segura la API de **Groq AI** mediante el uso del modelo de procesamiento de lenguaje natural `llama-3.3-70b-versatile`. 

### ¿Cómo funciona en la aplicación?
1. **Instrucciones de Contexto (System Prompt)**: Se entrena conceptualmente al modelo como un psicopedagogo experto en autismo, indicándole que retorne exactamente 3 sugerencias cortas (máximo 4 palabras cada una) por línea para garantizar la comprensión del menor.
2. **Petición del Tutor**: Desde la vista de creación de rutinas, al hacer clic en "Sugerir con IA", el ViewModel invoca de forma asíncrona al `AiRepository.kt` para consultar el modelo con el turno de la rutina correspondiente.
3. **Manejo de Errores Reactivo**: El sistema monitoriza los estados de red a través de flujos observables (`isAiLoading` y `aiError`). Si la API de Groq falla o hay problemas de conexión, la aplicación interrumpe la carga de inmediato y despliega un `AlertDialog` informativo al tutor, previniendo pantallas congeladas.

---

## 📸 Evidencias Visuales (Capturas de Pantalla)

*[Las siguientes capturas de pantalla serán insertadas en los correspondientes marcadores de posición]*

* `Pantalla de Bienvenida de U-Pad (WelcomeScreen) y Selector de Roles`
<img width="723" height="1600" alt="WhatsApp Image 2026-06-28 at 9 01 38 AM (3)" src="https://github.com/user-attachments/assets/65c0da67-eafd-4e48-b7d1-ace815470a0e" />
<img width="723" height="1600" alt="WhatsApp Image 2026-06-28 at 9 01 38 AM (2)" src="https://github.com/user-attachments/assets/3fa0f043-6db0-46b9-b2bf-2c9b6cc5b1f3" />
<img width="720" height="1600" alt="WhatsApp Image 2026-06-28 at 9 01 53 AM" src="https://github.com/user-attachments/assets/01a805b9-005a-4c58-aab0-24ef8d4136c4" />

* `Panel de Edición de Rutinas`
<img width="723" height="1600" alt="WhatsApp Image 2026-06-28 at 9 01 38 AM (1)" src="https://github.com/user-attachments/assets/b71ea66b-c720-4832-9861-230a049573dc" />

* `Mapa de Seguimiento GPS Activo del Niño`
<img width="723" height="1600" alt="WhatsApp Image 2026-06-28 at 9 01 38 AM" src="https://github.com/user-attachments/assets/a317f8bc-bc8a-42cc-be33-b7cdf2ba1f85" />

---

## Video Demostrativo
* **Demostración Práctica**: [Video Demostrativo del funcionamiento y flujo de U-Pad](https://youtu.be/fVQU3RGt8KI)

---

## 👥 Integrantes del Proyecto

* Mamani Puma, Anyelina Yolit
* Mansilla Tovar, Maria Fernanda
* Ricasca Montes, Angelo Joseph
* Vargas Jucharo, Angelo Rodrigo

---

## 🔒 Configuración e Instrucciones de Credenciales

Para compilar y desplegar el proyecto con éxito, se requieren las siguientes API Keys y configuraciones de servicios:

1. **Archivo `local.properties`**: créalo en la raíz del proyecto a partir de `local.properties.example` (está ignorado por git) con las llaves que `app/build.gradle.kts` inyecta en el build:
   ```properties
   GOOGLE_CLIENT_ID=tu_client_id_de_google_sign_in_aqui
   MAPS_API_KEY=tu_clave_del_sdk_de_google_maps_aqui

   # Opción recomendada: proxy de IA (la clave de Groq se queda en el servidor)
   AI_PROXY_URL=https://us-central1-TU_PROYECTO.cloudfunctions.net/aiChat

   # Solo para desarrollo, si aún no desplegaste el proxy. Esta clave queda
   # dentro del APK y puede extraerse: no la uses en builds que vayas a compartir.
   GROQ_API_KEY=tu_clave_de_api_de_groq_aqui
   ```
   Si `AI_PROXY_URL` está definida, la app usa el proxy y `GROQ_API_KEY` debe dejarse vacía.
2. **Archivo `google-services.json`**: descárgalo desde Firebase Console y ubícalo en la carpeta `/app` para conectar Firestore, Storage y Auth.

### Reglas de seguridad y proxy de IA (Firebase)

El repositorio incluye las reglas de Firestore (`firestore.rules`) y Storage (`storage.rules`), y la Cloud Function `aiChat` (`functions/`) que reenvía las peticiones de IA a Groq sin exponer la clave. Se despliegan con [Firebase CLI](https://firebase.google.com/docs/cli):

```bash
firebase use TU_PROYECTO
firebase deploy --only firestore:rules,storage

# Proxy de IA (requiere el plan Blaze)
cd functions && npm install && cd ..
firebase functions:secrets:set GROQ_API_KEY
firebase deploy --only functions
```

> Las reglas actuales documentan una limitación: el dispositivo del niño todavía no inicia sesión en Firebase, por lo que sus documentos se protegen prohibiendo listados y validando lo que se escribe. Ver los comentarios de `firestore.rules`.

---

### Calidad: tests, lint y CI

```bash
./gradlew testDebugUnitTest lintDebug
```

Cada push a `main` y cada pull request ejecuta en GitHub Actions los tests unitarios, Android Lint y la compilación del APK de debug (`.github/workflows/android.yml`).

### Build de release

```bash
./gradlew assembleRelease
```

El build de release usa R8 (código reducido y ofuscado). Para que salga firmado, copia `keystore.properties.example` como `keystore.properties` y completa los datos de tu keystore; sin ese archivo se genera `app-release-unsigned.apk`.

---

## 💻 Instrucciones de Ejecución

1. Clona el repositorio e introduce las credenciales detalladas en el paso anterior.
2. Abre el proyecto en **Android Studio**.
3. Sincroniza los archivos de Gradle.
4. Despliega la aplicación en un dispositivo emulador o físico con **API level 24 (Android 7.0) o superior**.
