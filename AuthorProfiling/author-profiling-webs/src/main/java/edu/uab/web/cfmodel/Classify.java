/**
 *
 */
package edu.uab.web.cfmodel;

import edu.uab.web.controllers.Result;
import org.apache.mahout.math.Vector;

/**
 * @author sjmaharjan
 */
public interface Classify {
    public void intializeModel();

    public Vector classifyFull(Vector testVector);

    public String predict(Vector testVector);

    public Result getPredictionScores(Vector instance);

}
