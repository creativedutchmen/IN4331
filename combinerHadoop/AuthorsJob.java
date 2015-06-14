package combinerHadoop;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import combinerHadoop.Authors;

public class AuthorsJob {
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println("Usage: AuthorsJob <in> <out>");
			System.exit(2);
		}

		Configuration conf = new Configuration();

		Job job = new Job(conf, "Authors count");
		job.setMapperClass(Authors.AuthorsMapper.class);
		job.setReducerClass(Authors.CountReducer.class);

		// Because the job is a simple count() of the records,
		// it is completely safe to do the operation on each
		// of the worker nodes, and combining the results afterwards.
		// This also means that the Reducer itself is suitable as a reducer.
		job.setCombinerClass(Authors.CountReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}