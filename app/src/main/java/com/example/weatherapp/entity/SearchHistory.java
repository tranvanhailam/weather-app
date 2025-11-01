package com.example.weatherapp.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "search_history")
@AllArgsConstructor(onConstructor_ = @Ignore)
@NoArgsConstructor
@Getter
@Setter
public class SearchHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
}
