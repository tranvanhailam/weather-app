package com.example.weatherapp.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.entity.SearchHistory;
import com.example.weatherapp.helper.BottomNavHelper;
import com.example.weatherapp.repository.SearchHistoryRepository;
import com.example.weatherapp.utils.ThemeUtils;

public class MyLocationActivity extends AppCompatActivity {

    SearchHistoryRepository searchHistoryRepository;
    TextView textViewCity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavHelper.setupBottomNav(this);
//        ThemeUtils.applySavedMode(this);

        textViewCity= findViewById(R.id.city);
        searchHistoryRepository= new SearchHistoryRepository(MyLocationActivity.this);

//        SearchHistory searchHistory= new SearchHistory();
//        searchHistory.setName("Hanoi");
//        searchHistoryRepository.addSearchHistory(searchHistory);
//
//        searchHistoryRepository.getSearchHistoryById(1)
//                .observe(this, item -> textViewCity.setText(item.getName()));




    }
}