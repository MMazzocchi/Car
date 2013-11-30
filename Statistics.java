import java.io.PrintWriter;

public class Statistics {
    
    private int cars;
    private int peds;
    private double duration;
    private Welford pedStats;
    private double pedWaitMin;
    private double pedWaitMax;
    private Welford carStats;
    private double carWaitMin;
    private double carWaitMax;
    
    public Statistics() {
        cars = 0;
        peds = 0;
        duration = 0.0;
        pedStats = new Welford(20);
        carStats = new Welford(20);
    }
    
    public void addCar() {
        cars++;
    }
    
    public void addPed() {
        peds++;
    }
    
    public void setDuration(double time) {
        duration = time;
    }
    
    public void addPedWaitTime(double time) {
        if(pedStats.getSize() == 0) {
            pedWaitMin = time;
            pedWaitMax = time;
        } else {
            if(time < pedWaitMin) pedWaitMin = time;
            if(time > pedWaitMax) pedWaitMax = time;
        }
        pedStats.addDataPoint(time);
    }
    
    public void addCarWaitTime(double time) {
        if(carStats.getSize() == 0) {
            carWaitMin = time;
            carWaitMax = time;
        } else {
            if(time < carWaitMin) carWaitMin = time;
            if(time > carWaitMax) carWaitMax = time;
        }
        carStats.addDataPoint(time);
    }
    
    private void printFile() {
        try {
            PrintWriter pw = new PrintWriter("acwait.dat", "UTF-8");
            for(int lag=1; lag<=20; lag++) {
                pw.println(pedStats.getSampleAutoCorrelation(lag)+" "+carStats.getSampleAutoCorrelation(lag));
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void printStats() {
        System.out.println("OUTPUT "+peds);
        System.out.println("OUTPUT "+cars);
        System.out.println("OUTPUT "+duration);
        System.out.println("OUTPUT "+pedWaitMin+" "+pedStats.getSampleMean()+" "+Math.sqrt(pedStats.getVariance())+" "+pedWaitMax);
        System.out.println("OUTPUT "+carWaitMin+" "+carStats.getSampleMean()+" "+Math.sqrt(carStats.getVariance())+" "+carWaitMax);
        
        printFile();
    }
}
