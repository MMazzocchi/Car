
public class Main {
    
    public static void main(String[] args) {
        if(args.length < 8) {
            System.out.println("Usage: SIM M <time (min)> <seed> " +
    	                       "<ped arrivals file> <autos arrival file> " +
            		           "<ped rates file> <autos rates file> <trace file>");
        } else {
            new Crosswalk(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
            		args[3], args[4], args[5], args[6], args[7]);
        }
    }
}
