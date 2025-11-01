package com.example.weatherapp.repository;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Dao;

import com.example.weatherapp.dao.SearchHistoryDao;
import com.example.weatherapp.database.WeatherAppDatabase;
import com.example.weatherapp.entity.SearchHistory;

import java.util.List;

public class SearchHistoryRepository {

    private final SearchHistoryDao searchHistoryDao;

    public SearchHistoryRepository(Context ctx) {
        this.searchHistoryDao = WeatherAppDatabase.getInstance(ctx).searchHistoryDao();
    }

    // Quan sát tự cập nhật
    public LiveData<List<SearchHistory>> getAllSearchHistory() {
        return searchHistoryDao.getAll();
    }


    public LiveData<SearchHistory> getSearchHistoryById(int id) {
        return searchHistoryDao.getById(id);
    }


    // Tác vụ nền (đừng chạy trên UI thread)
    public void addSearchHistory(SearchHistory c) {
        WeatherAppDatabase.DB_EXECUTOR.execute(() -> {
            searchHistoryDao.insert(c);
//            if (onDone != null) onDone.run();
        });
    }

    public void updateSearchHistory(SearchHistory c) {
        WeatherAppDatabase.DB_EXECUTOR.execute(() -> {
            searchHistoryDao.update(c);
//            if (onDone != null) onDone.run();
        });
    }

    public void deleteSearchHistory(SearchHistory c) {
        WeatherAppDatabase.DB_EXECUTOR.execute(() -> {
            searchHistoryDao.delete(c);
//            if (onDone != null) onDone.run();
        });
    }

//    public void searchByPrefix(String q, java.util.function.Consumer<List<SearchHistory>> callback) {
//        WeatherAppDatabase.DB_EXECUTOR.execute(() -> {
//            List<SearchHistory> result = searchHistoryDao.searchByPrefix(q);
//            if (callback != null) callback.accept(result);
//        });
//    }
}