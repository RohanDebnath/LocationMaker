package com.example.toiletfinder;
public class MarkerData {

    private String markerId; // Unique identifier for the marker
    private double latitude;
    private double longitude;
    private String title;
    private String details;

    public MarkerData() {
        // Default constructor required for Firebase
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public MarkerData(String markerId, double latitude, double longitude, String title, String details) {
        this.markerId = markerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.details = details;
    }

    public String getMarkerId() {
        return markerId;
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


