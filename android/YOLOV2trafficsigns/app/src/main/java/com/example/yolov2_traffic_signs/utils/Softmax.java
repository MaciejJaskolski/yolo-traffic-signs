package com.example.yolov2_traffic_signs.utils;

public class Softmax {
    private final double[] values;

    public Softmax(double[] vals) {
        this.values = vals;
    }

    public double[] getSoftmax() {

        double sum  = 0;
        for (int i = 0; i<values.length;i++) {
            values[i] = Math.exp(values[i]);
            sum += values[i];
        }

        for (int i = 0; i<values.length;i++) {
            values[i] = values[i] / sum;
        }

        return values;
    }
}
