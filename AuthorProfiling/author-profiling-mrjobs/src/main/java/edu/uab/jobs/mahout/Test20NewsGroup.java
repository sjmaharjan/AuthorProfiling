package edu.uab.jobs.mahout;

//public class Test20NewsGroup {
//}

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.classifier.ClassifierResult;

import org.apache.mahout.classifier.ResultAnalyzer;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.Dictionary;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import edu.uab.jobs.writables.TextTuple;

public class Test20NewsGroup {
	private String inputFile;
	private String modelFile;

	private Test20NewsGroup() {
	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		Test20NewsGroup runner = new Test20NewsGroup();
		if (runner.parseArgs(args)) {
			runner.run(new PrintWriter(System.out, true));
		}
	}

	public void run(PrintWriter output) throws IOException, InstantiationException, IllegalAccessException {

		// contains the best model
		OnlineLogisticRegression classifier = ModelSerializer.readBinary(
				new FileInputStream(modelFile), OnlineLogisticRegression.class);

		Dictionary newsGroups = new Dictionary();
		Multiset<String> overallCounts = HashMultiset.create();

//		List<File> files = Lists.newArrayList();
//		for (File newsgroup : base.listFiles()) {
//			if (newsgroup.isDirectory()) {
//				newsGroups.intern(newsgroup.getName());
//				files.addAll(Arrays.asList(newsgroup.listFiles()));
//			}
//		}
//		System.out.printf("%d test files\n", files.size());
		
		 
		newsGroups.intern("10s_male"); 
		newsGroups.intern("20s_male"); 
		newsGroups.intern("30s_male"); 
		newsGroups.intern("10s_female"); 
		newsGroups.intern("20s_female"); 
		newsGroups.intern("30s_female"); 
//		newsGroups.intern("dadada"); 
		
		ResultAnalyzer ra = new ResultAnalyzer(newsGroups.values(), "DEFAULT");

		Configuration conf = new Configuration();
		Path path = new Path(inputFile);		
		FileSystem fs = FileSystem.get(path.toUri(), conf);
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
		Text key=(Text)reader.getKeyClass().newInstance();
		TextTuple value= (TextTuple) reader.getValueClass().newInstance();
		while (reader.next(key, value)) {
			//actual target category
			String[] rext = key.toString().split("\\.");
			String kwext = rext[0];
			String[] parts = kwext.toString().split("_");
			String age = parts[2]; 
			String gender = parts[3]; // 004
			int actual = newsGroups.intern(age + "_" + gender);
			//encode the features into vectors
			NewsgroupHelper helper = new NewsgroupHelper();
			Vector v = helper.encodeFeatureVector(value);
			Vector result = classifier.classifyFull(v);

			int cat = result.maxValueIndex();
			double score = result.maxValue();
			double ll = classifier.logLikelihood(actual, v);
			ClassifierResult cr = new ClassifierResult(newsGroups.values().get(
					cat), score, ll);
			ra.addInstance(newsGroups.values().get(actual), cr);
		}
//		for (File file : files) {
//			String ng = file.getParentFile().getName();
//
//			int actual = newsGroups.intern(ng);
//			NewsgroupHelper helper = new NewsgroupHelper();
//			Vector input = helper.encodeFeatureVector(); // no leak type ensures this is a normal
//									// vector
//			Vector result = classifier.classifyFull(input);
//			int cat = result.maxValueIndex();
//			double score = result.maxValue();
//			double ll = classifier.logLikelihood(actual, input);
//			ClassifierResult cr = new ClassifierResult(newsGroups.values().get(
//					cat), score, ll);
//			ra.addInstance(newsGroups.values().get(actual), cr);
//
//		}
		output.printf("%s\n\n", ra.toString());
	}

	boolean parseArgs(String[] args) {
		DefaultOptionBuilder builder = new DefaultOptionBuilder();

		Option help = builder.withLongName("help")
				.withDescription("print this list").create();

		ArgumentBuilder argumentBuilder = new ArgumentBuilder();
		Option inputFileOption = builder
				.withLongName("input")
				.withRequired(true)
				.withArgument(
						argumentBuilder.withName("input").withMaximum(1)
								.create())
				.withDescription("where to get training data").create();

		Option modelFileOption = builder
				.withLongName("model")
				.withRequired(true)
				.withArgument(
						argumentBuilder.withName("model").withMaximum(1)
								.create())
				.withDescription("where to get a model").create();

		Group normalArgs = new GroupBuilder().withOption(help)
				.withOption(inputFileOption).withOption(modelFileOption)
				.create();

		Parser parser = new Parser();
		parser.setHelpOption(help);
		parser.setHelpTrigger("--help");
		parser.setGroup(normalArgs);
		parser.setHelpFormatter(new HelpFormatter(" ", "", " ", 130));
		CommandLine cmdLine = parser.parseAndHelp(args);

		if (cmdLine == null) {
			return false;
		}

		inputFile = (String) cmdLine.getValue(inputFileOption);
		modelFile = (String) cmdLine.getValue(modelFileOption);
		return true;
	}

}
