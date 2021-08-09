package com.example.myapplication

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import android.util.Log
import java.nio.charset.Charset

open class BluetoothReceiveTask(socket: BluetoothSocket?) : Thread() {

    val TAG = "BluetoothReceiveTask"

    protected var mInputStream: InputStream? = null
    protected var mSocket: BluetoothSocket? = null

    @Volatile protected var mIsCancel = false

    @Volatile var engine_load = -1.0
    @Volatile var coolant_temp = -1
    @Volatile var update_coolant_temp = false
    @Volatile var manifold_abs_press = -1
    @Volatile var engine_rpm = -1
    @Volatile var update_vehicle_rpm = false
    @Volatile var vehicle_spd = -1
    @Volatile var update_vehicle_spd = false
    @Volatile var air_temp = -1
    @Volatile var throttle_pos_body = -1.0
    @Volatile var abs_barometric_press = -1
    @Volatile var voltage = -1.0
    @Volatile var throttle_pos_pedal = -1.0
    @Volatile var AC_Relay = false
    @Volatile var BreakOn = false
    @Volatile var VTECOn = false

    /**
     * コンストラクタ
     */
    init{
        mIsCancel = false;

        if (socket == null) {
            Log.e(TAG, "parameter socket is null.");
        }else{
            try {
                mInputStream = socket.inputStream;
                mSocket = socket;
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
    }

    fun hexStringToByte(hexString: String) = hexString.toInt(16).toByte()

    override fun run(){
        //var buffer: byte[] = new byte[64];
        //var buffer = arrayOfNulls<byte>(64);

        var receiveData = ByteArray(64)
        var receiveDataLen = 0

        Log.i(TAG, "start read task.");
        while (mInputStream != null) {

            if (mIsCancel)
                break;

            try {

                var buffer = ByteArray(16)
                var readSize = mInputStream!!.read(buffer)

                for (b in buffer) {

                    if( readSize == 0)
                        break
                    readSize--

                    if( b.toInt() == 0x0D ) {
                        var str = ""
                        for (b in receiveData.copyOf(receiveDataLen)) {
                            str += String.format("%02X", b)
                        }
                        Log.e(TAG,  "size : " + receiveDataLen + " byte, message : " + str );
                        Log.e(TAG,  "size : " + receiveDataLen + " byte, message : " + receiveData.copyOf(receiveDataLen).toString(
                            Charset.defaultCharset()) );

                        str = receiveData.copyOf(receiveDataLen).toString(Charset.defaultCharset())
                        if( str != "OK" && str != "DATA ERROR" && str != "NO DATA") {

                            val srv = (receiveData.copyOfRange(0, 2)).toString(Charset.defaultCharset())
                            val pid = (receiveData.copyOfRange(3, 5)).toString(Charset.defaultCharset())

                            if (srv == "41") {
                                when (pid) {
                                    "04" -> {    // Calculated Engine Load PID (Honda CLV)
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        engine_load = ((data1 * 100.0) / 255.0)
                                    }
                                    "05" -> {    // Engine coolant temperature PID
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        coolant_temp = data1 - 40
                                    }
                                    "0B" -> {    // Intake manifold absolute pressure PID
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        manifold_abs_press = data1 - 0
                                    }
                                    "0C" -> {    // Engine RPM PID - Two bytes
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        val data2 = receiveData.copyOfRange(9, 11)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        engine_rpm = ((data1 * 256) + data2) / 4
                                    }
                                    "0D" -> {    // Vehicle speed PID
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        vehicle_spd = data1 - 0
                                    }
                                    "0F" -> {    // Intake air temperature PID
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        air_temp = data1 - 40
                                    }
                                    "11" -> {    // Throttle Position Sensor (sensor potentiometer on throttle body)
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        throttle_pos_body = ((data1 * 100.0) / 255.0)
                                    }
                                    "33" -> {    // Absolute Barometric Pressure PID
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        abs_barometric_press = data1 - 0
                                    }
                                    "42" -> {    // Control module voltage PID (ECU Voltage)
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        val data2 = receiveData.copyOfRange(9, 11)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        voltage = ((data1 * 256.0) + data2) / 1000.0
                                    }
                                    "45" -> {    // Relative Throttle Position Sensor (Pedal Position) PID (scaled from TPS value)
                                        val data1 = receiveData.copyOfRange(6, 8)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        throttle_pos_pedal = ((data1 * 100.0) / 255.0)
                                    }
                                }
                            }else if (srv == "60") {
                                when (pid) {
                                    "08" -> {    // Custom PID - Relay and switch flags
                                        val data1 = receiveData.copyOfRange(6, 7)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        AC_Relay = (data1 and 0x02) == 0x02
                                        BreakOn  = (data1 and 0x08) == 0x08
                                        VTECOn   = (data1 and 0x80) == 0x80
                                    }
                                    "0A" -> {    // Custom PID - Relay and switch flags
                                        val data1 = receiveData.copyOfRange(6, 7)
                                            .toString(Charset.defaultCharset()).toInt(16)
                                        //AC_Relay = (data1 and 0x02) == 0x02
                                    }
                                }
                            }
                        }
                        receiveData = ByteArray(64)
                        receiveDataLen = 0
                        break

                    } else if( b.toInt() != 0x0A ){
                        receiveData[receiveDataLen] = b
                        receiveDataLen ++
                    }
                }
//                if (readSize == 64) {
//                    // 処理.
//                    var str = ""
//                    for (b in buffer) {
//                        str += String.format("%02X", b)
//                    }
//                    Log.e(TAG,  "size : " + readSize + " byte, message : " + str );
//                } else {
//                    Log.e(TAG, "NG " + readSize + "byte");
//                }

                Thread.sleep(0);
            } catch (e: IOException) {
                e.printStackTrace();
                break;
            } catch (e: InterruptedException) {
                Log.e(TAG, "InterruptedException!!");
                // NOP.
                break;
            }
        }
        Log.i(TAG, "exit read task.");
    }

    fun cancel(){
        mIsCancel = true
    }

    fun finish(){
        if (mSocket == null) {
            return;
        }

        try {
            mSocket!!.close();
        } catch (e: IOException) {
            e.printStackTrace();
        }

    }
}