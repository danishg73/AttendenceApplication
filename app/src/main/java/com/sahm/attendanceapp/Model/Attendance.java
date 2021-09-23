package com.sahm.attendanceapp.Model;

public class Attendance {

    private String key, time, status;

    public Attendance() {
    }

    public Attendance(String key, String time, String status) {
        this.key = key;
        this.time = time;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
