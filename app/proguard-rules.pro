# Firebase
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep data model
-keep class es.pedrazamiguez.expenseshareapp.data.model.** { *; }

# Other dependencies
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keep class org.koin.** { *; }
-keepclassmembers class org.koin.** { *; }
-keep class coil.** { *; }
-keepclassmembers class coil.** { *; }
-dontwarn coil.**
-keep class timber.log.Timber { *; }
-keepclassmembers class timber.log.Timber { *; }
-dontwarn timber.log.Timber
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.navigation.** { *; }
-keepclassmembers class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**
