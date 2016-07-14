package edu.uab.jobs.liblinear;

import edu.uab.jobs.vectorize.GenderAndAgeGroupLabel;
import edu.uab.jobs.vectorize.LabelExtractor;
import edu.uab.jobs.vectorize.Pan14Label;
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

import java.io.*;
import java.util.*;


/**
 * Created by suraj on 3/29/14.
 */
public class PrepareLibLinearData {

    public static void loadAndWriteVetors(String filePattern, String outputFile) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(outputFile), false)));
        Configuration conf = new Configuration();
        // FileSystem fs = FileSystem.get(conf);
        for (Pair<Text, VectorWritable> record
                : new SequenceFileDirIterable<Text, VectorWritable>(new Path(filePattern), PathType.GLOB, null, null, true,
                conf)) {
            String label = record.getFirst().toString();
            Vector feature = record.getSecond().get();
//            out.write(String.valueOf(PrepareLibLinearData.getLabelIndex(label)));
            out.write(label);
            out.write(" ");
            out.write(PrepareLibLinearData.vectorToLibLinearString(feature));
            out.flush();

        }
        out.close();

    }


    public static int getLabelIndex(String label) {

        LabelExtractor le = new Pan14Label();
        String classificationClass = le.extractLabel(label);
        return le.getPredefinedLabels().get(classificationClass);

    }

    public static String vectorToLibLinearString(Vector vector) {
        Iterator<Vector.Element> iter = vector.iterateNonZero();
        List<TermIndexWeight> vectorTerms = new ArrayList<TermIndexWeight>();
        while (iter.hasNext()) {
            Vector.Element e = iter.next();
            vectorTerms.add(new TermIndexWeight(e.index(), e.get()));
        }

        // Sort results in ascending order by index
        Collections.sort(vectorTerms, new Comparator<TermIndexWeight>() {
            @Override
            public int compare(TermIndexWeight one, TermIndexWeight two) {
                return Double.compare(one.index, two.index);
            }
        });

        StringBuilder featureString = new StringBuilder();
        boolean first = true;
        for (TermIndexWeight elt : vectorTerms) {
            if (first) {
                first = false;
            } else {
                featureString.append(' ');
            }

            featureString.append(String.valueOf(elt.index + 1)).append(":").append(String.valueOf((float) elt.weight));
        }
        featureString.append('\n');
        return featureString.toString();
    }


    //ref mahout cluster code
    private static class TermIndexWeight {
        private final int index;
        private final double weight;

        TermIndexWeight(int index, double weight) {
            this.index = index;
            this.weight = weight;
        }

        public int getIndex() {
            return index;
        }

        public double getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return index + ":" + weight;
        }
    }


    public static void main(String[] args) throws IOException {
        String inputDir = args[0];
        String outputFile = args[1];
        PrepareLibLinearData.loadAndWriteVetors(inputDir, outputFile);
    }

}
