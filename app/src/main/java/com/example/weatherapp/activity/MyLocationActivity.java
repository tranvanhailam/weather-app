package com.example.weatherapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.api.Client;
import com.example.weatherapp.helper.BottomNavHelper;
import com.example.weatherapp.repository.SearchHistoryRepository;
import com.example.weatherapp.service.WeatherAlertService;
import com.example.weatherapp.utils.StringUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyLocationActivity extends AppCompatActivity {


    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    TextView textViewCity, textViewBigTemp, textViewSmallTemp, textViewHumidity, textViewWind, textViewDate;
    TextView textViewCurrentTimeInCard, textViewCurrentTempInCard;
    LinearLayout linearLayoutForecastScroll;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    SearchHistoryRepository searchHistoryRepository;
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
        setContentView(R.layout.activity_my_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavHelper.setupBottomNav(this);

        textViewCity = findViewById(R.id.tvCity);
        textViewBigTemp = findViewById(R.id.tvBigTemp);
        textViewSmallTemp = findViewById(R.id.tvSmallTemp);
        textViewHumidity = findViewById(R.id.tvHumidity);
        textViewWind = findViewById(R.id.tvWind);
        textViewDate = findViewById(R.id.tvDate);

        textViewCurrentTimeInCard = findViewById(R.id.tvCurrentTimeInCard);
        textViewCurrentTempInCard = findViewById(R.id.tvCurrentTempInCard);
        linearLayoutForecastScroll = findViewById(R.id.llLayoutForecastScroll);

        searchHistoryRepository= new SearchHistoryRepository(MyLocationActivity.this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

//        SearchHistory searchHistory= new SearchHistory();
//        searchHistory.setName("Hanoi");
//        searchHistoryRepository.addSearchHistory(searchHistory);
//
//        searchHistoryRepository.getSearchHistoryById(1)
//                .observe(this, item -> textViewCity.setText(item.getName()));

        getCurrentLocationAndLoadData();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getWeatherForCurrentLocation();
    }
    private void getWeatherForCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        //  Lấy vị trí GPS mới nhất thay vì vị trí cache
        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();


                // Gọi service cảnh báo
                WeatherAlertService.checkWeatherAndNotify(this, lat, lon);
            } else {
                Toast.makeText(this, "Không lấy được vị trí!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  Khi quay lại màn hình, tự cập nhật vị trí mới nhất
    @Override
    protected void onResume() {
        super.onResume();
        getWeatherForCurrentLocation();
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
            loadWeatherForCity("Hanoi");
            loadWeatherInCardForCity("Hanoi");
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
                            loadWeatherForCity(cityName);
                            loadWeatherInCardForCity(cityName);
                        } catch (Exception e) {
                            postError();
                        }
                    });


                } else {
                    // Display error message if location is null
                    textViewCity.setText("Unable to get location");
                }
            }
        });
    }

    private void loadWeatherForCity(String cityNameInput) {
        io.execute(() -> {
            try {
                // B1: Geocoding
                //api get info city, timezone from open meteo
                JSONObject geo = Client.request("https://geocoding-api.open-meteo.com/v1/search?name=" + cityNameInput + "&count=1&language=en"); //request
                JSONArray results = geo.optJSONArray("results"); //lấy phần mảng ra khỏi JSON gốc -> JsonArr
                //optJSONArray("results") → lấy mảng con trong JSON gốc.
                //getJSONObject(i) → lấy từng object trong mảng.
                //getString("name") → lấy giá trị cụ thể từ object.

                //{
                //  "results": [
                //    {
                //      "id": 1581130,
                //      "name": "Hanoi",
                //      "latitude": 21.0245,
                //      "longitude": 105.84117,
                //      "elevation": 10.0,
                //      "feature_code": "PPLC",
                //      "country_code": "VN",
                //      "timezone": "Asia/Bangkok",
                //      "population": 8053663
                //    }
                //  ],
                //  "generationtime_ms": 0.2959
                //}

                //optJSONArray("results") // trả về null, không lỗi ->

                //[
                //  {
                //    "id": 1581130,
                //    "name": "Hanoi",
                //    "latitude": 21.0245,
                //    "longitude": 105.84117,
                //    "elevation": 10.0,
                //    "feature_code": "PPLC",
                //    "country_code": "VN",
                //    "timezone": "Asia/Bangkok",
                //    "population": 8053663
                //  }
                //]

                if (results == null || results.length() == 0) {
                    return;
                }

                JSONObject cityInfo = results.getJSONObject(0);
                String cityName = cityInfo.optString("name", "null");
                double latitude = cityInfo.getDouble("latitude");
                double longitude = cityInfo.getDouble("longitude");
                String timezone = cityInfo.optString("timezone", "Asia/Bangkok");

                // B2: Forecast current
                String forecastUrl = "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + latitude
                        + "&longitude=" + longitude
                        + "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m"
                        + "&timezone=" + StringUtils.encode(timezone);

                JSONObject weather = Client.request(forecastUrl); //request
                JSONObject current = weather.optJSONObject("current");
                if (current == null) {
                    postError();
                    return;
                }

                double temp = current.optDouble("temperature_2m", Double.NaN);
                int humidity = current.optInt("relative_humidity_2m", -1);
                double wind = current.optDouble("wind_speed_10m", Double.NaN);
                String time = current.optString("time", "");


                runOnUiThread(() -> {
                    textViewBigTemp.setText(Double.isNaN(temp) ? "--" : temp + "°C");
                    try {
                        textViewDate.setText(StringUtils.formatDateFromIso8601Time(time));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    textViewCity.setText(cityName);
                    textViewSmallTemp.setText(Double.isNaN(temp) ? "--" : temp + "°C");
                    textViewHumidity.setText(humidity < 0 ? "--" : humidity + "%");
                    textViewWind.setText(wind < 0 ? "--" : wind + "km/h");
                });
            } catch (Exception e) {
                postError();
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
                JSONObject hourly = weather.optJSONObject("hourly");
                if (hourly == null) {
                    postError();
                    return;
                }

                JSONArray times = hourly.optJSONArray("time");
                JSONArray temps = hourly.optJSONArray("temperature_2m");
                if (times == null || temps == null) return;

                int total = Math.min(times.length(), temps.length());


                int start = findStartIndex(times);               // Tìm index giờ hiện tại (hoặc giờ đầu tiên >= now)
                int end = Math.min(start + 24, total);           // lấy 24 giờ kế tiếp

//                LayoutInflater inflater = LayoutInflater.from(this);


                runOnUiThread(() -> {
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
                postError();
            }
        });
    }

    private void postError() {
        runOnUiThread(() -> {
            textViewCity.setText("--");
            textViewSmallTemp.setText("--");
            textViewHumidity.setText("--");
            textViewWind.setText("--");
        });
    }

}