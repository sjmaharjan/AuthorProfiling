package edu.uab.console.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.classifier.ConfusionMatrix;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created  on 3/2/14.
 */
public class Helper {


    public static final String DICTIONARY_FILE = "dictionary.file";
    public static final String IDF_FILE = "idf.file";

    public static String extractAuthorID(String name) {
        return name.substring(0, name.indexOf("."));

    }

    public static String extractAuthorAgeGroup(String name) {
        String label = null;
        try {
            label = name.substring(name.indexOf("_", name.indexOf("_") + 1) + 1, name.lastIndexOf("_"));
//            if (label.equalsIgnoreCase("65-XX") || label.equalsIgnoreCase("65-xx")|| label.equalsIgnoreCase("65-plus") || label.equalsIgnoreCase("65-PLUS"))
//                label = "65+";
        } catch (StringIndexOutOfBoundsException e) {
            label = "xxx";
        }
        return label.toLowerCase();

    }

    public static String extractAuthorGender(String name) {
        String label = null;
        try {
            label = name.substring(name.lastIndexOf("_") + 1, name.indexOf(".", name.lastIndexOf("_") + 1));

        } catch (StringIndexOutOfBoundsException e) {
            label = "xxx";
        }
        return label.toLowerCase();

    }

    public static Map<String, Integer> loadSequenceFile(String file) throws IOException {
        Map<String, Integer> dictionary = new HashMap<String, Integer>();
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.getLocal(conf);

        Path path = new Path(file);
        SequenceFile.Reader reader = null;
        try {
            reader = new SequenceFile.Reader(fs, path, conf);
            Text key = new Text();
            IntWritable value = new IntWritable();
            while (reader.next(key, value)) {
                dictionary.put(key.toString(), value.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(reader);
        }

        return dictionary;
    }

    public static Map<String, Integer> wordCount(List<String> tokens) {
        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        for (String word : tokens) {
            if (wordCount.containsKey(word)) {
                wordCount.put(word, wordCount.get(word) + 1);
            } else {
                wordCount.put(word, 1);
            }
        }
        return wordCount;
    }

    public static int maximum(int[] myArray) {
        assert myArray != null;
        int max = myArray[0];
        for (int i = 0; i < myArray.length; i++) {
            if (myArray[i] > max) {
                max = myArray[i];
            }
        }
        return max;
    }


    public static int maximum(Map<String, Integer> wordCount) {
        assert wordCount != null;
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        return maxEntry.getValue();
    }


    public static void printAccuracy(ConfusionMatrix cf) {
        //get labels
        int correct = 0;
        int total = 0;

        for (String label : cf.getLabels()) {
            correct += cf.getCorrect(label);
            total += cf.getTotal(label);
        }
        System.out.println(cf.toString());
        System.out.println("-------------------------------------------------------");
        System.out.println("Accuracy  = " + String.format("%.2f", correct * 100.0 / total) + " %");
        System.out.println("=======================================================");

    }

    public static String getLabel(String filename) {
        return Helper.extractAuthorAgeGroup(filename) + "_" + Helper.extractAuthorGender(filename);
    }

    public static void main(String[] args) {
        System.out.println(Helper.extractAuthorID("0a012eb53f5e7bad0691c9a0fcb761d6_en_35-49_male.xml"));
        System.out.println(Helper.extractAuthorGender("0a012eb53f5e7bad0691c9a0fcb761d6_en_35-49_male.xml"));
        System.out.println(Helper.extractAuthorAgeGroup("0a012eb53f5e7bad0691c9a0fcb761d6_en_65-xx_female.xml"));
        System.out.println(Helper.getLabel("0a012eb53f5e7bad0691c9a0fcb761d6_en_65-xx_female.xml"));
    }
}
