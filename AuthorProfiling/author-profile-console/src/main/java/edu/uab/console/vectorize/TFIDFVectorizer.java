package edu.uab.console.vectorize;

import edu.uab.console.model.AgeGroup;
import edu.uab.console.model.Gender;
import edu.uab.console.utils.Helper;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

import java.util.*;

/**
 * Created by suraj on 3/29/14.
 */
public class TFIDFVectorizer extends AbstractVectorizer {

    private Map<String, Integer> dictionary;
    private Map<String, Integer> idfDictionary;
    private int numberOfDocuments;
    private int dimentioin;


    public TFIDFVectorizer(Map<String, Integer> dictionary, Map<String, Integer> idfDictionary, int numberOfDocuments) {
        this.idfDictionary = idfDictionary;
        this.dictionary = dictionary;
        this.numberOfDocuments = numberOfDocuments;
        this.dimentioin = dictionary.size();
    }

    @Override
    public Vector createVector(List<String> tokens) {
        assert tokens != null;
        //perform word count
        if (tokens.size() > 0) {
            Map<String, Integer> wordCount = Helper.wordCount(tokens);
            Vector tfidf = new RandomAccessSparseVector(dimentioin, wordCount.size());

            int maxTermFrequency = Helper.maximum(wordCount);
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                String word = entry.getKey();
                int tokenCount = entry.getValue();
                if (dictionary.containsKey(word)) {
                    tfidf.setQuick(dictionary.get(word), (((double) tokenCount) / maxTermFrequency) * idfScore(word));
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    tfidf.setQuick(unSeenWordIndex, tfidf.get(unSeenWordIndex) + (((double) tokenCount) / maxTermFrequency) * idfScore(word));
                }
            }
            return tfidf;
        } else {
            return new RandomAccessSparseVector(0, 0);
        }
    }

    private double idfScore(String word) {
        int idfScore = idfDictionary.containsKey(word) ? this.idfDictionary.get(word) : 0;
        return Math.log(((double) numberOfDocuments) / (1 + idfScore));
    }

    public static void main(String[] args) {
        List<String> tokens = new ArrayList<String>();
        Map<String, Integer> dict = new HashMap<String, Integer>();
        //add class labels format:age_gender
        dict.put("a", 0);
        dict.put("b", 1);
        dict.put("c", 2);
        dict.put("d", 3);

        AbstractVectorizer vectorizer = new TFIDFVectorizer(dict, dict, 5);
        Vector v = vectorizer.createVector(tokens);
        System.out.println(v);
        System.out.println(v.size() );


    }
}
