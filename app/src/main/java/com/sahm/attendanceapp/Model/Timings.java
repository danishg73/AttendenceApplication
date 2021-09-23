package com.sahm.attendanceapp.Model;

public class Timings
{
    String key,shiftname,start_time,end_time,days,manager;

    public Timings() {
    }

    public Timings(String key,String shiftname, String start_time, String end_time, String days, String manager) {
        this.key = key;
        this.shiftname = shiftname;
        this.start_time = start_time;
        this.end_time = end_time;
        this.days = days;
        this.manager = manager;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getShiftname() {
        return shiftname;
    }

    public void setShiftname(String shiftname) {
        this.shiftname = shiftname;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

}
