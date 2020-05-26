package air.art.projectzespolowy2020;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/* Gruppen-Projekt 2020, M. Skubisz, S. Witusiak
   Die Applikation, die Information von die Sensoren ein hebt
*/
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Button connectionSettingsButton;
    Button pulseButton;
    Button pressureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initiate custom toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Switch to new settings activity
        connectionSettingsButton = (Button) findViewById(R.id.connectionSettings_button);
        connectionSettingsButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ConnectionSettings.class)));

        pulseButton = (Button) findViewById(R.id.pulseButton);
        pulseButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PulseActivity.class)));

        pressureButton = (Button) findViewById(R.id.pressureButton);
        pressureButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, BloodPressureActivity.class)));

        int PERMISSION_REQUEST_COARSE_LOCATION = 1;
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BtAdPseudoSingleton.bluetoothAdapter = bluetoothManager.getAdapter();


        //BluetoothAdapter, that represents device's Bluetooth radio (one for entire system)
        int REQUEST_ENABLE_BT = 1;

        //Check whether bluetooth is supported on a device
        if(BtAdPseudoSingleton.bluetoothAdapter != null) {
            //If not enabled, enable it
            if (!BtAdPseudoSingleton.bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        //If a device does not support Bt, make a toast
        }else {
            Toast.makeText(this, "Das Geraet stuetzt den Bluetooth nicht!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Handle REQUEST_ENABLE_BT const, passed as requestCode
        switch(requestCode){
            case RESULT_OK:{
                Toast.makeText(this, "Der Bluetooth ist aktiviert", Toast.LENGTH_LONG).show();
                break;
            }
            case RESULT_CANCELED:{
                Toast.makeText(this, "Etwas ist schlecht, der Bluetooth ist nicht eingeschaltet", Toast.LENGTH_LONG).show();
                break;
            }
            default:
                break;
        }
    }


    //Set toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);

        return true;
    }

    //Handle chosen option in toolbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
