/**
 *
 */
package edu.uab.web.controllers;

import java.io.IOException;
import java.util.*;

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

import edu.uab.web.services.ClassificationService;
import edu.uab.web.utils.DocumentVector;

import javax.annotation.Resource;


/**
 * @author sjmaharjan
 */
@Controller
public class DocumentController {

    private static final Logger log = LoggerFactory
            .getLogger(DocumentController.class);
    private static Map<String, Integer> dictionary;
    private static Map<String, Integer> idf;
    @Autowired
    @Qualifier("enNbClassificationService")
    private ClassificationService classify;


    @Autowired
    @Qualifier("esNbClassificationService")
    private ClassificationService esClassify;

    private static Map<String, Integer> spanishDictionary;
    private static Map<String, Integer> spanishIdf;


    public DocumentController() {


        try {
            dictionary = DocumentVector
                    .makedictionary(new ClassPathResource("/model/english/dictionary.file").getFile().getAbsolutePath());
           // idf = DocumentVector
            //        .makedictionary(new ClassPathResource("/model/english/idf.file").getFile().getAbsolutePath());

            spanishDictionary = DocumentVector
                    .makedictionary(new ClassPathResource("/model/spanish/dictionary.file").getFile().getAbsolutePath());
          //  spanishIdf = DocumentVector
            //        .makedictionary(new ClassPathResource("/model/spanish/idf.file").getFile().getAbsolutePath());

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    Result processSubmit(@ModelAttribute("document") Document document,
                         BindingResult result) {
        log.info("the content is {}", document.getContent());
        Result scores = null;
        if (!result.hasErrors()) {
            Vector test;
            if (document.getLanguage().equalsIgnoreCase("en")) {
                test = document.NGramTfIdfVector(dictionary);
                scores = classify.classificationResult(test);
            } else {
                test = document.NGramTfIdfVector(spanishDictionary);
                scores = esClassify.classificationResult(test);
            }

        }

        return scores;


    }

}
