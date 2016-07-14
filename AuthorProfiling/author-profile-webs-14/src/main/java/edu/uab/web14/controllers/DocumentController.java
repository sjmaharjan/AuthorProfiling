/**
 *
 */
package edu.uab.web14.controllers;

import java.io.IOException;
import java.util.*;

import edu.uab.console.classifier.Classifier;
import edu.uab.console.classifier.naivebayes.NaiveBayesClassifier;
import edu.uab.console.classifier.svm.LibLinearSvmClassifier;
import edu.uab.console.model.AgeGroup;
import edu.uab.console.model.Gender;
import edu.uab.console.model.Result;
import edu.uab.console.tokenize.NGram;
import edu.uab.console.tokenize.Tokenize;
import edu.uab.console.tokenize.analyzers.ArkTweetAnalyzer;
import edu.uab.console.utils.Helper;
import edu.uab.console.vectorize.AbstractVectorizer;
import edu.uab.console.vectorize.TFIDFVectorizer;
import edu.uab.web14.controllers.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.math.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;


/**
 * @author sjmaharjan
 */
@Controller
public class DocumentController {

    private static final Logger log = LoggerFactory
            .getLogger(DocumentController.class);

    private static Map<Integer, String> labels = new LinkedHashMap<Integer, String>();
    //add class labels format:age_gender
    static {
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
        }
    private Analyzer analyzer = new ArkTweetAnalyzer(Version.LUCENE_40);
    private Tokenize tokenizer = new NGram(2, 3, true, analyzer);
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private String enModelDirectory;
    private static Map<String, Integer> enDictionary;
    private static int enNumberOfTrainingDocs = 12564;
    private static Map<String, Integer> enIdf;
    private AbstractVectorizer enVectorizer;
    private Classifier enClassifier;

    private String esModelDirectory;
    private static Map<String, Integer> esDictionary;
    private static int esNumberOfTrainingDocs = 1538;
    private static Map<String, Integer> esIdf;
    private AbstractVectorizer esVectorizer;
    private Classifier esClassifier;

    public DocumentController() {
        try {
            enModelDirectory = new ClassPathResource("/model/en").getFile().getAbsolutePath();
            enDictionary = Helper.loadSequenceFile(enModelDirectory + FILE_SEPARATOR + Helper.DICTIONARY_FILE);
            enIdf = Helper.loadSequenceFile(enModelDirectory + FILE_SEPARATOR + Helper.IDF_FILE);
            enVectorizer = new TFIDFVectorizer(enDictionary, enIdf, enNumberOfTrainingDocs);
            enClassifier = new LibLinearSvmClassifier(enModelDirectory, labels);

            esModelDirectory = new ClassPathResource("/model/es").getFile().getAbsolutePath();
            esIdf = Helper.loadSequenceFile(esModelDirectory + FILE_SEPARATOR + Helper.IDF_FILE);
            esDictionary = Helper.loadSequenceFile(esModelDirectory + FILE_SEPARATOR + Helper.DICTIONARY_FILE);
            esVectorizer = new TFIDFVectorizer(esDictionary, esIdf, esNumberOfTrainingDocs);
            esClassifier = new LibLinearSvmClassifier(esModelDirectory, labels);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String setupForm(Model model) {
        Document document = new Document();
        model.addAttribute(document);
        return "documentForm";
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public
    @ResponseBody
    Result processSubmit(@ModelAttribute("document") Document document) {
        log.info("the content is {}", document.getContent());
        List<String> tokens = new ArrayList<String>();
        Result scores = null;
        try {
            tokens = tokenizer.tokens(document.getContent());
            log.info("tokens", tokens);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(document.getLanguage().equalsIgnoreCase("en")) {
            Vector testVector = enVectorizer.createVector(tokens);
            scores = enClassifier.predict_with_probabilities(testVector);
            scores.rearrangeProbsEn();
            log.info("classifiedClass en", scores.getPrediction());
       } else {
            Vector testVector = esVectorizer.createVector(tokens);
            scores = esClassifier.predict_with_probabilities(testVector);
            scores.rearrangeProbsEs();
            log.info("classifiedClass es", scores.getPrediction());
       }

        return scores;
    }

}
