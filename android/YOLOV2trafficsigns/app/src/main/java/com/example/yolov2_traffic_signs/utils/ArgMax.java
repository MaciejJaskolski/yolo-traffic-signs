package com.example.yolov2_traffic_signs.utils;

import android.util.Pair;

public class ArgMax {
    private double[] values;

    public ArgMax(double[] vals) {
        this.values = vals;
    }

    public Pair<Integer, Double> getMaxIndex() {
        int maxIndex = 0;
        for (int i=0; i<values.length; i++) {
            if (values[maxIndex] < values[i]) {
                maxIndex = i;
            }
        }

        return new Pair<Integer, Double>(maxIndex, values[maxIndex]);
    }
}
