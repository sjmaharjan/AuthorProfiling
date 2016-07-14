/**
 * 
 */
package edu.uab.jobs.mahout;

import java.io.IOException;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

import edu.uab.jobs.writables.TextTuple;

public final class NewsgroupHelper {
	private static final Version LUCENE_VERSION = Version.LUCENE_36;

	public static final int FEATURES = 10000;

	private final Random rand = RandomUtils.getRandom();
	private final Analyzer analyzer = new StandardAnalyzer(LUCENE_VERSION);
	private final FeatureVectorEncoder encoder = new StaticWordValueEncoder(
			"body");
	private final FeatureVectorEncoder bias = new ConstantValueEncoder(
			"Intercept");

	public FeatureVectorEncoder getEncoder() {
		return encoder;
	}

	public FeatureVectorEncoder getBias() {
		return bias;
	}

	public Random getRandom() {
		return rand;
	}

	/**
	 * @param file
	 * @param actual
	 * @param overallCounts
	 * @return
	 * @throws IOException
	 */
	public Vector encodeFeatureVector(TextTuple word_counts) throws IOException {

		// encode text to vector
		// create sparse vector
		Vector v = new RandomAccessSparseVector(FEATURES);
		bias.addToVector((byte[]) null, 1, v);
		String word = "";
		double count;

		// here we encode log of word count into vector
		for (String word_count : word_counts.getEntries()) {
			String[] tokens = word_count.split(":");
			for (int i = 0; i < tokens.length - 1; i++)
				word += tokens[i] + ":";
			word = word.substring(0, word.length() - 1);
			count = Double.parseDouble(tokens[tokens.length - 1]);
			encoder.addToVector(word, count, v);
		}
		return v;
	}

}
