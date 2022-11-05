package com.example.ugd89_b_10712_project1

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ugd89_b_10712_project1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var proximitySensor: Sensor
    lateinit var sensorManager: SensorManager
    private var  mCamera: Camera? = null
    private var mCameraView: CameraView? = null
    private var currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    private val CHANNEL_ID = "channel_notification"
    private val notificationId = 101
    private var binding: ActivityMainBinding? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private lateinit var square: TextView
    var proximitySensorEventListener: SensorEventListener? = object : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type==Sensor.TYPE_PROXIMITY){
                if (event.values[0] ==0f){
                    changeCamera()
                }
            }
        }
    }

    private fun changeCamera(){

        try {
            mCamera?.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace();
        }
        mCamera?.release()

        if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
            mCamera?.release()
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else {
            mCamera?.release()
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        try {
            mCamera = Camera.open(currentCameraId)
        }catch (e: Exception){
            Log.d("error", "Failed to get Camera" + e.message)
        }
        if (mCamera != null){
            mCameraView = CameraView(this, mCamera!!)
            val camera_view = findViewById<View>(R.id.FlCamera) as FrameLayout
            camera_view.addView(mCameraView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        createNotificationChannel()
        try {
            mCamera = Camera.open()
        }catch (e: Exception){
            Log.d("error", "Failed to get Camera" + e.message)
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (mCamera != null){
            mCameraView = CameraView(this, mCamera!!)
            val camera_view = findViewById<View>(R.id.FlCamera) as FrameLayout
            camera_view.addView(mCameraView)
        }
        @SuppressLint("MissingInflatedId", "LocalSuppress") val imageClose =
            findViewById<View>(R.id.imgClose) as ImageButton
            imageClose.setOnClickListener{ view: View? -> System.exit(0)}


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setUpSensorStuff()

        if(proximitySensor == null){
            Toast.makeText(this,"No proximity sensor found in device..", Toast.LENGTH_SHORT).show()
            finish()
        }else{
            sensorManager.registerListener(
                proximitySensorEventListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val channel1 = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel1)
        }
    }
    private fun sendNotification() {

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications)
            .setContentTitle("Modul89_B_10789_PROJECT2")
            .setContentText("Selamat anda sudah berhasil Modul 8 dan 9")
            .setPriority(NotificationCompat.PRIORITY_LOW)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            currentAcceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 12) {
                sendNotification()
            }
        }
    }
}