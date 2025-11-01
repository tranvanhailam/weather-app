package com.example.weatherapp.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.helper.BottomNavHelper;
import com.example.weatherapp.repository.SearchHistoryRepository;
import com.example.weatherapp.utils.ThemeUtils;

public class SettingsActivity extends AppCompatActivity {

    SearchHistoryRepository searchHistoryRepository;
    Switch switchTheme, switchAllowNoti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavHelper.setupBottomNav(this);

        searchHistoryRepository= new SearchHistoryRepository(SettingsActivity.this);

        switchTheme = findViewById(R.id.switchTheme);
        switchAllowNoti= findViewById(R.id.switchAllowNoti);

        switchTheme.setOnCheckedChangeListener(null);
        switchTheme.setChecked(ThemeUtils.isDark(this));
        switchTheme.setOnCheckedChangeListener(themeListener);

    }

    private final CompoundButton.OnCheckedChangeListener themeListener =
            (btn, isChecked) -> {
                int ui = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                boolean isNightNow = (ui == Configuration.UI_MODE_NIGHT_YES);
                if (isChecked != isNightNow) {
                    ThemeUtils.setDark(this, isChecked); // KHÔNG gọi recreate() ở đây
                }
            };

}