package edu.uab.jobs.features.stylistic;

import edu.uab.jobs.tokenizer.Twokenize;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by suraj on 3/19/14.
 */
public class Emoticon extends Style {
    private Vector featureVector;

    public Emoticon(Map<String, Integer> dictionary, List<String> tokens) {
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
        Vector vector = new RandomAccessSparseVector(1, 1);
        Pattern Emoticons = Pattern.compile(Twokenize.emoticon);
        for (String emoticon : tokens) {
            Matcher m1 = Emoticons.matcher(emoticon);
            if (m1.find()) {
                int emoticonId = dictionary.get("EMOTICON");
                vector.setQuick(emoticonId, vector.getQuick(emoticonId) + 1);
            }
        }
        return vector;
    }
}
