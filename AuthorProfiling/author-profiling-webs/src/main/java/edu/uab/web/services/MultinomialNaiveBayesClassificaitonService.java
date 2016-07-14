/**
 * 
 */
package edu.uab.web.services;

import java.nio.channels.UnsupportedAddressTypeException;

import edu.uab.web.controllers.Result;
import org.apache.mahout.math.Vector;

import edu.uab.web.cfmodel.Classify;

/**
 * @author sjmaharjan
 * 
 */
public class MultinomialNaiveBayesClassificaitonService implements
		ClassificationService {

	Classify classification;

	public Classify getClassification() {
		return classification;
	}

	public void setClassification(Classify classification) {
		this.classification = classification;
	}

	@Override
	public String classify(Vector instance) {
		return classification.predict(instance);
	}

    @Override
    public Result classificationResult(Vector instance) {
        return classification.getPredictionScores(instance);
    }

}
