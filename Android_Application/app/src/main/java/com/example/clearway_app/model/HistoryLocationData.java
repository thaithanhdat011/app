package com.example.clearway_app.model;

public class HistoryLocationData {
    private String address;
    private String time;

    public HistoryLocationData(String address, String time) {
        this.address = address;
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
