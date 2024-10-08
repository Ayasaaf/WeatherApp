package fr.eya.weatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.compose.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, display a toast
                Toast.makeText(this, "Permission granted, you can access location", Toast.LENGTH_SHORT).show()
            } else {
                // Permission not granted, display a toast
                Toast.makeText(this, "No location permission", Toast.LENGTH_SHORT).show()
            }
        }

        // Check and request location permission
        checkAndRequestLocationPermission()

        setContent {
            WeatherAppUI(fusedLocationClient)
        }
    }

    private fun checkAndRequestLocationPermission() {
        when {
            // Permission is already granted
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, proceed with fetching weather
            }

            else -> {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

@Composable
fun WeatherAppUI(fusedLocationClient: FusedLocationProviderClient) {
    var weather by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val apiKey = "3a6fc0a544fb9c0595e8629ccac8fdfc" // Replace with your OpenWeather API key
    val context = LocalContext.current
    // Fetch weather when button is clicked
    fun fetchWeather() {
        isLoading = true
        getCurrentLocationAndFetchWeather(
            fusedLocationClient,
            context ,
            apiKey
        ) { fetchedWeather, error ->
            isLoading = false
            if (error != null) {
                weather = error
            } else {
                weather = fetchedWeather
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedWeatherImage()

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { fetchWeather() }) {
            Text(text = "Get Weather")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.Green)
        } else if (weather != null) {
            Text(text = "Current Weather: $weather", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AnimatedWeatherImage() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.weathers))

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(200.dp)
    )
}

// Assuming you have defined this function to fetch location and weather
fun getCurrentLocationAndFetchWeather(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,  // Pass context from MainActivity
    apiKey: String,
    onWeatherFetched: (String?, String?) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,  // Use the passed context
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onWeatherFetched(null, "Location permission not granted.")
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude

            // Log the latitude and longitude
            println("Latitude: $latitude, Longitude: $longitude")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.getCurrentWeather(
                        latitude = latitude.toString(),
                        longitude = longitude.toString(),
                        apiKey = apiKey
                    )

                    val weatherData = "${response.weather.first().description.capitalize(Locale.ROOT)}, ${response.main.temp}°C"
                    onWeatherFetched(weatherData, null)

                } catch (e: Exception) {
                    onWeatherFetched(null, "Failed to fetch weather: ${e.message}")
                }
            }
        } else {
            onWeatherFetched(null, "Failed to get location.")
        }
    }
}