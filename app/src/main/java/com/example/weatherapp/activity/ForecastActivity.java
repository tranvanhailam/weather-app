package com.example.weatherapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.adapter.DailyAdapter;
import com.example.weatherapp.api.Client;
import com.example.weatherapp.entity.DailyRow;
import com.example.weatherapp.helper.BottomNavHelper;
import com.example.weatherapp.repository.SearchHistoryRepository;
import com.example.weatherapp.utils.StringUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForecastActivity extends AppCompatActivity {


    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    LinearLayout linearLayoutForecastScroll;

    SearchHistoryRepository searchHistoryRepository;
    TextView textViewDate;
    ListView listViewDaily;
    ArrayList<DailyRow> arrayList;
    private FusedLocationProviderClient fusedLocationClient;

    public static int findStartIndex(JSONArray times) {
        SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
        Date now = new Date();
        long nowMillis = now.getTime();

        for (int i = 0; i < times.length(); i++) {
            String s = times.optString(i, null);
            if (s == null) continue;
            try {
                Date t = isoFmt.parse(s);
                if (t != null && t.getTime() >= nowMillis) {
                    return i; // lấy index đầu tiên >= thời gian hiện tại
                }
            } catch (Exception ignored) {
            }
        }
        return 0; // fallback nếu không tìm được
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forecast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavHelper.setupBottomNav(this);

        searchHistoryRepository= new SearchHistoryRepository(ForecastActivity.this);
        linearLayoutForecastScroll = findViewById(R.id.llLayoutForecastScroll);
        textViewDate = findViewById(R.id.tvDate);
        listViewDaily = findViewById(R.id.lvDaily);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getCurrentLocationAndLoadData();

    }

    // Xử lý kết quả của yêu cầu cấp quyền, chạy sau khi user chọn các lựa chọn trong hộp thoại cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Nếu được cấp quyền, lấy vị trí
            getCurrentLocationAndLoadData();
        } else {
            // Nếu quyền bị từ chối, hiển thị ...
//            loadWeatherForCity("Hanoi");
            loadWeatherInCardForCity("Hanoi");
            loadWeatherNextForecast("Hanoi");
        }
    }

    private void getCurrentLocationAndLoadData() {
        // Kiểm tra xem quyền vị trí có được cấp không
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu cấp quyền nếu không được cấp
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        // Lấy vị trí nếu đã được cấp quyền truy cập vị trí
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Get latitude and longitude
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    //api get city name
                    String reverseUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=" + latitude + "&longitude=" + longitude + "&localityLanguage=en";

                    io.execute(() -> {
                        try {
                            JSONObject json = Client.request(reverseUrl); // apiResponse là chuỗi JSON
                            String cityName = json.optString("city", "null");
//                            loadWeatherForCity(cityName);
                            loadWeatherInCardForCity(cityName);
                            loadWeatherNextForecast(cityName);
                        } catch (Exception e) {
//                            postError();
                        }
                    });


                } else {
                    // Display error message if location is null
//                    textViewCity.setText("Unable to get location");
                }
            }
        });
    }

    private void loadWeatherInCardForCity(String cityNameInput) {
        io.execute(() -> {
            try {
                // B1: Geocoding
                //api get info city, timezone from open meteo
                JSONObject geo = Client.request("https://geocoding-api.open-meteo.com/v1/search?name=" + cityNameInput + "&count=1&language=en"); //request
                JSONArray results = geo.optJSONArray("results"); //lấy phần mảng ra khỏi JSON gốc -> JsonArr

                if (results == null || results.length() == 0) {
                    return;
                }

                JSONObject cityInfo = results.getJSONObject(0);
                String cityName = cityInfo.optString("name", "null");
                double latitude = cityInfo.getDouble("latitude");
                double longitude = cityInfo.getDouble("longitude");
                String timezone = cityInfo.optString("timezone", "Asia/Bangkok");

                // B2: Forecast hourly
                String forecastUrl = "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + latitude
                        + "&longitude=" + longitude
                        + "&hourly=temperature_2m"
                        + "&forecast_days=2"
                        + "&timezone=" + StringUtils.encode(timezone);

                JSONObject weather = Client.request(forecastUrl); //request
//                JSONObject hourly_units = weather.optJSONObject("hourly_units");
                JSONObject hourly = weather.optJSONObject("hourly");
                if (hourly == null) {
//                    postError();
                    return;
                }

//                String time = hourly_units.optString("time", "");
                JSONArray times = hourly.optJSONArray("time");
                JSONArray temps = hourly.optJSONArray("temperature_2m");
                if (times == null || temps == null) return;

                int total = Math.min(times.length(), temps.length());


                int start = findStartIndex(times);               // Tìm index giờ hiện tại (hoặc giờ đầu tiên >= now)
                int end = Math.min(start + 24, total);           // lấy 24 giờ kế tiếp

//                LayoutInflater inflater = LayoutInflater.from(this);


                runOnUiThread(() -> {
//                    try {
//                        textViewDate.setText(StringUtils.formatDateFromIso8601Time(time));
//                    } catch (ParseException e) {
//                        throw new RuntimeException(e);
//                    }

                    LayoutInflater inflater = LayoutInflater.from(this);

                    for (int i = start; i < end; i++) {
                        String isoTime = times.optString(i, null);
                        double temp = temps.optDouble(i, Double.NaN);
                        if (isoTime == null || Double.isNaN(temp)) continue;

                        // Lấy giờ từ chuỗi "2025-11-05T19:00"
                        int hour;
                        try {
                            hour = Integer.parseInt(isoTime.substring(11, 13));
                        } catch (Exception e) {
                            continue; // skip nếu format lạ
                        }

                        // Chọn layout theo giờ của thẻ
                        int layoutRes = (hour >= 18 || hour < 6)
                                ? R.layout.item_card_my_location_scroll_dark   // ban đêm
                                : R.layout.item_card_my_location_scroll;        // ban ngày

                        View card = inflater.inflate(layoutRes, linearLayoutForecastScroll, false);

                        TextView textViewTime = card.findViewById(R.id.tvCurrentTimeInCard);
                        TextView textViewTemp = card.findViewById(R.id.tvCurrentTempInCard);
//        ImageView imgIcon = card.findViewById(R.id.imgIcon);

                        textViewTime.setText(StringUtils.formatHour24(isoTime));
                        textViewTemp.setText(Math.round(temp) + "°C");
//        imgIcon.setImageResource(R.drawable.ic_moon_cloud_fast_wind);

                        linearLayoutForecastScroll.addView(card);
                    }
                });


            } catch (Exception e) {
//                postError();
            }
        });
    }

    private void loadWeatherNextForecast(String cityNameInput) {

        io.execute(() -> {
            try {
                // B1: Geocoding
                //api get info city, timezone from open meteo
                JSONObject geo = Client.request("https://geocoding-api.open-meteo.com/v1/search?name=" + cityNameInput + "&count=1&language=en"); //request
                JSONArray results = geo.optJSONArray("results"); //lấy phần mảng ra khỏi JSON gốc -> JsonArr

                if (results == null || results.length() == 0) {
                    return;
                }

                JSONObject cityInfo = results.getJSONObject(0);
                String cityName = cityInfo.optString("name", "null");
                double latitude = cityInfo.getDouble("latitude");
                double longitude = cityInfo.getDouble("longitude");
                String timezone = cityInfo.optString("timezone", "Asia/Bangkok");

                // B2: Forecast hourly
//                String forecastUrl = "https://api.open-meteo.com/v1/forecast"
//                        + "?latitude=" + latitude
//                        + "&longitude=" + longitude
//                        + "&hourly=temperature_2m"
//                        + "&forecast_days=5"
//                        + "&timezone=" + StringUtils.encode(timezone);

                String forecastUrl = "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&daily=temperature_2m_max,temperature_2m_min,temperature_2m_mean" +
                        "&forecast_days=5" +
                        "&timezone=" + StringUtils.encode(timezone);

                JSONObject weather = Client.request(forecastUrl); //request
                JSONObject daily = weather.optJSONObject("daily");
                if (daily == null) {
//                    postError();
                    return;
                }

                JSONArray times = daily.optJSONArray("time");
                JSONArray temperature_2m_min = daily.optJSONArray("temperature_2m_min");
                JSONArray temperature_2m_max = daily.optJSONArray("temperature_2m_max");

                if (times == null || temperature_2m_min == null || temperature_2m_max == null)
                    return;


                runOnUiThread(() -> {
                    textViewDate.setText(StringUtils.formatDateFromIso8601Time3(times.optString(0, "")));
                    arrayList = new ArrayList<>();

                    int length = Math.min(times.length(), Math.min(temperature_2m_min.length(), temperature_2m_max.length()));

                    for (int i = 0; i < length; i++) {
                        String time = times.optString(i, "");
                        double tempMin = temperature_2m_min.optDouble(i, Double.NaN);
                        double tempMax = temperature_2m_max.optDouble(i, Double.NaN);

                        // Chuyển ngày ISO sang định dạng dễ đọc (ví dụ "Tue, Jul 23")
                        String displayDate = StringUtils.formatDateFromIso8601Time2(time);

                        String displayDayOfWeek = StringUtils.getDayOfWeekFromIsoDate(time);

                        // Gộp nhiệt độ hiển thị, ví dụ: "20° / 30°"
                        String tempDisplay = String.format(Locale.US, "%.0f°C / %.0f°C", tempMin, tempMax);

                        // Thêm vào danh sách hiển thị
                        arrayList.add(new DailyRow(displayDayOfWeek, displayDate, tempDisplay, R.drawable.ic_moon_cloud_fast_wind));
                    }

                    DailyAdapter dailyAdapter = new DailyAdapter(ForecastActivity.this, R.layout.item_card_next_forcast, arrayList);
                    listViewDaily.setAdapter(dailyAdapter);

                });


            } catch (Exception e) {
//                postError();
            }
        });


    }

}