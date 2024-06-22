
package cr.ac.una.andersonRymichaelS

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import cr.ac.una.andersonRymichaelS.Entity.MarkedPlace
import cr.ac.una.andersonRymichaelS.dao.WikiDao
import cr.ac.una.andersonRymichaelS.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private var contNotificacion = 2
    private val notifiedPlaces = mutableSetOf<String>()
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    private val LOCATION_DIFFERENCE_THRESHOLD = 0.01

    private lateinit var database: AppDatabase
    private lateinit var wikiDao: WikiDao

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        startForeground(1, createNotification("Service running"))

        database = AppDatabase.getDatabase(this)
        wikiDao = database.wikiDao()

        requestLocationUpdates()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "locationServiceChannel",
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, "locationServiceChannel")
            .setContentTitle("Location Service")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000
        ).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                if (isSignificantLocationChange(location.latitude, location.longitude)) {
                    saveMarkedPlace(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun isSignificantLocationChange(latitude: Double, longitude: Double): Boolean {
        val latitudeDifference = abs(latitude - lastLatitude)
        val longitudeDifference = abs(longitude - lastLongitude)
        return if (latitudeDifference > LOCATION_DIFFERENCE_THRESHOLD || longitudeDifference > LOCATION_DIFFERENCE_THRESHOLD) {
            lastLatitude = latitude
            lastLongitude = longitude
            true
        } else {
            false
        }
    }

    private fun saveMarkedPlace(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(this@LocationService, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val placeName = addresses[0].locality
                    val currentTimeMillis = System.currentTimeMillis()
                    val wikipediaArticleTitle = getWikipediaArticleTitle(placeName)
                    val markedPlace = MarkedPlace(
                        latitude = latitude,
                        longitude = longitude,
                        detectedAt = currentTimeMillis,
                        wikipediaArticleTitle = wikipediaArticleTitle,
                        placeName = placeName
                    )
                    wikiDao.insertMarkedPlace(markedPlace)
                    sendNotification("Saved marked place: $placeName")
                } else {
                    sendNotification("Unable to fetch address")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sendNotification("Error saving marked place")
            }
        }
    }

    private fun getWikipediaArticleTitle(placeName: String?): String {
        // Aquí deberías implementar la lógica para obtener el título del artículo de Wikipedia
        // relacionado con el lugar marcado.
        return "Placeholder Wikipedia Article Title"
    }

    private fun sendNotification(message: String) {
        contNotificacion++

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "locationServiceChannel")
            .setContentTitle("Location Service Notification")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher, "Show", pendingIntent)
            .build()

        notificationManager.notify(contNotificacion, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
