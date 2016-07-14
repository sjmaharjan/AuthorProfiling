/**
 * 
 */
package edu.uab.jobs.mahout;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.ep.State;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.Dictionary;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;

import edu.uab.jobs.writables.TextTuple;

public class Train20NewsGroup {

	public Train20NewsGroup() {
	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		// training folder : base location
		
		// collection to store the objects and their count
		Multiset<String> overallCounts = HashMultiset.create();
		// hold target categories' numeric representation
		Dictionary newsGroups = new Dictionary();

		NewsgroupHelper helper = new NewsgroupHelper();
		helper.getEncoder().setProbes(2);

//		OnlineLogisticRegression learningAlgorithm1 = new OnlineLogisticRegression(
//				20, NewsgroupHelper.FEATURES, new L1()).alpha(1)
//				.stepOffset(1000).decayExponent(0.9).lambda(3.0e-5)
//				.learningRate(20);
		
		 AdaptiveLogisticRegression learningAlgorithm = new AdaptiveLogisticRegression(20, NewsgroupHelper.FEATURES, new L1());
		   

		//load all the training files 
//		List<File> files = Lists.newArrayList();
//		for (File newsgroup : base.listFiles()) {
//			if (newsgroup.isDirectory()) {
//				// target category's numeric representation is stored in newsGroupDictionary
//				newsGroups.intern(newsgroup.getName());
//				files.addAll(Arrays.asList(newsgroup.listFiles()));
//			}
//		}
		 
		newsGroups.intern("10s_male"); 
		newsGroups.intern("20s_male"); 
		newsGroups.intern("30s_male"); 
		newsGroups.intern("10s_female"); 
		newsGroups.intern("20s_female"); 
		newsGroups.intern("30s_female"); 
		
		
		String inputFile = args[0];
		Configuration conf = new Configuration();
		Path path = new Path(inputFile);
		FileSystem fs = FileSystem.get(path.toUri(), conf);
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
		Text key=(Text)reader.getKeyClass().newInstance();
		TextTuple value= (TextTuple) reader.getValueClass().newInstance();
		//feature encoding
		//and then training 
		while (reader.next(key, value)) {
			//actual target category
//			if(key.toString().equals("/77c08e273727551c3d11225337e20a76_en_30s_male.txt")) {
//				System.out.println("\nskipped: " + key);
//				continue;
//			}
			System.out.println(key);
			String[] rext = key.toString().split("\\.");
			String kwext = rext[0];
			String[] parts = kwext.split("_");
			String age = parts[2]; 
			String gender = parts[3]; // 004
			int actual = newsGroups.intern(age + "_" + gender);
			//encode the features into vectors
			Vector v = helper.encodeFeatureVector(value);
			//train the model
			learningAlgorithm.train(actual, v);

		}
		 
//		File base = new File(args[0]);
//		if(base.isDirectory())
//			System.out.println("base:        " + base.getPath());
//		System.out.println("args           " +args[0]);
//		for (File f : base.listFiles()) {
//			if(f.isDirectory())
//				continue;
//			String inputFile = f.getPath();
//			Configuration conf = new Configuration();
//			Path path = new Path(inputFile);
//			FileSystem fs = FileSystem.get(path.toUri(), conf);
//			SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
//			Text key=(Text)reader.getKeyClass().newInstance();
//			TextTuple value= (TextTuple) reader.getValueClass().newInstance();
//			//feature encoding
//			//and then training 
//			while (reader.next(key, value)) {
//				//actual target category
//				System.out.println("key:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n" + key);
//				String[] rext = key.toString().split("\\.");
//				System.out.println(rext);
//				String kwext = rext[0];
//				String[] parts = kwext.split("_");
//				String age = parts[2]; 
//				String gender = parts[3]; // 004
//				int actual = newsGroups.intern(age + "_" + gender);
//				//encode the features into vectors
//				Vector v = helper.encodeFeatureVector(value);
//				//train the model
//				learningAlgorithm.train(actual, v);
//	
//			}
//		}
		//close the training process
		learningAlgorithm.close();

		System.out.println("exiting main");

		//write the learned model to the file
		ModelSerializer.writeBinary("/tmp/news-group.model", learningAlgorithm
				.getBest().getPayload().getLearner().getModels().get(0));

//		List<Integer> counts = Lists.newArrayList();
//		System.out.printf("Word counts\n");
//		for (String count : overallCounts.elementSet()) {
//			counts.add(overallCounts.count(count));
//		}
//		Collections.sort(counts, Ordering.natural().reverse());
//		int k = 0;
//		for (Integer count : counts) {
//			System.out.printf("%d\t%d\n", k, count);
//			k++;
//			if (k > 1000) {
//				break;
//			}
//		}
	}
}
