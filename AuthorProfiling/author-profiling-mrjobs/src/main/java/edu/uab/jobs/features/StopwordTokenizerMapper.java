package edu.uab.jobs.features;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: prasha
 * Date: 1/24/14
 * Time: 12:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class StopwordTokenizerMapper  extends Mapper<Text, Text, Text, TextTuple> {
    private Analyzer analyzer;
    ShingleAnalyzerWrapper shingleAnalyzer;
    public static final HashSet ENGLISH_STOP_WORDS_SET;

    static {
        ENGLISH_STOP_WORDS_SET = new HashSet(Arrays.asList("a", "about", "above", "after", "again", "against",
                "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before",
                "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did",
                "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from",
                "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's",
                "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll",
                "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more",
                "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other",
                "ought", "our", "ours ", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll",
                "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs",
                "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've",
                "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll",
                "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who",
                "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
                "your", "yours", "yourself", "yourselves"));
    }

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
        TextTuple document = new TextTuple();
        TokenStream stream = analyzer.tokenStream(key.toString(),
                new StringReader(value.toString()));
//		shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);
//        shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 3, 3, " ", false, false);
//        TokenStream stream = shingleAnalyzer.tokenStream(key.toString(),
//                new StringReader(value.toString()));
        CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                String word = new String(termAtt.buffer(), 0, termAtt.length());
                if(ENGLISH_STOP_WORDS_SET.contains(word)){
                    document.add(word);
                }
            }
        }
        context.write(key, document);
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
    }
}
