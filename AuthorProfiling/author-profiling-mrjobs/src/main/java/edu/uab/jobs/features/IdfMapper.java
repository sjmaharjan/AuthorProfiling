/**
 *
 */
package edu.uab.jobs.features;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sjmaharjan
 */
public class IdfMapper extends Mapper<Text, TextTuple, Text, IntWritable> {

    private Analyzer analyzer;

    @Override
    protected void setup(Context context) throws IOException,
            InterruptedException {
        super.setup(context);
        analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
    }

    @Override
    protected void map(Text key, TextTuple value, Context context)
            throws IOException, InterruptedException {
        // list of unique strings in file
        List<String> unqWords = new ArrayList<String>();

        for (String word : value.getEntries()) {
            if (!hasString(unqWords, word)) {
                context.write(new Text(word), new IntWritable(1));
                unqWords.add(word);
            }
        }
    }



    protected boolean hasString(List<String> unqWords, String word) {
        for (String str : unqWords) {
            if (str.trim().toLowerCase().equals(word.toLowerCase()))
                return true;
        }
        return false;
    }

}

// /**
// *
// */
// package edu.uab.jobs.features.tf;
//
// import java.io.IOException;
// import java.io.StringReader;
// import java.util.Map;
//
//
// import org.apache.hadoop.io.IntWritable;
// import org.apache.hadoop.io.MapWritable;
// import org.apache.hadoop.io.Text;
// import org.apache.hadoop.mapreduce.Mapper;
// import org.apache.hadoop.mapreduce.Mapper.Context;
// import org.apache.lucene.analysis.Analyzer;
// import org.apache.lucene.analysis.TokenStream;
// import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
// import org.apache.lucene.util.Version;
//
// import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
// import edu.uab.jobs.writables.TextTuple;
//
// /**
// * @author sjmaharjan
// *
// */
// public class TermFrequencyMapper extends Mapper<Text, Text, Text, TextTuple>
// {
//
// private Analyzer analyzer;
//
// @Override
// protected void setup(Context context) throws IOException,
// InterruptedException {
// super.setup(context);
// analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
// }
//
// @Override
// protected void map(Text key, Text value, Context context)
// throws IOException, InterruptedException {
// // key as file name and value as file content
// MapWritable termFrequency = new MapWritable();
// TextTuple docuemnt= new TextTuple();
// int totalWordCount = 0;
// TokenStream stream = analyzer.tokenStream(key.toString(),
// new StringReader(value.toString()));
// CharTermAttribute termAtt = stream
// .getAttribute(CharTermAttribute.class);
// stream.reset();
// while (stream.incrementToken()) {
// if (termAtt.length() > 0) {
// Text word = new Text(new String(termAtt.buffer(), 0,
// termAtt.length()));
// if (termFrequency.containsKey(word)) {
// IntWritable frequeny = (IntWritable) termFrequency
// .get(word);
// frequeny.set(frequeny.get() + 1);
//
// } else {
// termFrequency.put(word, new IntWritable(1));
// }
// totalWordCount++;
// }
// }
//
// context.write(key, docuemnt);
// }
// }
