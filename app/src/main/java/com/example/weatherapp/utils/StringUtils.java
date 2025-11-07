package com.example.weatherapp.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtils {
    public static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    public static String formatDateFromIso8601Time(String iso8601Time) throws ParseException { //July 22, 2021
        try {
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
            Date date = inFmt.parse(iso8601Time);
            SimpleDateFormat outFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            return outFmt.format(date);
        } catch (Exception e) {
            return iso8601Time;
        }
    }

    public static String formatDateFromIso8601Time2(String iso8601Time) { //with input: 2025-11-08
        try {
            // Định dạng đầu vào: 2025-11-08
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = inFmt.parse(iso8601Time);

            // Đầu ra: July 22, 2021
            SimpleDateFormat outFmt = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
            return outFmt.format(date);
        } catch (Exception e) {
            return iso8601Time;
        }
    }

    public static String formatDateFromIso8601Time3(String iso8601Time) { //with input: 2025-11-08
        try {
            // Định dạng đầu vào: 2025-11-08
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = inFmt.parse(iso8601Time);

            // Đầu ra: July 22, 2021
            SimpleDateFormat outFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            return outFmt.format(date);
        } catch (Exception e) {
            return iso8601Time;
        }
    }


    public static String getDayOfWeekFromIsoDate(String isoDate) {
        try {
            // Định dạng đầu vào: 2025-11-08
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = inFmt.parse(isoDate);

            // Đầu ra chỉ hiển thị thứ (Monday, Tuesday, ...)
            SimpleDateFormat outFmt = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            return outFmt.format(date);
        } catch (Exception e) {
            return "";
        }
    }


    public static String formatHour24(String iso8601Time) { //10:00am

        try {
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
            Date date = inFmt.parse(iso8601Time);
            SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            return outFmt.format(date);
        } catch (Exception e) {
            return iso8601Time;
        }
    }


}
