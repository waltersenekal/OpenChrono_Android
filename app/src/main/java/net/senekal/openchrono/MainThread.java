package net.senekal.openchrono;

import static com.polidea.rxandroidble3.internal.logger.LoggerUtil.bytesToHex;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.polidea.rxandroidble3.RxBleConnection;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import java.util.Map;
import java.util.HashMap;

import net.senekal.openchrono.db.AppDatabase;
import net.senekal.openchrono.db.DatabaseClient;
import net.senekal.openchrono.db.table.Ammo;
import net.senekal.openchrono.db.table.Device;
import net.senekal.openchrono.db.table.Gun;
import net.senekal.openchrono.db.table.ShotEntry;
import net.senekal.openchrono.pages.pageSetup;
import net.senekal.openchrono.pages.pageSetupAddChronograph;
import net.senekal.openchrono.pages.pageSetupAddGun;
import net.senekal.openchrono.pages.pageSetupAddAmmo;
import net.senekal.openchrono.pages.pageChronographMain;
import net.senekal.openchrono.pages.pageMain;
import net.senekal.openchrono.utils.Sleep;
import net.senekal.openchrono.utils.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import io.reactivex.rxjava3.disposables.Disposable;


public class MainThread extends Thread {

    private static final String TAG = "MainThread";

    private final MainActivity mMainActivityContext;

    private Runnable onPageLoadedCallback;

    private final String mHtmlFolder;

    private AppDatabase mDB;
    private BleDevice bleDevice;
    private Boolean bleDeviceConnected = false;

    private Device lastChronograph = null;
    private Gun lastGun = null;
    private Ammo lastAmmo = null;



    private boolean isAborted = false;

