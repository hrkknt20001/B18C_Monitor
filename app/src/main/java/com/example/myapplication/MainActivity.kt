package com.example.myapplication

//import android.R
import android.app.Notification
import android.bluetooth.BluetoothAdapter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.anastr.speedviewlib.TubeSpeedometer
import com.github.anastr.speedviewlib.components.Style
import kotlinx.android.synthetic.main.fragment_screen_slide_page_2.*
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

private const val NUM_PAGES = 3

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
    private var engineRPM : TubeSpeedometer? = null;
    private var textView_water_temp :TextView? = null
    private var textView_intake_temp  :TextView? = null
    private var textView_time  :TextView? = null
    private var textView_speed :TextView? = null
    private var textView_gear_pos :TextView? = null

    private var finalGear :Double = 4.4
    private var tireSize : Double = 596 * Math.PI / 1000.0
    private var mm2km : Double = 60.0 / 1000.0

    private var mTimer : Timer?      = null;
    private var mHandler: Handler = Handler()

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when(position){
                    0 -> {
                        textView_coolant_temp = findViewById<TextView>(R.id.textView_pid_05);
                        textView_engine_load = findViewById<TextView>(R.id.textView_pid_04);
                        textView_manifold_abs_press = findViewById<TextView>(R.id.textView_pid_0b);
                        textView_engine_rpm = findViewById<TextView>(R.id.textView_pid_0c);
                        textView_vehicle_spd = findViewById<TextView>(R.id.textView_pid_0d);
                        textView_air_temp = findViewById<TextView>(R.id.textView_pid_0f);
                        textView_throttle_pos_b = findViewById<TextView>(R.id.textView_pid_11);
                        textView_throttle_pos_p = findViewById<TextView>(R.id.textView_pid_45);
                        textView_ecu_voltage = findViewById<TextView>(R.id.textView_pid_42);
                        textView_gear_pos = findViewById<TextView>(R.id.textView_gear) ;
                    }
                    1 -> {
                        engineRPM = findViewById<TubeSpeedometer>(R.id.awesomeSpeedometer);
                        textView_water_temp = findViewById<TextView>(R.id.textView_water_temp);
                        textView_intake_temp = findViewById<TextView>(R.id.textView_intake_temp);
                        textView_time = findViewById<TextView>(R.id.textView_time);
                        textView_speed = findViewById<TextView>(R.id.textView_speed);
                        textView_gear_pos = findViewById<TextView>(R.id.textView_gear_2) ;
                    }
                    2 -> {

                    }
                }
            }
        })

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter

//        val button: Button = findViewById<Button>(R.id.button1)
//        button.setOnClickListener{
//            mOutputStream!!.write("0105\r".toByteArray())
//            //mOutputStream!!.write("ATD\r".toByteArray())
//        }


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

                    if( textView_coolant_temp != null) {
                        textView_coolant_temp!!.setText(
                            mReceiveTask!!.coolant_temp.toString()
                        )
                        if (mReceiveTask!!.coolant_temp < 70) {
                            textView_coolant_temp!!.setTextColor(Color.BLUE)
                        } else if (110 < mReceiveTask!!.coolant_temp) {
                            textView_coolant_temp!!.setTextColor(Color.RED)
                        } else {
                            textView_coolant_temp!!.setTextColor(Color.WHITE)
                        }
                    }
                    if( textView_engine_rpm != null) {
                        textView_engine_rpm!!.setText(
                            mReceiveTask!!.engine_rpm.toString()
                        )
                        if (8000 < mReceiveTask!!.engine_rpm) {
                            textView_engine_rpm!!.setTextColor(Color.RED)
                        }
                    }

                    if( textView_vehicle_spd != null) {
                        textView_vehicle_spd!!.setText(
                            mReceiveTask!!.vehicle_spd.toString()
                        )
                    }

                    if( textView_engine_load != null) {
                        textView_engine_load!!.setText(
                            mReceiveTask!!.engine_load.toInt().toString()
                        )
                    }

                    if( textView_manifold_abs_press!= null) {
                        textView_manifold_abs_press!!.setText(
                            mReceiveTask!!.manifold_abs_press.toString()
                        )
                    }

                    if(textView_air_temp != null) {
                        textView_air_temp!!.setText(
                            mReceiveTask!!.air_temp.toString()
                        )
                    }

                    if( textView_throttle_pos_b!=null) {
                        textView_throttle_pos_b!!.setText(
                            mReceiveTask!!.throttle_pos_body.toInt().toString()
                        )
                    }

                    if( textView_throttle_pos_p!=null) {
                        textView_throttle_pos_p!!.setText(
                            mReceiveTask!!.throttle_pos_pedal.toInt().toString()
                        )
                    }

                    if(textView_ecu_voltage!=null) {
                        textView_ecu_voltage!!.setText(
                            String.format("%.1f", mReceiveTask!!.voltage.toFloat())
                        )
                    }

                    //engineRPM?.speedTo(3000f, 200)
                    engineRPM?.speedTo(mReceiveTask!!.engine_rpm.toFloat(), 0)
