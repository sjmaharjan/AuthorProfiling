package edu.uab.console.main;

import com.beust.jcommander.JCommander;
import edu.uab.console.classifier.Classifier;
import edu.uab.console.classifier.naivebayes.NaiveBayesClassifier;
import edu.uab.console.classifier.similarity.CosineSimilarity;
import edu.uab.console.classifier.svm.LibLinearSvmClassifier;
import edu.uab.console.io.DailyStrengthReader;
import edu.uab.console.io.OutputWriter;
import edu.uab.console.io.Pan13TextReader;
import edu.uab.console.io.XmlReader;
import edu.uab.console.model.AgeGroup;
import edu.uab.console.model.Author;
import edu.uab.console.model.Gender;
import edu.uab.console.model.Language;
import edu.uab.console.tokenize.CharNGram;
import edu.uab.console.tokenize.NGram;
import edu.uab.console.tokenize.Tokenize;
import edu.uab.console.tokenize.analyzers.ArkTweetAnalyzer;
import edu.uab.console.tokenize.analyzers.Pan2013Analyzer;
import edu.uab.console.utils.Helper;
import edu.uab.console.vectorize.AbstractVectorizer;
import edu.uab.console.vectorize.TFIDFVectorizer;
import edu.uab.jobs.App;
import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.ConfusionMatrix;
import org.apache.mahout.math.Vector;
import org.hsqldb.jdbc.jdbcDataSource;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.util.*;

/**
 * Created  on 3/2/14.
 */
public class AppMain {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private Map<String, Integer> dictionary;
    private Map<String, Integer> idf;
    private Tokenize tokenizer;
    private AbstractVectorizer vectorizer;
    private Classifier classifier;

    public AppMain(Tokenize tokenizer, AbstractVectorizer vectorizer, Classifier classifier, Map<String, Integer> dictionary, Map<String, Integer> idf) {
        this.tokenizer = tokenizer;
        this.vectorizer = vectorizer;
        this.classifier = classifier;
        this.idf = idf;
        this.dictionary = dictionary;
    }


    public void run(String inputDirectory, String outputDirectory) {
        File folder = new File(inputDirectory);
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles);

        List<String> symbols = classifier.labels();

        ConfusionMatrix cf = new ConfusionMatrix(symbols, "DEFAULT");

        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().equals(".DS_Store") || listOfFiles[i].getName().equals("truth.txt") || listOfFiles[i].getName().toLowerCase().contains("_na"))
                    continue;
//                System.out.println("Reading file " + listOfFiles[i].getName());
                //
