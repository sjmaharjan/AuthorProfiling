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
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
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


public class PrepareTFIDFTestVectorMapper extends Mapper<Text, Text, IntWritable, VectorWritable> {
    private static final Logger log = LoggerFactory.getLogger(PrepareTFIDFTestVectorMapper.class);

    private Analyzer analyzer;
    private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();
    private final OpenObjectIntHashMap<String> idf = new OpenObjectIntHashMap<String>();
    private int numberOfDocuments;
    private int dimension;
    private LabelExtractor labelExtractor;
    ShingleAnalyzerWrapper shingleAnalyzer;


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
        int skip = 0;
        for (URI idfFile : distributedCacheFiles) {
            if (skip > 1) {
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
        this.labelExtractor = new GenderAndAgeGroupLabel();


    }

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        OpenObjectIntHashMap<String> termFrequency = new OpenObjectIntHashMap<String>();
        analyzer = new AuthorProfilingAnalyzer(Version.LUCENE_CURRENT);
        shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);
        TokenStream stream = shingleAnalyzer.tokenStream(key.toString(),
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

        int[] counts = termFrequency.values().elements();

        final int maxTermFrequency = counts.length > 0 ? AuthorProfileHelper.maximum(counts) : 0;

        //vector creation
        final Vector instance = new RandomAccessSparseVector(this.dimension, termFrequency.size());
        termFrequency.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                if (dictionary.containsKey(first)) {
                    int labelIndex = dictionary.get(first);
                    instance.setQuick(labelIndex, (((double) second) / maxTermFrequency) * getIDFScore(first));
                } else {
                    int unSeenWordIndex = dictionary.get("UNSEEN_WORD");
                    instance.setQuick(unSeenWordIndex, instance.get(unSeenWordIndex) + (((double) second) / maxTermFrequency) * getIDFScore(first));
                }
                return true;
            }
        });
        String actualLabel = labelExtractor.extractLabel(key.toString());
        context.write(new IntWritable(labelExtractor.getPredefinedLabels().get(actualLabel)), new VectorWritable(instance));
    }


    public double getIDFScore(String word) {
        int idfScore = idf.containsKey(word) ? this.idf.get(word) : 0;
        return Math.log(((double) numberOfDocuments) / (1 + idfScore));
    }

}

