package air.art.projectzespolowy2020;

import android.app.IntentService;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;



import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;

/*
* Class that handles connection with the band +? reads data
* It runs on a new background thread (IntentService, NOT Service), in order not to overload UI Thread.
*/


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ConnectionService extends IntentService {

    static final String TAG =  "ConnectionService";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "air.art.projectzespolowy2020.action.FOO";
    private static final String ACTION_BAZ = "air.art.projectzespolowy2020.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "air.art.projectzespolowy2020.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "air.art.projectzespolowy2020.extra.PARAM2";

    public ConnectionService() {
        super("ConnectionService");
    }

    //Current connection state
    private int connectionState = STATE_DISCONNECTED;

    //Declare Bluetooth Gatt instance
    private BluetoothGatt bluetoothGatt;


    //Declare availible connection states
    public final static String ACTION_GATT_CONNECTED =
            "air.art.projectzespolowy2020.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "air.art.projectzespolowy2020.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "air.art.projectzespolowy2020.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "air.art.projectzespolowy2020.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "air.art.projectzespolowy2020.EXTRA_DATA";


    //Handler to put messages on UI Thread
    Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ConnectionService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ConnectionService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Connect with GATT server
        bluetoothGatt = BtAdPseudoSingleton.device.connectGatt(this, false, gattCallback);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // Methods from BLE API, based on Android Developer BLE overview
    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //Set actual connection + perform action connected with it
                        connectionState = STATE_CONNECTED;
                        broadcastUpdate(ACTION_GATT_CONNECTED);

                        //Inform user, by toast launched on the UI Thread thanks to handler
                        handler.post(() ->
                            Toast.makeText(getApplicationContext(), "Connected to GATT server!", Toast.LENGTH_LONG).show()
                        );

                        //If connected - discover devices
                        Log.i(TAG, "Attempting to start service discovery:" +
                                bluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //Set actual connection + perform action connected with it
                        connectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    //If discovery succeed
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //Perform action connected with it
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                        //Print out UUIDs of detected services
                        for(BluetoothGattService item : bluetoothGatt.getServices()){
                            Log.i(TAG, item.getUuid().toString());
                        }

                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //Perform action connected with it + send read characteristic
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                        Log.w(TAG, "I READ SOMETHING!");
                    }
                }

            };


    //Destroy service
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed!");
    }

    //Method to collect info from BluetoothGattCallback
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        /*TODO Handle this in BroadcastReciever somewhere*/
        sendBroadcast(intent);
    }

    //Method to collect info from BluetoothGattCallback
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        /*TODO Handle this in BroadcastReciever somewhere*/
        sendBroadcast(intent);
    }


}
