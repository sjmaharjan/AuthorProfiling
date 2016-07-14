package edu.uab.jobs.naivebayes;

import edu.uab.jobs.utils.AuthorProfileHelper;
import edu.uab.jobs.vectorize.TFVectorPerLabelMapper;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.common.mapreduce.VectorSumReducer;
import org.apache.mahout.math.VectorWritable;

import java.io.IOException;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 12/18/13
 * Time: 10:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrainNBDriver extends Configured implements Tool {

    public static final String MODEL = "nb_models";
    public static final String ALPHA_I = "alphaI";
    public static final String MODEL_TYPE = "model";


    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 5) {
            System.err.printf(
                    "Usuage : %s [generic options ] <base input> <output> <model> <model type[age| gender| agegender|pan]> <vector>\n ",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;

        }

        //paths for input and vector output
        Path base = new Path(args[0]);
        Path vectors = new Path(base, AuthorProfileHelper.VECTORS);
        Path dictionaryFile = new Path(base, AuthorProfileHelper.VOCABULARY_FILE);

        getConf().set(TrainMultinomialNaiveBayesDriver.MODEL_TYPE, args[3]);

        //add the file with filter words to distributed cache
        DistributedCache.setCacheFiles(new URI[]{dictionaryFile.toUri()}, getConf());

        Job job = new Job(getConf());
        job.setJobName("Train multinomial naive bayes Topics ");
        job.setJarByClass(TrainNBDriver.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(VectorWritable.class);

        //input is the tokens in tokens directory
        FileInputFormat.setInputPaths(job, new Path(vectors,args[4]));
        //vecotors in
        FileOutputFormat.setOutputPath(job, new Path(vectors, args[1]));

        job.setMapperClass(TrainNBayesMapper.class);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setCombinerClass(VectorSumReducer.class);
        job.setReducerClass(VectorSumReducer.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setNumReduceTasks(8);

        boolean succeeded = job.waitForCompletion(true);
        if (!succeeded)
            throw new IllegalStateException("Job failed!");


        return 1;
    }

    public static void main(String args[]) throws Exception {
        Path base = new Path(args[0]);
        Path vector = new Path(base, AuthorProfileHelper.VECTORS);
        Path categoryVector = new Path(vector, args[1]);
        Path modelPath = new Path(base, TrainMultinomialNaiveBayesDriver.MODEL);

        TrainNBDriver train = new TrainNBDriver();
        int exitCode = ToolRunner.run(train, args);
        //create model
        MultinomialNaiveBayesModel model = train.readModel(base, categoryVector);
        train.getConf().setFloat(TrainMultinomialNaiveBayesDriver.ALPHA_I, 1.0f);
        //serialize the model
        model.serialize(new Path(modelPath, args[2]), train.getConf());
       System.exit(exitCode);

    }

    public MultinomialNaiveBayesModel readModel(Path basePath, Path categoryVector) throws IOException {
        String modelType = getConf().get(TrainMultinomialNaiveBayesDriver.MODEL_TYPE);
        int categoryCount = 0;
        if (modelType.equalsIgnoreCase("age")) {
            categoryCount = 3;
        } else if (modelType.equalsIgnoreCase("gender")) {
            categoryCount = 2;
        } else if(modelType.equalsIgnoreCase("agegender")) {
            categoryCount = 6;
        }else{
            categoryCount=10;
        }

        return NaiveBayesUtils.readModel(basePath, categoryVector, getConf(), categoryCount);

    }
}
