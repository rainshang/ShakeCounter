package live.preventure.shakecounter

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message

class SensorService : Service(), SensorEventListener {

    private val mBinder = SensorBinder()
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private val G = 9.81
    private val THRESHOLD = 1.5
    var isCounting = false
    var startTime = 0L
    var shakeCount = 0

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            isValid = true
            sendEmptyMessageDelayed(0, 500)
        }
    }
    private var isValid = true // in this 500ms window

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            accelerometer = it
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Required for sensor usage but not required for this project.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event!!.values // The three variables [x. y. z] used in shake calculations
        if (isValid) {
            val gx = values[0] / G
            val gy = values[1] / G
            val gz = values[2] / G
            val force = Math.sqrt(gx * gx + gy * gy + gz * gz)
            if (force > THRESHOLD) {
                shakeCount++
                isValid = false
            }
        }
    }

    fun startListenAcceleration() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        isCounting = true
        startTime = System.currentTimeMillis()
        shakeCount = 0
        mHandler.sendEmptyMessage(0)
    }

    fun stopListenAcceleration() {
        sensorManager.unregisterListener(this)
        isCounting = false
        shakeCount = 0
        mHandler.removeMessages(0)
    }

    inner class SensorBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }
}
