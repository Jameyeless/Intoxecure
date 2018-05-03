package com.intoxecure.intoxecure;

import android.util.Log;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class AccelBasedStepDetect {
    enum SampleType {
        valley,
        intermediate,
        peak
    }
    private class Sample {
        SampleType sampleType;
        long sampleTime;
        double sampleValue;
        long sampleIndex;

        Sample(double value, long time, SampleType type, long index) {
            this.sampleValue = value;
            this.sampleTime = time;
            this.sampleType = type;
            this.sampleIndex = index;
        }
    }

    private long K, M, n;
    private long count;
    private double alpha, beta;
    private double mu_p, sigma_p, mu_v, sigma_v, mu_alpha, sigma_alpha;

    private LinkedList<Sample> sampleList;
    private LinkedList<Sample> peakList;
    private LinkedList<Sample> valleyList;
    private Sample prevSample, currSample, nextSample;

    AccelBasedStepDetect() {
        this.K = 25;
        this.M = 10;
        this.alpha = 4;
        this.beta = 1/3;
        this.count = 0;
        this.prevSample = null;
        this.currSample = null;
        this.nextSample = null;
        this.mu_alpha = 10;
        this.sampleList = new LinkedList<>();
        this.peakList = new LinkedList<>();
        this.valleyList = new LinkedList<>();
    }

    long iterate(double sampleValue, long sampleTime) {
        long ret_val;

        // remove excess samples
        while (sampleList.size() >= K)
            sampleList.remove(0);

        // add old value of nextSample (i.e. currSample) to sampleList
        if (nextSample != null)
            sampleList.add(nextSample);

        // store nextSample
        nextSample = new Sample(sampleValue, sampleTime, SampleType.intermediate, n++);


        if (sampleList.size() >= 2)
            prevSample = sampleList.get(sampleList.size()-2);
        if (sampleList.size() >= 1)
            currSample = sampleList.get(sampleList.size()-1);

        ret_val = stepDetection();

        if (sampleList.size() >= 2)
            sampleList.set(sampleList.size()-2, prevSample);

        if (sampleList.size() >= 1)
            sampleList.set(sampleList.size()-1, currSample);

        return ret_val;
    }

    private long stepDetection() {
        if (sampleList.size() >= 2) {
            SampleType currSampleType = DetectCandidate();

            if (currSampleType == SampleType.peak) {
                if (prevSample.sampleType == SampleType.intermediate) {
                    currSample.sampleType = SampleType.peak;
                    UpdatePeak();
                } else if (prevSample.sampleType == SampleType.valley &&
                        (currSample.sampleIndex - peakList.getLast().sampleIndex) > (mu_p - sigma_p/beta)) {
                    currSample.sampleType = SampleType.peak;
                    UpdatePeak();
                    mu_alpha = (peakList.getLast().sampleValue + valleyList.getLast().sampleValue)/2;
                } else if (prevSample.sampleType == SampleType.peak &&
                        (currSample.sampleIndex - peakList.getLast().sampleIndex) <= (mu_p - sigma_p/beta) &&
                        currSample.sampleValue > peakList.getLast().sampleValue) {
                    UpdatePeak();
                }
            } else if (currSampleType == SampleType.valley) {
                Log.d("prevSample.sampleType" , (prevSample.sampleType == SampleType.valley)? "valley" : (prevSample.sampleType == SampleType.intermediate)? "intermediate" : "peak");
                //Log.d("n-nv","currSample.sampleIndex - valleyList.getLast().sampleIndex");
                if (prevSample.sampleType == SampleType.peak &&
                        (currSample.sampleIndex - valleyList.getLast().sampleIndex) > (mu_v - sigma_v/beta)) {
                    Log.d("valley_test1", "true");
                    currSample.sampleType = SampleType.valley;
                    UpdateValley();
                    count++;
                    mu_alpha = (peakList.getLast().sampleValue + valleyList.getLast().sampleValue)/2;
                } else if (prevSample.sampleType == SampleType.valley &&
                        (currSample.sampleIndex - valleyList.getLast().sampleIndex) <= (mu_v - sigma_v/beta) &&
                        currSample.sampleValue < valleyList.getLast().sampleValue) {
                    UpdateValley();
                }
            }
            Log.d("currSampleType", (currSampleType == SampleType.valley)? "valley" : (currSampleType == SampleType.intermediate)? "intermediate" : "peak");
            Log.d("count", Long.toString(count));
            Log.d("sampleList.size()", Integer.toString(sampleList.size()));
            Log.d("peakList.size()",Integer.toString(peakList.size()));
            Log.d("valleyList.size()",Integer.toString(valleyList.size()));
            Log.d("mu_p", Double.toString(mu_p));
            Log.d("mu_v", Double.toString(mu_v));
            Log.d("mu_alpha", Double.toString(mu_alpha));
            Log.d("sigma_p", Double.toString(sigma_p));
            Log.d("sigma_v", Double.toString(sigma_v));
            Log.d("sigma_alpha", Double.toString(sigma_alpha));
            return count;
        }

        // compute mean of sampleList
        Iterator<Sample> sampleIterator = sampleList.iterator();
        double mean = 0;
        while (sampleIterator.hasNext()) {
            mean += sampleIterator.next().sampleValue;
        }
        mean /= sampleList.size();

        // compute standard dev of sampleList
        sampleIterator = sampleList.iterator();
        sigma_alpha = 0;
        while (sampleIterator.hasNext()) {
            sigma_alpha += Math.pow(sampleIterator.next().sampleValue,2);
        }
        sigma_alpha = Math.sqrt(sigma_alpha/sampleList.size() - Math.pow(mean,2));

        return 0;
    }

    private SampleType DetectCandidate() {
        double maxTest = Math.max(Math.max(prevSample.sampleValue,nextSample.sampleValue), mu_alpha + sigma_alpha/alpha);
        double minTest = Math.min(Math.min(prevSample.sampleValue,nextSample.sampleValue), mu_alpha - sigma_alpha/alpha);
        Log.d("sampleValue > maxTest",Double.toString(currSample.sampleValue) + " > " + Double.toString(maxTest) + ": " + Boolean.toString(currSample.sampleValue > maxTest));
        Log.d("sampleValue < minTest",Double.toString(currSample.sampleValue) + " > " + Double.toString(minTest) + ": " + Boolean.toString(currSample.sampleValue < minTest));
        if (currSample.sampleValue > maxTest) {
            return SampleType.peak;
        } else if (currSample.sampleValue < minTest) {
            return SampleType.valley;
        } else {
            return SampleType.intermediate;
        }
    }

    private void UpdatePeak() {
        Iterator<Sample> peakIterator;
        long sum, prevSampleIndex, currSampleIndex;

        // Remove old samples if peak list is full
        while (peakList.size() >= M) {
            peakList.remove(0);
        }
        // Add current sample to list of peaks
        peakList.add(currSample);

        // Re-compute mean;
        peakIterator = peakList.iterator();
        mu_p = 0;
        if (peakIterator.hasNext()) {
            prevSampleIndex = peakIterator.next().sampleIndex;
            while (peakIterator.hasNext()) {
                currSampleIndex = peakIterator.next().sampleIndex;
                mu_p += currSampleIndex - prevSampleIndex;
            }
        }
        mu_p /= peakList.size()-1;

        // Re-compute standard dev
        peakIterator = peakList.iterator();
        sigma_p = 0;
        while (peakIterator.hasNext()) {
            prevSampleIndex = peakIterator.next().sampleIndex;
            while (peakIterator.hasNext()) {
                currSampleIndex = peakIterator.next().sampleIndex;
                sigma_p += Math.pow(currSampleIndex - prevSampleIndex, 2);
            }
        }
        sigma_p = Math.sqrt(sigma_p/(peakList.size()-1)- Math.pow(mu_p,2));
    }

    private void UpdateValley() {
        Iterator<Sample> valleyIterator;
        long sum, prevSampleIndex, currSampleIndex;

        // Remove old samples if valley list is full
        while (valleyList.size() >= M) {
            valleyList.remove(0);
        }
        // Add current sample to list of valleys
        valleyList.add(currSample);

        // Re-compute mean;
        valleyIterator = valleyList.iterator();
        mu_v = 0;
        if (valleyIterator.hasNext()) {
            prevSampleIndex = valleyIterator.next().sampleIndex;
            while (valleyIterator.hasNext()) {
                currSampleIndex = valleyIterator.next().sampleIndex;
                mu_v += currSampleIndex - prevSampleIndex;
            }
        }
        mu_v /= valleyList.size()-1;

        // Re-compute standard dev
        valleyIterator = valleyList.iterator();
        sigma_v = 0;
        while (valleyIterator.hasNext()) {
            prevSampleIndex = valleyIterator.next().sampleIndex;
            while (valleyIterator.hasNext()) {
                currSampleIndex = valleyIterator.next().sampleIndex;
                sigma_v += Math.pow(currSampleIndex - prevSampleIndex, 2);
            }
        }
        sigma_v = Math.sqrt(sigma_v/(valleyList.size()-1) - Math.pow(mu_v,2));
    }
}
// UVC858