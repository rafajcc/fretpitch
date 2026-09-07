# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# The previous rule was too broad and made minification ineffective:
# -keep class com.fretpitch.** { *; }

# Keep Hilt and Inject related classes (Hilt usually provides its own, but these are safer)
-keepattributes *Annotation*
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room related rules (Room usually provides its own, but these help with some edge cases)
-keep class * extends androidx.room.RoomDatabase
-keep class com.fretpitch.data.source.local.db.entity.** { *; }
-keep class com.fretpitch.data.source.local.db.dao.** { *; }

# Protect Domain Models and Enums from obfuscation
# Essential for DataStore persistence using enum names (valueOf)
-keep class com.fretpitch.domain.model.** { *; }

# Compose related rules are usually bundled in the libraries.
