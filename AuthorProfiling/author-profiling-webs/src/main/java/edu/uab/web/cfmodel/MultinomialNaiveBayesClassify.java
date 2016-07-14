/**
 *
 */
package edu.uab.web.cfmodel;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.uab.web.controllers.Result;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SparseRowMatrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

import edu.uab.jobs.naivebayes.MultinomialNaiveBayesClassifier;
import edu.uab.jobs.naivebayes.MultinomialNaiveBayesModel;
import edu.uab.jobs.vectorize.LabelExtractor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * @author sjmaharjan
 */
public class MultinomialNaiveBayesClassify implements Classify {

    private static final Logger log = LoggerFactory
            .getLogger(MultinomialNaiveBayesClassify.class);

    MultinomialNaiveBayesModel model = null;
    MultinomialNaiveBayesClassifier classifier = null;
    LabelExtractor labelExtractor = null;
    String modelFile;

    public MultinomialNaiveBayesClassify(Resource modelFile, LabelExtractor le) throws IOException {
        this.modelFile = modelFile.getFile().getAbsolutePath();
        this.labelExtractor = le;
        this.intializeModel();

    }


    @Override
    public void intializeModel() {
        try {
            this.model = MultinomialNaiveBayesClassify.materalize(modelFile);
            this.classifier = new MultinomialNaiveBayesClassifier(this.model);
        } catch (FileNotFoundException e) {
            log.info("Couldn't find the model file {}", modelFile);
        } catch (IOException e) {
            System.err.println("IO EXCEPTION");
            e.printStackTrace();
        }

    }


    @Override
    public Vector classifyFull(Vector testVector) {
        Vector scoresPerLabel = classifier.classifyFull(testVector);
        log.info("The scores per label vector is {}", scoresPerLabel);
        return scoresPerLabel;
    }


    @Override
    public String predict(Vector testVector) {
        String predictedClass = null;
        int bestIdx = Integer.MIN_VALUE;
        double bestScore = Long.MIN_VALUE;
        Vector scoresPerLabel = classifyFull(testVector);
        for (Vector.Element element : scoresPerLabel) {
            if (element.get() > bestScore) {
                bestScore = element.get();
                bestIdx = element.index();
            }
        }
        if (bestIdx != Integer.MIN_VALUE) {
            predictedClass = this.labelExtractor.getSwappedKeyValueMap().get(
                    bestIdx);
        }
        return predictedClass;
    }


    public Result getPredictionScores(Vector instance) {
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
            predictedClass = this.labelExtractor.getSwappedKeyValueMap().get(
                    bestIdx);
        }
        Double[] probs = new Double[6];
            for (int i = 0; i < scoresPerLabel.size(); i++) {
                probs[i] = scoresPerLabel.get(i);

            }
        return new Result(predictedClass, probs);
    }

    public static MultinomialNaiveBayesModel materalize(String modelPath)
            throws FileNotFoundException, IOException {
        Vector countInstancesPerLabel = null;
        Matrix weightsPerLabelAndFeature;
        float alphaI;
        int vocabularySize;
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.getLocal(conf);
        FSDataInputStream in = fs.open(new Path(modelPath,
                "naiveBayesModel.bin"));
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

    /**
     * @param args
     * @throws IOException
     * @throws NumberFormatException
     */
    // public static void main(String[] args) throws NumberFormatException,
    // IOException {
    // Map<String, Integer> dictionary = DocumentVector
    // .makedictionary("src/main/resources/model/dictionary.file");
    // String testDocument = "I am planning to go to Nepal";
    // Vector instance = DocumentVector.vectorize(testDocument, dictionary);
    // Classify classification = new MultinomialNaiveBayesClassify(
    // "src/main/resources/model", new GenderAndAgeGroupLabel());
    // String predicted = classification.predict(instance);
    // System.out.println("The predicted class is :" + predicted);
    //
    // }
}
