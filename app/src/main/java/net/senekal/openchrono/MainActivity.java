package net.senekal.openchrono;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.provider.Settings;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;


import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.senekal.openchrono.db.table.Device;
import net.senekal.openchrono.utils.FileHelper;
import net.senekal.openchrono.utils.Hash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String[] mustHavePermissions = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION };


    private static final int PERMISSION_REQUEST_CODE = 123;
    private boolean isBusyCheckingPermissions = false;
    private boolean isAllPermissionsGranted = false;

    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isTimerRunning = false;

    private WebAppInterface webAppInterface;

    private String htmlFolder;

    private WebAppInterface.RunnableJsonDataString jsonDataCallback = null;

    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        htmlFolder = this.getFilesDir().getAbsolutePath() + "/res/html";
        validateHtmlFolder();

        mWebView = findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        webAppInterface = new WebAppInterface(this);
        mWebView.addJavascriptInterface(webAppInterface, "Android");
        loadPageFromStorage("splash.shtml","");
        if (checkPermission()) {
            if (checkOverlayPermission()) {
                startApp();
            } else {
                requestOverlayPermission();
            }
        }
        // Handle back button press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if(jsonDataCallback != null){
                    jsonDataCallback.run("{action:Back}");
                }else {
                    // Example: Show a confirmation dialog before exiting
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Exit Application")
                            .setMessage("Are you sure you want to exit?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Finish the activity
                                finish();
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
    }

    private boolean checkPermission() {
        boolean result = false;
        List<String> requirePermission = new ArrayList<>();

        for (String permission : mustHavePermissions) {
            if ( ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requirePermission.add(permission);
            }
        }

        if (!requirePermission.isEmpty()) {
            ActivityCompat.requestPermissions(this, requirePermission.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }else{
            result = true;
        }
        return result;
    }

    private boolean checkOverlayPermission() {
        return Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                startApp();
            } else {
                Toast.makeText(this, "Overlay permission denied by the user", Toast.LENGTH_SHORT).show();
                startAppFailed();
            }
        }
    }
    private void startApp(){
        MainThread mMainThread = new MainThread(this);
        mMainThread.start();
    }

    private void startAppFailed(){
        String html_folder = this.getFilesDir( ).getAbsolutePath( ) + "/res/html";
        String htmlData = FileHelper.readFileContent( html_folder + "/no_permission.shtml" );
        loadPageFromString(htmlData,
                () -> {
                    Log.i(TAG,"Runnable callback executed");
                },
                (String btnData) -> {
                    Log.i(TAG,"Button callback executed with data: " + btnData);
                    switch(btnData ){
                        case "retry":
                            if(checkPermission()){
                                if (checkOverlayPermission()) {
                                    startApp();
                                } else {
                                    requestOverlayPermission();
                                }
                            }
                            break;
                        default:
                            //We will exit the application now
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                            break;
                    }
                },
                null
        );

    }

    public void loadPageFromString(String data, Runnable callback, WebAppInterface.RunnableJsonDataString jsonData_callback,String postDelay) {
        runOnUiThread( ( )->{
            this.jsonDataCallback = jsonData_callback;
            mWebView.clearHistory();
            mWebView.clearCache(true);
            CookieManager.getInstance().removeAllCookies(null);

            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

            mWebView.loadUrl("about:blank");

            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

            mWebView.loadDataWithBaseURL("file:///" + htmlFolder + "/", data,"text/html", "UTF-8", null);
            mWebView.setWebViewClient(new MyWebViewClient(mWebView) {
                boolean isReloaded = false;

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (!isReloaded) {
                        isReloaded = true;
                        mWebView.reload(); // Force reload once
                    } else {
                        runOnUiThread(callback);
                        if (postDelay != null) {
                            mWebView.loadUrl(postDelay);
                        }
                    }
                }
            });
        } );
        webAppInterface.setJsonDataCallback( jsonData->{
            Log.d(TAG, "Json Data Received: " + jsonData);
            this.jsonDataCallback = null;
            if(jsonData_callback != null){
                jsonData_callback.run(jsonData);
            }
        } );
    }

    public void loadPageFromStorage ( String filename,String NewValue){
        this.runOnUiThread( ( )->{
            Log.i(TAG, "Loading Page: " + htmlFolder + "/" + filename);
            File file = new File(htmlFolder, filename);
            try {
                mWebView.loadUrl(String.valueOf(file.toURI().toURL()));
                mWebView.setWebChromeClient(new MyWebChromeClient());
                if( !NewValue.isEmpty( ) ) {
                    mWebView.setWebViewClient(new MyWebViewClient(mWebView){
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            mWebView.loadUrl("javascript:init('" + NewValue + "')");
                        }
                    });
                }
            } catch ( MalformedURLException e) {
                Log.e(TAG, Objects.requireNonNull( e.getMessage( ) ) );
            }
        } );

    }


    private void validateHtmlFolder(){
        File MyDir = this.getFilesDir();
        String dataPath = MyDir.getPath()+ "/res/html";

        try {
            File htm_data_path = new File(dataPath);
            if (!htm_data_path.exists() ) {
                copyDirOrFileFromAssetManager("html", dataPath);
            }else{
                if(!Hash.doesContentMatch(this,"file_info.csv")){
                    FileHelper.deleteFolder(htm_data_path);
                    copyDirOrFileFromAssetManager( "html", dataPath );
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull( e.getMessage( ) ) );
        }
    }
    private void copyDirOrFileFromAssetManager(String arg_assetDir, String arg_destinationDir) throws IOException {
        File dest_dir = new File(arg_destinationDir);
        FileHelper.createDir(dest_dir);
        AssetManager asset_manager = getApplicationContext().getAssets();
        String[] files = asset_manager.list(arg_assetDir);

        for ( int i = 0; i < Objects.requireNonNull( files ).length; i++)
        {
            String abs_asset_file_path = FileHelper.addTrailingSlash(arg_assetDir) + files[i];
            String[] sub_files = asset_manager.list(abs_asset_file_path);

            if (sub_files!=null && sub_files.length == 0)
            {
                String dest_file_path = FileHelper.addTrailingSlash(arg_destinationDir) + files[i];
                copyAssetFile(abs_asset_file_path, dest_file_path);
            } else
            {
                copyDirOrFileFromAssetManager(abs_asset_file_path, FileHelper.addTrailingSlash(arg_destinationDir) + files[i]);
            }
        }
    }
    private void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException{
        try (InputStream in = getApplicationContext( ).getAssets( ).open( assetFilePath ); OutputStream out = new FileOutputStream( destinationFilePath ) ) {

            byte[] buf = new byte[ 1024 ];
            int len;
            while ( ( len = in.read( buf ) ) > 0 ) {
                out.write( buf, 0, len );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                if (checkOverlayPermission()) {
                    startApp();
                } else {
                    requestOverlayPermission();
                }
            } else {
                Toast.makeText(this, "Permission denied by the user", Toast.LENGTH_SHORT).show();
                startAppFailed();
            }
        }
    }

}
