package edu.uab.console.tokenize;

import edu.uab.console.model.Documents;

import java.io.IOException;
import java.util.List;

/**
 * Created by suraj on 3/29/14.
 */
public interface Tokenize {

    public List<String> tokens(Documents documents) throws IOException;
    public List<String> tokens(String text) throws IOException;

}
