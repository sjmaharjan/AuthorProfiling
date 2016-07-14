package edu.uab.jobs.utils;

import edu.uab.jobs.naivebayes.MultinomialNaiveBayesModel;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;


import java.io.IOException;
import java.util.*;

/**
 * Created by suraj on 1/10/14.
 */
public class TopFeatures {

    public static Map<Integer, List<TermIndexWeight>> getTopFeaturesPerClass(int n, String modelPath) throws IOException {
        Map<Integer, List<TermIndexWeight>> result = new HashMap<Integer, List<TermIndexWeight>>();
        Path modelFile = new Path(modelPath);
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        MultinomialNaiveBayesModel model = MultinomialNaiveBayesModel.materialize(modelFile, conf);

        Matrix matrix = model.getWeightsPerLabelAndFeature();
        for (int i = 0; i < matrix.rowSize(); i++) {
            Vector v = matrix.viewRow(i);
            List<TermIndexWeight> top = TopFeatures.topFeaturesInVector(v, n);
            result.put(i, top);

        }

        return result;
    }


    public static List<String> readDictionary(String dictionaryPath) throws IOException {
        List<String> dictionary = new ArrayList<String>();
        Path dictionaryFile = new Path(dictionaryPath);
        Configuration conf = new Configuration();
        for (Pair<Writable, IntWritable> record
                : new SequenceFileIterable<Writable, IntWritable>(dictionaryFile, true, conf)) {
            dictionary.add(record.getFirst().toString());

        }
        return dictionary;
    }

    public static List<TermIndexWeight> topFeaturesInVector(Vector v, int n) {
        List<TermIndexWeight> result = new ArrayList<TermIndexWeight>();
        List<TermIndexWeight> vectorTerms = new ArrayList<TermIndexWeight>();
        for (Vector.Element e : v) {
            vectorTerms.add(new TermIndexWeight(e.index(), e.get()));
        }

        // Sort results in reverse order (ie weight in descending order)
        Collections.sort(vectorTerms, new Comparator<TermIndexWeight>() {
            @Override
            public int compare(TermIndexWeight one, TermIndexWeight two) {
                return Double.compare(two.weight, one.weight);
            }
        });

        for (int i = 0; i < n; i++) {
            TermIndexWeight t = vectorTerms.get(i);
            result.add(t);
        }

        return result;

    }

    //ref mahout cluster code
    private static class TermIndexWeight {
        private final int index;
        private final double weight;

        TermIndexWeight(int index, double weight) {
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
            return
                    "index=" + index +
                            ", weight=" + weight +
                            ' ';
        }
    }

    public static void main(String args[]) throws IOException {
        HashSet<String> age10 = new HashSet<String>();
        HashSet<String> age20 = new HashSet<String>();
        HashSet<String> age30 = new HashSet<String>();
        HashSet<String> male = new HashSet<String>();
        HashSet<String> female = new HashSet<String>();

        Map<Integer, List<TermIndexWeight>> topFeature = TopFeatures.getTopFeaturesPerClass(Integer.parseInt(args[2]), args[0]);
        List<String> dictionary= TopFeatures.readDictionary(args[1]);

        for (Map.Entry<Integer, List<TermIndexWeight>> entry : topFeature.entrySet()) {
            System.out.println(entry.getKey());
            for (TermIndexWeight s : entry.getValue()) {
//                System.out.println("\t" + s.toString() + " " + dictionary.get(s.getIndex()));
                if (entry.getKey() == 0) {
                    age10.add(dictionary.get(s.getIndex()));
                    male.add(dictionary.get(s.getIndex()));
                }
                else if (entry.getKey() == 1) {
                    age10.add(dictionary.get(s.getIndex()));
                    female.add(dictionary.get(s.getIndex()));
                }
                else if (entry.getKey() == 2) {
                    age20.add(dictionary.get(s.getIndex()));
                    male.add(dictionary.get(s.getIndex()));
                }
                else if (entry.getKey() == 3) {
                    age20.add(dictionary.get(s.getIndex()));
                    female.add(dictionary.get(s.getIndex()));
                }
                else if (entry.getKey() == 4) {
                    age30.add(dictionary.get(s.getIndex()));
                    male.add(dictionary.get(s.getIndex()));
                }
                else if (entry.getKey() == 5) {
                    age30.add(dictionary.get(s.getIndex()));
                    female.add(dictionary.get(s.getIndex()));
                }

//                System.out.println(dictionary.get(s.getIndex()));
            }
            System.out.println("=====================================================");
        }

        System.out.println(age10);
        System.out.println(age20);
        System.out.println(age30);
        System.out.println(male);
        System.out.println(female);


    }


}
