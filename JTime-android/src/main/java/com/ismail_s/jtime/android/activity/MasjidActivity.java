package com.ismail_s.jtime.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.ismail_s.jtime.android.R;

public class MasjidActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masjid);
        String masjidName = getIntent().getStringExtra(Constants.MASJID_NAME);
        TextView masjidNameView = (TextView) findViewById(R.id.masjid_name);
        masjidNameView.setText(masjidName);
    }
}
