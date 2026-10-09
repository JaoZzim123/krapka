package com.jaozzim123.krapka;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ponte usada pelo app web para salvar as exportações no aparelho.
 * PNG vai para Imagens/Krapka (aparece na galeria); ZIP de camadas vai para Downloads/Krapka.
 * O arquivo chega em pedaços (base64) para não estourar a memória com imagens grandes.
 */
public class NativeBridge {
    private static class Job {
        Uri uri;
        OutputStream out;
        String where;
    }

    private final Activity act;
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger();

    NativeBridge(Activity act) {
        this.act = act;
    }

    @JavascriptInterface
    public String begin(String name, String mime) {
        try {
            String safe = name == null ? "krapka" : name.replaceAll("[\\\\/:*?\"<>|]", "-");
            boolean image = mime != null && mime.startsWith("image/");
            ContentValues v = new ContentValues();
            v.put(MediaStore.MediaColumns.DISPLAY_NAME, safe);
            v.put(MediaStore.MediaColumns.MIME_TYPE, mime == null ? "application/octet-stream" : mime);
            v.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    (image ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DOWNLOADS) + "/Krapka");
            v.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri coll = image
                    ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    : MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            ContentResolver cr = act.getContentResolver();
            Job j = new Job();
            j.uri = cr.insert(coll, v);
            if (j.uri == null) return "";
            j.out = cr.openOutputStream(j.uri, "w");
            j.where = image ? "Imagens/Krapka" : "Downloads/Krapka";
            String id = "j" + seq.incrementAndGet();
            jobs.put(id, j);
            return id;
        } catch (Exception e) {
            return "";
        }
    }

    @JavascriptInterface
    public String append(String id, String b64) {
        Job j = jobs.get(id);
        if (j == null) return "err:job";
        try {
            j.out.write(Base64.decode(b64, Base64.DEFAULT));
            return "ok";
        } catch (Exception e) {
            abort(id, j);
            return "err:" + e.getMessage();
        }
    }

    @JavascriptInterface
    public String finish(String id) {
        Job j = jobs.remove(id);
        if (j == null) return "err:job";
        try {
            j.out.close();
            ContentValues v = new ContentValues();
            v.put(MediaStore.MediaColumns.IS_PENDING, 0);
            act.getContentResolver().update(j.uri, v, null, null);
            final String msg = "Salvo em " + j.where;
            act.runOnUiThread(() -> Toast.makeText(act, msg, Toast.LENGTH_SHORT).show());
            return "ok:" + j.where;
        } catch (Exception e) {
            abort(null, j);
            return "err:" + e.getMessage();
        }
    }

    private void abort(String id, Job j) {
        if (id != null) jobs.remove(id);
        try { j.out.close(); } catch (Exception ignored) { }
        try { act.getContentResolver().delete(j.uri, null, null); } catch (Exception ignored) { }
    }
}
