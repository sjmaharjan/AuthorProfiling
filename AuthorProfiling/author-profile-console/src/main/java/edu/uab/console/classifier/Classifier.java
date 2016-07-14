package edu.uab.console.classifier;


import edu.uab.console.model.Result;
import org.apache.mahout.math.Vector;
import java.util.List;

/**
 * Created by suraj on 3/25/14.
 */
public interface Classifier {
    public List<String> labels();

    public  void loadModel();

    public  String predict(Vector instance);

    public Result predict_with_probabilities(Vector instance);




}
