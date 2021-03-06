package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*Acitivty connected to settings menu*/
public class ConnectionSettings extends AppCompatActivity {

    private static final String TAG = "ConnSettingsActivity";

    public static boolean isMeasuring;

    private Button scanButton;
    private ListView mainLstView;

    //List of avalible devices' name
    private ArrayList<String> lstDevices;
    //Array adapter for List View
    private ArrayAdapter<String> lstAdapter;

    //Set of BLE Devices - not to multiplicate same devices again and again
    private Set<BluetoothDevice> BLEDevices;

    //Scanner used for detecting devices
    final BluetoothLeScanner BLEScanner = BtAdPseudoSingleton.bluetoothAdapter.getBluetoothLeScanner();

    Intent connServiceIntent;
    Thread serviceThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BLEDevices = new HashSet<>();

        mainLstView = (ListView) findViewById(R.id.device_listview);

        //Initialize adapter for list view
        lstDevices = new ArrayList<>();
        lstAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lstDevices);
        mainLstView.setAdapter(lstAdapter);

        //Initialize pairedButton and attach listener to it
        scanButton = (Button) findViewById(R.id.paired_button);
        scanButton.setOnClickListener((view) -> findPairedDevices());

        mainLstView.setOnItemClickListener((parent, view, position, id) ->
        {
            //stop service if already running in bg (if someone swaps device on the list)
            if(connServiceIntent != null)
                stopService(connServiceIntent);

            String name = (String) parent.getItemAtPosition(position);

            //If someone chooses bracelet from devices -  HBracelet...
            if(name.startsWith("HBracelet")) {
                String[] deviceName = name.split("RSSI");
                Log.d(TAG, deviceName[0]);

                /*
                for(BluetoothDevice item : BLEDevices){

                    if(item.getName() != null){
                        if(item.getName().equals(deviceName[0]));
                           BtAdPseudoSingleton.device = item;                                 //IF NOT DO THE OTHER WAY IN SCAN METHOD
                    }
                }
                 */

                //start bg service if someone presses our Bracelet
                serviceThread = new Thread(){
                    @Override
                    public void run() {
                        startService(new Intent(getApplicationContext(), ConnectionService.class));
                    }
                };
                serviceThread.start();
            }else{
                Toast.makeText(this, "Unknown Device", Toast.LENGTH_LONG).show();
                Log.i(TAG, name);
            }
        });
    }

    private boolean mScanning;

    //Handler to manage message queue + main looper (Handler() - deprecated)
    private Handler handler = new Handler(Looper.getMainLooper());

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    //Method that starts scanning - after SCAN_PERIOD, break scanning
    private void scanLeDevice(final boolean enable) {

        if(connServiceIntent != null)
            stopService(connServiceIntent);

        if (enable) {
            // Stops scanning after a pre-defined scan period, add this to message queue (its still UI Thread!).
            handler.postDelayed(() -> {
                mScanning = false;
                BLEScanner.stopScan(leScanCallback);
                Toast.makeText(this, "SCAN FINISHED!", Toast.LENGTH_LONG).show();
            }, SCAN_PERIOD);

            mScanning = true;
            BLEScanner.startScan(leScanCallback);
        } else {
            mScanning = false;
            BLEScanner.stopScan(leScanCallback);
        }
    }


    //Report scan resultados
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //Add device to the message queue - notify bout it ListView
            handler.post(() ->
            {
                //Accept only singnals that are stronger than -70 RSSI (we want a bracelet to be near our device)
                if(result.getRssi() > -70) {
                    //Add to the list view stuff that hasnt appeared earlier (add to LV if its possible to add smth to the HashSet)
                    if (BLEDevices.add(result.getDevice())) {
                        if (result.getDevice().getName() != null) {
                            if (result.getDevice().getName().startsWith("HBracelet"))
                                BtAdPseudoSingleton.device = result.getDevice();             //TODO Do it more elegant way!

                            //Add device name + signal strength
                            lstDevices.add(result.getDevice().getName() + " RSSI: " + result.getRssi());
                            lstAdapter.notifyDataSetChanged();
                        }
                    }
                    Log.i(TAG, "YEAH " + result.getDevice().getName());
                    Log.i(TAG, "WEITER...");
                }
            });

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "SCAN FAILED");
        }
    };


    //Before scanning for devices, clear all the previous stuff
    public void findPairedDevices(){
        BLEDevices.clear();
        lstDevices.clear();
        lstAdapter.notifyDataSetChanged();
        scanLeDevice(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Register receiver to be responsive to certain actions
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectionService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ConnectionService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ConnectionService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ConnectionService.ACTION_DATA_AVAILABLE);
        registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    //Reciever to handle broadcasts from connection service
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (ConnectionService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "Connected! - Action in the reciever");
            } else if (ConnectionService.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (ConnectionService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (ConnectionService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };

}
