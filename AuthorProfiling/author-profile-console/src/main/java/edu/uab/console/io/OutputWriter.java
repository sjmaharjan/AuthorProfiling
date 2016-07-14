package edu.uab.console.io;

import edu.uab.console.model.Author;

import java.io.*;

/**
 * Created  on 3/2/14.
 */
public class OutputWriter {
//    <author id="{author-id}"
//    type="blog|twitter|socialmedia|reviews"
//    lang="en|es"
//    age_group="18-24|25-34|35-49|50-64|65-xx"
//    gender="male|female"
//            />

    public static void writeOutput(Author a, String outputDir) {

        try {

            File file = new File(outputDir);
            if (!file.exists()) {
                if (file.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }


            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(file,a.getAuthorID() + ".xml"),false), "utf-8"));
            out.write(a.getOutputString());

            /* Close the output stream */
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}





