package edu.uab.jobs.utils;

import com.google.common.io.Closeables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.io.Writable;
import org.apache.mahout.common.Pair;

import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import sun.misc.IOUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 11/19/13
 * Time: 7:45 PM
 * To change this template use File | Settings | File Templates.
 */
public final class AuthorProfileHelper {
    //declare constants

    public static final String VOCABULARY_FILE = "dictionary.file";
    public static final String IDF_FILE = "idf.file";
    public static final String FEATURE_COUNT_FILE = "feature_count.file";
    public static final String VECTORS = "vectors";
    public static final String OUTPUT_FILES_PATTERN = "part-*";
    public static final String TOKENS = "tokens";

    /**
     * read the idf count file
     * creates the word to int value index vocabulary file
     * create a feature count file
     *
     * @param idfCountPath
     * @param dictionaryPathBase
     * @param baseConf
     * @param threshold
     * @throws IOException
     */
    public static void createVocabularyFile(Path idfCountPath,
                                            Path dictionaryPathBase,
                                            Configuration baseConf,
                                            float threshold) throws IOException {
        int vocabularyCount = 0;
        Configuration conf = new Configuration(baseConf);
        FileSystem fs = FileSystem.get(idfCountPath.toUri(), conf);
        Path vocabularyFile = new Path(dictionaryPathBase, VOCABULARY_FILE);
        SequenceFile.Writer vocabularyWriter = new SequenceFile.Writer(fs, conf, vocabularyFile, Text.class, IntWritable.class);
        try {
            int i = 0;
            Path filesPattern = new Path(idfCountPath, OUTPUT_FILES_PATTERN);
            for (Pair<Text, IntWritable> record
                    : new SequenceFileDirIterable<Text, IntWritable>(filesPattern, PathType.GLOB, null, null, true, conf)) {
                Text key = record.getFirst();
                if (threshold <= record.getSecond().get()) {
                    vocabularyWriter.append(key, new IntWritable(i++));
                }
            }
            vocabularyWriter.append(new Text("UNSEEN_WORD"), new IntWritable(i++));
            vocabularyCount = i;
        } finally {
            Closeables.closeQuietly(vocabularyWriter);
        }

//        //create a vocabulary count file
//        Path featureFile = new Path(dictionaryPathBase, FEATURE_COUNT_FILE);
//        SequenceFile.Writer featureCountWriter = null;
//        try {
//            featureCountWriter = SequenceFile.createWriter(fs, conf, featureFile, Text.class, IntWritable.class);
//            featureCountWriter.append(new Text("FEATURES_COUNT"), new IntWritable(vocabularyCount));
//
//        } finally {
//            Closeables.closeQuietly(featureCountWriter);
//        }

        createFeatureCountFile(dictionaryPathBase, vocabularyCount);
    }


    public static void createVocabularyAndIDFFile(Path idfCountPath,
                                            Path dictionaryPathBase,
                                            Configuration baseConf,
                                            float threshold) throws IOException {
        int vocabularyCount = 0;
        Configuration conf = new Configuration(baseConf);
        FileSystem fs = FileSystem.get(idfCountPath.toUri(), conf);
        Path vocabularyFile = new Path(dictionaryPathBase, VOCABULARY_FILE);
        Path idf=new Path(dictionaryPathBase, IDF_FILE);
        SequenceFile.Writer idfFile=new SequenceFile.Writer(fs, conf, idf, Text.class, IntWritable.class);

        SequenceFile.Writer vocabularyWriter = new SequenceFile.Writer(fs, conf, vocabularyFile, Text.class, IntWritable.class);
        try {
            int i = 0;
            Path filesPattern = new Path(idfCountPath, OUTPUT_FILES_PATTERN);
            for (Pair<Text, IntWritable> record
                    : new SequenceFileDirIterable<Text, IntWritable>(filesPattern, PathType.GLOB, null, null, true, conf)) {
                Text key = record.getFirst();
                if (threshold <= record.getSecond().get()) {
                    vocabularyWriter.append(key, new IntWritable(i++));
                    idfFile.append(key,record.getSecond());
                }
            }
            vocabularyWriter.append(new Text("UNSEEN_WORD"), new IntWritable(i++));
            vocabularyCount = i;
        } finally {
            Closeables.closeQuietly(vocabularyWriter);
            Closeables.closeQuietly(idfFile);

        }

//        //create a vocabulary count file
//        Path featureFile = new Path(dictionaryPathBase, FEATURE_COUNT_FILE);
//        SequenceFile.Writer featureCountWriter = null;
//        try {
//            featureCountWriter = SequenceFile.createWriter(fs, conf, featureFile, Text.class, IntWritable.class);
//            featureCountWriter.append(new Text("FEATURES_COUNT"), new IntWritable(vocabularyCount));
//
//        } finally {
//            Closeables.closeQuietly(featureCountWriter);
//        }

        createFeatureCountFile(dictionaryPathBase, vocabularyCount);
    }



    public static void createFeatureCountFile(Path dictionaryPathBase, int vocabularyCount) throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);

        //create a vocabulary count file
        Path featureFile = new Path(dictionaryPathBase, FEATURE_COUNT_FILE);
        SequenceFile.Writer featureCountWriter = null;
        try {
            featureCountWriter = SequenceFile.createWriter(fs, conf, featureFile, Text.class, IntWritable.class);
            featureCountWriter.append(new Text("FEATURES_COUNT"), new IntWritable(vocabularyCount));

        } finally {
            Closeables.closeQuietly(featureCountWriter);
        }
    }


    public static void createVocabularyFileForTopic(String topicFilePath,
                                                    Path dictionaryPathBase ) throws IOException {
        int vocabularyCount = 0;
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path vocabularyFile = new Path(dictionaryPathBase, VOCABULARY_FILE);
        SequenceFile.Writer vocabularyWriter = new SequenceFile.Writer(fs, conf, vocabularyFile, Text.class, IntWritable.class);



        int i = 0;
        String line;
        String[] tokens;
        //read topic file from local filesystem
        BufferedReader in = new BufferedReader(new InputStreamReader(fs.open(new Path(topicFilePath))));
        try {
            while ((line = in.readLine()) != null) {
                tokens = line.split(",");
                vocabularyWriter.append(new Text(tokens[0]), new IntWritable(i++));
            }
            vocabularyWriter.append(new Text("UNSEEN_WORD"), new IntWritable(i++));
            vocabularyCount = i;
        } finally {
            in.close();
            Closeables.closeQuietly(vocabularyWriter);
        }
        createFeatureCountFile(dictionaryPathBase, vocabularyCount);
    }


    public static long maximum(long [] myArray){
        long max=myArray[0];
        for(int i = 0; i < myArray.length; i++) {
            if(myArray[i] > max) {
                max = myArray[i];
            }
        }
        return max;
    }

    public static int maximum(int [] myArray){
        int max=myArray[0];
        for(int i = 0; i < myArray.length; i++) {
            if(myArray[i] > max) {
                max = myArray[i];
            }
        }
        return max;
    }

}
