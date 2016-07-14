package edu.uab.jobs.naivebayes;


import com.google.common.io.Closeables;
import edu.uab.jobs.utils.AuthorProfileHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;

import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

import java.io.IOException;


/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 11/19/13
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public final class NaiveBayesUtils {


    public static MultinomialNaiveBayesModel readModel(Path base, Path categoryVector, Configuration conf, int categoryCount) throws IOException {
        float alphaI = conf.getFloat(TrainMultinomialNaiveBayesDriver.ALPHA_I, 1.0f);
        int numberOfCategory = categoryCount;
        FileSystem fs = FileSystem.get(base.toUri(), conf);
        int featureCount = 0;
        //read number of feature file
        SequenceFile.Reader featureReader = null;
        try {
            featureReader = new SequenceFile.Reader(fs, new Path(base, AuthorProfileHelper.FEATURE_COUNT_FILE), conf);
            Text key = new Text();
            IntWritable value = new IntWritable();
            while (featureReader.next(key, value)) {
                featureCount = value.get();
            }

        } finally {
            Closeables.closeQuietly(featureReader);
        }


        Vector countsPerCategory = null;

        Matrix scoresPerLabelAndFeature = new SparseMatrix(numberOfCategory, featureCount);
        Path filesPattern = new Path(categoryVector, AuthorProfileHelper.OUTPUT_FILES_PATTERN);
        for (Pair<IntWritable, VectorWritable> record : new SequenceFileDirIterable<IntWritable, VectorWritable>(filesPattern, PathType.GLOB, null, null, true, conf)) {
            int key = record.getFirst().get();
            VectorWritable value = record.getSecond();
            if (key == -1) {
                countsPerCategory = value.get();
            } else {
                scoresPerLabelAndFeature.assignRow(key, value.get());
            }
        }
        MultinomialNaiveBayesModel model = new MultinomialNaiveBayesModel(countsPerCategory, scoresPerLabelAndFeature, alphaI, featureCount);
        return model;

    }
}
