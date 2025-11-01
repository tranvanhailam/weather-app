package com.example.weatherapp.helper;

import android.app.Activity;
import android.content.Intent;

import com.example.weatherapp.activity.ForecastActivity;
import com.example.weatherapp.activity.MyLocationActivity;
import com.example.weatherapp.R;
import com.example.weatherapp.activity.SearchActivity;
import com.example.weatherapp.activity.SettingsActivity;

public class BottomNavHelper {
    public static void setupBottomNav(Activity activity) {
        activity.findViewById(R.id.navMyLocation).setOnClickListener(v ->
                activity.startActivity(new Intent(activity, MyLocationActivity.class)));

        activity.findViewById(R.id.navSearch).setOnClickListener(v ->
                activity.startActivity(new Intent(activity, SearchActivity.class)));

        activity.findViewById(R.id.navForecast).setOnClickListener(v ->
                activity.startActivity(new Intent(activity, ForecastActivity.class)));

        activity.findViewById(R.id.navSettings).setOnClickListener(v ->
                activity.startActivity(new Intent(activity, SettingsActivity.class)));
    }
}
