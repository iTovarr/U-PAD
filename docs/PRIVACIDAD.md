# Política de privacidad de U-Pad (borrador)

> **Este documento es un borrador técnico, no un texto legal revisado.** Describe con precisión qué datos trata la app según su código, para que sirva de base a una política definitiva. Antes de publicar la app debe revisarlo una persona con conocimientos legales y completarse los datos marcados como *[pendiente]*.

Última actualización del borrador: noviembre de 2026.

## 1. Quién es responsable

- Responsable del tratamiento: *[pendiente: nombre de la persona u organización]*
- Contacto para consultas y solicitudes: *[pendiente: correo electrónico]*

U-Pad es un proyecto académico desarrollado en Tecsup. Mientras no exista una entidad responsable identificada, la app no debería ofrecerse al público general.

## 2. A quién va dirigida

U-Pad la usan dos personas:

- **El tutor** (padre, madre o persona a cargo), mayor de edad, que crea la cuenta y configura la app.
- **El niño o niña**, que usa su dispositivo sin crear cuenta ni introducir datos personales.

Todos los datos del menor los introduce o autoriza el tutor. Al tratarse de datos de un menor de edad, y potencialmente de datos relativos a su salud o desarrollo, el tutor debe dar su consentimiento expreso antes de usar la app. *[pendiente: la app todavía no muestra una pantalla de consentimiento.]*

## 3. Qué datos se tratan

| Dato | Quién lo aporta | Para qué se usa |
|---|---|---|
| Correo electrónico y nombre del tutor | Tutor, al registrarse | Crear la cuenta e identificar al tutor. |
| Foto de perfil del tutor | Cuenta de Google, si inicia sesión con Google | Mostrarla en el panel. |
| Nombre, edad e intereses del niño | Tutor | Personalizar la experiencia. |
| Foto del niño | Tutor (opcional) | Mostrarla en el perfil. |
| Rutinas: actividades, horarios y pictogramas | Tutor | Mostrar la agenda visual al niño. |
| Estado de cada actividad (completada o no) | Dispositivo del niño | Mostrar el progreso al tutor. |
| Emoción registrada tras cada actividad | Niño | Bitácora de emociones y análisis para el tutor. |
| Ubicación del dispositivo del niño | Dispositivo del niño (solo plan premium y con permiso) | Mostrar al tutor dónde está el dispositivo. |
| Identificador del dispositivo (ANDROID_ID) y modelo | Dispositivo del niño | Vincular el dispositivo con el tutor. |
| Token de notificaciones push | Dispositivo del tutor | Enviarle avisos cuando el niño completa una actividad. |
| Idioma, tema y plan elegidos | Tutor | Recordar sus preferencias. |

La app **no** recoge contactos, mensajes, historial de navegación ni datos de otras aplicaciones, y no muestra publicidad.

### Sobre la ubicación

La ubicación solo se envía cuando se cumplen tres condiciones: el dispositivo del niño está vinculado, el tutor tiene el plan premium y se ha concedido el permiso de ubicación. Mientras se comparte, el dispositivo muestra una notificación permanente que lo indica. Se guarda únicamente la última posición conocida, no un historial de recorridos.

## 4. Dónde se guardan y quién más los recibe

Los datos se almacenan en **Firebase**, de Google (Authentication, Cloud Firestore, Cloud Storage y Cloud Messaging). *[pendiente: indicar la región del proyecto de Firebase.]*

Además, la app envía datos a estos servicios:

| Servicio | Qué recibe | Cuándo |
|---|---|---|
| **Groq** (modelo de lenguaje) | El texto que el tutor escribe al pedir una rutina a medida, y un resumen del día con los nombres de las actividades, su estado y la emoción registrada. No se envían el nombre del niño, su foto ni su ubicación. | Solo cuando el tutor pulsa una función de IA. |
| **ARASAAC** (Gobierno de Aragón) | Las palabras que el tutor busca y las solicitudes de imagen de los pictogramas elegidos. | Al buscar o mostrar pictogramas. |
| **Google Maps** | La posición que se muestra en el mapa. | Al abrir el mapa de seguimiento. |

No se venden datos ni se ceden a terceros con fines comerciales.

## 5. Cuánto tiempo se conservan

Los datos se conservan mientras exista la cuenta del tutor. *[pendiente: la app no ofrece todavía una opción para borrar la cuenta y sus datos; hasta que exista, las solicitudes de borrado deben atenderse manualmente desde la consola de Firebase.]*

Al desvincular un dispositivo se borra su documento, incluida su última ubicación.

## 6. Seguridad

- La comunicación con todos los servicios usa HTTPS.
- La app desactiva las copias de seguridad del sistema para que los datos locales no se copien a la nube ni se transfieran a otro dispositivo.
- El acceso a los datos del tutor en Firestore exige haber iniciado sesión como ese tutor.
- **Limitación actual:** el dispositivo del niño no inicia sesión, por lo que las rutinas y la ubicación se protegen impidiendo listar documentos y exigiendo conocer su identificador, no mediante una identidad verificada. Está documentado como deuda técnica en `docs/ARQUITECTURA.md`.

## 7. Derechos del tutor

El tutor puede solicitar el acceso, la rectificación y la supresión de sus datos y de los del menor a su cargo, así como oponerse a su tratamiento, escribiendo al contacto indicado en el apartado 1.

*[pendiente: adaptar este apartado a la normativa aplicable. Para una app ofrecida en Perú es la Ley N.° 29733, Ley de Protección de Datos Personales, y su reglamento; si se ofrece en otros países, puede aplicar también su normativa sobre datos de menores.]*

## 8. Cambios en esta política

Cualquier cambio se publicará en este mismo documento, indicando la fecha de actualización.
