package edu.uab.console.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suraj on 3/25/14.
 */
public class Documents {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private List<Document> documents;
    private String url;

    public Documents() {
        documents = new ArrayList<Document>();
    }

    public void addDocument(Document d) {
        this.documents.add(d);
    }

    public void removeDocument(Document d) {
        this.documents.remove(d);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getAllDocumentsContent() {
        StringBuilder content = new StringBuilder();
        boolean first = true;
        for (Document d : documents) {
            if (first) {
                content.append(d.getContent());
                first = false;
            } else {
                content.append(LINE_SEPARATOR);
                content.append(d.getContent());
            }

        }
        return content.toString();
    }

    @Override
    public String toString() {
        return "Documents{" +
                ", url='" + url + '\'' +
                '}';
    }
}
