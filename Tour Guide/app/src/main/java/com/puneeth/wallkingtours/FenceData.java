package com.puneeth.wallkingtours;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

public class FenceData implements Serializable {

    String id;
    String address;
    String latitude;
    String longitude;
    String radius;
    String description;
    String fenceColor;
    String image;
    private final int type = Geofence.GEOFENCE_TRANSITION_ENTER ;

    public FenceData(String id, String address, String latitude, String longitude, String radius, String description, String fenceColor, String image) {
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.description = description;
        this.fenceColor = fenceColor;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getRadius() {
        return radius;
    }

    public String getDescription() {
        return description;
    }

    public String getFenceColor() {
        return fenceColor;
    }

    public String getImage() {
        return image;
    }

    public int getType() {
        return type;
    }
}
