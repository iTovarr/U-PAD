# Guía de contribución

## Preparar el entorno

1. Copia `local.properties.example` como `local.properties` y completa las claves.
2. Coloca tu `google-services.json` en `app/`.
3. Abre el proyecto en Android Studio y sincroniza Gradle.

Ninguno de esos dos archivos se sube al repositorio. Los detalles están en el [README](README.md).

## Antes de subir cambios

Ejecuta lo mismo que la integración continua:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Los tres pasos deben terminar sin errores. Si tocas la lógica pura (`utils/`, mapeadores, ViewModels con dobles), añade o actualiza sus tests en `app/src/test`.

## Commits

Un commit por cada cambio pequeño y coherente, con el formato de [Conventional Commits](https://www.conventionalcommits.org/es/):

```
tipo(ámbito): resumen en imperativo y en minúsculas

Cuerpo opcional que explica qué se cambió y, sobre todo, por qué.
```

Tipos habituales: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`, `perf`, `style`.

Ejemplos del historial:

```
fix(pairing): rechazar códigos de vinculación usados o expirados
refactor(data): usar TaskItemMapper en FirebaseRepository
feat(i18n): traducir los textos fijos de LoginScreen
```

## Convenciones del código

- **Firebase solo en `data/`.** Las pantallas y los ViewModels usan repositorios; no llaman a `FirebaseAuth` ni a `FirebaseFirestore`.
- **Dependencias por constructor.** Los ViewModels reciben sus repositorios desde `AppContainer` a través de `AppViewModelFactory`.
- **Rutas en `Routes`.** No se escriben rutas de navegación como texto fuera de ese archivo.
- **Textos en `strings.xml`**, en los seis idiomas (`values`, `values-en`, `values-fr`, `values-de`, `values-pt`, `values-ru`).
- **Colores del tema.** Antes de escribir un color a mano, comprueba si ya existe en `ui/theme/`.
- **Errores con `Log.e`** y una etiqueta por archivo, no con `printStackTrace()`.

La estructura general está explicada en [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).
