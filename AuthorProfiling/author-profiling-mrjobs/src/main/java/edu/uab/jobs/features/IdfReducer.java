package edu.uab.jobs.features;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class IdfReducer extends Reducer<Text, IntWritable, Text, IntWritable>{

	@Override
	protected void reduce(Text key, Iterable<IntWritable> values,
			Context context) throws IOException, InterruptedException {
		System.out.print("key: ");
		System.out.println(key);
		// variable to store the result
		int sum = 0;
		for (IntWritable value : values) {
			sum += value.get();
		}
		// write word and its frequency in to context object
		context.write(key, new IntWritable(sum));
	}
	
}
