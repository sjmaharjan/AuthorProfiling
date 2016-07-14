package edu.uab.console.io;

import edu.uab.console.model.Author;
import edu.uab.console.model.Documents;
import edu.uab.console.utils.Helper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

/**
 * Created by suraj on 4/3/14.
 */
public class Pan13TextReader {
    public static Author readText(String fileName) {
        Author author = new Author();

        BufferedReader reader = null;
        try {
            File fXmlFile = new File(fileName);
            InputStream inputStream = new FileInputStream(fXmlFile);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            StringBuilder builder = new StringBuilder();

            author.setAuthorID(Helper.extractAuthorID(fXmlFile.getName()));
            author.setType("blog");
            author.setLanguage("en");
            author.setGender("xxx");
            author.setAgeGroup("xxx");


            Documents documents = new Documents();
            documents.setUrl("url");
            edu.uab.console.model.Document document = new edu.uab.console.model.Document();


            document.setId("id");
            document.getUrl("url");

            String readline = null;
            while ((readline = reader.readLine()) != null) {
                builder.append(readline);

            }

            document.setContent(builder.toString());
            documents.addDocument(document);
            author.setDocs(documents);

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return author;
    }
}
