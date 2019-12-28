package com.example.yolov2_traffic_signs.models;

import androidx.annotation.Keep;

@Keep
public class TrafficSigns {
    public int id;
    public double latitude;
    public double longitude;
    public double rotation;

    public TrafficSigns(int id, double lat, double lng, double rotation) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.rotation = rotation;
    }

    public TrafficSigns() {}
}
