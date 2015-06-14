package combinerHadoop;

import java.io.IOException;
import java.util.Scanner;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class Authors {

	public static class AuthorsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text author = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
				Scanner line = new Scanner(value.toString());
				line.useDelimiter("\t");
				author.set(line.next());
				context.write(author, one);
		}
	}

	public static class CountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
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
}