# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep kotlinx.serialization generated serializers (used by the DeepSeek JSON layer).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.liyuqihxc.voxledger.**$$serializer { *; }
-keepclassmembers class com.liyuqihxc.voxledger.** {
    *** Companion;
}
-keepclasseswithmembers class com.liyuqihxc.voxledger.** {
    kotlinx.serialization.KSerializer serializer(...);
}
