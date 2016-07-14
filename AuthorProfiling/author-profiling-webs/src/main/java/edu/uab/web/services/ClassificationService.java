/**
 * 
 */
package edu.uab.web.services;

import edu.uab.web.controllers.Result;
import org.apache.mahout.math.Vector;

/**
 * @author sjmaharjan
 *
 */
public interface ClassificationService {
	public String classify(Vector instance);
    public Result classificationResult(Vector instance);
	
}
