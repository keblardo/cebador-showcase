// © 2026 Cristian Luis Celie. All rights reserved. Portfolio excerpt from Cebador: shared for review only, not licensed for use, copying or redistribution.
package com.cebador.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;

    /** Puente JS <-> widget: la web guarda la sesión acá y la relee al volver a primer plano. */
    private final class Bridge {
        @JavascriptInterface
        public String getSession() {
            return SessionStore.getSessionJson(getApplicationContext());
        }

        @JavascriptInterface
        public void setSession(String sessionJson, String kcalMapJson, String snackLabel) {
            SessionStore.save(getApplicationContext(), sessionJson, kcalMapJson, snackLabel);
            CebadorWidget.updateAll(getApplicationContext());
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(new Bridge(), "Android");
        webView.setWebViewClient(new WebViewClient());
        setContentView(webView);

        webView.loadUrl("file:///android_asset/diario.html");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si el widget cebó mientras la app estaba cerrada, la web toma la sesión más nueva.
        if (webView != null) webView.evaluateJavascript("window.__cebadorResume && window.__cebadorResume();", null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
