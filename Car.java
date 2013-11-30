import Light.LightStatus;



public class Car {
    public enum Origin {LEFT, RIGHT};
    public enum CarStatus {CONSTANT, ACCELERATE, DECELERATE, STOP};
    
    private int id;
    private CarStatus carStatus;
    private Car ahead;
    
    private double acceleration; // ft/min
    private double tempSpeed; // ft/min
    private double maxSpeed; // ft/min
    private double position; // feet
    
    private double arrivalTime;
    private double optimumExit;
    
    public Car(int carId, Origin origin, double arrivalTime) {
        id = carId;
        this.arrivalTime = arrivalTime;
        carStatus = CarStatus.CONSTANT;
        
        // Calculate the speed of this car and it's initial position
        maxSpeed = (Crosswalk.random.Uniform(5)*10.0)+25.0;
        if(origin == Origin.LEFT) {
            position = 0;
        } else {
            maxSpeed *= -1;
            position = Metrics.STREET_LENGTH;
        }
        
        maxSpeed = (maxSpeed * 5280)/60.0;
        tempSpeed = maxSpeed;
        acceleration = (acceleration * 5280)/60.0;
        optimumExit = this.arrivalTime + (Metrics.STREET_LENGTH/maxSpeed);
    }
    
    public void follow(Car c) {
        ahead = c;
    }
    
    public boolean canMakeLight(){
    	if(position + (tempSpeed * Metrics.WALK_YELLOW) > Metrics.WALK_LEFT)
    		return true;
    	return false;
    }
    
    public Event reactToLight(Light.LightStatus lightStatus){
    	if(lightStatus == Light.LightStatus.GREEN){
    		
    	}else if(lightStatus == Light.LightStatus.YELLOW){
    		
    	}else if(lightStatus == Light.LightStatus.RED){
    		
    	}
    }
    
    public Event changeState(double currentTime){
    	double delay;
    	
    	if(ahead.carStatus == CarStatus.CONSTANT){
    		// if you will catch up and have to slow down
    		// 
    		// if you wont have to do anything
    		
    	}else if(ahead.carStatus == CarStatus.STOP){
    		
    	}else if(ahead.carStatus == CarStatus.ACCELERATE){
    		// possibly accelerate to maxSpeed
    		// possibly accelerate to constant speed of car in front
    		// possibly accelerate and then have to decelerate
    	}else if(ahead.carStatus == CarStatus.DECELERATE){
    		// possibly decelerate to a stop
    		// possibly decelerate to a constant speed
    		
    	}
    	return new CarEvent(currentTime + delay, EventType.CAR_STATUS, id);
    }
    
    //Return an event signifying when this car exits the simulation
    public Event exitEvent(double currentTime) {
        double stopPoint;
        if(tempSpeed < 0) {
            stopPoint = 0;
        } else {
            stopPoint = Metrics.STREET_LENGTH;
        }
        double stopTime = (stopPoint - position)/tempSpeed;
        position = stopPoint;
        return new CarEvent(currentTime + stopTime, EventType.CAR_EXIT, id);
    }
}
