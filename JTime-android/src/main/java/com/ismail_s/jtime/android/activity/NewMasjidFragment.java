package com.ismail_s.jtime.android.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ismail_s.jtime.android.MasjidPojo;
import com.ismail_s.jtime.android.R;
import com.ismail_s.jtime.android.RestClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewMasjidFragment extends Fragment {
    private ArrayList<TextView> dateTextViews = new ArrayList<>();
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public NewMasjidFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NewMasjidFragment newInstance(int sectionNumber) {
        NewMasjidFragment fragment = new NewMasjidFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_masjid, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        String masjidName = getActivity().getIntent().getStringExtra(Constants.MASJID_NAME);
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
        return rootView;
    }

    private String formatCalendar(GregorianCalendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(calendar.getTime());
    }

    public View findViewById(int id) {
        return getActivity().findViewById(id);
    }
}
