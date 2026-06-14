# ── Debugging (Stack Traces lesbar halten) ────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ── Kotlin Coroutines ─────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ── Room ──────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.**

# Deine konkreten Room-Klassen
-keep class com.lifecost.app.data.db.** { *; }

# ── Koin ──────────────────────────────────────────────────
-keepnames class io.insert.koin.** { *; }
-dontwarn io.insert.koin.**
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ── Domain & Data Models (Room + Gson dürfen nicht umbenennen) ─
-keep class com.lifecost.app.domain.model.** { *; }
-keep class com.lifecost.app.data.repository.** { *; }

# ── Google Play Billing ───────────────────────────────────
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ── Firebase ──────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ── DataStore ─────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ── Compose ───────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ── ViewModels ────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# ── Enums (ExpenseCategory etc.) ──────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}