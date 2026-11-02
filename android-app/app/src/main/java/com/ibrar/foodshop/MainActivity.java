package com.ibrar.foodshop;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.webkit.*;

public class MainActivity extends Activity {
  WebView w;
  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    w = new WebView(this);
    setContentView(w);
    WebSettings s = w.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setSupportMultipleWindows(true);
    s.setJavaScriptCanOpenWindowsAutomatically(true);
    w.setWebViewClient(new WebViewClient() {
      @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
        String u = r.getUrl().toString();
        if (u.contains("github.io")) return false;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u)));
        return true;
      }
    });
    w.setWebChromeClient(new WebChromeClient() {
      @Override public boolean onCreateWindow(WebView v, boolean d, boolean g, Message m) {
        WebView t = new WebView(MainActivity.this);
        t.setWebViewClient(new WebViewClient() {
          @Override public boolean shouldOverrideUrlLoading(WebView x, WebResourceRequest r) {
            startActivity(new Intent(Intent.ACTION_VIEW, r.getUrl()));
            return true;
          }
        });
        ((WebView.WebViewTransport) m.obj).setWebView(t);
        m.sendToTarget();
        return true;
      }
    });
    w.loadUrl("https://ibrarhussain3736-maker.github.io/IBRAR-SHOP/");
  }
  @Override public void onBackPressed() {
    if (w.canGoBack()) w.goBack(); else super.onBackPressed();
  }
}
