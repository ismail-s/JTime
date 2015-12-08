package com.ismail_s.jtime.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.widget.TextView;

import com.ismail_s.jtime.android.MasjidPojo;
import com.ismail_s.jtime.android.R;
import com.ismail_s.jtime.android.RestClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MasjidActivity extends Activity {
    private ArrayList<TextView> dateTextViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masjid);
        String masjidName = getIntent().getStringExtra(Constants.MASJID_NAME);
        TextView masjidNameView = (TextView) findViewById(R.id.masjid_name);
        masjidNameView.setText(masjidName);
        MasjidPojo masjidTimes = (new RestClient()).getMasjidTimes(masjidName);
        dateTextViews.add((TextView) findViewById(R.id.fajr_date));
        dateTextViews.add((TextView) findViewById(R.id.zohar_date));
        dateTextViews.add((TextView) findViewById(R.id.asr_date));
        dateTextViews.add((TextView) findViewById(R.id.magrib_date));
        dateTextViews.add((TextView) findViewById(R.id.esha_date));
        GregorianCalendar[] times = masjidTimes.getTimes();
        for (int i = 0; i < dateTextViews.size(); i++) {
            dateTextViews.get(i).setText(formatCalendar(times[i]));
        }
    }

    private String formatCalendar(GregorianCalendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(calendar.getTime());
    }
}
