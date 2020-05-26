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

public class BloodPressureActivity extends AppCompatActivity {

    private static final String TAG = "BloodPressureActivity";

    //TextView to display stuff
    TextView sys_text, dia_text;

    //pulse, oxygen, flag = obvious
    int systolic, diastolic;

    Button startMeasureButton, stopMeasureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_pressure);

        sys_text = (TextView) findViewById(R.id.sys_view);
        dia_text = (TextView) findViewById(R.id.dia_view);

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
        registerReceiver(pulseReceiver, new IntentFilter("GetBloodPressureData"));
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
            systolic = intent.getIntExtra(Consts.SYSTOLIC,-1);
            diastolic = intent.getIntExtra(Consts.DIASTOLIC,-1);

            Log.i(TAG, "Systolic: " + String.valueOf(systolic));
            Log.i(TAG, "Diastolic: " + String.valueOf(diastolic));

            sys_text.setText(String.valueOf(systolic) + " mmHg");
            dia_text.setText(String.valueOf(diastolic) + " mmHg");
        }
    };
}
