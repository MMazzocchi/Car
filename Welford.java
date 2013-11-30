/**
 * Description: Welford object used for one-pass calculations of mean, variance, covariance, and j-lagged autocorrelation. 
 * @author Dustin Liang, Dylan Chau
 * 
 * Version: 1.0 
 */
public class Welford {
    private int maxlag;
    private double x_bar, var;
    private int n;
    private double xi;
    private double[] W;
    private double[] X;
    
    /**
     * Used for Welford objects with a maxlag > 0
     * @param maxlag = maxlag of the dataset
     */
    public Welford(int maxlag) {
        this.maxlag = maxlag;
        x_bar = 0;
        var = 0;
        n = 0;
        xi = 0;
        W = new double[maxlag+1];
        X = new double[maxlag+1];
    }
    
    /**
     * Used for Welford objects that do not have a maxlag (maxlag of 0)
     */
    public Welford() {
        this.maxlag = 0;
        x_bar = 0;
        var = 0;
        n = 0;
    }
    
    /**
     * add a new data point to the sample.
     * @param x : new data point
     */
    public void addDataPoint(double x) {
        n++;
        xi = x;
        updateMeanVariance(x);
        if (maxlag > 0) {
            updateLists(x);
        }
    }
    
    private void updateMeanVariance(double x) {    
        var = var +  ((x - x_bar) * (x-x_bar)) * ((double)(n - 1) / n);
        x_bar = x_bar + ((1.0 / n) * (x - x_bar));
    }
    
    private void updateLists(double x) {
        if (n == 1) {
            X[1] = x;
        } else if (n > 1 && n <= maxlag) {
            for (int j = 1; j <= n-1; j++) {
                W[j] = W[j] + ((double)(n - 1) / n) * (x - x_bar) * (X[n-j] - x_bar);
            }    
            X[n] = x;
            
        } else {
            for (int j = 1; j <= maxlag; j++) {
                W[j] = W[j] + ((double)(n - 1) / n) * (x - x_bar) * (X[((n - j) % maxlag)+1] - x_bar);
            }
            X[(n % maxlag)+1] = x;
        }
    }
    
    /**
     * get the current sample correlation given a lag <= maxlag
     * @param lag = lag value < maxlag
     * @return Correlation (double) between -1 and 1
     */
    public double getSampleAutoCorrelation(int lag) {
        if (lag > 0 && lag <= maxlag) {
            return W[lag] / var;
        }
        System.out.println("Invalid lag value entered.");
        System.exit(-1);
        return -2;
    }
    
    /**
     * Get the current covariance with the current datapoints. 
     * @param lag = lag value < maxlag
     * @return covariance (double)
     */
    public double getCovariance(int lag) {
        if (lag > 0 && lag <= maxlag) {
            return W[lag];
        }
        System.out.println("Invalid lag value entered.");
        System.exit(-1);
        return -2;
    }
    
    /**
     * return the sample mean of the current data points
     * @return mean (double)
     */
    public double getSampleMean() {
        return x_bar;
    }
    
    /**
     * return the variance of the current data points 
     * @return variance (double)
     */
    public double getVariance() {
        return var / n;
    }

    /**
     * Prints the mean and variance    
     */
    public void print() {
        System.out.println("x-bar: " + x_bar + ", var: " + (var/n));
    }
    
    /**
     * get most recent data point entered. 
     * @return x (double)
     */
    public double getX() {
        return xi;
    }
    
    /**
     * Get the current total number of data points. 
     * @return
     */
    public int getSize() {
        return n;
    }
}
