package edu.uab.console.model;

/**
 * Created by suraj on 3/25/14.
 */
public class Document {

    private String id;
    private String url;
    private String content="";

    public Document() {

    }

    public Document(String id, String url, String content) {
        this.id = id;
        this.url = url;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl(String url) {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
