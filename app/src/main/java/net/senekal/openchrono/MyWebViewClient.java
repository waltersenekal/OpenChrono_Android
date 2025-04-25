package net.senekal.openchrono;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {

    private final String TAG = "MyWebViewClient";
    private final WebView mWebView;

    public MyWebViewClient(WebView webView) {
        this.mWebView = webView;
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        Log.d(TAG, "on Key Event: " + event.toString());
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            // Dismiss the keyboard
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return true; // Consumes the event
        }
        return false; // Allows the event to propagate
    }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG, "on PageLoaded: " + url);
    }
}

