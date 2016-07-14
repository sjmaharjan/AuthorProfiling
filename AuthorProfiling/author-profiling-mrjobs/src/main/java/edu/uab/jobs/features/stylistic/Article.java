package edu.uab.jobs.features.stylistic;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/19/14.
 */
public class Article extends Style {

    private Vector featureVector;

    private static final List<String> ARTICLES = Arrays.asList("a", "an", "the", "alot");


    public Article(Map<String, Integer> dictionary, List<String> tokens) {
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
        Vector vector = new RandomAccessSparseVector(ARTICLES.size(), 4);
        for (String word : tokens) {
            if (ARTICLES.contains(word.toLowerCase())) {
                int wordId = dictionary.get("ARTICLE_" + word);
                vector.setQuick(wordId, vector.getQuick(wordId) + 1);
            }
        }
        return vector;
    }
}
