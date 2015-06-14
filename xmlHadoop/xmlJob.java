package xmlHadoop;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

import java.io.IOException;
import java.util.Scanner;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class xmlJob {
	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();

		Job actorJob = new Job(conf, "Actor Stats");
		actorJob.setMapperClass(xmlJob.MoviesMapper.class);
		actorJob.setReducerClass(xmlJob.ActorReducer.class);

		actorJob.setOutputKeyClass(org.apache.hadoop.io.Text.class);
		actorJob.setOutputValueClass(org.apache.hadoop.io.Text.class);

		FileInputFormat.addInputPath(actorJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(actorJob, new Path("/title-and-actor.txt"));
		actorJob.waitForCompletion(true);

		Job directorJob = new Job(conf, "Director Stats");
		directorJob.setMapperClass(xmlJob.MoviesMapper.class);
		directorJob.setReducerClass(xmlJob.DirectorReducer.class);

		directorJob.setOutputKeyClass(org.apache.hadoop.io.Text.class);
		directorJob.setOutputValueClass(org.apache.hadoop.io.Text.class);

		FileInputFormat.addInputPath(directorJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(directorJob, new Path("/director-and-title.txt"));
		directorJob.waitForCompletion(true);
	}

	public static class MoviesMapper extends Mapper<LongWritable, org.apache.hadoop.io.Text, org.apache.hadoop.io.Text, org.apache.hadoop.io.Text> {
		private final static IntWritable one = new IntWritable(1);
		private org.apache.hadoop.io.Text author = new org.apache.hadoop.io.Text();

		public void map(LongWritable key, org.apache.hadoop.io.Text value, Context context) throws IOException, InterruptedException {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				Path path = new Path(value.toString());
				Document document = db.parse(new File(path.toString())); 

				Element root = document.getDocumentElement();
				NodeList movies = root.getElementsByTagName("movie");

				for (int i = 0; i < movies.getLength(); i++) {
					Element movie = (Element)movies.item(i);
					NodeList titleNode = movie.getElementsByTagName("title");
					String title = titleNode.item(0).getFirstChild().getNodeValue();

					Element director = (Element)movie.getElementsByTagName("director").item(0);
					Element directorFirstNameEl = (Element)director.getElementsByTagName("first_name").item(0);
					Element directorLastNameEl = (Element)director.getElementsByTagName("last_name").item(0);
					String directorName = directorFirstNameEl.getFirstChild().getNodeValue() + " " + directorLastNameEl.getFirstChild().getNodeValue();

					Element directorBirthYearEl = (Element)director.getElementsByTagName("birth_date").item(0);
					String directorBirthYear = directorBirthYearEl.getFirstChild().getNodeValue();

					NodeList actorsList = movie.getElementsByTagName("actor");
					for (int j = 0; j < actorsList.getLength(); j++) {
						Element actor = (Element)actorsList.item(j);

						Element firstNameEl = (Element)actor.getElementsByTagName("first_name").item(0);
						Element lastNameEl = (Element)actor.getElementsByTagName("last_name").item(0);
						String actorName = firstNameEl.getFirstChild().getNodeValue() + " " + lastNameEl.getFirstChild().getNodeValue();

						Element actorBirthYearEl = (Element)actor.getElementsByTagName("birth_date").item(0);
						String actorBirthYear = actorBirthYearEl.getFirstChild().getNodeValue();

						Element roleEl = (Element)actor.getElementsByTagName("role").item(0);
						String role = roleEl.getFirstChild().getNodeValue();
						context.write(
							new org.apache.hadoop.io.Text(title),
							new org.apache.hadoop.io.Text(actorName + "\t" + actorBirthYear + "\t" + role + "\t" + directorName + "\t" + directorBirthYear)
						);
					}
				}
			}
			catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}
	}

	public static class ActorReducer extends Reducer<org.apache.hadoop.io.Text, org.apache.hadoop.io.Text, org.apache.hadoop.io.Text, org.apache.hadoop.io.Text> {
		private IntWritable result = new IntWritable();

		public void reduce(org.apache.hadoop.io.Text key, Iterable<org.apache.hadoop.io.Text> values, Context context) throws IOException, InterruptedException {
			for (org.apache.hadoop.io.Text value : values) {
				Scanner line = new Scanner(value.toString());
				line.useDelimiter("\t");
				context.write(key, new org.apache.hadoop.io.Text(line.next() + "\t" + line.next() + "\t" + line.next()));
			}
		}
	}

	public static class DirectorReducer extends Reducer<org.apache.hadoop.io.Text, org.apache.hadoop.io.Text, org.apache.hadoop.io.Text, org.apache.hadoop.io.Text> {
		Scanner line;

		public void reduce(org.apache.hadoop.io.Text key, Iterable<org.apache.hadoop.io.Text> values, Context context) throws IOException, InterruptedException {
			// For some reason the Iterable does not accept the next() method, and taking the first element is not possible.
			// Unfortunately my Java skills are not good enough to find a more elegant method than this...
			for (org.apache.hadoop.io.Text value : values) {
				line = new Scanner(value.toString());
				line.useDelimiter("\t");
				for (int i = 0; i < 3; i++) {
					line.next();
				}
			}
			// Switch the key and values around - the director is the key here, not the movie.
			context.write(new org.apache.hadoop.io.Text(line.next()),  new org.apache.hadoop.io.Text(key.toString() + "\t" + line.next()));
		}
	}
}