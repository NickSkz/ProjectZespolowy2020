package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class ConnectionSettings extends AppCompatActivity {

    private static final String TAG = "ConnSettingsActivity";

    private Button pairedButton;

    private ListView mainLstView;

    private ArrayList<String> lstDevices;
    private ArrayAdapter<String> lstAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mainLstView = (ListView) findViewById(R.id.device_listview);

        lstDevices = new ArrayList<>();
        lstAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lstDevices);

        //Initialize pairedButton and attach listener to it
        pairedButton = (Button) findViewById(R.id.paired_button);
        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findPairedDevices();
            }
        });
    }

    public void findPairedDevices(){
        //Set of all paired devices
        Set<BluetoothDevice> pairedDev = BtAdPseudoSingleton.bluetoothAdapter.getBondedDevices();

        String deviceName, deviceAdress;

        //For each item in the Set wyloguj imie i adres
        for(BluetoothDevice item : pairedDev){
            deviceName = item.getName();
            deviceAdress = item.getAddress();

            lstDevices.add(deviceName);
            Log.i(TAG, "Device's name: " + deviceName + ", Device's adress: " + deviceAdress);
        }

        mainLstView.setAdapter(lstAdapter);
    }
}
