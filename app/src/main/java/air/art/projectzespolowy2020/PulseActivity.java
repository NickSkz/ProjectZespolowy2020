package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

public class PulseActivity extends AppCompatActivity {

    private static final String TAG = "PulseActivity";

    //TextView to display stuff
    TextView pulseText, oxygenText;

    //pulse, oxygen
    int pulse, oxygen;

    Button startMeasureButton, stopMeasureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        pulseText = (TextView) findViewById(R.id.bpm_view);
        oxygenText = (TextView) findViewById(R.id.oxygen_view);

        //On start button write characteristic to WRITE CHANNEL to get stuff from tha bracelet
        startMeasureButton = (Button) findViewById(R.id.startMeasureButton);
        startMeasureButton.setOnClickListener(view -> {
            if(BtAdPseudoSingleton.bluetoothGatt != null && !ConnectionSettings.isMeasuring){
                ConnectionSettings.isMeasuring = true;
                BluetoothGattCharacteristic writeChar = BtAdPseudoSingleton.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
                writeChar.setValue(Consts.openLiveDataStream);
                BtAdPseudoSingleton.bluetoothGatt.writeCharacteristic(writeChar);
            }
        });

        //On close write characteristic that stops live measure
        stopMeasureButton = (Button) findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(view -> {
            if(BtAdPseudoSingleton.bluetoothGatt != null && ConnectionSettings.isMeasuring){
                ConnectionSettings.isMeasuring = false;
                BluetoothGattCharacteristic writeChar = BtAdPseudoSingleton.bluetoothGatt.getService(Consts.THE_SERVICE).getCharacteristic(Consts.THE_WRITE_CHAR);
                writeChar.setValue(Consts.closeLiveDataStream);
                BtAdPseudoSingleton.bluetoothGatt.writeCharacteristic(writeChar);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(pulseReceiver, new IntentFilter("GetPulseData"));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(pulseReceiver);
        super.onPause();
    }


    //Listen to incoming Pulse and Oxygen signals
    private BroadcastReceiver pulseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pulse = intent.getIntExtra(Consts.PULSE,-1);
            oxygen = intent.getIntExtra(Consts.OXYGEN,-1);

            Log.i(TAG, "Pulse: " + String.valueOf(pulse));
            Log.i(TAG, "Oxygen: " + String.valueOf(oxygen));

            pulseText.setText(String.valueOf(pulse) + " BPM");
            oxygenText.setText(String.valueOf(oxygen) + "%");
        }
    };

}
