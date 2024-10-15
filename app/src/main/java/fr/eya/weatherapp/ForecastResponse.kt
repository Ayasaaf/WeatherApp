package fr.eya.weatherapp

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long, // Timestamp of the forecast
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String // Date and time in text format
)

data class City(
    val name: String,
    val country: String
)
