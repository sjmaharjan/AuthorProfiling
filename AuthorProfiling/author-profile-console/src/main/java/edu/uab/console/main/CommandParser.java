package edu.uab.console.main;

import com.beust.jcommander.Parameter;

/**
 * Created  on 3/2/14.
 */
public class CommandParser {

    @Parameter(names = "-i", description = "path/to/test/corpus")
     String input;

    @Parameter(names = "-o", description = "path/to/output/directory")
     String output;

    @Parameter(names = "-m", description = "path/to/classification/model")
     String model;

//    @Parameter(names = "-l", description = "en|es")
//     String language;
//
//    @Parameter(names = "-g", description = "blog|twitter|socialmedia|reviews")
//     String type;


}
