
public class Car {
    public enum Origin {LEFT, RIGHT};
    
    private int id;
    
    private double speed; // ft/min
    private double position; // feet
    private Car ahead;
    private Car behind;
    private double waitStart;
    
    public Car(int carId, Origin origin, double t) {
        id = carId;
        //J Change
        // Calculate the speed of this car and it's initial position
        speed = (Crosswalk.random.Uniform(5)*10.0)+25.0;
        if(origin == Origin.LEFT) {
            position = 0;
        } else {
            speed *= -1;
            position = Metrics.STREET_LENGTH;
        }
        
        speed = (speed * 5280)/60.0;
        
        waitStart = -1;
    }
    
    public void reactToLight(Light.LightStatus stat) {
    	
    }
    
    public boolean canMakeLight() {
    	return false;
    }
    
    //Start recording wait time
    public void startWait(double currentTime) {
        waitStart = currentTime;
    }
    
    //Return the wait time of this car; return 0 if none
    public double waitTime(double currentTime) {
        if(waitStart == -1) {
            return 0.0;
        } else {
            return currentTime - waitStart;
        }
    }
    
    //Return an event signifying when this car arrives at the light
    public Event crossArrival(double currentTime) {
        double stopPoint;
        if(speed < 0) {
            stopPoint = Metrics.WALK_RIGHT;
        } else {
            stopPoint = Metrics.WALK_LEFT;
        }
        double stopTime = (stopPoint - position)/speed;
        position = stopPoint;
        return new CarEvent(currentTime + stopTime, EventType.CAR_AT_LIGHT, id);
    }
    
    //Return an event signifying when this car exits the simulation
    public Event exitEvent(double currentTime) {
        double stopPoint;
        if(speed < 0) {
            stopPoint = 0;
        } else {
            stopPoint = Metrics.STREET_LENGTH;
        }
        double stopTime = (stopPoint - position)/speed;
        position = stopPoint;
        return new CarEvent(currentTime + stopTime, EventType.CAR_EXIT, id);
    }
    
    public void follow(Car c) {
    	ahead = c;
    }
    
    public Car getAhead() {
    	return ahead;
    }
    
    public Car getBehind() {
    	return behind;
    }
}
