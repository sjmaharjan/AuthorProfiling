package edu.uab.jobs.features;

import edu.uab.jobs.naivebayes.TestMultinomialNaiveBayesDriver;
import edu.uab.jobs.utils.AuthorProfileHelper;
import edu.uab.jobs.writables.FeaturesLinkedHashMap;

import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/14/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 * <p/>
 * Reads a tab delimited file  word \t count
 * get the threshold form the configuration and removes least frequent ones
 */
public class FilterLeastFrequentMapper extends Mapper<Text, TextTuple, Text, TextTuple> {

    private static final Logger log = LoggerFactory.getLogger(FilterLeastFrequentMapper.class);

    private Set<String> filterWords = new HashSet<String>(); //holds word to be filtered out
    private static float MIN_THRESHOLD = 1;          //min threshold default to 1

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        //get the threshold from configuration and set the threshold count

        MIN_THRESHOLD = context.getConfiguration().getFloat("THRESHOLD", MIN_THRESHOLD);

        URI[] localFiles = DistributedCache.getCacheFiles(context.getConfiguration());

        //read dictionary file word:id
        Path dictionaryFile = new Path(localFiles[0].getPath());
        // key is word value is id

        for (Pair<Writable, IntWritable> record
                : new SequenceFileIterable<Writable, IntWritable>(dictionaryFile, true, context.getConfiguration())) {
            filterWords.add(record.getFirst().toString());

        }
//        if (cacheFiles != null && cacheFiles.length > 0) {
//            String line;
//            String[] tokens;
//            BufferedReader in = new BufferedReader(new FileReader(cacheFiles[0].toString()));
//
//            try {
//                while ((line = in.readLine()) != null) {
//                    tokens = line.split("\t", 2);
//                    if (Float.parseFloat(tokens[1]) >= MIN_THRESHOLD)
//                        filterWords.add(tokens[0]);
//                }
//            } finally {
//                in.close();
//            }
//        }
//
//        if (cacheFiles != null && cacheFiles.length > 0) {
//            Path filesPattern = new Path(cacheFiles[0].toString(), AuthorProfileHelper.OUTPUT_FILES_PATTERN);
//            for (Pair<Text, IntWritable> record
//                    : new SequenceFileDirIterable<Text, IntWritable>(filesPattern, PathType.GLOB, null, null, true, context.getConfiguration())) {
//                Text key = record.getFirst();
//                System.out.println("here======" + key.toString() + record.getSecond().get());
//                if (MIN_THRESHOLD <= record.getSecond().get()) {
//                    filterWords.add(key.toString());
//                }
//            }
//        }
//        Iterator iter = filterWords.iterator();
//        while (iter.hasNext()) {
//        	System.out.println("filterwords11:==={}"+iter.next());
//        }
    }

    @Override
    protected void map(Text key, TextTuple value, Context context) throws IOException, InterruptedException {
        TextTuple document = new TextTuple();
        for (String word : value.getEntries()) {
            if (this.filterWords.contains(word)) {
                document.add(word);
            }
        }
        if (document.length() > 0)
            context.write(key, document);
    }
}
