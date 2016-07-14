package edu.uab.jobs.naivebayes;

import com.google.common.io.Closeables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.math.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/19/13
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 * Reference Mahout's Naive Bayes
 */
public class MultinomialNaiveBayesModel {
    private final Vector countInstancesPerLabel;
    private final Matrix weightsPerLabelAndFeature;
    private final float alphaI;
    private final int numFeatures;
    private final double totalInstances;

    public MultinomialNaiveBayesModel(Vector countInstancesPerLabel, Matrix weightsPerLabelAndFeature, float alphaI, int numFeatures) {
        this.countInstancesPerLabel = countInstancesPerLabel;
        this.weightsPerLabelAndFeature = weightsPerLabelAndFeature;
        this.alphaI = alphaI;
        this.numFeatures = numFeatures;
        this.totalInstances = this.countInstancesPerLabel.zSum();
    }

    public float getAlphaI() {
        return alphaI;
    }

    public double getNumFeatures() {
        return numFeatures;
    }

    public Matrix getWeightsPerLabelAndFeature(){
        return this.weightsPerLabelAndFeature;
    }

    public double weight(int label, int feature) {
        return weightsPerLabelAndFeature.getQuick(label, feature);
    }

    public int numberOfCategories() {
        return this.countInstancesPerLabel.size();
    }

    public double getTotalCountOfInstances() {
        return this.totalInstances;
    }

    public double getCategoryCount(int label){
        return this.countInstancesPerLabel.get(label);
    }

    public static MultinomialNaiveBayesModel materialize(Path output, Configuration conf) throws IOException {
        FileSystem fs = output.getFileSystem(conf);
        Vector countInstancesPerLabel = null;
        Matrix weightsPerLabelAndFeature;
        float alphaI;
        int vocabularySize;

        FSDataInputStream in = fs.open(new Path(output, "naiveBayesModel.bin"));
        try {
            alphaI = in.readFloat();
            vocabularySize = in.readInt();
            countInstancesPerLabel = VectorWritable.readVector(in);
            weightsPerLabelAndFeature = new SparseRowMatrix(countInstancesPerLabel.size(), vocabularySize);
            for (int label = 0; label < weightsPerLabelAndFeature.numRows(); label++) {
                weightsPerLabelAndFeature.assignRow(label, VectorWritable.readVector(in));
            }
        } finally {
            Closeables.closeQuietly(in);
        }
        MultinomialNaiveBayesModel model = new MultinomialNaiveBayesModel(countInstancesPerLabel, weightsPerLabelAndFeature, alphaI, vocabularySize);
        return model;
    }

    public void serialize(Path output, Configuration conf) throws IOException {
        FileSystem fs = output.getFileSystem(conf);
        FSDataOutputStream out = fs.create(new Path(output, "naiveBayesModel.bin"));
        try {
            out.writeFloat(alphaI); //smoothing parameter
            out.writeInt(numFeatures);   //vocabulary size
            VectorWritable.writeVector(out, countInstancesPerLabel);
            for (int row = 0; row < weightsPerLabelAndFeature.numRows(); row++) {
                VectorWritable.writeVector(out, weightsPerLabelAndFeature.viewRow(row));
            }
        } finally {
            Closeables.closeQuietly(out);
        }
    }

    public Vector createScoringVector() {
        return this.countInstancesPerLabel.like();
    }
}
