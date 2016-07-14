package edu.uab.console.tokenize;

import edu.uab.console.model.Document;
import edu.uab.console.model.Documents;
import edu.uab.console.tokenize.analyzers.ArkTweetAnalyzer;
import edu.uab.jobs.writables.TextTuple;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suraj on 3/29/14.
 */
public class CharNGram implements Tokenize {

    private int min;
    private int max;


    public CharNGram(int gram) {
        this(gram, gram);
    }

    public CharNGram(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public List<String> tokens(Documents documents) throws IOException {
        List<String> tokens = new ArrayList<String>();
        //strip off the html tags
        HTMLStripCharFilter stripHtml = new HTMLStripCharFilter(new StringReader(documents.getAllDocumentsContent().toString()));
        NGramTokenizer gramTokenizer = new NGramTokenizer(stripHtml, this.min, this.max);
        TokenStream stream = new LowerCaseFilter(Version.LUCENE_40, gramTokenizer);
        CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                tokens.add(termAtt.toString());
            }
        }
        stream.end();
        stream.close();
        return tokens;
    }

    @Override
    public List<String> tokens(String text) throws IOException {
        List<String> tokens = new ArrayList<String>();
        //strip off the html tags
        HTMLStripCharFilter stripHtml = new HTMLStripCharFilter(new StringReader(text));
        NGramTokenizer gramTokenizer = new NGramTokenizer(stripHtml, this.min, this.max);
        TokenStream stream = new LowerCaseFilter(Version.LUCENE_40, gramTokenizer);
        CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                tokens.add(termAtt.toString());
            }
        }
        stream.end();
        stream.close();
        return tokens;
    }

    public static void main(String[] args) throws IOException {
        String text = " <div class=\"fontRenderer\"><div id=\"originalText_d28fbe21263e1b2b0c78de7214da8be0\">IT IS WHAT IT IS.... :-);-)</div>\n" +
                "                <script type=\"text/javascript\">\n" +
                "                        var filenames = [\"/s/j/class.fontrenderer.js\"];\n" +
                "\n" +
                "                        ComCore.BootLoader.loadJavascriptFiles(filenames, false, function() \n" +
                "                        {\n" +
                "                                new ComCore.FontRenderer(\"_0acd6e735cdf96ddf360ece3e8f5186559306826a70c9fd316fbf7be7b833edc\", \"IT+IS+WHAT+IT+IS\", \"d28fbe21263e1b2b0c78de7214da8be0\");\n" +
                "                        });\n" +
                "                </script></div>";


        Documents doc = new Documents();
        doc.addDocument(new Document("1", "url", text));
        CharNGram tokens = new CharNGram(2,6);
        System.out.println(tokens.tokens(doc));
    }
}
