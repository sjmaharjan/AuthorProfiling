package edu.uab.jobs.naivebayes;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import edu.uab.jobs.vectorize.AgeGroupLabel;
import edu.uab.jobs.vectorize.GenderAndAgeGroupLabel;
import edu.uab.jobs.vectorize.GenderLabel;
import edu.uab.jobs.vectorize.LabelExtractor;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 11/19/13
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 * Reference Mahout's naive bayes
 */
public class MultinomialNaiveBayesTestMapper extends Mapper<Text, Text, Text, VectorWritable> {

    private MultinomialNaiveBayesClassifier classifier;
    private Analyzer analyzer;
    private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();
    private int dimension;
    private LabelExtractor labelExtractor;


    //read dictionary for word index
    //read in model file to initialize classification model
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        org.apache.hadoop.conf.Configuration conf = context.getConfiguration();
        URI[] localFiles = DistributedCache.getCacheFiles(conf);


        //read dictionary file word:id
        Path dictionaryFile = new Path(localFiles[1].getPath());
        // key is word value is id
        int countFeatures = 0;
        for (Pair<Writable, IntWritable> record
                : new SequenceFileIterable<Writable, IntWritable>(dictionaryFile, true, conf)) {
            dictionary.put(record.getFirst().toString(), record.getSecond().get());
            countFeatures++;
        }
        this.dimension = countFeatures;

        //model path
        Path modelPath = new Path(localFiles[0].toString());
        MultinomialNaiveBayesModel model = MultinomialNaiveBayesModel.materialize(modelPath, conf);
        classifier = new MultinomialNaiveBayesClassifier(model);
        analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);

        String modelType = conf.get(TestMultinomialNaiveBayesDriver.MODEL_TYPE);
        if (modelType.equalsIgnoreCase("age")) {
            this.labelExtractor = new AgeGroupLabel();
        } else if (modelType.equalsIgnoreCase("gender")) {
            this.labelExtractor = new GenderLabel();
        } else {
            this.labelExtractor = new GenderAndAgeGroupLabel();
        }


    }

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        //tokenize the Text and create term frequency vector using the dictionary
        //classify the vector against the model
        //write result of classification

        OpenObjectIntHashMap<String> termFrequency = new OpenObjectIntHashMap<String>();


        TokenStream stream = analyzer.tokenStream(key.toString(),
                new StringReader(value.toString()));
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

        final Vector instance = new RandomAccessSparseVector(this.dimension, termFrequency.size());
        termFrequency.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                if (dictionary.containsKey(first)) {
                    int labelIndex = dictionary.get(first);
                    instance.setQuick(labelIndex, second);
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    instance.setQuick(unSeenWordIndex, instance.get(unSeenWordIndex)+second);
                }
                return true;
            }
        });

        Vector result = classifier.classifyFull(instance);
        //the key is the expected value
        String actualLabel = labelExtractor.extractLabel(key.toString());
        context.write(new Text(actualLabel), new VectorWritable(result));

    }


}
