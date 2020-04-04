package air.art.projectzespolowy2020;

import android.bluetooth.BluetoothAdapter;


/**
 * Created by skubi on 18.03.2020.
 */

final class BtAdPseudoSingleton{
    //Initiate BluetoothAdapter, that represents device Bluetooth radio (one for entire system)
    static public BluetoothAdapter bluetoothAdapter;

    private BtAdPseudoSingleton(){

    }
}
