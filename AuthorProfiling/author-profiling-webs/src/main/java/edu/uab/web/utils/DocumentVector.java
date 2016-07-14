/**
 *
 */
package edu.uab.web.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import edu.uab.jobs.utils.AuthorProfileHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;


/**
 * @author sjmaharjan
 */
public final class DocumentVector {

    public static final int NO_OF_DOCS = 236600;
    public static final int ES_NO_OF_DOCS = 75900;

    public static Map<String, Integer> makedictionary(String dictionaryFile)
            throws NumberFormatException, IOException {
        Map<String, Integer> dictionary = new HashMap<String, Integer>();
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.getLocal(conf);

        Path path = new Path(dictionaryFile);
        SequenceFile.Reader reader = null;
        try {
            reader = new SequenceFile.Reader(fs, path, conf);
            Text key = new Text();
            IntWritable value = new IntWritable();
            while (reader.next(key, value)) {
                dictionary.put(key.toString(), value.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(reader);
        }

        return dictionary;
    }


    public static Vector vectorize(String testDocument,
                                   final Map<String, Integer> dictionary) throws IOException {

        OpenObjectIntHashMap<String> termFrequency = DocumentVector
                .wordCounts(testDocument);
        final Vector instance = new RandomAccessSparseVector(dictionary.size(),
                termFrequency.size());
        termFrequency.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                if (dictionary.containsKey(first)) {
                    int labelIndex = dictionary.get(first);
                    instance.setQuick(labelIndex, second);
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    instance.setQuick(unSeenWordIndex, instance.get(unSeenWordIndex) + second);
                }
                return true;
            }
        });
        return instance;

    }


    public static Vector tfIdfNGramVectorize(String document, final Map<String, Integer> dictionary, final Map<String, Integer> idf, final String language) throws IOException {
        OpenObjectIntHashMap<String> termFrequency = new OpenObjectIntHashMap<String>();
        Analyzer analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
        ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);
        TokenStream stream = shingleAnalyzer.tokenStream("tokens",
                new StringReader(document));
        CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                String word = new String(termAtt.buffer(), 0, termAtt.length());
                if (termFrequency.containsKey(word)) {
                    int val = termFrequency.get(word);
                    termFrequency.put(word, (val + 1));
                } else termFrequency.put(word, 1);
            }
        }

        int[] counts = termFrequency.values().elements();

        final int maxTermFrequency = counts.length > 0 ? AuthorProfileHelper.maximum(counts) : 0;

        final Vector instance = new RandomAccessSparseVector(dictionary.size());
        termFrequency.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                if (dictionary.containsKey(first)) {
                    int labelIndex = dictionary.get(first);
                    instance.setQuick(labelIndex, (((double) second) / maxTermFrequency) * getIDFScore(first, idf, language));
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    instance.setQuick(unSeenWordIndex, instance.get(unSeenWordIndex) + (((double) second) / maxTermFrequency) * getIDFScore(first, idf, language));
                }
                return true;
            }
        });

        return instance;
    }



    public static Vector NGramVectorize(String document, final Map<String, Integer> dictionary) throws IOException {
        OpenObjectIntHashMap<String> termFrequency = new OpenObjectIntHashMap<String>();
        Analyzer analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
        ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);
        TokenStream stream = shingleAnalyzer.tokenStream("tokens",
                new StringReader(document));
        CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                String word = new String(termAtt.buffer(), 0, termAtt.length());
                if (termFrequency.containsKey(word)) {
                    int val = termFrequency.get(word);
                    termFrequency.put(word, (val + 1));
                } else termFrequency.put(word, 1);
            }
        }


        final Vector instance = new RandomAccessSparseVector(dictionary.size());
        termFrequency.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                if (dictionary.containsKey(first)) {
                    int labelIndex = dictionary.get(first);
                    instance.setQuick(labelIndex, ((double) second) );
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    instance.setQuick(unSeenWordIndex, instance.get(unSeenWordIndex) + ((double) second) );
                }
                return true;
            }
        });

        return instance;
    }

    public static OpenObjectIntHashMap<String> wordCounts(String document)
            throws IOException {
        Analyzer analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_40);
        OpenObjectIntHashMap<String> termFrequency = new OpenObjectIntHashMap<String>();

        TokenStream stream = analyzer.tokenStream("tokens", new StringReader(
                document));
        CharTermAttribute termAtt = stream
                .getAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            if (termAtt.length() > 0) {
                String word = new String(termAtt.buffer(), 0, termAtt.length());
                if (termFrequency.containsKey(word)) {
                    int val = termFrequency.get(word);
                    termFrequency.put(word, (val + 1));
                } else
                    termFrequency.put(word, 1);
            }
        }
        return termFrequency;
    }


    public static double getIDFScore(String word, final Map<String, Integer> idf, String language) {
        int idfScore = idf.containsKey(word) ? idf.get(word) : 0;
        if (language.equalsIgnoreCase("en"))
            return Math.log(((double) DocumentVector.NO_OF_DOCS) / (1 + idfScore));
        else
            return Math.log(((double) DocumentVector.ES_NO_OF_DOCS) / (1 + idfScore));
    }

    //
    // /**
    // * @param args
    // * @throws IOException
    // * @throws NumberFormatException
    // */
    // public static void main(String[] args) throws NumberFormatException,
    // IOException {
    // Map<String, Integer> dictionary = DocumentVector
    // .makedictionary("src/main/resources/model/dictionary.file");
    // System.out.println(dictionary.toString());
    // }

}
