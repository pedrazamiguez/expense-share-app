############################################################################
# 🔥 FIREBASE / FIRESTORE
############################################################################

# Preserve annotations and signatures
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Keep Firebase SDK classes (Firestore, Auth, Messaging, etc.)
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Google Play Services (used by Firebase)
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Firestore Document models (reflection-based mapping)
-keep class es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.** {
    <fields>;
    <methods>;
    <init>();
}

############################################################################
# 🧱 DOMAIN LAYER (Models, UseCases, Repositories)
############################################################################

# Keep domain models (used by serialization, mapping, or tests)
-keep class es.pedrazamiguez.expenseshareapp.domain.model.** { *; }

# Keep use cases (for Koin reflection / constructor injection)
-keep class es.pedrazamiguez.expenseshareapp.domain.usecase.** { *; }
-keepclassmembers class es.pedrazamiguez.expenseshareapp.domain.usecase.** {
    <init>(...);
    *;
}

# Keep repositories and their methods
-keep class es.pedrazamiguez.expenseshareapp.domain.repository.** { *; }
-keepclassmembers class es.pedrazamiguez.expenseshareapp.domain.repository.** {
    <init>(...);
    *;
}

############################################################################
# 🧩 PRESENTATION LAYER (ViewModels, UI)
############################################################################

# Keep all ViewModels for Koin + Jetpack reflection
-keep class es.pedrazamiguez.expenseshareapp.ui.**.*ViewModel { *; }
-keepclassmembers class es.pedrazamiguez.expenseshareapp.ui.**.*ViewModel {
    <init>(...);
    *;
}

# Keep composables, navigation, and other UI reflection-based classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

############################################################################
# ⚙️ DEPENDENCY INJECTION / UTILITIES
############################################################################

# Koin (uses reflection for module discovery)
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Coil (image loader)
-keep class coil.** { *; }
-dontwarn coil.**

# Timber (logging)
-keep class timber.log.Timber { *; }
-dontwarn timber.log.Timber

############################################################################
# 🧠 DEBUGGING / REFLECTION SUPPORT
############################################################################

# Keep attribute information for better stack traces and reflection
-keepattributes SourceFile, LineNumberTable
-keepattributes Exceptions
