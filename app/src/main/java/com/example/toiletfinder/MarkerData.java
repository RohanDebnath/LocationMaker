package com.example.toiletfinder;
public class MarkerData {

    private double latitude;
    private double longitude;
    private String title;
    private String details;

    public MarkerData() {
        // Default constructor required for Firebase
    }

    public MarkerData(double latitude, double longitude, String title, String details) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.details = details;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }
}

