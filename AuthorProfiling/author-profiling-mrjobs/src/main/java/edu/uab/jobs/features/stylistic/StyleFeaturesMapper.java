package edu.uab.jobs.features.stylistic;


import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.mahout.math.VectorWritable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by suraj on 3/28/14.
 */
public class StyleFeaturesMapper extends Mapper<Text, Text, IntWritable, VectorWritable> {

    private Map<String, Integer> dictionary= new HashMap<String,Integer>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {





    }

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {




    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
    }
}
