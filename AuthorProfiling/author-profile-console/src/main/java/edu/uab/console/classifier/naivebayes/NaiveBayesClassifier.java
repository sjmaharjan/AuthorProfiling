package edu.uab.console.classifier.naivebayes;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import edu.uab.console.classifier.Classifier;
import edu.uab.console.model.Result;
import edu.uab.jobs.naivebayes.MultinomialNaiveBayesClassifier;
import edu.uab.jobs.naivebayes.MultinomialNaiveBayesModel;
import edu.uab.jobs.vectorize.LabelExtractor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SparseRowMatrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.hsqldb.jdbc.jdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/31/14.
 */
public class NaiveBayesClassifier implements Classifier {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesClassifier.class);


    private MultinomialNaiveBayesModel model = null;
    private MultinomialNaiveBayesClassifier classifier = null;
    private Map<Integer, String> labels;
    private String modelFile;


    public static final String MODEL_FILE = "naivebayes.model";

    public NaiveBayesClassifier(String modelFile, Map<Integer, String> labels) {
        this.modelFile = modelFile;
        this.labels = labels;
        loadModel();

    }

    @Override
    public List<String> labels() {
        return Lists.newArrayList(labels.values());
    }

    @Override
    public void loadModel() {
        try {
            this.model = NaiveBayesClassifier.materalize(modelFile);
            this.classifier = new MultinomialNaiveBayesClassifier(this.model);
        } catch (FileNotFoundException e) {
            System.out.println("Model not found");
            log.info("Couldn't find the model file {}", modelFile);
        } catch (IOException e) {
            System.err.println("IO EXCEPTION");
            e.printStackTrace();
        }

    }

    public static MultinomialNaiveBayesModel materalize(String modelFile) throws IOException {
        Vector countInstancesPerLabel = null;
        Matrix weightsPerLabelAndFeature;
        float alphaI;
        int vocabularySize;
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.getLocal(conf);
        FSDataInputStream in = fs.open(new Path(modelFile, NaiveBayesClassifier.MODEL_FILE));
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
        log.info("The feature count {} alpha {} \n", vocabularySize, alphaI);
        log.info("The model matrix is {} \n", weightsPerLabelAndFeature);
        MultinomialNaiveBayesModel model = new MultinomialNaiveBayesModel(
                countInstancesPerLabel, weightsPerLabelAndFeature, alphaI,
                vocabularySize);
        return model;
    }


    public Vector classifyFull(Vector instance) {
        Vector scoresPerLabel = classifier.classifyFull(instance);
        log.info("The scores per label vector is {}", scoresPerLabel);
        return scoresPerLabel;
    }

    @Override
    public String predict(Vector instance) {
        if (instance.size() > 0) {
            String predictedClass = null;
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
