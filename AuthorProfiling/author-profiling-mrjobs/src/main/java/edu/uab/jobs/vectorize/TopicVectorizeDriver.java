package edu.uab.jobs.vectorize;

import edu.uab.jobs.features.TopicMapper;
import edu.uab.jobs.utils.AuthorProfileHelper;
import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 12/18/13
 * Time: 5:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicVectorizeDriver extends Configured implements Tool {
    private static final Logger log = LoggerFactory.getLogger(TopicVectorizeDriver.class);

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println(args.length+ " "+ args[0] + " "+args[1] +" "+args[2]);

            System.err.printf(
                    "Usuage : %s [generic options ] <tokens input> <vector output> <topic file path>\n ",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;

        }
        Path dictionaryFile = new Path(new Path(args[1]).getParent().getParent(), AuthorProfileHelper.VOCABULARY_FILE);
        DistributedCache.setCacheFiles(new URI[]{dictionaryFile.toUri(),new Path(args[2]).toUri()}, getConf());


        Job job = new Job(getConf());
        job.setJobName("Topic  words ");
        job.setJarByClass(TopicVectorizeDriver.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(VectorWritable.class);
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(TopicVectorizeMapper.class);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setNumReduceTasks(0);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        boolean succeeded = job.waitForCompletion(true);
        if (!succeeded)
            throw new IllegalStateException("Job failed!");
        return 1;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        log.info("Creating dictinary and number of features file for given topics");
        AuthorProfileHelper.createVocabularyFileForTopic(args[2],new Path(args[1]).getParent().getParent());
        log.info("Job topic mapper start");
        TopicVectorizeDriver topic=new TopicVectorizeDriver();
        int exitCode = ToolRunner.run(topic, args);


        System.exit(exitCode);

    }
}
