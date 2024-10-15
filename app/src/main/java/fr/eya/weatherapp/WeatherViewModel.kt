package fr.eya.weatherapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _forecast = MutableStateFlow<ForecastResponse?>(null)
    val forecast: StateFlow<ForecastResponse?> = _forecast

    // Fetch the 5-day forecast
    fun getFiveDayForecast(latitude: String, longitude: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getFiveDayForecast(latitude, longitude, apiKey)
                _forecast.value = response
            } catch (e: Exception) {
                // Handle error
                Log.e("WeatherViewModel", "Error fetching forecast: ${e.message}")
            }
        }
    }
}