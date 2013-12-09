
public class Random {
    
    static Lehmer lehmer;
    
    public Random(long seed) {
        lehmer = new Lehmer();
        lehmer.plantSeeds(seed);
    }
    
    public double Uniform(int stream) {
        lehmer.selectStream(stream);
        return lehmer.random();
    }
    
    public double Exponential(int stream) {
        double exp = -Math.log(Uniform(stream))/Metrics.LAMBDA;
        return exp;
    }
    
    public double Exponential(int stream, double lambda) {
        double exp = -Math.log(Uniform(stream))/lambda;
        return exp;
    }
        
    public boolean Bernoulli(double p, int stream) {
        return Uniform(stream) <= p;
    }

}
