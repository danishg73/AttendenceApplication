package com.sahm.attendanceapp.Model;

public class Holiday
{
    String key,place,date,week;

    public Holiday() {
    }

    public Holiday(String key, String place, String date,String week) {
        this.key = key;
        this.place = place;
        this.date = date;
        this.week=week;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
