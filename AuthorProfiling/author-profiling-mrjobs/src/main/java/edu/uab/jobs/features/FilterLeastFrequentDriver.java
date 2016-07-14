package edu.uab.jobs.features;

import edu.uab.jobs.utils.AuthorProfileHelper;
import edu.uab.jobs.writables.FeaturesLinkedHashMap;
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

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/14/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterLeastFrequentDriver extends Configured implements Tool {


    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println(args.length+ " "+ args[0] + " "+args[1] +" "+args[2]);

            System.err.printf(
                    "Usuage : %s [generic options ] <input> <output> <filter_filename> <threshold>\n ",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;

        }
        //add the file with filter words to distributed cache
        Path tempPath=new Path(args[2]).getParent()   ;
        Path dictionaryPath=new Path(tempPath,AuthorProfileHelper.VOCABULARY_FILE)  ;

        DistributedCache.setCacheFiles(new URI[]{dictionaryPath.toUri()}, getConf());
       // DistributedCache.addCacheFile(new Path(args[2]).toUri(), getConf());
        //set threshold
        getConf().setFloat("THRESHOLD",  null != args[3]? Float.parseFloat(args[3]) : 1.0f);

        Job job = new Job(getConf());
        job.setJobName("Filter Least Frequent words ");
        job.setJarByClass(FilterLeastFrequentDriver.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(TextTuple.class);
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(FilterLeastFrequentMapper.class);
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

        FilterLeastFrequentDriver fl=new FilterLeastFrequentDriver();
        float threshold=  null != args[3]? Float.parseFloat(args[3]) : 1.0f;
        AuthorProfileHelper.createVocabularyAndIDFFile(new Path(args[2]),new Path(args[2]).getParent(),new Configuration(),threshold);
        int exitCode = ToolRunner.run(fl, args);


        System.exit(exitCode);

    }


}
