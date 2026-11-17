# Créditos y licencias de terceros

U-Pad se apoya en contenidos y librerías de terceros. Este documento recoge sus licencias y lo que implican para el proyecto.

El código propio de U-Pad se distribuye bajo la [licencia MIT](../LICENSE). Lo que sigue se refiere a lo que no es del proyecto y mantiene su propia licencia.

## Pictogramas de ARASAAC

Los símbolos pictográficos utilizados son propiedad del Gobierno de Aragón y han sido creados por Sergio Palao para [ARASAAC](https://arasaac.org), que los distribuye bajo Licencia Creative Commons [BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.es).

Esa licencia impone tres condiciones:

- **Reconocimiento (BY):** hay que citar la autoría. La app lo hace en el buscador de pictogramas, y este documento y el README lo recogen.
- **No comercial (NC):** los pictogramas no pueden usarse con fines comerciales.
- **Compartir igual (SA):** las obras derivadas de los pictogramas deben distribuirse con la misma licencia.

### Pendiente antes de publicar: uso comercial

U-Pad incluye un plan Premium de pago. Mientras el pago sea simulado, como ahora, no hay explotación comercial. **Si se cobra de verdad por la app o por el plan Premium, el uso de los pictogramas deja de encajar en la condición "No comercial".** Antes de dar ese paso hay que elegir una de estas vías:

1. Pedir autorización expresa a ARASAAC para el uso previsto.
2. Sustituir los pictogramas por un conjunto con licencia que permita el uso comercial.
3. Mantener la app gratuita.

## Servicios externos

| Servicio | Uso en la app | Condiciones |
|---|---|---|
| Firebase (Google) | Autenticación, base de datos, archivos y notificaciones push | [Términos de Firebase](https://firebase.google.com/terms) |
| Google Maps Platform | Mapa de seguimiento | [Términos de Google Maps Platform](https://cloud.google.com/maps-platform/terms) |
| Groq | Sugerencias y análisis con IA | [Términos de Groq](https://groq.com/terms-of-use) |
| API de ARASAAC | Búsqueda e imágenes de pictogramas | Licencia CC BY-NC-SA, ver arriba |

## Librerías de código abierto

Las librerías incluidas en la app se distribuyen bajo la licencia Apache 2.0, salvo que se indique otra cosa.

| Librería | Licencia |
|---|---|
| Kotlin y kotlinx.coroutines (JetBrains) | Apache 2.0 |
| AndroidX y Jetpack Compose (Google) | Apache 2.0 |
| Navigation Compose, DataStore, Biometric, Credentials (AndroidX) | Apache 2.0 |
| Firebase Android SDK | Apache 2.0 |
| Google Play Services (ubicación, identidad) | [Condiciones del SDK de Android](https://developer.android.com/studio/terms) |
| Maps Compose | Apache 2.0 |
| Retrofit, OkHttp y conversor Gson (Square) | Apache 2.0 |
| Coil | Apache 2.0 |
| JUnit 4 (solo tests) | Eclipse Public License 1.0 |

Las versiones exactas están en `gradle/libs.versions.toml`.
