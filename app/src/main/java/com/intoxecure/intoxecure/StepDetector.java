package com.intoxecure.intoxecure;

import android.util.Log;

import java.util.LinkedList;
import java.util.ListIterator;

public class StepDetector {
    enum State {
        init,
        valley,
        intermediate,
        peak
    }
    class ReturnVal {
        State state;
        long count;

        ReturnVal(State state, long count) {
            this.state = state;
            this.count = count;
        }
    }
    private class Sample {
        Sample(double currValue, long currIndex) {
            value = currValue;
            index = currIndex;
        }
        double value;
        long index;
    }

    private long n, count;
    private double M, K, alpha, beta;
    private double mu_p, sigma_p, mu_v, sigma_v, mu_alpha, sigma_alpha;
    private Sample prevSample, currSample, nextSample;
    private State prevState;
    private LinkedList<Sample> peakList, valleyList, sampleList;

    StepDetector() {
        this.n = 0;
        this.M = 10;
        this.K = 25;
        this.alpha = 4;
        this.beta = 0.3333333333333333333333;
        this.mu_alpha = 10;
        this.sigma_alpha = 0;
        this.prevSample = new Sample(10, 0);
        this.currSample = new Sample(10, 0);
        this.nextSample = new Sample(10, 0);
        this.prevState = State.init;
        this.peakList = new LinkedList<Sample>();
        this.valleyList = new LinkedList<Sample>();
        this.sampleList = new LinkedList<Sample>();
        peakList.add(new Sample(10,0));
        valleyList.add(new Sample(10,0));
        sampleList.add(new Sample(10, 0));
    }

    long Iterate(double newValue) {
        ReturnVal returnVal = StepDetection(newValue, n++, prevState, count);
        count = returnVal.count;
        if (returnVal.state == State.valley)
            prevState = State.valley;
        else if (returnVal.state == State.peak)
            prevState = State.peak;
        return count;
    }

    ReturnVal StepDetection(double nextValue, long nextIndex, State prevState, long count) {
        prevSample = currSample;
        currSample = nextSample;
        nextSample = new Sample(nextValue, nextIndex);
        State candState = DetectCandidate(prevSample.value, currSample.value, nextSample.value, mu_alpha, sigma_alpha, alpha);
        State currState = State.intermediate;

        if (candState == State.peak) {
            if (prevState == State.init) {
                currState = State.peak;
                UpdatePeak(currSample.value, currSample.index, false);
            } else if (prevState == State.valley && (currSample.index - peakList.peekLast().index) > (mu_p - sigma_p/beta)) {
                currState = State.peak;
                UpdatePeak(currSample.value, currSample.index, false);
                mu_alpha = (peakList.peekLast().value+valleyList.peekLast().value)/2;
            } else if (prevState == State.peak && (currSample.index - peakList.peekLast().index) <= (mu_p - sigma_p/beta) && currSample.value > peakList.peekLast().value) {
                UpdatePeak(currSample.value, currSample.index, true);
            }
        } else if (candState == State.valley) {
            if (prevState == State.peak && (currSample.index - valleyList.peekLast().index) > (mu_v - sigma_v/beta)) {
                currState = State.valley;
                UpdateValley(currSample.value, currSample.index, false);
                if (sigma_alpha > 1)
                    count += 1;
                mu_alpha = (peakList.peekLast().value+valleyList.peekLast().value)/2;
            } else if (prevState == State.valley && (currSample.index - valleyList.peekLast().index) <= (mu_v - sigma_v/beta) && currSample.value < peakList.peekLast().value) {
                UpdateValley(currSample.value, currSample.index, true);
            }
        }

        ListIterator<Sample> iterator;
        while (sampleList.size() >= K)
            sampleList.poll();
        sampleList.offer(currSample);

        iterator = sampleList.listIterator();
        double mean = 0;
        while (iterator.hasNext()) {
            mean += iterator.next().value;
        }
        mean /= sampleList.size();
        iterator = sampleList.listIterator();
        sigma_alpha = 0;
        while (iterator.hasNext()) {
            sigma_alpha += Math.pow(iterator.next().value,2);
        }
        sigma_alpha = Math.sqrt(sigma_alpha/sampleList.size() - Math.pow(mean,2));


        return new ReturnVal(currState,count);
    }

    private State DetectCandidate(double prevValue, double currValue, double nextValue, double mu_alpha, double sigma_alpha, double alpha) {
        if (currValue > Math.max(Math.max(prevValue,nextValue), mu_alpha + sigma_alpha/alpha))
            return State.peak;
        else if (currValue < Math.min(Math.min(prevValue,nextValue), mu_alpha - sigma_alpha/alpha))
            return State.valley;
        else
            return State.intermediate;
    }

    private void UpdatePeak(double currValue, long currIndex, boolean replaceLast) {
        ListIterator<Sample> iterator;
        long index1, index2;

        if (replaceLast) {
            while (peakList.size() > M)
                peakList.poll();
            if (peakList.size() > 0)
                peakList.set(peakList.size()-1, new Sample(currValue,currIndex));
            else
                peakList.offer(new Sample(currValue,currIndex));
        } else {
            while (peakList.size() >= M)
                peakList.poll();
            peakList.offer(new Sample(currValue,currIndex));
        }

        if (peakList.size() < 2) {
            mu_p = 0;
            sigma_p = 0;
        } else {
            // update mu_p
            iterator = peakList.listIterator();
            index1 = iterator.next().index;
            mu_p = 0;
            while (iterator.hasNext()) {
                index2 = iterator.next().index;
                mu_p += index2 - index1;
                index1 = index2;
            }
            mu_p /= peakList.size()-1;

            // update sigma_p
            iterator = peakList.listIterator();
            index1 = iterator.next().index;
            sigma_p = 0;
            while (iterator.hasNext()) {
                index2 = iterator.next().index;
                sigma_p += Math.pow(index2 - index1,2);
                index1 = index2;
            }
            sigma_p = Math.sqrt(sigma_p/(peakList.size()-1) - Math.pow(mu_p,2));
        }
    }

    private void UpdateValley(double currValue, long currIndex, boolean replaceLast) {
        ListIterator<Sample> iterator;
        long index1, index2;

        if (replaceLast) {
            while (valleyList.size() > M)
                valleyList.poll();
            if (valleyList.size() > 0)
                valleyList.set(valleyList.size()-1, new Sample(currValue,currIndex));
            else
                valleyList.offer(new Sample(currValue,currIndex));
        } else {
            while (valleyList.size() >= M)
                valleyList.poll();
            valleyList.offer(new Sample(currValue,currIndex));
        }

        if (valleyList.size() < 2) {
            mu_v = 0;
            sigma_v = 0;
        } else {
            // update mu_v
            iterator = valleyList.listIterator();
            index1 = iterator.next().index;
            mu_v = 0;
            while (iterator.hasNext()) {
                index2 = iterator.next().index;
                mu_v += index2 - index1;
                index1 = index2;
            }
            mu_v /= valleyList.size()-1;

            // update sigma_v
            iterator = valleyList.listIterator();
            index1 = iterator.next().index;
            sigma_v = 0;
            while (iterator.hasNext()) {
                index2 = iterator.next().index;
                sigma_v += Math.pow(index2 - index1,2);
                index1 = index2;
            }
            sigma_v = Math.sqrt(sigma_v/(valleyList.size()-1) - Math.pow(mu_v,2));
        }
    }
}
