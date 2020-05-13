package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

public class PulseActivity extends AppCompatActivity {

    private static final String TAG = "PulseActivity";

    //TextView to display stuff
    TextView pulseText, oxygenText;

    //pulse, oxygen, flag = obvious
    int pulse, oxygen;
    boolean isMeasuring = false;

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
            if(BtAdPseudoSingleton.bluetoothGatt != null && !isMeasuring){
                isMeasuring = true;
                //This is the characteristic
                BluetoothGattCharacteristic writeChar = BtAdPseudoSingleton.bluetoothGatt.getService(UUID.fromString("000001ff-3c17-d293-8e48-14fe2e4da212")).getCharacteristic(UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb"));
                //This is the byte chain that needs to be written to get measurements
                byte[] test = {(byte)-85, (byte)0, (byte)0, (byte)9, (byte)-63, (byte)123, (byte)0, (byte)64, (byte)5, (byte)0, (byte)6, (byte)0, (byte)4, (byte)0, (byte)1, (byte)5, (byte)2};
                writeChar.setValue(test);
                BtAdPseudoSingleton.bluetoothGatt.writeCharacteristic(writeChar);
            }
        });

        //TODO Beende das somehow!!!
        stopMeasureButton = (Button) findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(view -> {
            if(BtAdPseudoSingleton.bluetoothGatt != null && isMeasuring){
                isMeasuring = false;
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

            pulseText.setText(String.valueOf(pulse));
            oxygenText.setText(String.valueOf(oxygen));
        }
    };

}
