package edu.uab.console.classifier.svm;

import com.google.common.collect.Lists;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import edu.uab.console.classifier.Classifier;
import edu.uab.console.model.Result;
import edu.uab.console.utils.Helper;
import edu.uab.console.vectorize.TermIndexWeight;
import org.apache.mahout.classifier.ConfusionMatrix;
import org.apache.mahout.math.Vector;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by suraj on 3/31/14.
 */
public class LibLinearSvmClassifier implements Classifier {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LibLinearSvmClassifier.class);

    private Map<Integer, String> labels;
    private Model model;
    private String modelFile;

    public static final String MODEL_FILE = "liblinear.model";

    public LibLinearSvmClassifier(String modelFile, Map<Integer, String> labelMap) {
        this.labels = labelMap;
        this.modelFile = modelFile;
        loadModel();
    }

    @Override
    public List<String> labels() {
        return Lists.newArrayList(labels.values());
    }

    @Override
    public void loadModel() {
        try {
            File m = new File(this.modelFile, LibLinearSvmClassifier.MODEL_FILE);
            model = Linear.loadModel(m);
           log.info("Done loading model");
        } catch (IOException e) {
            log.error("Could not load model file");
            e.printStackTrace();
        }

    }


    @Override
    public String predict(Vector instance) {
        assert instance != null;
        if (instance.size() > 0) {
            Iterator<Vector.Element> iter = instance.iterateNonZero();
            List<TermIndexWeight> vectorTerms = new ArrayList<TermIndexWeight>();
            while (iter.hasNext()) {
                Vector.Element e = iter.next();
                vectorTerms.add(new TermIndexWeight(e.index(), e.get()));
            }

            // Sort results in ascending order by index
            Collections.sort(vectorTerms, new Comparator<TermIndexWeight>() {
                @Override
                public int compare(TermIndexWeight one, TermIndexWeight two) {
                    return Double.compare(one.index, two.index);
                }
            });

            Feature[] features = new Feature[vectorTerms.size()];
            int i = 0;
            for (TermIndexWeight elt : vectorTerms) {
                int index = elt.index + 1;
                double value = elt.weight;
                FeatureNode feature = new FeatureNode(index, value);
                features[i] = feature;
                i++;
            }

            for(int j=0; j <features.length; j++) {
                System.out.print(features[j].getIndex() + ":" + features[j].getValue() + " ");
            }
            System.out.println();


            double prediction = Linear.predict(this.model, features);
//            double probabilities[] = new double[11];
//
//            Linear.predictProbability(this.model, features, probabilities);
//
//
//            for (double val : probabilities)
//                System.out.print(val + ",");


//            System.out.println( prediction);
//            System.out.print(labels.get((int) prediction)+",");

            return labels.get((int) prediction);
        } else {
            return "xxx_xxx";
        }
    }

    @Override
    public Result predict_with_probabilities(Vector instance) {
        assert instance != null;
        if (instance.size() > 0) {
            Iterator<Vector.Element> iter = instance.iterateNonZero();
            List<TermIndexWeight> vectorTerms = new ArrayList<TermIndexWeight>();
            while (iter.hasNext()) {
                Vector.Element e = iter.next();
                vectorTerms.add(new TermIndexWeight(e.index(), e.get()));
            }

            // Sort results in ascending order by index
            Collections.sort(vectorTerms, new Comparator<TermIndexWeight>() {
                @Override
                public int compare(TermIndexWeight one, TermIndexWeight two) {
                    return Double.compare(one.index, two.index);
                }
            });

            Feature[] features = new Feature[vectorTerms.size()];
            int i = 0;
            for (TermIndexWeight elt : vectorTerms) {
                int index = elt.index + 1;
                double value = elt.weight;
                FeatureNode feature = new FeatureNode(index, value);
                features[i] = feature;
                i++;
            }

            double prediction = Linear.predict(this.model, features);
            double[] probabilities = new double[labels.size()];
            Linear.predictProbability(this.model, features, probabilities);
            Result r = new Result(labels.get((int) prediction), probabilities);
            return r;
        } else {
            return new Result("xxx_xxx", new double[labels.size()]);
        }
    }


    public static void main(String[] args) throws IOException {
        File modelFile = new File("/Users/suraj/liblinear-1.94/heart_scale.model");
        Model model = Linear.loadModel(modelFile);
        System.out.println(model.toString());

        List<String> symbols = new ArrayList<String>(Arrays.asList("-1.0", "+1.0"));
        ConfusionMatrix cf = new ConfusionMatrix(symbols, "svm");
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("/Users/suraj/liblinear-1.94/heart_scale"));
            while ((line = br.readLine()) != null) {
                String[] vector = line.split(" ");
                Feature[] instance = new Feature[vector.length - 1];
                for (int i = 1; i < vector.length; i++) {
                    String[] indexValue = vector[i].split(":");
                    int index = Integer.parseInt(indexValue[0]);
                    float value = Float.parseFloat(indexValue[1]);
                    FeatureNode feature = new FeatureNode(index, value);
                    instance[i - 1] = feature;
                }

                double prediction = Linear.predict(model, instance);
                String pred = String.valueOf(prediction);
                if (prediction > 0) {
                    pred = "+" + String.valueOf(prediction);
                }
                cf.addInstance(vector[0] + ".0", pred);

                System.out.println(instance + " --> " + prediction);

            }

            System.out.println(cf.toString());
            System.out.println(cf.getAccuracy("+1.0"));
            System.out.println(cf.getAccuracy("-1.0"));
            int correct = cf.getCorrect("+1.0") + cf.getCorrect("-1.0");
            int total = cf.getTotal("+1.0") + cf.getTotal("-1.0");
            System.out.println("Correct: " + correct + ",Total: " + total);
            System.out.println("Accuracy=" + correct * 100.0 / total);
            Helper.printAccuracy(cf);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
