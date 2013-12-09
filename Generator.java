import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class Generator {

	int s;

	private HashMap<Double, Double> arrivals;
	private ArrayList<Double> arrivalTimes;

	private HashMap<Double, Double> speeds;
	private ArrayList<Double> speedCdf;

	public Generator(String arrival_file, String rates_file, int stream) {

		s = stream;

		arrivalTimes = new ArrayList<Double>();
		arrivals = new HashMap<Double, Double>();
		
		speeds = new HashMap<Double, Double>();
		speedCdf = new ArrayList<Double>();

		try {

			Path path = FileSystems.getDefault().getPath(arrival_file);
			Charset charset = Charset.forName("US-ASCII");
			BufferedReader reader = Files.newBufferedReader(path, charset);

			// Begin parsing file
			String line = reader.readLine();

			while(line != null) {
				String[] data = line.split("[ \t]+", 0);
				double time = Double.parseDouble(data[0]);
				arrivalTimes.add(time);
				arrivals.put(time, Double.parseDouble(data[1]));
				line = reader.readLine();
			}   
			reader.close();

		} catch (Exception x) {
			System.err.format("IOException: %s%n", x);
		}

		speeds = new HashMap<Double, Double>();
		speedCdf = new ArrayList<Double>();

		try {

			Path path = FileSystems.getDefault().getPath(rates_file);
			Charset charset = Charset.forName("US-ASCII");
			BufferedReader reader = Files.newBufferedReader(path, charset);

			// Begin parsing file
			String line = reader.readLine();
			double total = 0.0;
			while(line != null) {
				String[] data = line.split("[ \t]+", 0);
				double percent = Double.parseDouble(data[0]);
				speedCdf.add(total);
				speeds.put(total, Double.parseDouble(data[0]));
				total += percent;
				line = reader.readLine();
			}   
			reader.close();

		} catch (Exception x) {
			System.err.format("IOException: %s%n", x);
		}

	}

	public double nextArrivalTime(double currentTime) {
		for(double time : arrivalTimes) {
			if(currentTime <= time) {
				return Crosswalk.random.Exponential(s, arrivals.get(time));
			}
		}
		return arrivals.get(arrivalTimes.get(arrivalTimes.size()-1));
	}

	public double getRate() {
		double u = Crosswalk.random.Uniform(s+1);
		for(double p : speedCdf) {
			if(u >= p) {
				return speeds.get(p);
			}
		}
		return -1;
	}

}
