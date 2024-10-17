import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.google.android.gms.location.FusedLocationProviderClient
import fr.eya.weatherapp.ForecastItem
import fr.eya.weatherapp.WeatherViewModel

@Composable
fun ForecastScreen(fusedLocationClient: FusedLocationProviderClient, viewModel: WeatherViewModel) {
    val context = LocalContext.current // Get the context
    val forecast = viewModel.forecast.collectAsState().value

    var locationError by remember { mutableStateOf<String?>(null) }

    // Fetch the forecast based on location
    LaunchedEffect(Unit) {
        getCurrentLocationAndFetchForecast(fusedLocationClient, viewModel, context) { error ->
            locationError = error
        }
    }

    if (locationError != null) {
        Text(text = locationError ?: "Unknown error")
    } else if (forecast != null) {
        LazyColumn {
            items(forecast.list) { forecastItem ->
                ForecastItemRow(forecastItem)
            }
        }
    } else {
        // Show loading or error message
        CircularProgressIndicator(color = Color.Green , )    }
}

@Composable
fun ForecastItemRow(forecastItem: ForecastItem) {
    val weather = forecastItem.weather[0]

    // Random color for demonstration, you can replace this with your color logic
    val color = when (forecastItem.dt_txt.substring(0, 10)) { // Get date only (YYYY-MM-DD)
        "2024-10-17" -> Color.LightGray
        "2024-10-18" -> Color.Blue
        "2024-10-19" -> Color.Green
        "2024-10-20" -> Color.Yellow
        "2024-10-21" -> Color.Gray
        else -> Color.White
    }

    // Display forecast in a colored card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color) // Set the background color
        ) {
            Column {
                Text(
                    text = "Date: ${forecastItem.dt_txt}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Temperature: ${forecastItem.main.temp}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(text = weather.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
// Helper function to fetch forecast using location
fun getCurrentLocationAndFetchForecast(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: WeatherViewModel,
    context: Context,
    onError: (String?) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onError("Location permission not granted.")
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            // Log or print the latitude and longitude for debugging
            Log.d("LocationDebug", "Location: ${location.latitude}, ${location.longitude}")
            viewModel.getFiveDayForecast(location.latitude.toString(), location.longitude.toString(), "3a6fc0a544fb9c0595e8629ccac8fdfc")
        } else {
            onError("Failed to get location.")
        }
    }.addOnFailureListener { exception ->
        onError("Failed to get location: ${exception.message}")
    }
}
