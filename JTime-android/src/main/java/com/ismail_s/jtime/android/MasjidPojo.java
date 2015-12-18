package com.ismail_s.jtime.android;

import java.util.GregorianCalendar;

public class MasjidPojo {
    private String name;
    private GregorianCalendar fajrTime;
    private GregorianCalendar zoharTime;
    private GregorianCalendar asrTime;
    private GregorianCalendar magribTime;
    private GregorianCalendar eshaTime;

    public MasjidPojo(String name, GregorianCalendar fajrTime, GregorianCalendar zoharTime, GregorianCalendar asrTime, GregorianCalendar magribTime, GregorianCalendar eshaTime) {
        this.name = name;
        this.fajrTime = fajrTime;
        this.zoharTime = zoharTime;
        this.asrTime = asrTime;
        this.magribTime = magribTime;
        this.eshaTime = eshaTime;
    }

    public MasjidPojo(String name) {
        this.name = name;
    }

    public GregorianCalendar getFajrTime() {
        return fajrTime;
    }

    public void setFajrTime(GregorianCalendar fajrTime) {
        this.fajrTime = fajrTime;
    }

    public GregorianCalendar getZoharTime() {
        return zoharTime;
    }

    public void setZoharTime(GregorianCalendar zoharTime) {
        this.zoharTime = zoharTime;
    }

    public GregorianCalendar getAsrTime() {
        return asrTime;
    }

    public void setAsrTime(GregorianCalendar asrTime) {
        this.asrTime = asrTime;
    }

    public GregorianCalendar getMagribTime() {
        return magribTime;
    }

    public void setMagribTime(GregorianCalendar magribTime) {
        this.magribTime = magribTime;
    }

    public GregorianCalendar getEshaTime() {
        return eshaTime;
    }

    public void setEshaTime(GregorianCalendar eshaTime) {
        this.eshaTime = eshaTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GregorianCalendar[] getTimes() {
        return new GregorianCalendar[]{fajrTime, zoharTime, asrTime, magribTime, eshaTime};
    }
}
