/**
 * 
 */
package edu.uab.jobs.features;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author sjmaharjan
 * 
 */
public class BigramFrequencyMapper extends Mapper<Text, Text, Text, TextTuple> {

	private Analyzer analyzer;
	ShingleAnalyzerWrapper shingleAnalyzer;
	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
	}

	@Override
	protected void map(Text key, Text value, Context context)
			throws IOException, InterruptedException {
		// key as file name and value as file content
		HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
		TextTuple document= new TextTuple();
		int totalWordCount = 0;
		shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);
		TokenStream stream = shingleAnalyzer.tokenStream(key.toString(),
				new StringReader(value.toString()));
		CharTermAttribute termAtt = stream
				.getAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			if (termAtt.length() > 0) {
				String word = new String(termAtt.buffer(), 0,termAtt.length());
				if (termFrequency.containsKey(word)) {
					termFrequency.put(word, termFrequency.get(word)+1);

				} else {
					termFrequency.put(word, 1);
				}
				totalWordCount++;
			}
		}
		
		for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
			float prob = (float)entry.getValue()/totalWordCount;
		    document.add(entry.getKey()+":"+ prob);
		}
		context.write(key, document);
	}
}



///**
// * 
// */
//package edu.uab.jobs.features.tf;
//
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.Map;
//
//
//import org.apache.hadoop.io.IntWritable;
//import org.apache.hadoop.io.MapWritable;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapreduce.Mapper;
//import org.apache.hadoop.mapreduce.Mapper.Context;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.apache.lucene.util.Version;
//
//import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
//import edu.uab.jobs.writables.TextTuple;
//
///**
// * @author sjmaharjan
// * 
// */
//public class TermFrequencyMapper extends Mapper<Text, Text, Text, TextTuple> {
//
//	private Analyzer analyzer;
//
//	@Override
//	protected void setup(Context context) throws IOException,
//			InterruptedException {
//		super.setup(context);
//		analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
//	}
//
//	@Override
//	protected void map(Text key, Text value, Context context)
//			throws IOException, InterruptedException {
//		// key as file name and value as file content
//		MapWritable termFrequency = new MapWritable();
//		TextTuple docuemnt= new TextTuple();
//		int totalWordCount = 0;
//		TokenStream stream = analyzer.tokenStream(key.toString(),
//				new StringReader(value.toString()));
//		CharTermAttribute termAtt = stream
//				.getAttribute(CharTermAttribute.class);
//		stream.reset();
//		while (stream.incrementToken()) {
//			if (termAtt.length() > 0) {
//				Text word = new Text(new String(termAtt.buffer(), 0,
//						termAtt.length()));
//				if (termFrequency.containsKey(word)) {
//					IntWritable frequeny = (IntWritable) termFrequency
//							.get(word);
//					frequeny.set(frequeny.get() + 1);
//
//				} else {
//					termFrequency.put(word, new IntWritable(1));
//				}
//				totalWordCount++;
//			}
//		}
//		
//		context.write(key, docuemnt);
//	}
//}
