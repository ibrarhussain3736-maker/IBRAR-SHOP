package com.ibrar.foodshop;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

public class MainActivity extends Activity implements IUnityAdsInitializationListener {
  // Unity Ads settings. Set TEST_MODE = false only for the release you publish (never tap your own live ads).
  static final String GAME_ID = "800421437";
  static final boolean TEST_MODE = true;
  static final String P_INTER = "Interstitial_Android";
  static final String P_BANNER = "Banner_Android";
  static final String P_REWARD = "Rewarded_Android";
  WebView w;
  FrameLayout bannerBox;
  boolean ready = false, interLoaded = false, rewardLoaded = false;
  long lastInter = 0;

  @Override public void onInitializationComplete() {
    ready = true;
    runOnUiThread(() -> {
      BannerView bv = new BannerView(this, P_BANNER, new UnityBannerSize(320, 50));
      bv.load();
      bannerBox.addView(bv, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    });
    loadInter(); loadReward();
  }
  @Override public void onInitializationFailed(UnityAds.UnityAdsInitializationError e, String m) { ready = false; }

  void loadInter() {
    UnityAds.load(P_INTER, new IUnityAdsLoadListener() {
      @Override public void onUnityAdsAdLoaded(String id) { interLoaded = true; }
      @Override public void onUnityAdsFailedToLoad(String id, UnityAds.UnityAdsLoadError e, String m) { interLoaded = false; }
    });
  }
  void loadReward() {
    UnityAds.load(P_REWARD, new IUnityAdsLoadListener() {
      @Override public void onUnityAdsAdLoaded(String id) { rewardLoaded = true; }
      @Override public void onUnityAdsFailedToLoad(String id, UnityAds.UnityAdsLoadError e, String m) { rewardLoaded = false; }
    });
  }
  IUnityAdsShowListener after(final boolean inter) {
    return new IUnityAdsShowListener() {
      @Override public void onUnityAdsShowFailure(String id, UnityAds.UnityAdsShowError e, String m) { if (inter) loadInter(); else loadReward(); }
      @Override public void onUnityAdsShowStart(String id) {}
      @Override public void onUnityAdsShowClick(String id) {}
      @Override public void onUnityAdsShowComplete(String id, UnityAds.UnityAdsShowCompletionState st) {
        if (inter) { interLoaded = false; loadInter(); }
        else { rewardLoaded = false; loadReward();
          if (st == UnityAds.UnityAdsShowCompletionState.COMPLETED) w.evaluateJavascript("window.onRewarded&&window.onRewarded()", null); }
      }
    };
  }
  /** Called from the web page (window.AndroidAds). */
  public class AdBridge {
    @JavascriptInterface public void interstitial() {
      runOnUiThread(() -> {
        long now = System.currentTimeMillis();
        if (ready && interLoaded && now - lastInter > 120000) { lastInter = now; UnityAds.show(MainActivity.this, P_INTER, new UnityAdsShowOptions(), after(true)); }
      });
    }
    @JavascriptInterface public void rewarded() {
      runOnUiThread(() -> { if (ready && rewardLoaded) UnityAds.show(MainActivity.this, P_REWARD, new UnityAdsShowOptions(), after(false)); });
    }
    @JavascriptInterface public boolean rewardReady() { return ready && rewardLoaded; }
  }
  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    w = new WebView(this);
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.addView(w, new LinearLayout.LayoutParams(-1, 0, 1f));
    bannerBox = new FrameLayout(this);
    root.addView(bannerBox, new LinearLayout.LayoutParams(-1, -2));
    setContentView(root);
    w.addJavascriptInterface(new AdBridge(), "AndroidAds");
    UnityAds.initialize(getApplicationContext(), GAME_ID, TEST_MODE, this);
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
