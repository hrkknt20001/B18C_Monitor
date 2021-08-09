package com.example.myapplication

//import android.R
import android.bluetooth.BluetoothAdapter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    var mBluetoothAdapter: BluetoothAdapter? = null
    private var mReceiveTask: BluetoothReceiveTask? = null
    private var mOutputStream: OutputStream? = null;

    private var textView_coolant_temp :TextView? = null
    private var textView_engine_load :TextView? = null
    private var textView_manifold_abs_press  :TextView? = null
    private var textView_engine_rpm  :TextView? = null
    private var textView_vehicle_spd  :TextView? = null
    private var textView_air_temp  :TextView? = null
    private var textView_throttle_pos_b :TextView? = null
    private var textView_throttle_pos_p :TextView? = null
    private var textView_ecu_voltage :TextView? = null

    private var mTimer : Timer?      = null;
    private var mHandler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val button: Button = findViewById<Button>(R.id.button1)
//        button.setOnClickListener{
//            mOutputStream!!.write("0105\r".toByteArray())
//            //mOutputStream!!.write("ATD\r".toByteArray())
//        }

        textView_coolant_temp = findViewById<TextView>(R.id.textView_pid_05);
        textView_engine_load = findViewById<TextView>(R.id.textView_pid_04);
        textView_manifold_abs_press = findViewById<TextView>(R.id.textView_pid_0b);
        textView_engine_rpm = findViewById<TextView>(R.id.textView_pid_0c);
        textView_vehicle_spd = findViewById<TextView>(R.id.textView_pid_0d);
        textView_air_temp = findViewById<TextView>(R.id.textView_pid_0f);
        textView_throttle_pos_b = findViewById<TextView>(R.id.textView_pid_11);
        textView_throttle_pos_p = findViewById<TextView>(R.id.textView_pid_45);
        textView_ecu_voltage = findViewById<TextView>(R.id.textView_pid_42);

        Log.d("My App", "onCreate");

        // Bluetooth 通信手順
        // 1. BluetoothAdapterを取得します
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d("My App", "エラー Bluetooth なし");
            return;
        }
        Log.d("My App", "Bluetooth つかえるよー");

        // 2. ペアリング済みのデバイス一覧を取得します
        var connectTread: SerialPortProfileConnectThread? = null
        val pairedDevices = mBluetoothAdapter!!.bondedDevices

        // 3. 任意のデバイスへ接続します
        val deviceName = "DSD TECH HC-05"

        if (pairedDevices != null) {
            for (device in pairedDevices) {
                Log.d("My App", "Bluetooth：" + device.name);
                if (deviceName == device.name) {
                    connectTread = SerialPortProfileConnectThread(device);
                    break;
                }
            }

            if(connectTread!=null) {
                // 4. 接続前に Bluetooth デバイスの検索を停止します
                mBluetoothAdapter!!.cancelDiscovery();

                connectTread.start()

                // 5. SerialPortProfileConnectThreadを開始し、ソケットを接続します
                //    下記は sleep() を使用した暫定処理です.
                try {
                    Thread.sleep(2000)

                    mReceiveTask = BluetoothReceiveTask(connectTread.getSocket());
                    mReceiveTask!!.start();

                    mOutputStream = connectTread.getSocket()?.outputStream

                } catch (e: InterruptedException) {

                    e.printStackTrace()
                }
            }
        }else{
            Log.d("My App", "Bluetooth ペアリング済みのデバイスがないよー");
        }
        Log.d("My App", "おしまい");

        mTimer = Timer(true)
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {

                // mHandlerを通じてUI Threadへ処理をキューイング
                mHandler.post { //実行間隔分を加算処理

                    textView_coolant_temp!!.setText(
                        mReceiveTask!!.coolant_temp.toString()
                    )
                    if( mReceiveTask!!.coolant_temp < 70 ){
                        textView_coolant_temp!!.setTextColor(Color.BLUE)
                    }else if( 110 < mReceiveTask!!.coolant_temp ){
                        textView_coolant_temp!!.setTextColor(Color.RED)
                    }else{
                        textView_coolant_temp!!.setTextColor(Color.WHITE)
                    }

                    textView_engine_rpm!!.setText(
                        mReceiveTask!!.engine_rpm.toString()
                    )
                    if( 8000 < mReceiveTask!!.coolant_temp ){
                        textView_engine_rpm!!.setTextColor(Color.RED)
                    }

                    textView_vehicle_spd!!.setText(
                        mReceiveTask!!.vehicle_spd.toString()
                    )

                    textView_engine_load!!.setText(
                        mReceiveTask!!.engine_load.toInt().toString()
                    )
                    textView_manifold_abs_press!!.setText(
                        mReceiveTask!!.manifold_abs_press.toString()
                    )

                    textView_air_temp!!.setText(
                        mReceiveTask!!.air_temp.toString()
                    )
                    textView_throttle_pos_b!!.setText(
                        mReceiveTask!!.throttle_pos_body.toInt().toString()
                    )
                    textView_throttle_pos_p!!.setText(
                        mReceiveTask!!.throttle_pos_pedal.toInt().toString()
                    )
                    textView_ecu_voltage!!.setText(
                        String.format("%.1f", mReceiveTask!!.voltage.toFloat())
                    )
                }
            }
        }, 200, 200)
    }

    override fun onDetachedFromWindow() {

        if( mTimer != null) {
            mTimer!!.cancel();
            mTimer = null;
        }

        if (mReceiveTask != null) {
            mReceiveTask!!.cancel();
        }

        // キャンセル完了を待つ処理など.
        if (mReceiveTask != null) {
            mReceiveTask!!.finish();
            mReceiveTask = null;
        }
        super.onDetachedFromWindow()
    }
}
