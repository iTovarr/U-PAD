# Reglas de R8/ProGuard específicas de U-Pad.
# Las librerías (Firebase, Retrofit, OkHttp, Compose...) ya traen sus propias reglas.

# Conserva archivo y número de línea para poder leer los stack traces de producción.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson y Firestore leen los tipos genéricos y las anotaciones por reflexión.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# Modelos de la API de ARASAAC: Gson los rellena por nombre de campo.
-keep class com.example.upad.data.ArasaacPictogram { *; }
-keep class com.example.upad.data.Keyword { *; }

# Modelos que Firestore convierte por reflexión (set() con objetos y toObject()):
# si R8 renombra sus propiedades, los campos se guardan o leen con otro nombre.
-keep class com.example.upad.viewmodel.TaskItem { *; }
-keep class com.example.upad.viewmodel.UbicacionHijo { *; }
