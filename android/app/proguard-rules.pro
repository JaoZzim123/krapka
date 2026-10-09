# A ponte JavaScript precisa manter os nomes dos métodos.
-keepclassmembers class com.jaozzim123.krapka.NativeBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
