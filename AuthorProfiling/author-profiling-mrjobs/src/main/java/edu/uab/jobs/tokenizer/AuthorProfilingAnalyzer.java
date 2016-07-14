/**
 *
 */
package edu.uab.jobs.tokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;


public class AuthorProfilingAnalyzer extends Analyzer {
    private final Version matchVersion;
    public static final CharArraySet SPANISH_STOP_WORDS_SET;

    static {
        final List<String> RANKSNL_STOP_WORD = Arrays.asList("un", "una", "unas", "unos", "uno", "sobre", "todo", "también", "tras", "otro", "algún", "alguno",
                "alguna", "algunos", "algunas", "ser", "es", "soy", "eres", "somos", "sois", "estoy", "esta", "estamos", "estais", "estan", "como", "en", "para",
                "atras", "porque", "por qué", "estado", "estaba", "ante", "antes", "siendo", "ambos", "pero", "por", "poder", "puede", "puedo", "podemos",
                "podeis", "pueden", "fui", "fue", "fuimos", "fueron", "hacer", "hago", "hace", "hacemos", "haceis", "hacen", "cada", "fin", "incluso",
                "primero	desde", "conseguir", "consigo", "consigue", "consigues", "conseguimos", "consiguen", "ir", "voy", "va", "vamos", "vais",
                "van", "vaya", "gueno", "ha", "tener", "tengo", "tiene", "tenemos", "teneis", "tienen", "el", "la", "lo", "las", "los", "su", "aqui",
                "mio", "tuyo", "ellos", "ellas", "nos", "nosotros", "vosotros", "vosotras", "si", "dentro", "solo", "solamente", "saber", "sabes",
                "sabe", "sabemos", "sabeis", "saben", "ultimo", "largo", "bastante", "haces", "muchos", "aquellos", "aquellas", "sus", "entonces",
                "tiempo", "verdad", "verdadero", "verdadera	cierto", "ciertos", "cierta", "ciertas", "intentar", "intento", "intenta", "intentas",
                "intentamos", "intentais", "intentan", "dos", "bajo", "arriba", "encima", "usar", "uso", "usas", "usa", "usamos", "usais", "usan",
                "emplear", "empleo", "empleas", "emplean", "ampleamos", "empleais", "valor", "muy", "era", "eras", "eramos", "eran", "modo", "bien",
                "cual", "cuando", "donde", "mientras", "quien", "con", "entre", "sin", "trabajo", "trabajar", "trabajas", "trabaja", "trabajamos",
                "trabajais", "trabajan", "podria", "podrias", "podriamos", "podrian", "podriais", "yo", "aquel");
        final CharArraySet stopSet = new CharArraySet(Version.LUCENE_CURRENT,
                RANKSNL_STOP_WORD, false);
        SPANISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }


    public AuthorProfilingAnalyzer(Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.lucene.analysis.Analyzer#createComponents(java.lang.String,
     * java.io.Reader)
     */

    //pan14 analyzer
    @Override
    protected TokenStreamComponents createComponents(String fieldName,
                                                     Reader reader) {
        // TODO need to write more complex analyzer
        final Tokenizer source = new WhitespaceTokenizer(matchVersion, reader);
        TokenStream result = new LowerCaseFilter(matchVersion, source);

        CharTermAttribute termAtt = result
                .getAttribute(CharTermAttribute.class);
        StringBuilder buf = new StringBuilder();
        try {
            result.reset();
            while (result.incrementToken()) {
                if (termAtt.length() > 0) {
                    buf.append(termAtt.toString()).append(" ");
                }

            }
            result.end();
            result.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> tokens = Twokenize.tokenize(buf.toString());
        StringBuilder resultBuf = new StringBuilder();
        for (String token : tokens) {
            resultBuf.append(token).append(" ");
        }

        return new TokenStreamComponents(new WhitespaceTokenizer(matchVersion, new StringReader(resultBuf.toString())));

    }

    //pan13 analyzer
//    @Override
//    protected TokenStreamComponents createComponents(String fieldName,
//                                                     Reader reader) {
//        // TODO need to write more complex analyzer
//        final Tokenizer source = new StandardTokenizer(matchVersion, reader);
//        TokenStream result = new StandardFilter(matchVersion, source);
//        result = new LowerCaseFilter(matchVersion, result);
//        return new TokenStreamComponents(source, result);
//
//    }


    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        // wrap the Reader in a HTMLStripCharFilter chain.
        return new HTMLStripCharFilter(reader);
    }


    public static void main(String[] args) throws IOException {
        //		String text = "Hello<a href=\"http://heeel.com\"> prasha </a> <br/>";

        for (int i = 0; i < 4; i++) {
            Analyzer analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);

            String text = " <div class=\"fontRenderer\"><div id=\"originalText_d28fbe21263e1b2b0c78de7214da8be0\">IT IS WHAT IT IS.... :-);-)</div>\n" +
                    "                <script type=\"text/javascript\">\n" +
                    "                        var filenames = [\"/s/j/class.fontrenderer.js\"];\n" +
                    "\n" +
                    "                        ComCore.BootLoader.loadJavascriptFiles(filenames, false, function() \n" +
                    "                        {\n" +
                    "                                new ComCore.FontRenderer(\"_0acd6e735cdf96ddf360ece3e8f5186559306826a70c9fd316fbf7be7b833edc\", \"IT+IS+WHAT+IT+IS\", \"d28fbe21263e1b2b0c78de7214da8be0\");\n" +
                    "                        });\n" +
                    "                </script></div>";
            //String text = "hello this is a test. This is great, great.";

            // System.out.println(Twokenize.tokenize(text));

            TokenStream stream = analyzer.tokenStream("text", new StringReader(text));
            CharTermAttribute termAtt = stream
                    .getAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                if (termAtt.length() > 0) {
                    String word = new String(termAtt.buffer(), 0, termAtt.length());
                    System.out.print(word + " ");
                }
            }
            System.out.println();
        }
    }

}
