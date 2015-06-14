package tfidf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class tfidfJob {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: tfidfJob <in> <out>");
			System.exit(2);
		}

		Configuration conf = new Configuration();

		Job job = new Job(conf, "Word Frequency");
		job.setMapperClass(tfidfJob.WordFrequencyMapper.class);
		job.setReducerClass(tfidfJob.WordFrequencyReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path("/tmp/tmp001"));

		job.waitForCompletion(true);

		Job job2 = new Job(conf, "Word Count");

		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);

		job2.setMapperClass(tfidfJob.WordCountMapper.class);
		job2.setReducerClass(tfidfJob.WordCountReducer.class);
		FileInputFormat.addInputPath(job2, new Path("/tmp/tmp001"));
		FileOutputFormat.setOutputPath(job2, new Path(args[1]));
		job2.waitForCompletion(true);
	}

	public static class WordFrequencyMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String fileName = ((FileSplit)context.getInputSplit()).getPath().getName();

			Pattern p = Pattern.compile("\\w+");
        	Matcher m = p.matcher(value.toString());

        	while (m.find()) {
        		String input = m.group().toLowerCase();
        		if (!Character.isLetter(input.charAt(0)) || Character.isDigit(input.charAt(0))) {
        			continue;
        		}
        		word.set(input + "@" + fileName);
				context.write(word, one);
        	}
		}
	}

	public static class WordFrequencyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			int count = 0;
			for (IntWritable val: values) {
				count += val.get();
			}
			result.set(count);
			context.write(key, result);
		}
	}

	public static class WordCountMapper extends Mapper<LongWritable, Text, Text, Text> {

		private final static IntWritable one = new IntWritable(1);
		private Text filename = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] wordDocCounter = value.toString().split("\t");
			String[] wordDoc = wordDocCounter[0].split("@");
			context.write(new Text(wordDoc[1]), new Text(wordDoc[0] + "@" + wordDocCounter[1]));
		}
	}

	public static class WordCountReducer extends Reducer<Text, Text, Text, Text> {
		private IntWritable result = new IntWritable();
		private Text word = new Text();
		Map<String, Integer> counter = new HashMap<String, Integer>();
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			int count = 0;
			for (Text val: values) {
				String[] wordCount = val.toString().split("@");
				int c = Integer.valueOf(wordCount[1]);
				counter.put(wordCount[0], c);
				count += c;
			}

			for (String word : counter.keySet()) {
				context.write(new Text(word + "@" + key.toString()), new Text(counter.get(word) + "@" + count));
			}			
		}
	}
}