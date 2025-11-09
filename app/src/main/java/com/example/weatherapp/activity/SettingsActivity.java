package com.example.weatherapp.activity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
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
import com.example.weatherapp.widget.WeatherWidgetProvider;

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
                    // GỬI BROADCAST ĐỂ CẬP NHẬT WIDGET
                    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    intent.setComponent(new ComponentName(this, WeatherWidgetProvider.class));
                    AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
                    int[] ids = widgetManager.getAppWidgetIds(new ComponentName(this, WeatherWidgetProvider.class));
                    if (ids.length > 0) {
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    }
                    sendBroadcast(intent);
                }
            };

}