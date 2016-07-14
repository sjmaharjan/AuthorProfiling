package edu.uab.jobs.naivebayes;

import edu.uab.jobs.tokenizer.AuthorProfilingAnalyzer;
import edu.uab.jobs.utils.AuthorProfileHelper;
import edu.uab.jobs.vectorize.AgeGroupLabel;
import edu.uab.jobs.vectorize.GenderAndAgeGroupLabel;
import edu.uab.jobs.vectorize.GenderLabel;
import edu.uab.jobs.vectorize.LabelExtractor;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: prasha
 * Date: 1/24/14
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class StopwordTestNBTFIDFMapper extends Mapper<Text, Text, Text, VectorWritable> {
    private static final Logger log = LoggerFactory.getLogger(StopwordTestNBTFIDFMapper.class);
    private MultinomialNaiveBayesClassifier classifier;
    private Analyzer analyzer;
    private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();
    private final OpenObjectIntHashMap<String> idf = new OpenObjectIntHashMap<String>();
    private int numberOfDocuments;
    private int dimension;
    private LabelExtractor labelExtractor;
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


    //read dictionary for word index
    //read in model file to initialize classification model
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        org.apache.hadoop.conf.Configuration conf = context.getConfiguration();
        numberOfDocuments = conf.getInt("NUMBER_OF_DOCS", 1);
        URI[] distributedCacheFiles = DistributedCache.getCacheFiles(conf);

        //read dictionary file and populate dictionary map
        //read dictionary file word:id
        Path dictionaryFile = new Path(distributedCacheFiles[1].getPath());
        // key is word value is id
        int countFeatures = 0;
        for (Pair<Text, IntWritable> record
                : new SequenceFileIterable<Text, IntWritable>(dictionaryFile, true, conf)) {
            dictionary.put(record.getFirst().toString(), record.getSecond().get());
            context.progress();
            countFeatures++;
        }
        this.dimension = countFeatures;
        //read idf files and populate the idf map
        int skip=0;
        for (URI idfFile : distributedCacheFiles) {
            if(skip>1){
                log.info("Reading idf file {}", idfFile.toString());
                for (Pair<Text, IntWritable> record
                        : new SequenceFileIterable<Text, IntWritable>(new Path(idfFile.getPath()), true, conf)) {
                    if (dictionary.containsKey(record.getFirst().toString()))
                        idf.put(record.getFirst().toString(), record.getSecond().get());
                    context.progress();
                }
            }
            skip++;
        }


        //model path
        Path modelPath = new Path(distributedCacheFiles[0].getPath());
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
                if(ENGLISH_STOP_WORDS_SET.contains(word)){
                    if (termFrequency.containsKey(word)) {
                        int val = termFrequency.get(word);
                        termFrequency.put(word, (val + 1));
                    } else termFrequency.put(word, 1);
                }
            }
        }

        int[] counts= termFrequency.values().elements();

        final int maxTermFrequency= counts.length>0? AuthorProfileHelper.maximum(counts):0;

        //vector creation
        final Vector instance = new RandomAccessSparseVector(this.dimension, termFrequency.size());
        termFrequency.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                if (dictionary.containsKey(first)) {
                    int labelIndex = dictionary.get(first);
                    instance.setQuick(labelIndex, (((double)second)/maxTermFrequency) * getIDFScore(first));
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    instance.setQuick(unSeenWordIndex, instance.get(unSeenWordIndex)+(((double)second)/maxTermFrequency) * getIDFScore(first));
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


    public double getIDFScore(String word) {
        int idfScore = idf.containsKey(word) ? this.idf.get(word) : 0;
        return Math.log(((double) numberOfDocuments) / (1 + idfScore));
    }

}
