package com.v2v.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText cityET, countryET;
    private MaterialButton searchBtn;
    private TextView dataTV, greetingTV, cityTitleTV, extraTV, timeTV;
    private LinearLayout loadingOverlay;
    private ImageView weatherIcon, background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityET = findViewById(R.id.cityName);
        countryET = findViewById(R.id.countryCode);
        searchBtn = findViewById(R.id.search);
        dataTV = findViewById(R.id.data);
        weatherIcon = findViewById(R.id.weatherIcon);
        background = findViewById(R.id.background);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        cityTitleTV = findViewById(R.id.cityTitle);
        extraTV = findViewById(R.id.extraInfo);
        timeTV = findViewById(R.id.dateTime);

        

        searchBtn.setOnClickListener(v -> {
            String city = cityET.getText().toString().trim();
            String country = countryET.getText().toString().trim().toUpperCase();

            if (city.isEmpty() || country.isEmpty()) {
                Toast.makeText(this, "Please enter both city and country code.", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoading(true);
            searchForecastData(city, country);
        });
    }


    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void searchForecastData(String city, String country) {
        new Thread(() -> {
            try {
                String apiKey = "42145bab1c3300c05c643ff4994f10fa";
                String query = city + "," + country;
                String urlString = "https://api.openweathermap.org/data/2.5/forecast?q=" + query + "&appid=" + apiKey + "&units=metric";

                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int responseCode = con.getResponseCode();
                if (responseCode != 200) {
                    throw new Exception("City not found or invalid query.");
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                parseForecastData(result.toString(), city, country);

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, "City not found. Please check spelling and country code.", Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void parseForecastData(String result, String city, String country) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray list = jsonObject.getJSONArray("list");
            JSONObject firstItem = list.getJSONObject(0);

            JSONObject main = firstItem.getJSONObject("main");
            final double temperature = main.getDouble("temp");
            final double feelsLike = main.getDouble("feels_like");
            final int humidity = main.getInt("humidity");
            final int pressure = main.getInt("pressure");

            JSONObject wind = firstItem.getJSONObject("wind");
            final double windSpeed = wind.getDouble("speed");

            JSONArray weatherArray = firstItem.getJSONArray("weather");
            final String weatherMain = weatherArray.getJSONObject(0).getString("main");
            final String weatherDescription = weatherArray.getJSONObject(0).getString("description");

            runOnUiThread(() -> {
                dataTV.setText("Temperature: " + temperature + "°C\nDescription: " + weatherDescription);
                extraTV.setText("Feels like: " + feelsLike + "°C\nHumidity: " + humidity + "%\nPressure: " + pressure + " hPa\nWind: " + windSpeed + " m/s");
                cityTitleTV.setText("Weather in " + city + ", " + country);

                String time = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
                timeTV.setText("Last updated: " + time);

                switch (weatherMain.toLowerCase()) {
                    case "clear":
                        weatherIcon.setImageResource(R.drawable.ic_sunny);
                        background.setImageResource(R.drawable.bg1);
                        break;
                    case "clouds":
                        weatherIcon.setImageResource(R.drawable.ic_cloudy);
                        background.setImageResource(R.drawable.bg2);
                        break;
                    case "rain":
                    case "drizzle":
                        weatherIcon.setImageResource(R.drawable.ic_rainy);
                        background.setImageResource(R.drawable.bg6);
                        break;
                    case "thunderstorm":
                        weatherIcon.setImageResource(R.drawable.ic_storm);
                        background.setImageResource(R.drawable.bg4);
                        break;
                    case "snow":
                        weatherIcon.setImageResource(R.drawable.ic_snowy);
                        background.setImageResource(R.drawable.bg7);
                        break;
                    case "mist":
                    case "fog":
                    case "haze":
                        weatherIcon.setImageResource(R.drawable.ic_weather_default);
                        background.setImageResource(R.drawable.bg5);
                        break;
                    default:
                        weatherIcon.setImageResource(R.drawable.ic_weather_default);
                        background.setImageResource(R.drawable.bg3);
                        break;
                }

                showLoading(false);
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                showLoading(false);
                Toast.makeText(MainActivity.this, "Failed to parse forecast data.", Toast.LENGTH_SHORT).show();
            });
        }
    }

}