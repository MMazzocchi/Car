import java.io.PrintWriter;


public class TraceWriter {
	
	private PrintWriter file;
	
	public TraceWriter(String name) {
		openFile(name);
	}
	
	public void openFile(String name) {
        try {
            file = new PrintWriter(name, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void closeFile() {
		file.close();
	}
	
	private void printData(Object[] data) {
		String str = "";
		for(int c=0; c<5; c++) {
			if(c<data.length) {
				str += ""+data[c];
			} else {
				str += "0";
			}
			if(c < 4) {
				str += " ";
			}
		}
		file.println(str);
	}
	
	public void printCarSpawn(double time, int id, double speed, int side) {
		Object[] data = {time, 0, id, speed, side};
		printData(data);
	}
	
	public void printPedSpawn(double time, int id, double speed, int side) {
		Object[] data = {time, 1, id, speed, side};
		printData(data);
	}
	
	public void printCarSpeedChange(double time, int id, double speed) {
		Object[] data = {time, 2, id, speed};
		printData(data);
	}
	
	public void printLightChange(double time, int light) {
		Object[] data = {time, 3, light};
		printData(data);
	}
	
	public void printPedSpeedChange(double time, int id, double speed) {
		Object[] data = {time, 4, id, speed};
		printData(data);
	}
	
	public void printPedStartCross(double time, int id) {
		Object[] data = {time, 5, id};
		printData(data);
	}
	
	public void printCarExit(double time, int id) {
		Object[] data = {time, 6, id};
		printData(data);
	}
	
	public void printPedExit(double time, int id) {
		Object[] data = {time, 7, id};
		printData(data);
	}
	
	public void printEnd(double time) {
		Object[] data = {time, 8};
		printData(data);
	}
}
