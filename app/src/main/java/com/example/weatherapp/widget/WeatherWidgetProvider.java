package com.example.weatherapp.widget;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.activity.MyLocationActivity;
import com.example.weatherapp.api.Client;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherWidgetProvider extends AppWidgetProvider {

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);

            // Mặc định hiển thị đang tải
            views.setTextViewText(R.id.tvCity, "Loading...");
            views.setTextViewText(R.id.tvTemp, "--°C");
            appWidgetManager.updateAppWidget(appWidgetId, views);

            // Khi click -> mở MyLocationActivity
            Intent intent = new Intent(context, MyLocationActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

            // Gọi hàm cập nhật vị trí và thời tiết
            updateWeather(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWeather(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa cấp quyền
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
            views.setTextViewText(R.id.tvCity, "No permission");
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                // Lấy tên thành phố
                String reverseUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=" + lat + "&longitude=" + lon + "&localityLanguage=en";

                io.execute(() -> {
                    try {
                        JSONObject json = Client.request(reverseUrl);
                        String cityName = json.optString("city", "Unknown");

                        // Gọi Open-Meteo API để lấy nhiệt độ hiện tại
                        String weatherUrl = "https://api.open-meteo.com/v1/forecast"
                                + "?latitude=" + lat
                                + "&longitude=" + lon
                                + "&current=temperature_2m"
                                + "&timezone=auto";

                        JSONObject weather = Client.request(weatherUrl);
                        JSONObject current = weather.optJSONObject("current");
                        double temp = current != null ? current.optDouble("temperature_2m", Double.NaN) : Double.NaN;

                        // Cập nhật giao diện widget
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
                        views.setTextViewText(R.id.tvCity, cityName);
                        views.setTextViewText(R.id.tvTemp, Double.isNaN(temp) ? "--°C" : String.format("%.1f°C", temp));

                        appWidgetManager.updateAppWidget(appWidgetId, views);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