    public MainThread(MainActivity context) {
        Log.d(TAG, "MainThread()");
        mMainActivityContext = context;
        mHtmlFolder = mMainActivityContext.getFilesDir().getAbsolutePath() + "/res/html";
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public void run() {
        mDB = DatabaseClient.getInstance(mMainActivityContext);
        bleDevice = new BleDevice(mMainActivityContext);
        bleDeviceConnected = false;
        Sleep.s(1); // Sleep for 1 seconds to allow initialization
        onPageLoadedCallback = createOnPageLoadedCallback();
        isAborted = false; // Make sure the thread is not aborted
        loadMainPage();
        while (!isAborted) {
            mainLoop();
        }
    }

    private Runnable createOnPageLoadedCallback() {
        // The page has finished loading, perform any additional actions
        // For example, you can now interact with the loaded WebView
        // or perform any other logic specific to the page loading completion
        return this::handlePageLoaded;
    }

    private void handlePageLoaded() {
        // Implement your logic here if needed
    }

    private void mainLoop() {

    }


    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void loadMainPage() {
        Log.d(TAG, "loadMainPage()");
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Device> allChronographs = new ArrayList<>();
            List<Device> dbDevices = mDB.deviceDao().getAllDevices();
            List<Gun> dbGuns = mDB.gunDao().getAllGuns();
            List<Ammo> dbAmmos = mDB.ammoDao().getAllAmmo();
            if (dbDevices != null) {
                for (Device dbDevice : dbDevices) {
                    Log.d(TAG, "Found device in DB: " + dbDevice.nameFriendly + " - " + dbDevice.nameReal + " - " + dbDevice.macAddr);
                    Device chronograph = new Device(dbDevice.nameFriendly,dbDevice.nameReal,dbDevice.macAddr);
                    allChronographs.add(chronograph);
                }
            }
            String htmlData = pageMain.getPage(mHtmlFolder, allChronographs,dbGuns,dbAmmos);
            mMainActivityContext.runOnUiThread(() ->
                    mMainActivityContext.loadPageFromString(htmlData, onPageLoadedCallback, this::handlePageMainReturnResult, null)
            );
        });
    }


    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void loadSetupPage() {
        Log.d(TAG, "loadSetupPage()");
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Device> dbDevices = mDB.deviceDao().getAllDevices();
            List<Gun> dbGuns = mDB.gunDao().getAllGuns();
            List<Ammo> dbAmmos = mDB.ammoDao().getAllAmmo();

            String htmlData = pageSetup.getPage(mHtmlFolder, dbDevices,dbGuns,dbAmmos);
            mMainActivityContext.runOnUiThread(() ->
                    mMainActivityContext.loadPageFromString(htmlData, onPageLoadedCallback, this::handlePageSetupReturnResult, null)
            );
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void loadChronographAddPage() {

        // Wait for the scan to complete and update the UI
        Executors.newSingleThreadExecutor().execute(() -> {

            List<Device> allChronographs = bleDevice.scan(1000);
            String htmlData = pageSetupAddChronograph.getPage(mHtmlFolder, allChronographs);
            mMainActivityContext.runOnUiThread(() ->
                    mMainActivityContext.loadPageFromString(htmlData, onPageLoadedCallback, this::handlePageSetupAddChronographReturnResult, null)
            );
        });
    }

    private void loadAmmoAddPage() {
        Executors.newSingleThreadExecutor().execute(() -> {

            String htmlData = pageSetupAddAmmo.getPage(mHtmlFolder,"");
            mMainActivityContext.runOnUiThread(() ->
                    mMainActivityContext.loadPageFromString(htmlData, onPageLoadedCallback, this::handlePageSetupAddAmmoReturnResult, null)
            );
        });
    }
    private void loadGunAddPage() {
        Executors.newSingleThreadExecutor().execute(() -> {

            String htmlData = pageSetupAddGun.getPage(mHtmlFolder,"");
            mMainActivityContext.runOnUiThread(() ->
                    mMainActivityContext.loadPageFromString(htmlData, onPageLoadedCallback, this::handlePageSetupAddGunReturnResult, null)
            );
        });
    }




    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void loadChronographMainPage(Device chronograph, Gun gun, Ammo ammo, List<ShotEntry> shots) {
        if(bleDevice != null){
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean isConnected = false;
            if(!bleDevice.isConnected()){
                //We are not connected, open connection
                //boolean isConnected = bleDevice.connect("7C:DF:A1:B5:59:FA");
                isConnected = bleDevice.connect(chronograph.macAddr);
                if(isConnected){
                    bleDevice.subscribeToCharacteristic(this::shotReceived);
                    lastChronograph = chronograph;
                    lastGun = gun;
                    lastAmmo = ammo;
                }
            }else{
                isConnected = true;
            }

            String htmlData = pageChronographMain.getPage(mHtmlFolder, chronograph, gun, ammo, shots,isConnected);
            mMainActivityContext.runOnUiThread(() ->
                    mMainActivityContext.loadPageFromString(htmlData, onPageLoadedCallback, this::handlePageChronographMainReturnResult, null)
            );
        });
        }
    }


    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void shotReceived(byte[] value) {

        if(value !=null && value.length == 4) {
            int timeIn_us = ByteBuffer.wrap(value).getInt();
            Log.d(TAG, "Shot received: " + bytesToHex(value));
            if( lastChronograph !=null && lastGun != null && lastAmmo != null){
                ShotEntry shot = new ShotEntry(lastChronograph.id, lastGun.id, lastAmmo.id, timeIn_us,System.currentTimeMillis());
                Executors.newSingleThreadExecutor().execute(() -> {
                    mDB.shotEntryDao().insert(shot);
                    Log.d(TAG, "Shot saved to DB: " + shot.toString());
                    List<ShotEntry> shots = mDB.shotEntryDao().getUsageByDeviceAmmoAndGun(lastChronograph.id, lastGun.id, lastAmmo.id);
                    loadChronographMainPage(lastChronograph, lastGun, lastAmmo, shots);
                });
            } else {
                Log.d(TAG, "Shot received: null or empty");
            }

        } else {
            Log.d(TAG, "Shot received: null or empty");
        }


        Log.d(TAG, "Shot received: " + bytesToHex(value));
        DebugBuffer(value);
    }


    private void DebugBuffer(byte[] value) {
        // Convert to hex representation
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : value) {
            hexBuilder.append(String.format("%02X ", b));
        }
        String hexValue = hexBuilder.toString().trim();

