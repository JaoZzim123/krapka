package com.jaozzim123.krapka;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.io.InputStream;

/**
 * Krapka como app nativo: o app web inteiro vai dentro do APK (assets/www) e é servido
 * localmente em https://appassets.androidplatform.net, sem depender de nenhum site.
 */
public class MainActivity extends Activity {
    private static final String HOST = "appassets.androidplatform.net";
    private static final String START = "https://" + HOST + "/assets/www/index.html";
    private static final int PICK = 41;

    private WebView web;
    private ValueCallback<Uri[]> pending;
    private boolean pageReady;
    private Uri sharedUri;
    private int sharedSeq;
    private boolean sharedNew;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF15120F);
        web = new WebView(this);
        web.setBackgroundColor(0xFF15120F);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        // Android 15 desenha atrás das barras do sistema: afasta o conteúdo delas.
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets b = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                v.setPadding(b.left, b.top, b.right, b.bottom);
                return WindowInsets.CONSUMED;
            });
        }

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setTextZoom(100);

        final WebViewAssetLoader assets = new WebViewAssetLoader.Builder()
                .setDomain(HOST)
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .addPathHandler("/shared/", path -> openShared())
                .build();

        web.addJavascriptInterface(new NativeBridge(this), "KrapkaNative");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                return assets.shouldInterceptRequest(req.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (HOST.equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (ActivityNotFoundException ignored) { }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = url != null && url.endsWith("/index.html");
                deliverShared();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (pending != null) pending.onReceiveValue(null);
                pending = cb;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Escolher imagem"), PICK);
                } catch (ActivityNotFoundException e) {
                    pending = null;
                    return false;
                }
                return true;
            }
        });

        if (saved != null) web.restoreState(saved);
        else web.loadUrl(START);
        takeShared(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        takeShared(intent);
    }

    /** Foto aberta ou compartilhada com o Krapka a partir da galeria ou de outro app. */
    private void takeShared(Intent it) {
        if (it == null) return;
        Uri u = null;
        if (Intent.ACTION_SEND.equals(it.getAction())) {
            u = Build.VERSION.SDK_INT >= 33 ? it.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class)
                                            : it.getParcelableExtra(Intent.EXTRA_STREAM);
        } else if (Intent.ACTION_VIEW.equals(it.getAction())) {
            u = it.getData();
        }
        if (u == null) return;
        sharedUri = u;
        sharedSeq++;
        sharedNew = true;
        deliverShared();
    }

    private void deliverShared() {
        if (!pageReady || !sharedNew || sharedUri == null) return;
        sharedNew = false;
        String name = displayName(sharedUri);
        String js = "window.krapkaOpenURL&&krapkaOpenURL('/shared/" + sharedSeq + "'," + JSONObject.quote(name) + ")";
        web.evaluateJavascript(js, null);
    }

    private WebResourceResponse openShared() {
        Uri u = sharedUri;
        if (u == null) return null;
        try {
            String mime = getContentResolver().getType(u);
            InputStream in = getContentResolver().openInputStream(u);
            return new WebResourceResponse(mime != null ? mime : "image/jpeg", null, in);
        } catch (Exception e) {
            return null;
        }
    }

    private String displayName(Uri u) {
        try (Cursor c = getContentResolver().query(u, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                String n = c.getString(0);
                if (n != null) return n.replaceFirst("\\.[A-Za-z0-9]+$", "");
            }
        } catch (Exception ignored) { }
        return "imagem";
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != PICK || pending == null) return;
        Uri[] out = null;
        if (res == RESULT_OK && data != null && data.getData() != null) out = new Uri[]{data.getData()};
        pending.onReceiveValue(out);
        pending = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.removeJavascriptInterface("KrapkaNative");
            web.destroy();
        }
        super.onDestroy();
    }
}
