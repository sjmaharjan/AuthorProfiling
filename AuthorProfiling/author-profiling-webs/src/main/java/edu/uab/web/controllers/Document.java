/**
 *
 */
package edu.uab.web.controllers;

import java.io.IOException;
import java.util.Map;

import org.apache.mahout.math.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uab.web.utils.DocumentVector;

/**
 * @author sjmaharjan
 */
public class Document {
    private static Logger log = LoggerFactory.getLogger(Document.class);


    private String content;

    private String language;


    public Document() {
        super();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;

    }

    @Override
    public String toString() {
        return "Document{" +
                "content='" + content + '\'' +
                ", language='" + language + '\'' +
                '}';
    }

    public Vector toVector(Map<String, Integer> dictionary) {
        Vector instance = null;
        try {
            instance = DocumentVector.vectorize(this.content, dictionary);
        } catch (IOException e) {
            log.info("Cloudn't create vector");
            e.printStackTrace();
        }
        return instance;
    }

    public Vector toNGramTfIdfVector(Map<String, Integer> dictionary, Map<String, Integer> idf) {
        Vector instance = null;
        try {
            instance = DocumentVector.tfIdfNGramVectorize(this.content, dictionary, idf,this.language);
        } catch (IOException e) {
            log.info("Cloudn't create vector");
            e.printStackTrace();
        }
        return instance;
    }


    public Vector NGramTfIdfVector(Map<String, Integer> dictionary) {
        Vector instance = null;
        try {
            instance = DocumentVector.NGramVectorize(this.content, dictionary);
        } catch (IOException e) {
            log.info("Cloudn't create vector");
            e.printStackTrace();
        }
        return instance;
    }
}
