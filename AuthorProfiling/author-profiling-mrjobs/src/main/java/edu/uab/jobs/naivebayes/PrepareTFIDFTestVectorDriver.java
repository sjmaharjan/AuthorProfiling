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
 * Created by suraj on 4/3/14.
 */
public class PrepareTFIDFTestVectorDriver extends Configured implements Tool {
    private static final Logger log = LoggerFactory.getLogger(PrepareTFIDFTestVectorDriver.class);


    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 5) {
            System.err.printf(
                    "Usuage : %s [generic options ] <input> <output>  <dictionary> <idf> <number of docuemnts> \n ",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;

        }
        //paths for input and vector output
        Path test = new Path(args[0]);
        Path output = new Path(args[1]);

        Path dictionaryFile = new Path(args[2]);

        DistributedCache.addCacheFile(dictionaryFile.toUri(), getConf());

        //add idf file to cache
        Path idfFilePath = new Path(args[3]);
        DistributedCache.addCacheFile(idfFilePath.toUri(), getConf());

        getConf().setInt("NUMBER_OF_DOCS", Integer.parseInt(args[4]));


        Job job = new Job(getConf());
        job.setJobName("Test TFIDF Vector Creation    ");
        job.setJarByClass(PrepareTFIDFTestVectorDriver.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(VectorWritable.class);


        FileInputFormat.setInputPaths(job, test);
        FileOutputFormat.setOutputPath(job, output);

        job.setMapperClass(PrepareTFIDFTestVectorMapper.class);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setNumReduceTasks(0);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        boolean succeeded = job.waitForCompletion(true);
        if (!succeeded)
            throw new IllegalStateException("Job failed!");


        return 1;
    }

    public static void main(String args[]) throws Exception {
        TestNBTFIDFDriver testRunner = new TestNBTFIDFDriver();
        ToolRunner.run(new Configuration(), testRunner, args);
    }
}

