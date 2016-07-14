package edu.uab.jobs.features.stylistic;

import edu.uab.jobs.tokenizer.Twokenize;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by suraj on 3/19/14.
 */
public class Punctuation extends Style {
    private Vector featureVector;

    public Punctuation(Map<String, Integer> dictionary, List<String> tokens) {
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
        Pattern Punctuations = Pattern.compile(Twokenize.punctChars);
        Pattern Emoticons = Pattern.compile(Twokenize.emoticon);
        for (String token : tokens) {
            Matcher m1 = Emoticons.matcher(token);
            Matcher m2 = Twokenize.Contractions.matcher(token);
            Matcher m3 = Punctuations.matcher(token);
            if (m1.find() || m2.find())
                continue;
            while (m3.find()) {
                int punctId = dictionary.get("Punctuation");
                vector.setQuick(punctId, vector.getQuick(punctId) + 1);
            }
        }
        return vector;
    }
}
