package com.sahm.attendanceapp.Model;

public class AddEmployee
{
    String key,name,phone,email,password, type, attendance,shift,username;

    public AddEmployee() {
    }

    public AddEmployee(String key,String username, String phone, String email, String password, String type, String attendance,String name, String shift) {
        this.key = key;
        this.phone = phone;
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = type;
        this.attendance=attendance;
        this.name=name;
        this.shift=shift;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.name = username;
    }


    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String attendance) {
        this.attendance = shift;
    }





}
