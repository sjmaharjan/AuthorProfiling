package edu.uab.jobs.naivebayes;

import edu.uab.jobs.vectorize.AgeGroupLabel;
import edu.uab.jobs.vectorize.GenderAndAgeGroupLabel;
import edu.uab.jobs.vectorize.GenderLabel;
import edu.uab.jobs.vectorize.LabelExtractor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.classifier.ClassifierResult;
import org.apache.mahout.classifier.ResultAnalyzer;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: prasha
 * Date: 1/24/14
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class StopwordTestNBTFIDFDriver extends Configured implements Tool {
    private static final Logger log = LoggerFactory.getLogger(StopwordTestNBTFIDFDriver.class);
    public static final String MODEL_TYPE = "model";


    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 7) {
            System.err.printf(
                    "Usuage : %s [generic options ] <input> <output> <model> <dictionary> <model type [age| gender| agegender]> <idf> <number of docuemnts> \n ",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;

        }
        //paths for input and vector output
        Path test = new Path(args[0]);
        Path output = new Path(args[1]);
        Path model = new Path(args[2]);
        Path dictionaryFile = new Path(args[3]);
        DistributedCache.addCacheFile(model.toUri(), getConf());
        DistributedCache.addCacheFile(dictionaryFile.toUri(), getConf());

        //add idf file to cache
        Path idfFilePath = new Path(args[5]);
        FileSystem fs = idfFilePath.getFileSystem(getConf());

        FileStatus idfFilePathStatus = fs.getFileStatus(idfFilePath);

        if (idfFilePathStatus.isDir()) {
            for (FileStatus f : fs.listStatus(idfFilePath)) {
                if (f.getPath().getName().startsWith("part")) {
                    DistributedCache.addCacheFile(f.getPath().toUri(), getConf());
                }
            }
        }
        //pass the number of documents to mapper using configuration object
        getConf().setInt("NUMBER_OF_DOCS", Integer.parseInt(args[6]));


        getConf().set(TestMultinomialNaiveBayesDriver.MODEL_TYPE, args[4]);




        Job job = new Job(getConf());
        job.setJobName("Test NB TFIDF   ");
        job.setJarByClass(StopwordTestNBTFIDFDriver.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(VectorWritable.class);


        FileInputFormat.setInputPaths(job, test);
        FileOutputFormat.setOutputPath(job, output);

        job.setMapperClass(StopwordTestNBTFIDFMapper.class);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setNumReduceTasks(0);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        boolean succeeded = job.waitForCompletion(true);
        if (!succeeded)
            throw new IllegalStateException("Job failed!");


        return 1;
    }

    public static void main(String args[]) throws Exception {
        StopwordTestNBTFIDFDriver testRunner = new StopwordTestNBTFIDFDriver();
        ToolRunner.run(new Configuration(), testRunner, args);
        LabelExtractor labelExtractor;
        if (args[4].equalsIgnoreCase("age")) {
            labelExtractor = new AgeGroupLabel();
        } else if (args[4].equalsIgnoreCase("gender")) {
            labelExtractor = new GenderLabel();
        } else {
            labelExtractor = new GenderAndAgeGroupLabel();
        }

        Map<Integer, String> labelMap = labelExtractor.getSwappedKeyValueMap();

        //loop over the results and create the confusion matrix
        SequenceFileDirIterable<Text, VectorWritable> dirIterable =
                new SequenceFileDirIterable<Text, VectorWritable>(new Path(args[1]),
                        PathType.LIST,
                        PathFilters.partFilter(),
                        testRunner.getConf());
        ResultAnalyzer analyzer = new ResultAnalyzer(labelMap.values(), "DEFAULT");
        analyzeResults(labelMap, dirIterable, analyzer);
        log.info("Result {}", analyzer);

    }

    private static void analyzeResults(Map<Integer, String> labelMap,
                                       SequenceFileDirIterable<Text, VectorWritable> dirIterable,
                                       ResultAnalyzer analyzer) {
        for (Pair<Text, VectorWritable> pair : dirIterable) {
            int bestIdx = Integer.MIN_VALUE;
            double bestScore = Long.MIN_VALUE;
            for (Vector.Element element : pair.getSecond().get()) {
                if (element.get() > bestScore) {
                    bestScore = element.get();
                    bestIdx = element.index();
                }
            }
            if (bestIdx != Integer.MIN_VALUE) {
                ClassifierResult classifierResult = new ClassifierResult(labelMap.get(bestIdx), bestScore);
                analyzer.addInstance(pair.getFirst().toString(), classifierResult);
            }
        }
    }
}
