package com.example.weatherapp.database;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.weatherapp.dao.SearchHistoryDao;
import com.example.weatherapp.entity.SearchHistory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {SearchHistory.class}, version = 1)
public abstract class WeatherAppDatabase extends RoomDatabase {

    private static WeatherAppDatabase INSTANCE;

    // Pool chạy nền cho DB
    public static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(4);


    public abstract SearchHistoryDao searchHistoryDao();

    public static  WeatherAppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    WeatherAppDatabase.class,
                    "weather_app.db"
            ).build();
        }
        return INSTANCE;
    }
}