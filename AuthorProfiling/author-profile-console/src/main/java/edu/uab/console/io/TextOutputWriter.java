package edu.uab.console.io;

import edu.uab.console.model.Author;
import edu.uab.console.utils.Helper;
import org.apache.mahout.classifier.ConfusionMatrix;
import org.apache.mahout.math.Vector;

import java.io.*;
import java.util.List;

/**
 * Created by suraj on 4/7/14.
 */
public class TextOutputWriter {

    public static void writeOutput(Author a, String filename, String outputDir) {
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
                    new FileOutputStream(new File(file, filename), false), "utf-8"));
            out.write(a.getDocs().getAllDocumentsContent());

            /* Close the output stream */
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];

        File folder = new File(inputDir);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().equals(".DS_Store"))
                    continue;
                System.out.println("Reading file " + listOfFiles[i].getName());
                Author author = XmlReader.readXml(listOfFiles[i].getAbsolutePath());
                TextOutputWriter.writeOutput(author, listOfFiles[i].getName(), outputDir);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }

        }

        System.out.println("Done ....");
    }
}
