import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        Text(text = "Loading...")
    }
}

@Composable
fun ForecastItemRow(forecastItem: ForecastItem) {
    val weather = forecastItem.weather[0]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Date: ${forecastItem.dt_txt}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Temperature: ${forecastItem.main.temp}°C", style = MaterialTheme.typography.bodyMedium)
        Text(text = weather.description, style = MaterialTheme.typography.bodyMedium)
    }
}

// Helper function to fetch forecast using location
fun getCurrentLocationAndFetchForecast(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: WeatherViewModel,
    context: Context,  // Pass context here
    onError: (String?) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,  // Use context here
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onError("Location permission not granted.")
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            viewModel.getFiveDayForecast(location.latitude.toString(), location.longitude.toString(), "3a6fc0a544fb9c0595e8629ccac8fdfc")
        } else {
            onError("Failed to get location.")
        }
    }.addOnFailureListener {
        onError("Failed to get location: ${it.message}")
    }
}
