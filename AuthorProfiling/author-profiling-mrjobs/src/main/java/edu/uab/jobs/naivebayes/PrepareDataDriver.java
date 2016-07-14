package edu.uab.jobs.naivebayes;

import org.apache.hadoop.conf.Configured;
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
 * Created with IntelliJ IDEA. User: suraj Date: 11/7/13 Time: 3:34 PM To change
 * this template use File | Settings | File Templates.
 */
public class PrepareDataDriver extends Configured implements Tool {
	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.printf(
					"Usuage : %s [generic options ] <input> <output>\n ",
					getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		Job job = new Job(getConf());
		job.setJobName("Prepare data for mahout naive bayes classification: ");
		job.setJarByClass(PrepareDataDriver.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setMapperClass(PrepareDataMapper.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setNumReduceTasks(0);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		boolean succeeded = job.waitForCompletion(true);
		if (!succeeded)
			throw new IllegalStateException("Job failed!");
		return 1;
	}

	public static void main(String args[]) throws Exception {
		int exitCode = ToolRunner.run(new PrepareDataDriver(), args);
		System.exit(exitCode);
	}
}
