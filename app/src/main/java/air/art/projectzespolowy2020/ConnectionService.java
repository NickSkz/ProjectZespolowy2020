package air.art.projectzespolowy2020;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;

/*
* Class that handles connection with the band +? reads data
* It runs on a new background thread (IntentService, NOT Service), in order not to overload UI Thread.
*/
public class ConnectionService extends Service {

    static final String TAG =  "ConnectionService";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "air.art.projectzespolowy2020.action.FOO";
    private static final String ACTION_BAZ = "air.art.projectzespolowy2020.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "air.art.projectzespolowy2020.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "air.art.projectzespolowy2020.extra.PARAM2";

    //Declare Bluetooth Gatt instance
    private BluetoothGatt bluetoothGatt;

    //Current connection state
    private int connectionState = STATE_DISCONNECTED;
    //Declare availible connection states
    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "EXTRA_DATA";


    //Handler to put messages on UI Thread
    Handler handler = new Handler(Looper.getMainLooper());

    /*************************************TEST*****************************/
    List<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    /**********************************************************************/

    //Override onBind - mandatory
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    //On startService()
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Run this on a new thread - not to overload UI Thread
        Thread t = new Thread(){
            @Override
            public void run() {
                //Connect to GATT server
                bluetoothGatt = BtAdPseudoSingleton.device.connectGatt(getApplicationContext(), false, gattCallback);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Gatt all services and characteristics
                displayGattServices(bluetoothGatt.getServices());


                //Read them all
                for(int i = 0; i < mGattCharacteristics.size(); ++i){
                    for(BluetoothGattCharacteristic item : mGattCharacteristics.get(i)){
                        bluetoothGatt.readCharacteristic(item);

                        //TODO THIS IS TEMPORARY ABYSMAL SOLUTION!
                        //This is the key to all of previous problemos - wait till it reads characteristic
                        //If We dont wait previous reading operations will fail due to readCharacteristic is asonchronous - only one will be sucessful
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                /*
                while(true) {
                    bluetoothGatt.readCharacteristic(bluetoothGatt.getService(UUID.fromString("0000d0ff-3c17-d293-8e48-14fe2e4da212")).getCharacteristic(UUID.fromString("0000ffd3-0000-1000-8000-00805f9b34fb")));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                */
            }
        };
        t.start();

        return super.onStartCommand(intent, flags, startId);

    }


    // Methods from BLE API, based on Android Developer BLE overview
    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {

                    //if connected - notify user
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //Set actual connection + perform action connected with it
                        connectionState = STATE_CONNECTED;
                        broadcastUpdate(ACTION_GATT_CONNECTED);

                        //Inform user, by toast launched on the UI Thread thanks to handler
                        handler.post(() ->
                            Toast.makeText(getApplicationContext(), "Connected to GATT server!", Toast.LENGTH_LONG).show()
                        );

                        //If connected - discover devices
                        //If connected - discover devices
                        Log.i(TAG, "Attempting to start service discovery:" +
                                bluetoothGatt.discoverServices());

                        //if disconnected
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //Set actual connection + perform action connected with it
                        connectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    }
                }

                @Override
                //New services discovered
                //perform action on discovery
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
                // perform action after reading char
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //Perform action connected with it + send read characteristic

                        Log.i(TAG, "******************************THIS IS IT******************************");
                        bluetoothGatt.setCharacteristicNotification(characteristic, true);

                        //We need to set notification when particular value changes
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            if(descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                bluetoothGatt.writeDescriptor(descriptor);
                            }
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                        Log.i(TAG, "***************************************************************************\n");
                    }else{
                        Log.i(TAG, "WRONG!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        Log.i(TAG, Arrays.toString(characteristic.getValue()));
                    }
                }

                @Override
                // Characteristic notification - when it changes notify user
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    Log.i(TAG, "THINGS CHANGE!");
                    handler.post(() ->
                            Toast.makeText(getApplicationContext(),  "Andrew Golota!", Toast.LENGTH_LONG).show()
                    );
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

        sendBroadcast(intent);
    }

    //Method to collect info from BluetoothGattCallback
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        //show these bytes
        byte[] data = characteristic.getValue();
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for(byte i : data)
            stringBuilder.append(String.format("%02X ", i));

        Log.i(TAG, "Service it belongs to: " + characteristic.getService().getUuid());

        Log.i(TAG, "Raw bytes!: " + stringBuilder.toString());

        Log.i(TAG, "String value: " + characteristic.getStringValue(0));

        Log.i(TAG, "Properties: " + Integer.toBinaryString(characteristic.getProperties()));
        Log.i(TAG, "Permissions: " + Integer.toBinaryString(characteristic.getPermissions()));

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    //Add all characteristic to ArrayList
    private void displayGattServices(List<BluetoothGattService> gattServices) {

        //if null return
        if (gattServices == null)
            return;

        //Initialize container for all services and characteristics within them
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
            // Loops through available GATT Services.

            //got through all services
            for (BluetoothGattService gattService : gattServices) {

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<BluetoothGattCharacteristic>();
                // Loops through available Characteristics.

                //go through all characteristics in the service
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {
                    if(gattCharacteristic == null)
                        continue;

                    charas.add(gattCharacteristic);
                    Log.i(TAG, gattCharacteristic.getUuid().toString());
                }
                //add ArrayList of characteristics to the container (cell in the mGattCharacteristics corresponds to one service - in which there are characteristics)
                mGattCharacteristics.add(charas);
            }

        }
}


