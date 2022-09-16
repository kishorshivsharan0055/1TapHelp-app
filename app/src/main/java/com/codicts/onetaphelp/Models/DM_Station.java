package com.codicts.onetaphelp.Models;

public class DM_Station {
    public String stationID, stationName, phoneNo, distance, latitude, longitude;

    public String getStationID() {
        return stationID;
    }

    public String getStationName() {
        return stationName;
    }

    public String getStationPhone() {
        return phoneNo;
    }

    public String getDistance() {
            return distance.substring(0,(distance.indexOf(".") + 3));
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
