package com.example.weatherapp.utils;

public final class WeatherIconMapper {

    /**
     * Map Open-Meteo weather_code + isDay -> Weather Icons (ErikFlowers) glyph (unicode).
     * Trả về ví dụ: "\uf019" (wi-rain).
     */
    public static String getWiGlyph(int weatherCode, boolean isDay) {
        switch (weatherCode) {
            // 0–3: bầu trời/mây
            case 0: // Clear sky
                return isDay ? WiGlyphs.DAY_SUNNY : WiGlyphs.NIGHT_CLEAR;

            case 1: // Mainly clear
                // ngày: hơi có mây -> day-sunny-overcast; đêm: partly cloudy
                return isDay ? WiGlyphs.DAY_SUNNY_OVERCAST : WiGlyphs.NIGHT_PARTLY_CLOUDY;

            case 2: // Partly cloudy
                return isDay ? WiGlyphs.DAY_CLOUDY : WiGlyphs.NIGHT_CLOUDY_ALT;

            case 3: // Overcast
                return WiGlyphs.CLOUDY;

            // 45, 48: Fog
            case 45:
            case 48:
                return isDay ? WiGlyphs.DAY_FOG : WiGlyphs.NIGHT_FOG;

            // 51, 53, 55: Drizzle
            case 51:
            case 53:
            case 55:
                return isDay ? WiGlyphs.DAY_SPRINKLE : WiGlyphs.NIGHT_SPRINKLE_ALT;

            // 56, 57: Freezing drizzle
            case 56:
            case 57:
                return isDay ? WiGlyphs.DAY_RAIN_MIX : WiGlyphs.NIGHT_RAIN_MIX;

            // 61, 63, 65: Rain (light/moderate/heavy)
            case 61:
            case 63:
            case 65:
                return isDay ? WiGlyphs.DAY_RAIN : WiGlyphs.NIGHT_RAIN;

            // 66, 67: Freezing rain / sleet
            case 66:
            case 67:
                return isDay ? WiGlyphs.DAY_SLEET : WiGlyphs.NIGHT_SLEET;

            // 71, 73, 75: Snow (light/moderate/heavy)
            case 71:
            case 73:
            case 75:
                return isDay ? WiGlyphs.DAY_SNOW : WiGlyphs.NIGHT_SNOW;

            // 77: Snow grains
            case 77:
                return WiGlyphs.SNOWFLAKE_COLD;

            // 80, 81, 82: Rain showers (light/moderate/heavy)
            case 80:
                return isDay ? WiGlyphs.SHOWERS : WiGlyphs.NIGHT_STORM_SHOWERS_ALT; // night-alt-showers f029 cũng ổn
            case 81:
            case 82:
                return isDay ? WiGlyphs.DAY_STORM_SHOWERS : WiGlyphs.NIGHT_STORM_SHOWERS_ALT;

            // 85, 86: Snow showers
            case 85:
            case 86:
                return isDay ? WiGlyphs.DAY_SNOW_WIND : WiGlyphs.NIGHT_SNOW_WIND;

            // 95: Thunderstorm
            case 95:
                return isDay ? WiGlyphs.DAY_THUNDERSTORM : WiGlyphs.NIGHT_THUNDERSTORM_ALT;

            // 96, 99: Thunderstorm with hail
            case 96:
            case 99:
                return isDay ? WiGlyphs.DAY_HAIL : WiGlyphs.NIGHT_HAIL_ALT;

            default:
                return WiGlyphs.NA;
        }
    }
}
