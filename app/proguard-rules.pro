# --- TRANSPORT TRACKING SYSTEM PRODUCTION PROGUARD CONFIGURATION ---

# 1. Keep original stack trace details and source file attributes for accurate crash reports (e.g. Firebase Crashlytics)
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

# 2. Prevent obfuscation of all Firebase and Google Play Services classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# 3. Preserve critical model classes to ensure Firestore deserialization works perfectly via reflection
-keep class com.example.transporttrackingsystem.models.** { *; }
-keepclassmembers class com.example.transporttrackingsystem.models.** {
    public <fields>;
    public <methods>;
    public <init>(...);
}

# 4. Preserve JavaMail (com.sun.mail) packages used for secure OTP email pipelines
-keep class javax.mail.** { *; }
-keep class com.sun.mail.** { *; }
-dontwarn javax.mail.**
-dontwarn com.sun.mail.**
-dontwarn java.awt.**

# 5. Optimize Kotlin metadata and coroutines handling
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.jvm.internal.**