package edu.uab.jobs.features.stylistic;

import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/19/14.
 */
public class WordLength extends Style {
    private Vector featureVector;



    public WordLength(Map<String, Integer> dictionary, List<String> tokens) {
        super(dictionary, tokens);
        featureVector = computeFeatureVector();

    }


    @Override
    public double computeFeatureValue() {
        //TODO normalize
        return this.featureVector.zSum();
    }


    public Vector featureVector() {
        return this.featureVector;
    }


    private Vector computeFeatureVector() {
        Vector vector = new RandomAccessSparseVector();

        return vector;
    }
}
