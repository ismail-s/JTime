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
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_DATE = "date";

    public NewMasjidFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NewMasjidFragment newInstance(GregorianCalendar date) {
        NewMasjidFragment fragment = new NewMasjidFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_masjid, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        GregorianCalendar date = (GregorianCalendar) getArguments().getSerializable(ARG_DATE);
        textView.setText(getString(R.string.section_format, formatCalendarAsDate(date)));
        String masjidName = getActivity().getIntent().getStringExtra(Constants.MASJID_NAME);
        MasjidPojo masjidTimes = (new RestClient()).getMasjidTimes(masjidName);
        ArrayList<TextView> dateTextViews = new ArrayList<>();
        dateTextViews.add((TextView) rootView.findViewById(R.id.fajr_date));
        dateTextViews.add((TextView) rootView.findViewById(R.id.zohar_date));
        dateTextViews.add((TextView) rootView.findViewById(R.id.asr_date));
        dateTextViews.add((TextView) rootView.findViewById(R.id.magrib_date));
        dateTextViews.add((TextView) rootView.findViewById(R.id.esha_date));
        GregorianCalendar[] times = masjidTimes.getTimes();
        for (int i = 0; i < dateTextViews.size(); i++) {
            String res = formatCalendarAsTime(times[i]);
            TextView x = dateTextViews.get(i);
            assert x != null;
            x.setText(res);
        }
        return rootView;
    }

    private String formatCalendarAsTime(GregorianCalendar calendar) {
        return formatCalendar(calendar, "HH:mm");
    }

    private String formatCalendarAsDate(GregorianCalendar calendar) {
        return formatCalendar(calendar, "yyyy MMM dd");
    }

    private String formatCalendar(GregorianCalendar calendar, String formatString) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatString, Locale.getDefault());
        return formatter.format(calendar.getTime());
    }

}
