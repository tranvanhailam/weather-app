package com.example.weatherapp.widget;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.activity.MyLocationActivity;
import com.example.weatherapp.api.Client;
import com.example.weatherapp.utils.ThemeUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "WeatherData";
    private static final String KEY_CITY = "city";
    private static final String KEY_TEMP = "temp";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final long STALE_TIME = 30 * 60 * 1000L; // 30 phút

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);

        //  DÒNG NÀY – ĐỔI MÀU NỀN THEO DARK MODE A7B4E0 101A39
        boolean isDark = ThemeUtils.isDark(context);
        int bgColor = isDark ? 0xFF101A39 : 0xFFA7B4E0;
        int textColor = isDark ? 0xFFFFFFFF : 0xFF000000;
        int subTextColor = isDark ? 0xFFAAAAAA : 0xFF888888;

        views.setInt(R.id.widgetRoot, "setBackgroundColor", bgColor);
        views.setTextColor(R.id.tvTemp, textColor);
        views.setTextColor(R.id.tvCity, textColor);
        views.setTextColor(R.id.tvUpdated, subTextColor);

        // 1. Cache hợp lệ → dùng ngay
        if (hasValidCache(context)) {
            updateFromCache(context, views);
        }
        // 2. Không có cache → lấy mới (nếu có quyền)
        else if (hasLocationPermission(context)) {
            views.setTextViewText(R.id.tvCity, "Loading...");
            views.setTextViewText(R.id.tvTemp, "--°C");
            updateFromNetwork(context, appWidgetManager, appWidgetId);
        }
        // 3. Chưa có quyền → hướng dẫn người dùng
        else {
            views.setTextViewText(R.id.tvCity, "Tap to enable");
            views.setTextViewText(R.id.tvTemp, "--°C");
        }

        // Click widget → mở MyLocationActivity + truyền widgetId
        Intent intent = new Intent(context, MyLocationActivity.class);
        intent.putExtra("FROM_WIDGET", true);
        intent.putExtra("WIDGET_ID", appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /* -------------------------------------------------
       CACHE
       ------------------------------------------------- */
    private boolean hasValidCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0);
        return (System.currentTimeMillis() - lastUpdate) < STALE_TIME
                && prefs.contains(KEY_CITY)
                && prefs.contains(KEY_TEMP);
    }

    private void updateFromCache(Context context, RemoteViews views) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String city = prefs.getString(KEY_CITY, "Unknown");
        float temp = prefs.getFloat(KEY_TEMP, Float.NaN);
        views.setTextViewText(R.id.tvCity, city);
        views.setTextViewText(R.id.tvTemp, Float.isNaN(temp) ? "--°C" : String.format("%.1f°C", temp));
    }

    private void saveToCache(Context context, String city, double temp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CITY, city);
        editor.putFloat(KEY_TEMP, (float) temp);
        editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
        editor.apply();
    }

    /* -------------------------------------------------
       PERMISSION
       ------------------------------------------------- */
    private boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /* -------------------------------------------------
       NETWORK (với try/catch + failure listener)
       ------------------------------------------------- */
    private void updateFromNetwork(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Đảm bảo quyền đã được cấp trước khi gọi
        if (!hasLocationPermission(context)) {
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
            v.setTextViewText(R.id.tvCity, "No permission");
            appWidgetManager.updateAppWidget(appWidgetId, v);
            return;
        }

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);

        try {
            client.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location == null) {
                            showError(context, appWidgetManager, appWidgetId, "No location");
                            return;
                        }
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        fetchWeatherAndUpdate(context, appWidgetManager, appWidgetId, lat, lon);
                    })
                    .addOnFailureListener(e -> {
                        // SecurityException hoặc bất kỳ lỗi nào
                        showError(context, appWidgetManager, appWidgetId, "Error");
                    });
        } catch (SecurityException e) {
            showError(context, appWidgetManager, appWidgetId, "Permission denied");
        }
    }

    private void fetchWeatherAndUpdate(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId, double lat, double lon) {
        io.execute(() -> {
            try {
                // ---- Thành phố ----
                String reverseUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude="
                        + lat + "&longitude=" + lon + "&localityLanguage=en";
                JSONObject json = Client.request(reverseUrl);
                String cityName = json.optString("city", "Unknown");

                // ---- Nhiệt độ ----
                String weatherUrl = "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + lat
                        + "&longitude=" + lon
                        + "&current=temperature_2m"
                        + "&timezone=auto";

                JSONObject weather = Client.request(weatherUrl);
                JSONObject current = weather.optJSONObject("current");
                double temp = current != null ? current.optDouble("temperature_2m", Double.NaN) : Double.NaN;

                // Lưu cache
                saveToCache(context, cityName, temp);

                // Cập nhật UI
                RemoteViews updated = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
                updated.setTextViewText(R.id.tvCity, cityName);
                updated.setTextViewText(R.id.tvTemp,
                        Double.isNaN(temp) ? "--°C" : String.format("%.1f°C", temp));
                appWidgetManager.updateAppWidget(appWidgetId, updated);

            } catch (Exception e) {
                e.printStackTrace();
                showError(context, appWidgetManager, appWidgetId, "Error");
            }
        });
    }

    private void showError(Context context, AppWidgetManager appWidgetManager,
                           int appWidgetId, String message) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        v.setTextViewText(R.id.tvCity, message);
        v.setTextViewText(R.id.tvTemp, "--°C");
        appWidgetManager.updateAppWidget(appWidgetId, v);
    }

    /* -------------------------------------------------
       RECEIVE BROADCAST từ MyLocationActivity
       ------------------------------------------------- */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WeatherWidgetProvider.class);
            int[] ids = appWidgetManager.getAppWidgetIds(thisWidget);
            onUpdate(context, appWidgetManager, ids);

        }
    }
}