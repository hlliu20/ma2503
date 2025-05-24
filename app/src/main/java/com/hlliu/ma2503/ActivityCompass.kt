package com.hlliu.ma2503

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.util.Locale

class ActivityCompass : ComponentActivity(),SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var magnetometerSensor: Sensor

    private lateinit var compassImageView: ImageView
    private lateinit var directionTextView: TextView
    private lateinit var locationTextView: TextView

    private lateinit var locationManager: LocationManager

    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    private var azimuth = 0f
    private var useGoogle = false
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationSettings() {
        if (!isLocationEnabled(this)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(intent)
        } else {
            getCurrentLocation()
        }
    }
    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (status == ConnectionResult.SUCCESS) {
            useGoogle = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_compass)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        compassImageView = findViewById(R.id.ivCompass)
        directionTextView = findViewById(R.id.tvCompass)
        locationTextView = findViewById(R.id.tvLocation)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        checkGooglePlayServices()
        checkLocationSettings()
        checkLocationPermissions()
    }
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.entries.all {
            it.value == true
        }
        if (isGranted) {
            // 权限被授予
            Toast.makeText(this, "定位权限已开启", Toast.LENGTH_SHORT).show()
        } else {
            // 权限被拒绝
            Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show()
        }
        getCurrentLocation()
    }
    private fun checkLocationPermissions() {
        val permissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        if (permissions.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            getCurrentLocation()
        } else {
            requestNotificationPermissionLauncher.launch(permissions)
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
//            azimuth = (azimuth + 360) % 360
            directionTextView.text = buildString {
                append("方位角:")
                append(String.format(locale = Locale.CHINA, "%.1f",azimuth))
                append("\n俯仰角:")
                append(String.format(locale = Locale.CHINA, "%.1f",Math.toDegrees(orientationAngles[1].toDouble()).toFloat()))
                append("\n滚动角:")
                append(String.format(locale = Locale.CHINA, "%.1f",Math.toDegrees(orientationAngles[2].toDouble()).toFloat()))
            }
            updateCompassDisplay(azimuth)
        }
    }

    private fun updateCompassDisplay(azimuth: Float) {
        val rotateAnimation = RotateAnimation(
            -this.azimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            fillAfter = true
        }

        compassImageView.startAnimation(rotateAnimation)
        this.azimuth = azimuth

//        val direction = getDirection(azimuth)

    }

    private fun getDirection(azimuth: Float): String {
        return when {
            azimuth >= 315 || azimuth < 45 -> "North"
            azimuth >= 45 && azimuth < 135 -> "East"
            azimuth >= 135 && azimuth < 225 -> "South"
            else -> "West"
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("Not yet implemented")
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationProvider = LocationManager.GPS_PROVIDER
            locationManager.requestSingleUpdate(
                locationProvider,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        locationTextView.text =
                            getString(R.string.location_latitude_longitude, latitude.toString(), longitude.toString())
                    }
                },
                null
            )
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}