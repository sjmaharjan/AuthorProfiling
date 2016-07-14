package edu.uab.console.vectorize;

/**
 * Created by suraj on 3/31/14.
 */
public class TermIndexWeight {
    public final int index;
    public final double weight;

    public TermIndexWeight(int index, double weight) {
        this.index = index;
        this.weight = weight;
    }

    public int getIndex() {
        return index;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return index + ":" + weight;
    }
}
