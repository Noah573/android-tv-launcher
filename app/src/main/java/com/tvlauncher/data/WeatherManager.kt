package com.tvlauncher.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.tvlauncher.util.Constants
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Weather data returned by the manager.
 */
data class WeatherData(
    val city: String = Constants.DEFAULT_WEATHER_CITY,
    val temperature: Double = 0.0,
    val icon: WeatherIcon = WeatherIcon.CLOUDY
)

enum class WeatherIcon {
    SUNNY, PARTLY_CLOUDY, CLOUDY, RAINY, SNOWY, FOGGY, NIGHT_CLEAR
}

/**
 * Manages weather data retrieval from Open-Meteo API with local caching.
 */
class WeatherManager(context: Context) {

    companion object {
        private const val TAG = "WeatherManager"
        private const val API_URL =
            "https://api.open-meteo.com/v1/forecast?current=temperature_2m,weather_code&latitude=39.9&longitude=116.4"
    }

    private val prefsManager = PrefsManager(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    @Volatile
    private var cachedWeather: WeatherData? = null

    /**
     * Fetches weather data. Returns cached data if still valid, otherwise makes a network request.
     * On network failure returns cached data or a default [WeatherData].
     */
    fun getWeather(callback: (WeatherData) -> Unit) {
        // Check in-memory cache first
        cachedWeather?.let {
            callback(it)
            return
        }

        // Check disk cache
        if (prefsManager.isWeatherCacheValid()) {
            val cached = loadFromCache()
            if (cached != null) {
                cachedWeather = cached
                callback(cached)
                return
            }
        }

        // Network request
        val request = Request.Builder()
            .url(API_URL)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "Weather fetch failed: ${e.message}")
                val fallback = loadFromCache() ?: defaultWeather()
                cachedWeather = fallback
                callback(fallback)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        val fallback = loadFromCache() ?: defaultWeather()
                        cachedWeather = fallback
                        callback(fallback)
                        return
                    }

                    val body = response.body?.string() ?: throw IOException("Empty body")
                    val apiResponse = gson.fromJson(body, OpenMeteoResponse::class.java)
                    val temp = apiResponse.current?.temperature2m ?: 0.0
                    val weatherCode = apiResponse.current?.weatherCode ?: 0
                    val icon = weatherCodeToIcon(weatherCode)

                    val weatherData = WeatherData(
                        city = Constants.DEFAULT_WEATHER_CITY,
                        temperature = temp,
                        icon = icon
                    )

                    // Cache to disk and memory
                    prefsManager.saveWeatherCache(body)
                    cachedWeather = weatherData
                    callback(weatherData)
                } catch (e: Exception) {
                    Log.w(TAG, "Weather parse failed: ${e.message}")
                    val fallback = loadFromCache() ?: defaultWeather()
                    cachedWeather = fallback
                    callback(fallback)
                }
            }
        })
    }

    /**
     * Synchronous weather fetch for simple use cases. Returns cached or default.
     */
    fun getWeatherSync(): WeatherData {
        cachedWeather?.let { return it }

        if (prefsManager.isWeatherCacheValid()) {
            val cached = loadFromCache()
            if (cached != null) {
                cachedWeather = cached
                return cached
            }
        }

        return try {
            val request = Request.Builder().url(API_URL).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw IOException("Empty body")
            val apiResponse = gson.fromJson(body, OpenMeteoResponse::class.java)
            val temp = apiResponse.current?.temperature2m ?: 0.0
            val weatherCode = apiResponse.current?.weatherCode ?: 0
            val icon = weatherCodeToIcon(weatherCode)

            val weatherData = WeatherData(
                city = Constants.DEFAULT_WEATHER_CITY,
                temperature = temp,
                icon = icon
            )
            prefsManager.saveWeatherCache(body)
            cachedWeather = weatherData
            weatherData
        } catch (e: Exception) {
            Log.w(TAG, "Sync weather fetch failed: ${e.message}")
            val fallback = loadFromCache() ?: defaultWeather()
            cachedWeather = fallback
            fallback
        }
    }

    private fun loadFromCache(): WeatherData? {
        val json = prefsManager.getWeatherCache() ?: return null
        return try {
            val apiResponse = gson.fromJson(json, OpenMeteoResponse::class.java)
            val temp = apiResponse.current?.temperature2m ?: return null
            val weatherCode = apiResponse.current?.weatherCode ?: 0
            WeatherData(
                city = Constants.DEFAULT_WEATHER_CITY,
                temperature = temp,
                icon = weatherCodeToIcon(weatherCode)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun defaultWeather(): WeatherData {
        return WeatherData(
            city = Constants.DEFAULT_WEATHER_CITY,
            temperature = 0.0,
            icon = WeatherIcon.CLOUDY
        )
    }

    /**
     * Maps WMO weather codes to simplified weather icons.
     * See: https://open-meteo.com/en/docs
     */
    private fun weatherCodeToIcon(code: Int): WeatherIcon {
        return when (code) {
            0 -> WeatherIcon.SUNNY                          // Clear sky
            1, 2 -> WeatherIcon.PARTLY_CLOUDY               // Mainly clear, partly cloudy
            3 -> WeatherIcon.CLOUDY                          // Overcast
            in 45..48 -> WeatherIcon.FOGGY                   // Fog
            in 51..67 -> WeatherIcon.RAINY                   // Drizzle, rain
            in 71..77 -> WeatherIcon.SNOWY                   // Snow
            in 80..82 -> WeatherIcon.RAINY                   // Rain showers
            in 85..86 -> WeatherIcon.SNOWY                   // Snow showers
            in 95..99 -> WeatherIcon.RAINY                   // Thunderstorm
            else -> WeatherIcon.CLOUDY
        }
    }

    // ---- API Response Models ----

    private data class OpenMeteoResponse(
        @SerializedName("current") val current: CurrentWeather? = null
    )

    private data class CurrentWeather(
        @SerializedName("temperature_2m") val temperature2m: Double? = null,
        @SerializedName("weather_code") val weatherCode: Int? = null
    )
}
