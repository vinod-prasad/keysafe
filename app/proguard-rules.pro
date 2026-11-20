# KeySafe ProGuard & R8 Keep Rules

# Keep Kotlin Serialization DTOs
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class com.appnest.safe.credentials.vault.domain.model.** { *; }
-keepclassmembers class com.appnest.safe.credentials.vault.data.dto.** { *; }

# Keep SQLite and Database Helper
-keep class com.appnest.safe.credentials.vault.data.db.** { *; }
