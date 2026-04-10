package com.example.rootwebviewdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1001;
    private WebView webView;
    private boolean permissionRequestInFlight;
    private boolean overlayLaunchedForExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.in_app_title);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.addJavascriptInterface(new JsBridge(), "NativeBridge");
        webView.loadUrl("file:///android_asset/index.html");

        ensureOverlayPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionRequestInFlight = false;
    }

    private void ensureOverlayPermission() {
        if (Settings.canDrawOverlays(this) || permissionRequestInFlight) {
            return;
        }

        permissionRequestInFlight = true;
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
    }

    private void showFloatingOverlayAndExit() {
        if (overlayLaunchedForExit) {
            return;
        }
        overlayLaunchedForExit = true;

        Intent serviceIntent = new Intent(this, FloatingOverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionRequestInFlight = false;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }

        if (Settings.canDrawOverlays(this)) {
            showFloatingOverlayAndExit();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (permissionRequestInFlight || !Settings.canDrawOverlays(this)) {
            return;
        }
        showFloatingOverlayAndExit();
    }
}
