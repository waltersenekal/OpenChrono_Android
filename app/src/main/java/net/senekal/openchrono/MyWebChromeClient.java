package net.senekal.openchrono;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

public class MyWebChromeClient extends WebChromeClient {

    private static final String TAG = "WebViewConsole";

    @Override
    public boolean onConsoleMessage( ConsoleMessage consoleMessage) {
        Log.d(TAG, consoleMessage.message() + " -- From line "
                + consoleMessage.lineNumber() + " of "
                + consoleMessage.sourceId());
        return true;
    }
}

