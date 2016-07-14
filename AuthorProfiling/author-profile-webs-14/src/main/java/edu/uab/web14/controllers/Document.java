/**
 *
 */
package edu.uab.web14.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
}
