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
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import cr.ac.una.andersonRymichaelS.Entity.MarkedPlace
import cr.ac.una.andersonRymichaelS.dao.WikiDao
import cr.ac.una.andersonRymichaelS.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private lateinit var placesClient: PlacesClient
    private lateinit var database: AppDatabase
    private lateinit var wikiDao: WikiDao
    private var contNotification = 2
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    private val LOCATION_DIFFERENCE_THRESHOLD = 0.01

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Places.initialize(applicationContext, "AIzaSyBLiFVeg7U_Ugu5bMf7EQ_TBEfPE3vOSF4")
        placesClient = Places.createClient(this)
        database = AppDatabase.getDatabase(this)
        wikiDao = database.wikiDao()

        createNotificationChannel()
        startForeground(1, createNotification("Service running"))

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
            LocationRequest.PRIORITY_HIGH_ACCURACY, 10000
        ).apply {
            setFastestInterval(5000)
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

    private fun setFastestInterval(i: Int) {

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                if (isSignificantLocationChange(location.latitude, location.longitude)) {
                    getPlaceName(location.latitude, location.longitude)
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

    private fun getPlaceName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val placeName = addresses[0].locality
                sendNotification("Current Location: $placeName (Lat: $latitude, Long: $longitude)", placeName)
                if (placeName != null) {
                    fetchRelatedWikipediaContent(placeName)
                }
            } else {
                sendNotification("Location: Lat: $latitude, Long: $longitude", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendNotification("Location: Lat: $latitude, Long: $longitude", null)
        }
    }

    private fun fetchRelatedWikipediaContent(placeName: String) {
        val url = "https://en.wikipedia.org/api/rest_v1/page/related/${placeName.replace(" ", "_")}"
        Executors.newSingleThreadExecutor().execute {
            try {
                val apiResponse = URL(url).readText()
                val jsonObject = JSONObject(apiResponse)
                val pages = jsonObject.getJSONArray("pages")

                if (pages.length() > 0) {
                    val relatedArticles = mutableListOf<String>()
                    for (i in 0 until pages.length()) {
                        val page = pages.getJSONObject(i)
                        val title = page.getString("title")
                        relatedArticles.add(title)
                    }
                    if (relatedArticles.isNotEmpty()) {
                        saveMarkedPlace(placeName, relatedArticles[0])
                        sendNotification("Related Wikipedia Content: ${relatedArticles.joinToString(", ")}", placeName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveMarkedPlace(placeName: String, wikipediaArticleTitle: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentTimeMillis = System.currentTimeMillis()
            val markedPlace = MarkedPlace(
                latitude = lastLatitude,
                longitude = lastLongitude,
                detectedAt = currentTimeMillis,
                wikipediaArticleTitle = wikipediaArticleTitle,
                placeName = placeName
            )
            wikiDao.insertMarkedPlace(markedPlace)
        }
    }

    private fun sendNotification(message: String, placeName: String?) {
        contNotification++

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("place_name", placeName)

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

        notificationManager.notify(contNotification, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}