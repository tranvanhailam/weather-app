package com.example.weatherapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.activity.MyLocationActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherAlertService {

    private static final String CHANNEL_ID = "weather_alerts";

    public static void checkWeatherAndNotify(Context context, double lat, double lon) {
        new Thread(() -> {
            try {
                // 🔹 Gọi API Open-Meteo (miễn phí, không cần key)
                String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                        "&longitude=" + lon +
                        "&daily=weathercode,precipitation_sum&timezone=Asia/Bangkok";

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray weatherCodes = json.getJSONObject("daily").getJSONArray("weathercode");

                int todayCode = weatherCodes.getInt(0); // mã thời tiết hôm nay
//                int todayCode = 95; // 95 = giông bão -> đảm bảo tạo cảnh báo

                String alert = getWeatherAlert(todayCode);

                if (alert != null) {
                    sendNotification(context, "Cảnh báo thời tiết", alert);
                }
                // ⚡ Test cảnh báo nhanh (bỏ khi deploy thật)
                sendNotification(context, "Cảnh báo thử nghiệm", "Đây là thông báo test thời tiết!");


            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static String getWeatherAlert(int code) {
        switch (code) {
            case 0: case 1:
                return "☀️ Cảnh báo nắng nóng – tránh hoạt động ngoài trời lâu!";
            case 2: case 3:
                return null; // thời tiết đẹp
            case 45: case 48:
                return "🌫 Cảnh báo sương mù dày đặc – lái xe cẩn thận!";
            case 51: case 53: case 55:
                return "🌦 Có mưa nhẹ – nhớ mang theo áo mưa!";
            case 61: case 63: case 65:
                return "☔ Cảnh báo mưa lớn – nguy cơ ngập lụt và sạt lở đất ở vùng đồi núi!";
            case 71: case 73: case 75: case 77:
                return "🌨 Cảnh báo tuyết rơi – đường trơn trượt, di chuyển cẩn thận!";
            case 80: case 81: case 82:
                return "🌬 Cảnh báo mưa rào hoặc gió mạnh – nguy cơ sạt lở đất ở khu vực đồi núi!";
            case 85: case 86:
                return "❄️ Cảnh báo băng giá – chú ý khi đi lại!";
            case 95: case 96: case 99:
                return "⚡ Cảnh báo giông bão – có thể xảy ra sạt lở đất ở vùng đồi núi!";
            default:
                return null;
        }
    }


    private static void sendNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cảnh báo thời tiết",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MyLocationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning) // thay bằng icon thật của bạn
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }
}
