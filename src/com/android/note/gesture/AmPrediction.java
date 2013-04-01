package com.android.note.gesture;

public class AmPrediction {
    public final String name;

    public double score;

    AmPrediction(String label, double predictionScore) {
        name = label;
        score = predictionScore;
    }

    @Override
    public String toString() {
        return name;
    }
}
