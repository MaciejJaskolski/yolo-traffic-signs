package com.example.yolov2_traffic_signs.comparators;

import com.example.yolov2_traffic_signs.models.Detection;

import java.util.Comparator;

public class DetectionComparator implements Comparator<Detection> {
    @Override
    public int compare(final Detection detection1, final Detection detection2) {
        return Float.compare(detection2.getConfidence(), detection1.getConfidence());
    }
}