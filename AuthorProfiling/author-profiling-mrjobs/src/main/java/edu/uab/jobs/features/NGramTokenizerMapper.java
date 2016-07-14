package edu.uab.jobs.features;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import edu.uab.jobs.writables.FeaturesLinkedHashMap;
import edu.uab.jobs.writables.TextTuple;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 11/23/13
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class NGramTokenizerMapper extends Mapper<Text, Text, Text, TextTuple> {
    private Analyzer analyzer;
	ShingleAnalyzerWrapper shingleAnalyzer;

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
        TextTuple document = new TextTuple();
//        TokenStream stream = analyzer.tokenStream(key.toString(),
//                new StringReader(value.toString()));
		shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);
//        shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 2, " ", false, false);
		TokenStream stream = shingleAnalyzer.tokenStream(key.toString(),
				new StringReader(value.toString()));
		CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                String word = new String(termAtt.buffer(), 0, termAtt.length());
                document.add(word);
            }
        }
        context.write(key, document);
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
    }
}
