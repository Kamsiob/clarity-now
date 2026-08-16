# kotlinx.serialization keeps its generated serializers through reflection lookups.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.kamsiob.claritynow.** {
    *** Companion;
}
-keepclasseswithmembers class com.kamsiob.claritynow.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room generates implementations that are looked up by name.
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# Glance app widgets are instantiated by the platform from the manifest.
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
