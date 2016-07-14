package edu.uab.jobs.naivebayes;

import edu.uab.jobs.vectorize.*;
import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.function.ObjectLongProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.apache.mahout.math.map.OpenObjectLongHashMap;

import java.io.IOException;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 12/18/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 * Ref Mahout Naive Bayes's
 */
public class TrainNBayesMapper extends Mapper<Text, VectorWritable, IntWritable, VectorWritable> {

    public enum Counter {SKIPPED_INSTANCES}

    private final OpenObjectIntHashMap<String> dictionary = new OpenObjectIntHashMap<String>();
    private Vector priorCountPerLabel;
    private int numberOfLabels;
    private OpenObjectIntHashMap<String> labelIndex;
    private int dimension = 0;
    private LabelExtractor label;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        URI[] localFiles = DistributedCache.getCacheFiles(conf);

        //read dictionary file word:id
        Path dictionaryFile = new Path(localFiles[0].getPath());

        // key is word value is id
        int countFeatures = 0;
        for (Pair<Text, IntWritable> record
                : new SequenceFileIterable<Text, IntWritable>(dictionaryFile, true, conf)) {
            dictionary.put(record.getFirst().toString(), record.getSecond().get());
            countFeatures++;
        }
        this.dimension = countFeatures;

        //label extract  either send the class using context or do if else
        String modelType = conf.get(TrainMultinomialNaiveBayesDriver.MODEL_TYPE);
        if (modelType.equalsIgnoreCase("age")) {
            this.label = new AgeGroupLabel();
        } else if (modelType.equalsIgnoreCase("gender")) {
            this.label = new GenderLabel();
        } else if(modelType.equalsIgnoreCase("agegender")){
            this.label = new GenderAndAgeGroupLabel();
        }else{
            this.label=new Pan14Label();
        }
        //set the labelIndex
        this.labelIndex = this.label.getPredefinedLabels();
        this.numberOfLabels = this.labelIndex.size();
        priorCountPerLabel = new RandomAccessSparseVector(this.numberOfLabels);


    }


    @Override
    protected void map(Text key, VectorWritable value, final Context context) throws IOException {
        //get the index for the label/class category
        String cls = this.label.extractLabel(key.toString());
        if (labelIndex.containsKey(cls)) {
            try {
                int labelIdx = labelIndex.get(cls);
                context.write(new IntWritable(labelIdx), value);
                priorCountPerLabel.set(labelIdx, 1 + priorCountPerLabel.get(labelIdx));
            } catch (InterruptedException e) {
                context.getCounter(Counter.SKIPPED_INSTANCES).increment(1);
                e.printStackTrace();
            }
        } else {
            context.getCounter(Counter.SKIPPED_INSTANCES).increment(1);
        }

    }


    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        //special key -1 for counting number of documents that have the same labels
        context.write(new IntWritable(-1),
                new VectorWritable(priorCountPerLabel));
        super.cleanup(context);
    }

}
