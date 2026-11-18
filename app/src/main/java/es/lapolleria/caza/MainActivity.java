package es.lapolleria.caza;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
  private static final int SAVE_BACKUP = 4101;
  private static final int OPEN_BACKUP = 4102;
  private WebView webView;
  private String pendingBackup;

  public class BackupBridge {
    @JavascriptInterface public void exportBackup(String json) {
      runOnUiThread(() -> {
        pendingBackup = json;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "caza-polleria-copia-" + new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(new java.util.Date()) + ".json");
        startActivityForResult(intent, SAVE_BACKUP);
      });
    }

    @JavascriptInterface public void importBackup() {
      runOnUiThread(() -> {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_BACKUP);
      });
    }
  }

  private void notifyWeb(String method, String argument) {
    String call = "window." + method + "(" + JSONObject.quote(argument) + ")";
    webView.evaluateJavascript(call, null);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
    try {
      if (requestCode == SAVE_BACKUP && pendingBackup != null) {
        try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
          if (out == null) throw new IllegalStateException("No se pudo abrir el archivo.");
          out.write(pendingBackup.getBytes(StandardCharsets.UTF_8));
        }
        pendingBackup = null;
        webView.evaluateJavascript("window.cazaBackupSaved()", null);
      } else if (requestCode == OPEN_BACKUP) {
        try (InputStream in = getContentResolver().openInputStream(data.getData()); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          if (in == null) throw new IllegalStateException("No se pudo abrir el archivo.");
          byte[] buffer = new byte[8192]; int count;
          while ((count = in.read(buffer)) != -1) {
            if (out.size() + count > 10 * 1024 * 1024) throw new IllegalArgumentException("El archivo supera 10 MB.");
            out.write(buffer, 0, count);
          }
          notifyWeb("cazaReceiveBackup", out.toString("UTF-8"));
        }
      }
    } catch (Exception e) {
      notifyWeb("cazaBackupError", e.getMessage() == null ? "Error de lectura o escritura" : e.getMessage());
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    webView = new WebView(this);
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(true);
    settings.setAllowContentAccess(false);
    settings.setSupportZoom(false);
    webView.addJavascriptInterface(new BackupBridge(), "CazaBackup");
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
        String scheme = uri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
          startActivity(new Intent(Intent.ACTION_VIEW, uri));
          return true;
        }
        return false;
      }
    });
    webView.loadUrl("file:///android_asset/index.html");
    setContentView(webView);
  }

  @Override
  public void onBackPressed() {
    if (webView.canGoBack()) webView.goBack();
    else super.onBackPressed();
  }
}
