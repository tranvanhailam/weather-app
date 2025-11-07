package com.example.weatherapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.example.weatherapp.utils.StringUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    SearchHistoryRepository searchHistoryRepository;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    GridLayout gridLayoutSearch;
    LinearLayout linearLayoutCardSearchLocation;
    EditText editTextSearch;
    ImageButton imageButtonLocation;
    TextView textViewTempLocation, textViewCityLocation;
    LayoutInflater inflater;
    private FusedLocationProviderClient fusedLocationClient;

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

        searchHistoryRepository= new SearchHistoryRepository(SearchActivity.this);
        gridLayoutSearch = findViewById(R.id.gLayoutSearch);
        linearLayoutCardSearchLocation = findViewById(R.id.llLayoutCardSearchLocation);
        editTextSearch = findViewById(R.id.edtSearch);
        imageButtonLocation = findViewById(R.id.ibLocation);
        textViewTempLocation = findViewById(R.id.tvTempLocation);
        textViewCityLocation = findViewById(R.id.tvCityLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        inflater = LayoutInflater.from(this);
//        gridLayoutSearch.removeAllViews();

        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    // 👉 Thực hiện hành động khi người dùng nhấn nút tìm kiếm
                    String cityNameInput = editTextSearch.getText().toString();
                    gridLayoutSearch.removeAllViews();
                    loadWeatherForCityLocation(cityNameInput);
                    loadWeatherForCityVicinity(cityNameInput);
                    return true; // báo đã xử lý sự kiện
                }
                return false;

            }
        });

        imageButtonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextSearch.setText("");
                gridLayoutSearch.removeAllViews();
                getCurrentLocationAndLoadData();
            }
        });


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
            loadWeatherForCityLocation("Hanoi");
            loadWeatherForCityVicinity("Hanoi");
        }
    }

    private void getCurrentLocationAndLoadData() {
        // Kiểm tra xem quyền vị trí có được cấp không
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Mở hộp thoại yêu cầu cấp quyền nếu không được cấp
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
                            loadWeatherForCityLocation(cityName);
                            loadWeatherForCityVicinity(cityName);
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

    private void loadWeatherForCityLocation(String cityNameInput) {
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

                // B2: Forecast current
                String forecastUrl = "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + latitude
                        + "&longitude=" + longitude
                        + "&current=temperature_2m"//,relative_humidity_2m,precipitation,weather_code,wind_speed_10m"
                        + "&timezone=" + StringUtils.encode(timezone);

                JSONObject weather = Client.request(forecastUrl); //request
                JSONObject current = weather.optJSONObject("current");
                if (current == null) {
//                    postError();
                    return;
                }

                double temp = current.optDouble("temperature_2m", Double.NaN);
//                int humidity = current.optInt("relative_humidity_2m", -1);
//                double wind = current.optDouble("wind_speed_10m", Double.NaN);
//                String time = current.optString("time", "");


                runOnUiThread(() -> {

                    View card = inflater.inflate(R.layout.item_card_search_location, gridLayoutSearch, false);
                    TextView textViewTempLocation = card.findViewById(R.id.tvTempLocation);
                    TextView textViewCityLocation = card.findViewById(R.id.tvCityLocation);

                    textViewTempLocation.setText(Double.isNaN(temp) ? "--" : temp + "°C");
                    textViewCityLocation.setText(cityName);
                    gridLayoutSearch.addView(card);
//                    textViewTempLocation.setText(Double.isNaN(temp) ? "--" : temp + "°C");
//                    try {
//                        textViewDate.setText(StringUtils.formatDateFromIso8601Time(time));
//                    } catch (ParseException e) {
//                        throw new RuntimeException(e);
//                    }
//                    textViewCityLocation.setText(cityName);
//                    textViewSmallTemp.setText(Double.isNaN(temp) ? "--" : temp + "°C");
//                    textViewHumidity.setText(humidity < 0 ? "--" : humidity + "%");
//                    textViewWind.setText(wind < 0 ? "--" : wind + "km/h");
                });
            } catch (Exception e) {
//                postError();
            }
        });
    }

    private void loadWeatherForCityVicinity(String cityNameInput) {
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
                double latitude = cityInfo.getDouble("latitude"); // Vĩ độ – trục Bắc ↔ Nam
                double longitude = cityInfo.getDouble("longitude"); //Kinh độ – trục Đông ↔ Tây
                String timezone = cityInfo.optString("timezone", "Asia/Bangkok");

                // B2: Forecast current


                // 1° vĩ độ ≈ 111 km, 1° kinh độ ≈ 111 * cos(vĩ độ)
                double deltaLat = 60.0 / 111.0;  // tương đương ~0.5405° vĩ độ cho 60 km
                double deltaLon = 60.0 / (111.0 * Math.cos(Math.toRadians(latitude))); // kinh độ thay đổi theo vĩ độ hiện tại

                //  4 tọa độ lân cận 60 km

                double[][] vicinityPoints = {
                        {latitude + deltaLat, longitude},          // Hướng Bắc: đi lên trên bản đồ → tăng vĩ độ, giữ nguyên kinh độ
                        {latitude - deltaLat, longitude},          // Hướng Nam: đi xuống → giảm vĩ độ, giữ nguyên kinh độ
                        {latitude, longitude + deltaLon},          // Hướng Đông: đi sang phải → tăng kinh độ, giữ nguyên vĩ độ
                        {latitude, longitude - deltaLon}           // Hướng Tây: đi sang trái → giảm kinh độ, giữ nguyên vĩ độ
                };

                for (double[] point : vicinityPoints) {
                    double lat = point[0];
                    double lon = point[1];
                    loadWeatherForCityVicinityInCard(lat, lon, timezone);
                }


//                // ✅ In kết quả ra để kiểm tra
//                System.out.printf("Original: %.5f, %.5f%n", latitude, longitude);
//                System.out.printf("North (Bắc): %.5f, %.5f%n", latNorth, lonNorth);
//                System.out.printf("South (Nam): %.5f, %.5f%n", latSouth, lonSouth);
//                System.out.printf("East  (Đông): %.5f, %.5f%n", latEast, lonEast);
//                System.out.printf("West  (Tây): %.5f, %.5f%n", latWest, lonWest);


//                String forecastUrl = "https://api.open-meteo.com/v1/forecast"
//                        + "?latitude=" + latitude
//                        + "&longitude=" + longitude
//                        + "&current=temperature_2m"//,relative_humidity_2m,precipitation,weather_code,wind_speed_10m"
//                        + "&timezone=" + StringUtils.encode(timezone);
//
//                JSONObject weather = Client.request(forecastUrl); //request
//                JSONObject current = weather.optJSONObject("current");
//                if (current == null) {
////                    postError();
//                    return;
//                }
//
//                double temp = current.optDouble("temperature_2m", Double.NaN);
////                int humidity = current.optInt("relative_humidity_2m", -1);
////                double wind = current.optDouble("wind_speed_10m", Double.NaN);
////                String time = current.optString("time", "");
//
//
//                runOnUiThread(() -> {
//                    textViewTempLocation.setText(Double.isNaN(temp) ? "--" : temp + "°C");
////                    try {
////                        textViewDate.setText(StringUtils.formatDateFromIso8601Time(time));
////                    } catch (ParseException e) {
////                        throw new RuntimeException(e);
////                    }
//                    textViewCityLocation.setText(cityName);
////                    textViewSmallTemp.setText(Double.isNaN(temp) ? "--" : temp + "°C");
////                    textViewHumidity.setText(humidity < 0 ? "--" : humidity + "%");
////                    textViewWind.setText(wind < 0 ? "--" : wind + "km/h");
//                });
            } catch (Exception e) {
//                postError();
            }
        });
    }

    public void loadWeatherForCityVicinityInCard(double latitude, double longitude, String timezone) {

        io.execute(() -> {
            try {
                //api get city name
                String reverseUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=" + latitude + "&longitude=" + longitude + "&localityLanguage=en";


                JSONObject json = Client.request(reverseUrl); // apiResponse là chuỗi JSON
                String cityName = json.optString("city", "null");

                String forecastUrl = "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + latitude
                        + "&longitude=" + longitude
                        + "&current=temperature_2m"//,relative_humidity_2m,precipitation,weather_code,wind_speed_10m"
                        + "&timezone=auto";//+ StringUtils.encode(timezone);

                JSONObject weather = Client.request(forecastUrl); //request
                JSONObject current = weather.optJSONObject("current");
                if (current == null) {
                    return;
                }

                double temp = current.optDouble("temperature_2m", Double.NaN);

                runOnUiThread(() -> {

                    View card = inflater.inflate(R.layout.item_card_search_vicinity, gridLayoutSearch, false);
                    TextView textViewTempVicinity = card.findViewById(R.id.tvTempVicinity);
                    TextView textViewCityVicinity = card.findViewById(R.id.tvCityVicinity);

                    textViewTempVicinity.setText(Double.isNaN(temp) ? "--" : temp + "°C");
                    textViewCityVicinity.setText(cityName);
                    gridLayoutSearch.addView(card);
                });
            } catch (Exception e) {
            }
        });
    }
}