package com.ismail_s.jtime.android;

import java.util.GregorianCalendar;

public class RestClient {
    public MasjidPojo getMasjidTimes(String masjidName) {
        MasjidPojo res = new MasjidPojo(masjidName);
        res.setFajrTime(new GregorianCalendar(2015, 1, 1, 5, 30));
        res.setZoharTime(new GregorianCalendar(2015, 1, 1, 12, 0));
        res.setAsrTime(new GregorianCalendar(2015, 1, 1, 15, 0));
        res.setMagribTime(new GregorianCalendar(2015, 1, 1, 15, 12));
        res.setEshaTime(new GregorianCalendar(2015, 1, 1, 19, 45));
        return res;
    }
}
