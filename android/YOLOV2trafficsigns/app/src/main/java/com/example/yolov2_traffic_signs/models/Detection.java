package com.example.yolov2_traffic_signs.models;

import androidx.annotation.Keep;

@Keep
public class Detection {
    private final Integer classId;
    private final  String label;
    private final Float confidence;

    public Detection(final Integer classId, final String label, final Float confidence) {
        this.classId = classId;
        this.label = label;
        this.confidence = confidence;
    }

    public Float getConfidence() {
        return confidence;
    }

    public String getTitle() {
        return label;
    }

    public Integer getId() {
        return classId;
    }

}
