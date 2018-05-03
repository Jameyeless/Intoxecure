package com.intoxecure.intoxecure;



public class WeightedAverage {
    private static Double oldValue;
    private static double aveVal;

    public double compute(long currTime, double alpha){
        aveVal = Average(currTime, alpha);
        return aveVal;
    }

    private double Average(double value, double alpha) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;
    }
}