//                Author author = XmlReader.readXml(listOfFiles[i].getAbsolutePath());
                // extract author content and send it to prediction
                Author author = DailyStrengthReader.readText(listOfFiles[i].getAbsolutePath());
                System.out.print(listOfFiles[i].getName()+" ");
                try {
                    //tokenize
                    List<String> tokens = tokenizer.tokens(author.getDocs());
                    //vectorize
                    Vector testVector = vectorizer.createVector(tokens);
                    // System.out.println(testVector );
                    //do classification

                    String classifiedClass = classifier.predict(testVector); //returned string format age_gender
                    author.setAgeGroup(getAgeGroup(classifiedClass));
                    author.setGender(getGender(classifiedClass));
                    cf.addInstance(Helper.getLabel(listOfFiles[i].getName()), classifiedClass);
//                   System.out.print(Helper.getLabel(listOfFiles[i].getName()) + ",");
//                    if(Helper.getLabel(listOfFiles[i].getName()).equalsIgnoreCase(classifiedClass))
//                        System.out.print(1);
//                    else
//                        System.out.print(0);
//                    System.out.print(","+listOfFiles[i].getName());
//                    System.out.println();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                OutputWriter.writeOutput(author, outputDirectory);


            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }

        }
        //print confusion matrix and accuracy
        Helper.printAccuracy(cf);
    }

    private String getAgeGroup(String classifiedClass) {
        return classifiedClass.split("_")[0];
    }

    private String getGender(String classifiedClass) {
        return classifiedClass.split("_")[1];
    }

    public static String detectLanguage(String inputDirectory) {
        File folder = new File(inputDirectory);
        File[] listOfFiles = folder.listFiles();
        Author author;
        if (!listOfFiles[3].getName().equals("truth.txt"))
            author = XmlReader.readXml(listOfFiles[3].getAbsolutePath());
        else {
            author = XmlReader.readXml(listOfFiles[4].getAbsolutePath());
        }

        if (author.getLanguage() == Language.ENGLISH)
            return "english";
        else {
            return "spanish";
        }
    }


    public static void main(String args[]) {

        CommandParser cp = new CommandParser();
        new JCommander(cp, args);

        // parse the command line arguments

        String inputDirectory = cp.input.toString();
        String outputDirectory = cp.output.toString();
        String modelDirectory = cp.model.toString();
//        String language = cp.language.toString();
//        String type = cp.type.toString();

        //System.out.println(inputDirectory +" " +outputDirectory +" " +language +" " + type +" " + modelDirectory);

        //create necessary objects
        //blog en -> 107 es ->68
        //reviews -> 2912
        //socialmedia en -> 5428 es-> 896
        //twitter en-> 216 es-> 132
        // String lang = AppMain.detectLanguage(inputDirectory);
        String lang = "english";
        System.out.println("Loading model for " + lang);

        int numberOfTrainingDocs;


        if (lang.equalsIgnoreCase("english")) {

//            numberOfTrainingDocs = 12359;
            numberOfTrainingDocs = 10770;
//           numberOfTrainingDocs = 12564;

            modelDirectory = modelDirectory + AppMain.FILE_SEPARATOR + "en";
        } else {
            numberOfTrainingDocs = 10770;
            modelDirectory = modelDirectory + AppMain.FILE_SEPARATOR + "es";

        }


        // integer -> class label mapping
        Map<Integer, String> labels = new LinkedHashMap<Integer, String>();
        //add class labels format:age_gender
        labels.put(0, AgeGroup.AG_18_24.toString() + "_" + Gender.MALE.toString());
        labels.put(1, AgeGroup.AG_18_24.toString() + "_" + Gender.FEMALE.toString());
        labels.put(2, AgeGroup.AG_25_34.toString() + "_" + Gender.MALE.toString());
        labels.put(3, AgeGroup.AG_25_34.toString() + "_" + Gender.FEMALE.toString());
        labels.put(4, AgeGroup.AG_35_49.toString() + "_" + Gender.MALE.toString());
        labels.put(5, AgeGroup.AG_35_49.toString() + "_" + Gender.FEMALE.toString());
        labels.put(6, AgeGroup.AG_50_64.toString() + "_" + Gender.MALE.toString());
        labels.put(7, AgeGroup.AG_50_64.toString() + "_" + Gender.FEMALE.toString());
        labels.put(8, AgeGroup.AG_65_PLUS.toString() + "_" + Gender.MALE.toString());
        labels.put(9, AgeGroup.AG_65_PLUS.toString() + "_" + Gender.FEMALE.toString());
        labels.put(10, AgeGroup.AG_UNKNOWN.toString() + "_" + Gender.GENDER_UNKNOWN.toString());

        System.out.println(labels);


        try {
            Map<String, Integer> dictionary = Helper.loadSequenceFile(modelDirectory + AppMain.FILE_SEPARATOR + Helper.DICTIONARY_FILE);
            Map<String, Integer> idf = Helper.loadSequenceFile(modelDirectory + AppMain.FILE_SEPARATOR + Helper.IDF_FILE);
            Analyzer analyzer = new ArkTweetAnalyzer(Version.LUCENE_40);

            Tokenize tokenizer = new NGram(2, 3, true, analyzer);

            //Tokenize tokenizer = new CharNGram(3,5);

            AbstractVectorizer vectorizer = new TFIDFVectorizer(dictionary, idf,
                    numberOfTrainingDocs);

            Classifier classifier = new LibLinearSvmClassifier(modelDirectory, labels);
            System.out.println("Finished loading model");

            AppMain app = new AppMain(tokenizer, vectorizer, classifier, dictionary, idf);
            app.run(inputDirectory, outputDirectory);


        } catch (IOException e) {
            System.err.println("Could not find the file");
            e.printStackTrace();
        }


    }
}
