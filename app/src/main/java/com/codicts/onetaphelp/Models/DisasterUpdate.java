package com.codicts.onetaphelp.Models;

public class DisasterUpdate {
    public String disasterName, disasterTime, distance, radius,latitude, longitude, imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRadius() {
        return String.valueOf(Integer.valueOf(radius)/1000);
    }

    public String getDistance() {
        return distance.substring(0,(distance.indexOf(".") + 3));
    }

    public String getDisasterTime() {
        return disasterTime;
    }

    public String getDisasterName() {
        return disasterName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}

