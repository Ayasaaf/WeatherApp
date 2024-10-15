package fr.eya.weatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun WeatherAppUI(fusedLocationClient: FusedLocationProviderClient , viewModel: WeatherViewModel) {
    var weather by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val apiKey = "3a6fc0a544fb9c0595e8629ccac8fdfc" // Replace with your OpenWeather API key
    // Fetch weather when button is clicked
    fun fetchWeather(context: Context) {
        isLoading = true
        getCurrentLocationAndFetchWeather(
            fusedLocationClient,
          context ,
            apiKey
        ) { fetchedWeather, error ->
            isLoading = false
            weather = error ?: fetchedWeather
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedWeatherImage()

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { fetchWeather(context) }) {
            Text(text = "Get Weather")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color.Green)
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