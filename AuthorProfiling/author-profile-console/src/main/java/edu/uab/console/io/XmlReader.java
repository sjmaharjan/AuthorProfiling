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
 * Created  on 3/2/14.
 */
public class XmlReader {
    public static Author readXml(String fileName) {
        Author author = new Author();
        try {

            File fXmlFile = new File(fileName);
            InputStream inputStream = new FileInputStream(fXmlFile);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");


            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);


            doc.getDocumentElement().normalize();

            // System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            //Set author properties
            author.setAuthorID(Helper.extractAuthorID(fXmlFile.getName()));
            author.setType(doc.getDocumentElement().getAttribute("type"));
            author.setLanguage(doc.getDocumentElement().getAttribute("lang"));
            author.setGender(doc.getDocumentElement().getAttribute("gender"));
            author.setAgeGroup(doc.getDocumentElement().getAttribute("age_group"));


            //System.out.println("----------------------------");

            Documents documents = new Documents();
            documents.setUrl(doc.getDocumentElement().getAttribute("url"));
            NodeList nList = doc.getElementsByTagName("document");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    edu.uab.console.model.Document document = new edu.uab.console.model.Document();

                    Element eElement = (Element) nNode;
                    document.setId(eElement.getAttribute("id"));
                    document.getUrl(eElement.getAttribute("url"));
                    document.setContent(eElement.getTextContent());
                    documents.addDocument(document);


                }
            }
           // System.out.println(documents.getAllDocumentsContent());
            author.setDocs(documents);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Can't read xml file , object creation error");

        }
        return author;
    }


    public static void main(String args[]) {
        System.out.println(Helper.extractAuthorID("0a012eb53f5e7bad0691c9a0fcb761d6_en_35-49_male.xml"));
        Author a = XmlReader.readXml("/Users/suraj/Downloads/pan14-author-profiling-training-corpus-2014-02-24/pan14-author-profiling-training-corpus-blogs-2014-02-24/en/0a012eb53f5e7bad0691c9a0fcb761d6_en_35-49_male.xml");
        System.out.println(a);

    }
}
