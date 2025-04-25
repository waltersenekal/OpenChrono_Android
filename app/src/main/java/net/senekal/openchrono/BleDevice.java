package net.senekal.openchrono;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import java.util.UUID;

import com.polidea.rxandroidble3.RxBleClient;
import com.polidea.rxandroidble3.RxBleConnection;
import android.bluetooth.le.ScanSettings;

import net.senekal.openchrono.db.table.Device;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.core.Observable;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BleDevice {
    private static final String TAG = "BleDevice";
    private final Context context;
    private RxBleClient rxBleClient;
    private Disposable scanSubscription;

    private boolean isConnected = false;
    private Disposable connectionSubscription;
    private RxBleConnection activeConnection; // Add this field to store the connection
    private Disposable notificationSubscription;

    public BleDevice(Context context) {
        this.context = context;
        rxBleClient = RxBleClient.create(context);

    }

    public List<Device> scan(int TimeoutMilliSeconds) {
        Log.d(TAG, "Scanning for BLE devices...");

        Set<Device> foundDevices = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(1);

        scanSubscription = rxBleClient.scanBleDevices()
                .subscribe(
                        scanResult -> {
                            if (scanResult.getBleDevice().getName().startsWith("OpenChrono")) {
                                // Add unique devices based on MAC address
                                Device device = new Device(
                                        scanResult.getBleDevice().getName(),
                                        scanResult.getBleDevice().getName(),
                                        scanResult.getBleDevice().getMacAddress()
                                );
                                foundDevices.add(device);
                            }
                        },
                        throwable -> {
                            // Handle an error here
                            Log.e(TAG, "Scan error: ", throwable);
                            latch.countDown(); // Release latch on error
                        },
                        latch::countDown // Release latch when scan completes
                );

        try {
            // Wait for the timeout or until the latch is released
            latch.await(TimeoutMilliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Scan interrupted: ", e);
        } finally {
            stopScan(); // Ensure the scan is stopped
        }

        Log.d(TAG, "Done with Scanning for BLE devices...");
        return new ArrayList<>(foundDevices); // Return the list of unique devices
    }

    public void stopScan() {
        if (scanSubscription != null && !scanSubscription.isDisposed()) {
            scanSubscription.dispose();
            Log.d(TAG, "Scanning stopped.");
        }
    }

    public boolean connect(String MacAddr) {
        Log.d(TAG, "Attempting to connect to BLE device with MAC: " + MacAddr);

        CountDownLatch latch = new CountDownLatch(1);
        isConnected = false;

        connectionSubscription = rxBleClient.getBleDevice(MacAddr)
                .establishConnection(false)
                .doFinally(() -> {
                    isConnected = false;
                    activeConnection = null; // Clear the connection when terminated
                    Log.d(TAG, "Connection terminated for device: " + MacAddr);
                })
                .subscribe(
                        connection -> {
                            Log.d(TAG, "Connection successful to device: " + MacAddr);
                            isConnected = true;
                            activeConnection = connection; // Store the connection
                            latch.countDown();
                        },
                        throwable -> {
                            Log.e(TAG, "Connection failed: ", throwable);
                            latch.countDown();
                        }
                );

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                Log.e(TAG, "Connection timeout for device: " + MacAddr);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Connection interrupted: ", e);
        }

        if (!isConnected) {
            stopConnection();
        }

        return isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void stopConnection() {
        if (connectionSubscription != null && !connectionSubscription.isDisposed()) {
            connectionSubscription.dispose();
            Log.d(TAG, "Connection disposed.");
        }
        isConnected = false;
    }


    public List<String> getSupportedGatts() {
        Log.d(TAG, "Fetching supported GATT services using the existing connection...");

        List<java.lang.String> gattServices = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        if (isConnected && activeConnection != null) {
            activeConnection.discoverServices()
                    .subscribe(
                            rxBleDeviceServices -> {
                                rxBleDeviceServices.getBluetoothGattServices().forEach(service -> {
                                    gattServices.add(service.getUuid().toString());
                                });
                                latch.countDown();
                            },
                            throwable -> {
                                Log.e(TAG, "Failed to fetch GATT services: ", throwable);
                                latch.countDown();
                            }
                    );

            try {
                if (!latch.await(3, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Timeout while fetching GATT services.");
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Fetching GATT services interrupted: ", e);
            }
        } else {
            Log.e(TAG, "No active connection available to fetch GATT services.");
        }

        Log.d(TAG, "Done fetching GATT services.");
        return gattServices;
    }

    public List<String> getCharacteristics(String gattServiceUuid) {
        Log.d(TAG, "Fetching characteristics for GATT service: " + gattServiceUuid);

        List<String> characteristics = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        if (isConnected && activeConnection != null) {
            activeConnection.discoverServices()
                    .flatMapObservable(services -> {
                        // Find the GATT service by UUID
                        return Observable.fromIterable(
                                services.getBluetoothGattServices()
                                        .stream()
                                        .filter(service -> service.getUuid().equals(UUID.fromString(gattServiceUuid)))
                                        .findFirst()
                                        .map(service -> service.getCharacteristics())
                                        .orElse(new ArrayList<>())
                        );
                    })
                    .subscribe(
                            characteristic -> {
                                characteristics.add(characteristic.getUuid().toString());
                            },
                            throwable -> {
                                Log.e(TAG, "Failed to fetch characteristics: ", throwable);
                                latch.countDown();
                            },
                            latch::countDown // Release latch when done
                    );

            try {
                if (!latch.await(3, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Timeout while fetching characteristics.");
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Fetching characteristics interrupted: ", e);
            }
        } else {
            Log.e(TAG, "No active connection available to fetch characteristics.");
        }

        Log.d(TAG, "Done fetching characteristics for GATT service: " + gattServiceUuid);
        return characteristics;
    }

    public byte[] getCharacteristicValue(String gattServiceUuid, String characteristicUuid) {
        Log.d(TAG, "Fetching value for characteristic: " + characteristicUuid + " in GATT service: " + gattServiceUuid);

        final byte[][] characteristicValueHolder = {null}; // Use a holder for thread-safe access
        CountDownLatch latch = new CountDownLatch(1);

        if (isConnected && activeConnection != null) {
            activeConnection.discoverServices()
                    .flatMapObservable(services -> {
                        // Find the GATT service by UUID
                        return Observable.fromIterable(
                                services.getBluetoothGattServices()
                                        .stream()
                                        .filter(service -> service.getUuid().equals(UUID.fromString(gattServiceUuid)))
                                        .findFirst()
                                        .map(service -> service.getCharacteristics())
                                        .orElse(new ArrayList<>())
                        );
                    })
                    .filter(characteristic -> characteristic.getUuid().equals(UUID.fromString(characteristicUuid)))
                    .flatMapSingle(characteristic -> activeConnection.readCharacteristic(characteristic))
                    .subscribe(
                            value -> {
                                characteristicValueHolder[0] = value; // Store the value
                                latch.countDown();
                            },
                            throwable -> {
                                Log.e(TAG, "Failed to fetch characteristic value: ", throwable);
                                latch.countDown();
                            }
                    );

            try {
                if (!latch.await(3, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Timeout while fetching characteristic value.");
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Fetching characteristic value interrupted: ", e);
            }
        } else {
            Log.e(TAG, "No active connection available to fetch characteristic value.");
        }

        Log.d(TAG, "Done fetching value for characteristic: " + characteristicUuid);
        return characteristicValueHolder[0] != null ? characteristicValueHolder[0] : new byte[0];
    }

    public void subscribeToCharacteristic(notificationReceivedCallback onMessageReceived) {
        boolean result = false;
        UUID serviceUuid = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
        UUID characteristicUuid = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");

        if (isConnected && activeConnection != null) {
            notificationSubscription = activeConnection.setupNotification(characteristicUuid)
                    .flatMap(notificationObservable -> notificationObservable) // Get the notification data
                    .subscribe(
                            onMessageReceived::received,
                            throwable -> {
                                // Handle errors
                                Log.e(TAG, "Failed to subscribe to characteristic: ", throwable);
                            }
                    );
        }
    }

/**
     * This is the prototype for receiving callback notifications.
     */
    @FunctionalInterface
    public interface notificationReceivedCallback {
        void received(byte[] data);
    }

}