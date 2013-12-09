import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class PedGenerator {
	
	private HashMap<Double, Double> arrivals;
	
	public PedGenerator(String arrival_file, String rates) {

	        try {

	            Path path = FileSystems.getDefault().getPath(arrival_file);
	            Charset charset = Charset.forName("US-ASCII");
	            BufferedReader reader = Files.newBufferedReader(path, charset);

	            // Begin parsing file
	            String line = reader.readLine();

	            // Parse edges from file and add them to problem
	            while(line != null) {
	                String[] data = line.split("[ \t]+", 0);
	                
	                line = reader.readLine();
	            }   
	            reader.close();

	        } catch (Exception x) {
	            System.err.format("IOException: %s%n", x);
	        }

	}

}