// Convert to string with '?' for non-printable characters
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : value) {
            char c = (char) b;
            if (Character.isISOControl(c) || !Character.isDefined(c)) {
                stringBuilder.append('?');
            } else {
                stringBuilder.append(c);
            }
        }
        String stringValue = stringBuilder.toString();

        Log.d(TAG, "Characteristic Value (Hex): " + hexValue);
        Log.d(TAG, "Characteristic Value (String): " + stringValue);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void handlePageMainReturnResult(String oJsonString) {
        Log.i(TAG, "handlePageMainReturnResult( " + oJsonString + " )");
        json jsonData = new json(oJsonString);
        String action = jsonData.getValueAsString("action");
        switch (action) {
            case "Select": {
                Log.d(TAG, "loadMainPage returned select");
                String deviceSelection = jsonData.getValueAsString("chronograph");
                String gunSelection = jsonData.getValueAsString("gun");
                String ammoSelection = jsonData.getValueAsString("ammo");

                if (Objects.equals(deviceSelection, "none") || Objects.equals(gunSelection, "none") || Objects.equals(ammoSelection, "none")) {
                    Log.d(TAG, "do nothing and load main page again");
                    ErrorDialog.show(mMainActivityContext, "Not a valid selection");
                    loadMainPage();
                } else if ((!deviceSelection.isEmpty()) && (!gunSelection.isEmpty()) && (!ammoSelection.isEmpty())) {
                    String[] gunParts = gunSelection.split("~");
                    String[] ammoParts = ammoSelection.split("~");
                    Executors.newSingleThreadExecutor().execute(() -> {
                        Device device = mDB.deviceDao().getDeviceByMacAddr(deviceSelection);
                        Gun gun = mDB.gunDao().getGunByNameAndBrand(gunParts[0], gunParts[1]);
                        Ammo ammo = mDB.ammoDao().getAmmoByNameAndBrand(ammoParts[0], ammoParts[1]);
                        List<ShotEntry> shots = mDB.shotEntryDao().getUsageByDeviceAmmoAndGun(device.id, gun.id, ammo.id);
                        if (device != null && gun != null && ammo != null) {
                            loadChronographMainPage(device, gun, ammo,shots);
                        } else {
                            Log.d(TAG, "do nothing and load main page again");
                            ErrorDialog.show(mMainActivityContext, "Not a valid selection");
                            loadMainPage();
                        }
                    });
                } else {
                    Log.d(TAG, "do nothing and load main page again");
                    ErrorDialog.show(mMainActivityContext, "Not a valid selection");
                    loadMainPage();
                }
            }    break;
            case "Setup":
                Log.d(TAG, "loadSetupPage returned back");
                loadSetupPage();
                break;
            case "Back":
                Log.d(TAG, "loadMainPage returned back");
                loadMainPage();
                break;
            default:
                Log.e(TAG, "loadMainPage returned unhandled: " + action);
                loadMainPage();
                break;
        }
    }


    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void handlePageSetupReturnResult(String oJsonString) {
        Log.i(TAG, "handlePageMainReturnResult( " + oJsonString + " )");
        json jsonData = new json(oJsonString);
        String action = jsonData.getValueAsString("action");
        switch (action) {
            case "AddChronograph":
                Log.d(TAG, "loadSetupPage returned add chronograph");
                loadChronographAddPage();
                break;
            case "EditChronograph": {
                Log.d(TAG, "loadSetupPage returned edit chronograph");
                String selection = jsonData.getValueAsString("chronograph");
                //loadChronographEditPage(selection);
                ErrorDialog.show(mMainActivityContext, "Edit Chronograph is not implemented yet");
                //TODO: IMPLEMENT EDIT CHRONOGRAPH
                loadSetupPage();
            }
            break;
            case "AddGun": {
                Log.d(TAG, "loadSetupPage returned add gun");
                loadGunAddPage();
            }
            break;
            case "EditGun": {
                Log.d(TAG, "loadSetupPage returned edit gun");
                String selection = jsonData.getValueAsString("gun");
                ErrorDialog.show(mMainActivityContext, "Edit Gun is not implemented yet");
                //TODO: IMPLEMENT EDIT GUN
//                if (selection != null && !selection.equals("None")) {
//                    loadGunEditPage(selection);
//                } else {
                loadSetupPage();
//                }
            }
            break;
            case "AddAmmo": {
                Log.d(TAG, "loadSetupPage returned add ammo");
                loadAmmoAddPage();
            }
            break;
            case "EditAmmo": {
                Log.d(TAG, "loadSetupPage returned edit ammo");
                String selection = jsonData.getValueAsString("ammo");
                ErrorDialog.show(mMainActivityContext, "Edit Ammo is not implemented yet");
                //TODO: IMPLEMENT EDIT AMMO
//                if (selection != null && !selection.equals("None")) {
//                    loadAmmoEditPage(selection);
//                } else {
                    loadSetupPage();
//                }
            }
            break;
            case "Cancel":
            case "Back": {
                Log.d(TAG, "loadMainPage returned back");
                loadMainPage();
            }
            break;
            default: {
                Log.e(TAG, "loadMainPage returned unhandled: " + action);
                loadMainPage();
            }
            break;
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void handlePageChronographMainReturnResult(String oJsonString) {
        Log.i(TAG, "handlePageChronographMainReturnResult( " + oJsonString + " )");
        json jsonData = new json(oJsonString);
        String action = jsonData.getValueAsString("action");
        loadMainPage();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void handlePageSetupAddAmmoReturnResult(String oJsonString) {
        Log.i(TAG, "handlePageSetupAddAmmoReturnResult( " + oJsonString + " )");
        json jsonData = new json(oJsonString);
        String action = jsonData.getValueAsString("action");
        switch (action) {
            case "Save": {
                String name = jsonData.getValueAsString("name");
                String brand = jsonData.getValueAsString("brand");
                double calibre = Double.parseDouble(jsonData.getValueAsString("calibre"));
                double weight = Double.parseDouble(jsonData.getValueAsString("weight"));

                Executors.newSingleThreadExecutor().execute(() -> {
                    Ammo ammo = new Ammo(name, brand, calibre, weight);
                    mDB.ammoDao().updateOrInsert(ammo);
                });
                loadSetupPage();
            }
            break;
            case "Cancel": {
                Log.d(TAG, "loadSetupPage returned cancel");
                loadSetupPage();
            }
            break;
            default:
                loadMainPage();
                break;
        }
    }
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void handlePageSetupAddGunReturnResult(String oJsonString){
        Log.i(TAG, "handlePageSetupAddGunReturnResult( " + oJsonString + " )");
        json jsonData = new json(oJsonString);
        String action = jsonData.getValueAsString("action");
        switch (action) {
            case "Save": {
                String name = jsonData.getValueAsString("name");
                String brand = jsonData.getValueAsString("brand");
                double calibre = Double.parseDouble(jsonData.getValueAsString("calibre"));

                Executors.newSingleThreadExecutor().execute(() -> {
                    Gun gun = new Gun(name, brand, calibre);
                    mDB.gunDao().updateOrInsert(gun);
                });
                loadSetupPage();
            }
            break;
            case "Cancel": {
                Log.d(TAG, "loadSetupPage returned cancel");
                loadSetupPage();
            }
            break;
            default:
                loadMainPage();
                break;
        }
    }






























    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void handlePageSetupAddChronographReturnResult(String oJsonString) {
        Log.i(TAG, "handlePageChronographAddReturnResult( " + oJsonString + " )");
        json jsonData = new json(oJsonString);
        String action = jsonData.getValueAsString("action");
        switch (action) {
            case "Save":
                String data = jsonData.getValueAsString("chronograph");
                String friendlyName = jsonData.getValueAsString("friendlyName");
                String[] parts = data.split("~");
                if (parts.length == 2 && !friendlyName.isEmpty()) {
                    String realname = parts[0]; // "OpenChrono(59FA)"
                    String macAddress = parts[1]; // "7C:DF:A1:B5:59:FA"

                    Log.d(TAG, "Real Name: " + realname);
                    Log.d(TAG, "Friendly Name: " + friendlyName);
                    Log.d(TAG, "MAC Address: " + macAddress);

                    Executors.newSingleThreadExecutor().execute(() -> {
                        Device device = new Device(friendlyName, realname, macAddress);
                        mDB.deviceDao().updateOrInsert(device);
                    });
                    loadSetupPage();
                } else {
                    Log.d(TAG, "Invalid data format");
                    loadMainPage();
                }
                break;
            default:
                loadMainPage();
                break;
        }
    }
}
