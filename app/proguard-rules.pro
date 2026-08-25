# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
-keepattributes *Annotation*
-keep class com.fretpitch.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
