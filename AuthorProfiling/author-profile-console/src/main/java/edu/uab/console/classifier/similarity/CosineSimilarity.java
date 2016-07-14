package edu.uab.console.classifier.similarity;


import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import edu.uab.console.classifier.Classifier;
import edu.uab.console.model.Result;
import edu.uab.console.vectorize.TermIndexWeight;
import edu.uab.jobs.naivebayes.MultinomialNaiveBayesClassifier;
import edu.uab.jobs.naivebayes.MultinomialNaiveBayesModel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.math.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/31/14.
 */
public class CosineSimilarity implements Classifier {

    private static final Logger log = LoggerFactory.getLogger(CosineSimilarity.class);
    private Map<Integer, String> labels;
    private String modelFile;
    private Matrix weightsPerLabelAndFeature;
    private Vector countInstancesPerLabel;

    public static final String MODEL_FILE = "cosinesimilarity.model";

    public CosineSimilarity(String modelFile, Map<Integer, String> labels) {
        this.modelFile = modelFile;
        this.labels = labels;
        loadModel();

    }

    @Override
    public void loadModel() {
        try {
            System.out.println("Loding model --> Cosine Similarity");
            materalize(modelFile);
        } catch (FileNotFoundException e) {
            log.info("Couldn't find the model file {}", modelFile);
        } catch (IOException e) {
            System.err.println("IO EXCEPTION");
            e.printStackTrace();
        }

    }

    @Override
    public List<String> labels() {
        return Lists.newArrayList(labels.values());
    }

    public void materalize(String modelFile) throws IOException {
        countInstancesPerLabel = null;
        float alphaI;
        int vocabularySize;
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.getLocal(conf);
        FSDataInputStream in = fs.open(new Path(modelFile, CosineSimilarity.MODEL_FILE));
        try {
            alphaI = in.readFloat();
            vocabularySize = in.readInt();
            countInstancesPerLabel = VectorWritable.readVector(in);
            weightsPerLabelAndFeature = new SparseRowMatrix(
                    countInstancesPerLabel.size(), vocabularySize);
            for (int label = 0; label < weightsPerLabelAndFeature.numRows(); label++) {
                weightsPerLabelAndFeature.assignRow(label,
                        VectorWritable.readVector(in));
            }
        } finally {
            Closeables.closeQuietly(in);
        }
        log.info("The feature count {}  \n", vocabularySize);
        log.info("The model matrix is {} \n", weightsPerLabelAndFeature);


    }


    public double getScoreForLabelInstance(int label, Vector instance) {
        Vector v2 = weightsPerLabelAndFeature.viewRow(label);
        //average tf-idf scores

        double labelCount = countInstancesPerLabel.get(label);
        double weight= labelCount/countInstancesPerLabel.size();


        Vector v1 = instance;

        return weight*similarity(v1, v2);
       //return similarity(v1, v2);


    }

    public double similarity(Vector v1, Vector v2) {
        double result = 0.0;
        if (v1.size() != v2.size()) {
            throw new CardinalityException(v1.size(), v2.size());
        }
        double lengthSquaredv1 = v1.getLengthSquared();
        double lengthSquaredv2 = v2.getLengthSquared();
        double dotProduct = v2.dot(v1);
        double denominator = Math.sqrt(lengthSquaredv1) * Math.sqrt(lengthSquaredv2);
        try {
            result = dotProduct / denominator;

        } catch (ArithmeticException e) {
            log.info("Division error");
        }
        return result;
    }


    public Vector classifyFull(Vector instance) {
        Vector score = createScoringVector();
        for (int label = 0; label < numberOfCategories(); label++)
            score.set(label, getScoreForLabelInstance(label, instance));
        return score;
    }

    private int numberOfCategories() {
        return countInstancesPerLabel.size();
    }

    private Vector createScoringVector() {
        return countInstancesPerLabel.like();
    }


    @Override
    public String predict(Vector instance) {
        if (instance.size() > 0) {
            String predictedClass = "xxx_xxx";
            int bestIdx = Integer.MIN_VALUE;
            double bestScore = Long.MIN_VALUE;
            Vector scoresPerLabel = classifyFull(instance);
            for (Vector.Element element : scoresPerLabel) {
                if (element.get() > bestScore) {
                    bestScore = element.get();
                    bestIdx = element.index();
                }
            }
            if (bestIdx != Integer.MIN_VALUE) {
                predictedClass = labels.get(bestIdx);
            }
            return predictedClass;
        } else {
            return "xxx_xxx";
        }
    }

    @Override
    public Result predict_with_probabilities(Vector instance) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
