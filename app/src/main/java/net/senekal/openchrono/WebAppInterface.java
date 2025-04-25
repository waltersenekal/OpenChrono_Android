package net.senekal.openchrono;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {

    private final String TAG = "WebAppInterface";
    private Context mContext;
    private Handler mHandler;
    private RunnableJsonDataString ReceivedjsonDataCallback;


    public WebAppInterface(Context c) {
        ReceivedjsonDataCallback = null;
        mContext = c;
        mHandler = new Handler(c.getMainLooper());
    }


    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }


    public void setJsonDataCallback(RunnableJsonDataString callback) {
        this.ReceivedjsonDataCallback = callback;
    }


    @JavascriptInterface
    public void onJsonDataReceived(String jsonData) {
        Log.i(TAG, "onJsonDataReceived( " + jsonData + " )");
        // Check if a callback is set and trigger it on the main thread
        if (ReceivedjsonDataCallback != null) {
            mHandler.post(() -> ReceivedjsonDataCallback.run(jsonData));
        }
    }

    @FunctionalInterface
    public interface RunnableJsonDataString {

        void run(String jsonData);

    }
}