//                    if (8000 < mReceiveTask!!.engine_rpm) {
//                        engineRPM?.setSpeedometerColor(0xFF0000)
//                    }else{
//                        engineRPM?.setSpeedometerColor(0xFFFFFF)
//                    }

                    if( textView_water_temp != null) {
                        textView_water_temp!!.setText(
                            mReceiveTask!!.coolant_temp.toString()
                        )
                        if (mReceiveTask!!.coolant_temp < 70) {
                            textView_water_temp!!.setTextColor(Color.BLUE)
                        } else if (110 < mReceiveTask!!.coolant_temp) {
                            textView_water_temp!!.setTextColor(Color.RED)
                        } else {
                            textView_water_temp!!.setTextColor(Color.WHITE)
                        }
                    }

                    if(textView_intake_temp != null) {
                        textView_intake_temp!!.setText(
                            mReceiveTask!!.air_temp.toString()
                        )
                    }

                    if(textView_time != null) {
                        textView_time!!.setText(getTime())
                    }

                    if( textView_speed != null) {
                        textView_speed!!.setText(
                            mReceiveTask!!.vehicle_spd.toString()
                        )
                    }

                    if( textView_gear_pos != null) {

                        if( mReceiveTask!!.engine_rpm != -1 && mReceiveTask!!.vehicle_spd != -1 ){
                            var rate: Double = 0.0
                            var gearPos: Int = 0
                            rate = mReceiveTask!!.engine_rpm.toDouble() / finalGear * tireSize * mm2km / mReceiveTask!!.vehicle_spd.toDouble()

                            if( abs(rate-3.230) < 3.230 * 0.2 ){       //
                                textView_gear_pos!!.text = "1"
                            }else if( abs(rate-2.105) < 2.105 * 0.2 ){ // 0.421
                                textView_gear_pos!!.text = "2"
                            }else if( abs(rate-1.458) < 1.458 * 0.15 ){ // 0.291
                                textView_gear_pos!!.text = "3"
                            }else if( abs(rate-1.107) < 1.107 * 0.15 ){ // 0.221
                                textView_gear_pos!!.text = "4"
                            }else if( abs(rate-0.848) < 0.848 * 0.10 ){ // 0.169
                                textView_gear_pos!!.text = "5"
                            }else{
                                textView_gear_pos!!.text = "-"
                            }
                        }
                    }
                }
            }
        }, 200, 200)
    }

    fun getTime(): String {
        val date = Date()
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(date)
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

/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
 */
private class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = NUM_PAGES

    /* override fun createFragment(position: Int): Fragment = ScreenSlidePageFragment() */
    override fun createFragment(position: Int): Fragment {
        when( position ){
            0 -> {return ScreenSlidePageFragment1()}
            1 -> {return ScreenSlidePageFragment2()}
            else -> {return ScreenSlidePageFragment3()}
        }
    }
}