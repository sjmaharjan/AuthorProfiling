package edu.uab.console.vectorize;

import edu.uab.console.model.Documents;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.mahout.math.Vector;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suraj on 3/29/14.
 */
public abstract class AbstractVectorizer {

    public abstract Vector createVector(List<String> tokens);


}
