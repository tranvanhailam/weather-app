package com.example.weatherapp;

import android.app.Application;

import com.example.weatherapp.utils.ThemeUtils;

public class WeatherApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        ThemeUtils.applySavedMode(this); // CHỈ ở đây
    }
}