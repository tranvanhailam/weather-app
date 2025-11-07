package com.example.weatherapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weatherapp.R;
import com.example.weatherapp.entity.DailyRow;

import java.util.List;

public class DailyAdapter extends BaseAdapter {
    private final Context context;
    private final List<DailyRow> dailyRowList;
    int layout;
//    private final LayoutInflater layoutInflater;

    public DailyAdapter(Context context, int layout, List<DailyRow> dailyRowList) {
        this.context = context;
        this.layout = layout;
        this.dailyRowList = dailyRowList;
//        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return dailyRowList.size();
    }

    @Override
    public DailyRow getItem(int i) {
        return dailyRowList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(layout, null);
        TextView textViewDayTitle = convertView.findViewById(R.id.tvDayTitle);
        TextView textViewDayDate = convertView.findViewById(R.id.tvDayDate);
        TextView textViewDayTemp = convertView.findViewById(R.id.tvDayTemp);
        ImageView imageViewDayIcon = convertView.findViewById(R.id.imgDayIcon);
        DailyRow dailyRow = dailyRowList.get(i);
        textViewDayTitle.setText(dailyRow.getTitle());
        textViewDayDate.setText(dailyRow.getDate());
        textViewDayTemp.setText(dailyRow.getTemp());
        imageViewDayIcon.setImageResource(dailyRow.getIconRes());

        return convertView;
    }
}
