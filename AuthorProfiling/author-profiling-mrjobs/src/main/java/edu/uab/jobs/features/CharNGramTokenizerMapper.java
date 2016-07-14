package edu.uab.jobs.features;


import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 11/23/13
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class CharNGramTokenizerMapper extends Mapper<Text, Text, Text, TextTuple> {
    private int max;
    private int min;

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        HTMLStripCharFilter stripHtml = new HTMLStripCharFilter(new StringReader(value.toString()));
        NGramTokenizer gramTokenizer = new NGramTokenizer(stripHtml, 3, 5);
        TokenStream stream = new LowerCaseFilter(Version.LUCENE_CURRENT, gramTokenizer);
        TextTuple document = new TextTuple();

        CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                String word = new String(termAtt.buffer(), 0, termAtt.length());
                document.add(word);
            }
        }
        stream.end();
        stream.close();
        context.write(key, document);
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        min = context.getConfiguration().getInt("MIN", 1);
        max = context.getConfiguration().getInt("MAX", 1);
    }


    public static void main(String[] args) throws IOException {
        String text = " <div class=\"fontRenderer\"><div id=\"originalText_d28fbe21263e1b2b0c78de7214da8be0\">umbrella what is this I WANT</div>\n" +
                "                <script type=\"text/javascript\">\n" +
                "                        var filenames = [\"/s/j/class.fontrenderer.js\"];\n" +
                "\n" +
                "                        ComCore.BootLoader.loadJavascriptFiles(filenames, false, function() \n" +
                "                        {\n" +
                "                                new ComCore.FontRenderer(\"_0acd6e735cdf96ddf360ece3e8f5186559306826a70c9fd316fbf7be7b833edc\", \"IT+IS+WHAT+IT+IS\", \"d28fbe21263e1b2b0c78de7214da8be0\");\n" +
                "                        });\n" +
                "                </script></div>";
        for (int i = 0; i < 4; i++) {
            HTMLStripCharFilter stripHtml = new HTMLStripCharFilter(new StringReader(text));

            NGramTokenizer gramTokenizer = new NGramTokenizer(stripHtml, 3, 3);
//        TokenStream result = new StandardFilter(matchVersion, source);
            TokenStream stream = new LowerCaseFilter(Version.LUCENE_CURRENT, gramTokenizer);
            CharTermAttribute termAtt = stream
                    .getAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                if (termAtt.length() > 0) {
                    String word = new String(termAtt.buffer(), 0, termAtt.length());
                    System.out.print(word + ":");
                }
            }
            stream.end();

            stream.close();

            System.out.println();
        }
    }
}
