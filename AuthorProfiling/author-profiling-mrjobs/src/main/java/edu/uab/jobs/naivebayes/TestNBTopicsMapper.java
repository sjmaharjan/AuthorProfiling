package edu.uab.jobs.naivebayes;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import edu.uab.jobs.vectorize.AgeGroupLabel;
import edu.uab.jobs.vectorize.GenderAndAgeGroupLabel;
import edu.uab.jobs.vectorize.GenderLabel;
import edu.uab.jobs.vectorize.LabelExtractor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.*;
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

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 12/18/13
 * Time: 9:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestNBTopicsMapper extends Mapper<Text, Text, Text, VectorWritable> {

    private MultinomialNaiveBayesClassifier classifier;
    private Analyzer analyzer;
    private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();
    private int dimension;
    private LabelExtractor labelExtractor;

    private HashMap<String, Set<String>> wordToTopics = new HashMap<String, Set<String>>();

    //read dictionary for word index
    //read in model file to initialize classification model
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        org.apache.hadoop.conf.Configuration conf = context.getConfiguration();
        URI[] localFiles = DistributedCache.getCacheFiles(conf);

        Path topicFile = new Path(localFiles[2].getPath());
        FileSystem fs = topicFile.getFileSystem(context.getConfiguration());

        //read dictionary file word:id
        Path dictionaryFile = new Path(localFiles[1].getPath());



        String line;
        String[] tokens;
        BufferedReader in = new BufferedReader(new InputStreamReader(fs.open(topicFile)));
        // word:set<Topics>
        try {
            while ((line = in.readLine()) != null) {
                tokens = line.split(",");
                for (String word : Arrays.copyOfRange(tokens, 1, tokens.length)) {
                    if (wordToTopics.get(word) == null) {
                        TreeSet<String> set = new TreeSet<String>();
                        set.add(tokens[0]);
                        wordToTopics.put(word, set);
                    } else {
                        Set<String> set = wordToTopics.get(word);
                        set.add(tokens[0]);
                        wordToTopics.put(word, set);
                    }
                }

            }
        } finally {
            in.close();
        }


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
        analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
        //word count
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

        //vector creation
        final Vector instance = new RandomAccessSparseVector(this.dimension, termFrequency.size());
        termFrequency.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                if (wordToTopics.containsKey(first)) {
                    for (String topic : wordToTopics.get(first)) {
                        int labelIndex = dictionary.get(topic);
                        instance.setQuick(labelIndex, instance.get(labelIndex)+second);
                    }
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    instance.setQuick(unSeenWordIndex, instance.get(unSeenWordIndex)+second);
                }
                return true;
            }
        });

        //classification
        Vector result = classifier.classifyFull(instance);
        //the key is the expected value
        String actualLabel = labelExtractor.extractLabel(key.toString());
        context.write(new Text(actualLabel), new VectorWritable(result));

    }


}
