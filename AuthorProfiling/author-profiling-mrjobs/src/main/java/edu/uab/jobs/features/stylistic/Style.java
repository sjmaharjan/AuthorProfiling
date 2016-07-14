package edu.uab.jobs.features.stylistic;

import org.apache.lucene.analysis.Analyzer;
import org.apache.mahout.math.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/19/14.
 */
public abstract class Style {

    protected Map<String, Integer> dictionary = new HashMap<String, Integer>();
    protected List<String> tokens = new ArrayList<String>();

    public Style(Map<String, Integer> dictionary, List<String> tokens) {
        this.dictionary = dictionary;
        this.tokens = tokens;
    }

    public abstract double computeFeatureValue();

    public abstract Vector featureVector();
}
