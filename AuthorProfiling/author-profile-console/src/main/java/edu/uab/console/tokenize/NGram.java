package edu.uab.console.tokenize;

import edu.uab.console.model.Document;
import edu.uab.console.model.Documents;

import edu.uab.console.tokenize.analyzers.ArkTweetAnalyzer;
import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suraj on 3/29/14.
 */
public class NGram implements Tokenize {

    private Analyzer analyzer;

    private int min;
    private int max;
    private boolean unigrams;
    private ShingleAnalyzerWrapper ngram;
    private static final Logger log = LoggerFactory
            .getLogger(NGram.class);


    public NGram(int min, Analyzer analyzer) {
        this(min, min, false, analyzer);
    }

    public NGram(int min, int max, boolean unigram, Analyzer analyzer) {
        this.min = min;
        this.max = max;
        this.unigrams = unigram;
        this.analyzer = analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }


    @Override
    public List<String> tokens(Documents documents) throws IOException {
        List<String> tokens = new ArrayList<String>();
        TokenStream stream;
        if (min <= max && min != 1 && max != 1)      {
            ngram = new ShingleAnalyzerWrapper(analyzer, min, max, " ", unigrams, unigrams);

        stream = ngram.tokenStream(documents.getUrl(),
                new StringReader(documents.getAllDocumentsContent()));
        }else{
            stream=analyzer.tokenStream(documents.getUrl(),
                    new StringReader(documents.getAllDocumentsContent()));
        }
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
        log.info("in ngram:", text);
        List<String> tokens = new ArrayList<String>();
        TokenStream stream;
        if (min <= max && min != 1 && max != 1)      {
            ngram = new ShingleAnalyzerWrapper(analyzer, min, max, " ", unigrams, unigrams);

            stream = ngram.tokenStream("", new StringReader(text));
        }else{
            stream=analyzer.tokenStream("",
                    new StringReader(text));
        }
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
        return tokens;    }


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

        Analyzer analyzer = new ArkTweetAnalyzer(Version.LUCENE_40);
//        Documents doc = new Documents();
//        doc.addDocument(new Document("1", "url", text));
        NGram ngram = new NGram(2, 3, true, analyzer);


        System.out.println(ngram.tokens(text));
    }
}
