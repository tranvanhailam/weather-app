package com.example.weatherapp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DailyRow {
    public final String title; // Tuesday, Wednesday...
    public final String date;  // July 23...
    public final String temp;  // 20c...
    public final boolean isDay;
    public final int weatherCode;  // R.drawable...

}
