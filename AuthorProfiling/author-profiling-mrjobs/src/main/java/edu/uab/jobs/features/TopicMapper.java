package edu.uab.jobs.features;

import edu.uab.jobs.writables.FeaturesLinkedHashMap;

import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
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
public class TopicMapper extends Mapper<Text, TextTuple, Text, TextTuple> {


    private HashMap<String, String[]> topicWords = new HashMap<String, String[]>(); //holds word to be filtered out
    private static float MIN_THRESHOLD = 1;          //min threshold default to 1

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        //get the threshold from configuration and set the threshold count

        URI[] cacheFiles = DistributedCache.getCacheFiles(context.getConfiguration());

        if (cacheFiles != null && cacheFiles.length > 0) {
            String line;
            String[] tokens;
            BufferedReader in = new BufferedReader(new FileReader(cacheFiles[0].toString()));

            try {
                while ((line = in.readLine()) != null) {
                    tokens = line.split(",");
                    topicWords.put(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
                }
            } finally {
                in.close();
            }
        }
    }

    @Override
    protected void map(Text key, TextTuple value, Context context) throws IOException, InterruptedException {
//        TextTuple document = new TextTuple();
//        for (String word : value.getEntries()) {
//            for (String topic : topicWords.keySet()) {
//            	List<String> wordsInTopic = Arrays.asList(topicWords.get(topic));
//            	if(wordsInTopic.contains(word))
//            		document.add(topic);
//            }
//        }
//        context.write(key, document);


//        TextTuple document = new TextTuple();
        for (String topic : topicWords.keySet()) {
            List<String> wordsInTopic = Arrays.asList(topicWords.get(topic));
            int countOfWordsIntopic = 0;
            for (String word : value.getEntries()) {
                if(wordsInTopic.contains(word)) countOfWordsIntopic++;
            }
            //topic: countOfWordsIntopic
        }
//        context.write(key, document);
    }
}
