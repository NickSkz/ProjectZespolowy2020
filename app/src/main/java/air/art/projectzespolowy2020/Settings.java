package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Set;

public class Settings extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    Button pairedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        Set<BluetoothDevice> pairedDev = BtAdSingleton.bluetoothAdapter.getBondedDevices();

        String deviceName, deviceAdress;

        //For each item in the Set wyloguj imie i adres
        for(BluetoothDevice item : pairedDev){
            deviceName = item.getName();
            deviceAdress = item.getAddress();

            Log.i(TAG, "Device's name: " + deviceName + ", Device's adress: " + deviceAdress);
        }
    }
}
