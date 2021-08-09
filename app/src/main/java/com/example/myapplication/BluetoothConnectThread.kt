package com.example.myapplication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.*

/**
 * Bluetoohデバイスへの接続を行う
 */
open class BluetoothConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
    protected var TAG = "BluetoothConnectThread"
    protected var mmSocket: BluetoothSocket? = null

    /**
     * ソケット取得
     */
    fun getSocket(): BluetoothSocket? {
        return mmSocket
    }

    /**
     * コンストラクタ
     * 引数で指定されたdevice, uuidに対するBluetoothソケットオブジェクトの生成する。
     */
    init{
        var tmp: BluetoothSocket? = null
        try {
            // Bluetoothソケットオブジェクトの生成
            tmp = device.createRfcommSocketToServiceRecord(uuid)
        } catch (e: IOException) {
            e.printStackTrace()
            // NOP.
        }
        mmSocket = tmp
    }

    /**
     * 接続処理スレッド
     * コンストラクタで生成されたソケットを用いて接続処理を行う。
     */
    override fun run() {
        if (mmSocket == null) {
            return
        }
        try {
            mmSocket!!.connect()
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                mmSocket!!.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            return
        }
        Log.i(TAG, "Bluetooth connecting.")
    }
}