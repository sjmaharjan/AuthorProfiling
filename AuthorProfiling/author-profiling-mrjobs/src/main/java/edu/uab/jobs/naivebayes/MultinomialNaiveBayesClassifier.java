package edu.uab.jobs.naivebayes;

import org.apache.mahout.classifier.AbstractVectorClassifier;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.math.Vector;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/19/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 * Reference Mahout's Naive Bayes Implementations
 */
public class MultinomialNaiveBayesClassifier extends AbstractVectorClassifier {

    private final MultinomialNaiveBayesModel model;

    public MultinomialNaiveBayesClassifier(MultinomialNaiveBayesModel model) {
        this.model = model;
    }

    public MultinomialNaiveBayesModel getModel() {
        return model;
    }


    @Override
    public int numCategories() {
        return this.model.numberOfCategories();
    }

    public double getScoreForLabelInstance(int label, Vector instance) {
        double result = 0.0;
        Iterator<Vector.Element> elements = instance.iterateNonZero();
        while (elements.hasNext()) {
            Vector.Element e = elements.next();
            result += e.get() * getScoreForLabelFeature(label, e.index());
        }
        return result +Math.log(this.model.getCategoryCount(label) / this.model.getTotalCountOfInstances()); //add log of prior probabilty
       // return result;
    }

    @Override
    public Vector classifyFull(Vector instance) {
        Vector score = model.createScoringVector();
        for (int label = 0; label < model.numberOfCategories(); label++)
            score.set(label, getScoreForLabelInstance(label, instance));
        return score;
    }

    @Override
    public Vector classifyFull(Vector r, Vector instance) {
        r = classifyFull(instance);
        return r;
    }

    @Override
    public Vector classify(Vector instance) {
        throw new UnsupportedOperationException(" Not supported by Multinomial Naive Bayes");
    }

    @Override
    public double classifyScalar(Vector instance) {
        throw new UnsupportedOperationException("Not Supported by  Multinomial Naive Bayes");
    }

    public double getScoreForLabelFeature(int label, int feature) {
        return computeWeight(model.weight(label, feature), model.getCategoryCount(label), model.getAlphaI(),
                model.getNumFeatures());
    }

    public static double computeWeight(double featureLabelWeight, double labelWeight, double alphaI,
                                       double numFeatures) {
        double numerator = featureLabelWeight + alphaI;
        double denominator = labelWeight + alphaI * numFeatures;
        return Math.log(numerator / denominator);
    }
}
