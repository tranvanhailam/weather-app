package com.example.weatherapp.utils;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeUtils {
    private static final String PREF = "app_prefs";
    private static final String KEY_DARK = "dark_mode";

    public static boolean isDark(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK, false);
    }

    public static void setDark(Context ctx, boolean enabled) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_DARK, enabled).apply();

        int want = enabled ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != want) {
            AppCompatDelegate.setDefaultNightMode(want); // AppCompat sẽ tự recreate 1 lần
        }
    }

    public static void applySavedMode(Context ctx) {
        boolean enabled = isDark(ctx);
        int want = enabled ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != want) {
            AppCompatDelegate.setDefaultNightMode(want);
        }
    }
}
