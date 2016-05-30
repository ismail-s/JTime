-keepattributes InnerClasses,EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature

-keep class com.mikepenz.materialize.view.OnInsetsCallback { *; }
-keep class com.google.common.io.LineProcessor { *; }
-keep class com.google.android.gms.dynamic.zzd { *; }

# Once the next version of Fuel after v1.3.1 is released, this line
# can be removed.
-keep class com.github.kittinunf.fuel.android.util.AndroidEnvironment
