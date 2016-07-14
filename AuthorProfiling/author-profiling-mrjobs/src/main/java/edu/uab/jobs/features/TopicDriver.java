package edu.uab.jobs.features;

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

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/14/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicDriver extends Configured implements Tool {

    private float threshold;    //threshold
    private Path filterFileName;   // words with their counts


    public TopicDriver(float threshold, Path filterFileName) {
        this.threshold = threshold;
        this.filterFileName = filterFileName;
    }

    public TopicDriver() {
        this.threshold = 1;
        this.filterFileName = new Path("idf_count.txt");
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public Path getFilterFileName() {
        return filterFileName;
    }

    public void setFilterFileName(Path filterFileName) {
        this.filterFileName = filterFileName;
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.printf(
                    "Usuage : %s [generic options ] <input> <output>\n ",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;

        }
        //add the file with filter words to distributed cache
        DistributedCache.addCacheFile(this.getFilterFileName().toUri(), getConf());
        //set threshold
        getConf().setFloat("THRESHOLD", threshold);

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

        int exitCode = ToolRunner.run(new FilterLeastFrequentDriver(), args);
        System.exit(exitCode);

    }


}
