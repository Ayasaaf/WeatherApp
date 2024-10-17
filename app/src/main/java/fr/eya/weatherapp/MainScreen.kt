
package fr.eya.weatherapp



import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            navController.navigate("weather")
        }) {
            Text(text = "Get Current Temperature")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate("forecast")
        }) {
            Text(text = "Get Weather Forecast")
        }
    }
}

@Composable
fun WeatherScreen(fusedLocationClient: FusedLocationProviderClient, viewModel: WeatherViewModel) {
    WeatherAppUI(fusedLocationClient, viewModel)
}

