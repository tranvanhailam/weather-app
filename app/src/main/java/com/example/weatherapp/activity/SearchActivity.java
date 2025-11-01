package com.example.weatherapp.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.helper.BottomNavHelper;
import com.example.weatherapp.repository.SearchHistoryRepository;
import com.example.weatherapp.utils.ThemeUtils;

public class SearchActivity extends AppCompatActivity {

    SearchHistoryRepository searchHistoryRepository;
    TextView textViewPickTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavHelper.setupBottomNav(this);
//        ThemeUtils.applySavedMode(this);

        searchHistoryRepository= new SearchHistoryRepository(SearchActivity.this);

        textViewPickTitle= findViewById(R.id.tvPickTitle);

//        searchHistoryRepository.getSearchHistoryById(1)
//                .observe(this, item -> textViewPickTitle.setText(item.getName()));

    }
}