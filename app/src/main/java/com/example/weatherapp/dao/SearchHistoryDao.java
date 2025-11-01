package com.example.weatherapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weatherapp.entity.SearchHistory;

import java.util.List;

@Dao
public interface SearchHistoryDao {

    @Query("SELECT * FROM search_history")
    LiveData<List<SearchHistory>> getAll();

    @Query("SELECT * FROM search_history WHERE id = :id LIMIT 1")
    LiveData<SearchHistory> getById(int id);


    @Insert
    void insert(SearchHistory searchHistory);

    @Update
    void update(SearchHistory searchHistory);

    @Delete
    void delete(SearchHistory searchHistory);

    @Query("DELETE FROM search_history")
    void deleteAll();
}
