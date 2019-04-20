package live.preventure.shakecounter

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.support.v4.app.NotificationCompat

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
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.app_name)
            val channelName = getString(R.string.app_name)
            val chan = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH
            )
            chan.lightColor = Color.BLUE
            chan.importance = NotificationManager.IMPORTANCE_NONE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            channelId
        } else {
            ""
        }
        startForeground(
            (System.currentTimeMillis() / 1000).toInt(),
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.tip_sensor_service))
                .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0))
                .build()
        )
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
