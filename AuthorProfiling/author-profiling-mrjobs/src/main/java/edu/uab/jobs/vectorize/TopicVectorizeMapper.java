package edu.uab.jobs.vectorize;

import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
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
import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 12/18/13
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicVectorizeMapper extends Mapper<Text, TextTuple, Text, VectorWritable> {

    private static final Logger log = LoggerFactory.getLogger(TopicVectorizeMapper.class);
    private HashMap<String, Set<String>> wordToTopics = new HashMap<String, Set<String>>();//topic hash map
    private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();   //topic word : integer id

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        //TODO may write the number of features in context
        Configuration conf = context.getConfiguration();

        URI[] localFiles = DistributedCache.getCacheFiles(conf);


        //read dictionary file word:id
        Path dictionaryFile = new Path(localFiles[0].getPath());

        Path topicFile= new Path(localFiles[1].getPath());
        FileSystem fs = topicFile.getFileSystem(conf);
        log.info("Reading cached topic file {}",topicFile);

        String line;
        String[] tokens;
        BufferedReader in = new BufferedReader(new InputStreamReader(fs.open(topicFile)));

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


        log.info("Reading dictionary file {}",dictionaryFile.toString());

        for (Pair<Text, IntWritable> record
                : new SequenceFileIterable<Text, IntWritable>(dictionaryFile, true, conf)) {
            dictionary.put(record.getFirst().toString(), record.getSecond().get());
        }

        //print dictionary
        Iterator<Map.Entry<String,Set<String>>> iter = wordToTopics.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Set<String>> entry = iter.next();
            log.info("key {} Value{}",entry.getKey(),entry.getValue());
            }




        //print dictionary:
        dictionary.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                log.info("key: {} value:{}",first,second);
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });


    }

    @Override
    protected void map(Text key, TextTuple value, Context context) throws IOException, InterruptedException {
        Vector topicVector = new RandomAccessSparseVector(dictionary.size()); //for unseen words

       for(String word: value.getEntries()){
                if (wordToTopics.containsKey(word)) {
                    for (String topic : wordToTopics.get(word)) {
                        int labelIndex = dictionary.get(topic);
                        topicVector.setQuick(labelIndex, topicVector.get(labelIndex)+1);
                    }
                }

            }
        context.write(key, new VectorWritable(topicVector));
    }
}
