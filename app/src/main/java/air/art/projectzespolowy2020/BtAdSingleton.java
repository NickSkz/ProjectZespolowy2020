package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by skubi on 18.03.2020.
 */

class BtAdSingleton {
    //Initiate BluetoothAdapter, that represents device Bluetooth radio (one for entire system)
    static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BtAdSingleton(){

    }
}
