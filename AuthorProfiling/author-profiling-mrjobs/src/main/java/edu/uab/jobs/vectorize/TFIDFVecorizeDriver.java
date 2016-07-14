package edu.uab.jobs.vectorize;

import edu.uab.jobs.utils.AuthorProfileHelper;
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
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 12/21/13
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class TFIDFVecorizeDriver extends Configured implements Tool {
    private static final Logger log = LoggerFactory.getLogger(TFIDFVecorizeDriver.class);

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.printf(
                    "Usuage : %s [generic options ] <tokens input> <vector output> <dictionary file> <idf> <number of documents>\n ",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;

        }
        //add dictionary to cache

//        getConf().setLong("mapred.tasktracker.expiry.interval",180000)    ;
//        long milliSeconds = 1000*60*60;
//        getConf().setLong( "mapred.task.timeout" ,milliSeconds);

        Path dictionaryFile = new Path(args[2]);
        DistributedCache.addCacheFile(dictionaryFile.toUri(), getConf());

        //add idf file to cache
        Path idfFilePath = new Path(args[3]);
        DistributedCache.addCacheFile(idfFilePath.toUri(), getConf());
//        FileSystem fs = idfFilePath.getFileSystem(getConf());
//
//        FileStatus idfFilePathStatus = fs.getFileStatus(idfFilePath);
//
//        if (idfFilePathStatus.isDir()) {
//            for (FileStatus f : fs.listStatus(idfFilePath)) {
//                if (f.getPath().getName().startsWith("part")) {
//                    DistributedCache.addCacheFile(f.getPath().toUri(), getConf());
//                }
//            }
//        }
        //pass the number of documents to mapper using configuration object
        getConf().setInt("NUMBER_OF_DOCS", Integer.parseInt(args[4]));

        Job job = new Job(getConf());
        job.setJobName("TF IDF vectorization ");
        job.setJarByClass(TFIDFVecorizeDriver.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(VectorWritable.class);
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(TFIDFVectorizeMapper.class);
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

        log.info("Running TF IDF Jobs");
        int exitCode = ToolRunner.run(new TFIDFVecorizeDriver(), args);
        System.exit(exitCode);

    }
}
