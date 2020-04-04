package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*TODO FIX START/STOPLESCAN DEPRECATION */
/*Acitivty connected to settings menu*/
public class ConnectionSettings extends AppCompatActivity {

    private static final String TAG = "ConnSettingsActivity";

    private Button scanButton;
    private ListView mainLstView;

    //List of avalible devices' name
    private ArrayList<String> lstDevices;
    //Array adapter for List View
    private ArrayAdapter<String> lstAdapter;

    //Set of BLE Devices - not to multiplicate same devices again and again
    private Set<BluetoothDevice> BLEDevices;

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
    }

    private boolean mScanning;

    //Handler to manage message queue + main looper (Handler() - deprecated)
    private Handler handler = new Handler(Looper.getMainLooper());

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    //Method that starts scanning - after SCAN_PERIOD, break scanning
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period, add this to message queue (its still UI Thread!).
            handler.postDelayed(() -> {
                mScanning = false;
                BtAdPseudoSingleton.bluetoothAdapter.stopLeScan(leScanCallback);
            }, SCAN_PERIOD);

            mScanning = true;
            BtAdPseudoSingleton.bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            BtAdPseudoSingleton.bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    //Place where found devices are returned
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    //Add device to the message queue - notify bout it ListView
                    handler.post(() -> {
                        //Add to the list view stuff that hasnt appeared earlier - if its possible to at to the HashSet
                        if(BLEDevices.add(device)){
                            if(device.getName() != null) {
                                lstAdapter.add(device.getName());
                                lstAdapter.notifyDataSetChanged();
                            }
                        }
                        Log.i(TAG, "YEAH " + device.getName());
                        Log.i(TAG, "GOING...");
                    });
                }
            };



    //Before scanning for devices, clear all the previous stuff
    public void findPairedDevices(){
        BLEDevices.clear();
        lstDevices.clear();
        scanLeDevice(true);
    }
}
