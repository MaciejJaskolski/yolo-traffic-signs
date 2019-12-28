package com.example.yolov2_traffic_signs.models;

import androidx.annotation.Keep;

@Keep
public class CellRecognition {
    private double confidence;
    private double[] classes;

    public void setClasses(double[] classes) {
        this.classes = classes;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double[] getClasses() {
        return classes;
    }
}
