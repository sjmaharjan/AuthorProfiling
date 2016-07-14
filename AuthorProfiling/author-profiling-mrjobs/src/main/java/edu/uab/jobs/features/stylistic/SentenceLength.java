package edu.uab.jobs.features.stylistic;

import org.apache.mahout.math.Vector;

import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/19/14.
 */
public class SentenceLength extends Style {
    public SentenceLength(Map<String, Integer> dictionary, List<String> tokens) {
        super(dictionary, tokens);
    }

    @Override
    public double computeFeatureValue() {
        return 0;
    }


    public Vector featureVector() {
        return null;
    }
}
