package edu.uab.jobs.vectorize;

import edu.uab.jobs.naivebayes.TrainMultinomialNaiveBayesDriver;
import edu.uab.jobs.utils.AuthorProfileHelper;
import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.function.ObjectLongProcedure;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.apache.mahout.math.map.OpenObjectLongHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 12/21/13
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class TFIDFVectorizeMapper extends Mapper<Text, TextTuple, Text, VectorWritable> {
    private static final Logger log = LoggerFactory.getLogger(TFIDFVectorizeMapper.class);

    public enum Counter {SKIPPED_INSTANCES}

    ;
    private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();
    private final OpenObjectIntHashMap<String> idf = new OpenObjectIntHashMap<String>();
    private int numberOfDocuments;
    private int dimension = 0;


    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        numberOfDocuments = conf.getInt("NUMBER_OF_DOCS", 1);
        URI[] distributedCacheFiles = DistributedCache.getCacheFiles(conf);

        //read dictionary file and populate dictionary map
        //read dictionary file word:id
        Path dictionaryFile = new Path(distributedCacheFiles[0].getPath());
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
        boolean first = true;
        for (URI idfFile : distributedCacheFiles) {
            if (first) {
                first = false;
            } else {
                log.info("Reading idf file {}", idfFile.toString());
                for (Pair<Text, IntWritable> record
                        : new SequenceFileIterable<Text, IntWritable>(new Path(idfFile.getPath()), true, conf)) {
                    if (dictionary.containsKey(record.getFirst().toString()))
                        idf.put(record.getFirst().toString(), record.getSecond().get());
                    context.progress();
                }
            }
        }
//        //verify if the dictionary has been loaded
//        dictionary.forEachPair(new ObjectIntProcedure<String>() {
//
//            @Override
//            public boolean apply(String first, int second) {
//
//                log.info("dictionary key : {} value:{}",first,second);
//                return true;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });
//        //verify if the idf has been loaded
//        idf.forEachPair(new ObjectIntProcedure<String>() {
//            @Override
//            public boolean apply(String first, int second) {
//                log.info("idf key : {} value:{}",first,second);
//                return true;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });

    }


    @Override
    protected void map(Text key, TextTuple value, final Context context) throws IOException {


        OpenObjectIntHashMap<String> wordCount = new OpenObjectIntHashMap<String>();
        //word count
        for (String word : value.getEntries()) {
            if (wordCount.containsKey(word)) {
                wordCount.put(word, wordCount.get(word) + 1);
            } else {
                wordCount.put(word, 1);
            }
        }
        //find max term frequency
        int[] counts = wordCount.values().elements();
        log.info("The word count array size {}",counts.length);
        final int maxTermFrequency = AuthorProfileHelper.maximum(counts);

        final Vector vector = new RandomAccessSparseVector(dimension, wordCount.values().size());

        wordCount.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                vector.setQuick(dictionary.get(first), (((double)second) / maxTermFrequency) * getIDFScore(first));
                return true;
            }
        });

        try {
            context.write(key, new VectorWritable(vector));
        } catch (InterruptedException e) {
            context.getCounter(Counter.SKIPPED_INSTANCES).increment(1);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public double getIDFScore(String word) {
        int idfScore = idf.containsKey(word) ? this.idf.get(word) : 0;
        return Math.log(((double) numberOfDocuments) / (1 + idfScore));
    }


}
