package com.example.myapplication

import android.bluetooth.BluetoothDevice
import java.util.UUID

/**
 * シリアルポートプロファイルを使ってBluetooth接続する
 */
class SerialPortProfileConnectThread : BluetoothConnectThread {
    // "00001101-0000-1000-8000-00805f9b34fb" = SPP (シリアルポートプロファイル) の UUID.
    val SPP_UUID: UUID? =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    constructor(device: BluetoothDevice) :super(device, UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")){
        TAG = "SerialPortProfileConnectThread";

    }
}