package edu.uab.jobs.features.stylistic;

import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/19/14.
 */
public class Preposition extends Style {
    private Vector featureVector;

    private static final List<String> PREPOSITIONS = Arrays.asList("also", "although", "and", "as", "although", "because", "but", "cuz", "how", "however", "if", "nor", "or", "otherwise", "plus", "so", "then", "tho", "though", "til", "till", "unless", "until", "when", "whenever", "whereas", "whether", "while", "b\'coz", "either", "neither", "even");


    public Preposition(Map<String, Integer> dictionary, List<String> tokens) {
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
        Vector vector = new RandomAccessSparseVector(PREPOSITIONS.size(), 4);
        for (String word : tokens) {
            if (PREPOSITIONS.contains(word.toLowerCase())) {
                int wordId = dictionary.get("PREPOSITION_" + word);
                vector.setQuick(wordId, vector.getQuick(wordId) + 1);
            }
        }
        return vector;
    }
}
