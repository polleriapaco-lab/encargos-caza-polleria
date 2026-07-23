package es.lapolleria.caza;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
  private WebView webView;

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
