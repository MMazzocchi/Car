
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
    	// I think we should calculate if it is able to stop in time or not
    	if(position /* + distance to slow to a stop */ > Metrics.WALK_LEFT)
    		return false;
    	return true;
    }
    
    public Event reactToLight(Light.LightStatus lightStatus, double currentTime){
    	switch(lightStatus){
    		case GREEN:
    			// create event for accelerating
    			return new CarEvent(currentTime, EventType.CAR_ACCELERATE, id);
    		case YELLOW:
    	    	// calculate when it will need to start decelerating
    			double slowTime = (Metrics.WALK_LEFT /* - distance it takes to stop */ - position)/tempSpeed;
    	    	// create event for slowing down if it needs to
    			return new CarEvent(currentTime + slowTime, EventType.CAR_DECELERATE, id);
    	}
    	return null;
    }   
/*    
    public void changeState(CarStatus status){
    	double delay;
    	
    	if(ahead.carStatus == CarStatus.CONSTANT){
    		// if you will catch up and have to slow down
    		// 
    		// if you wont have to do anything
    		
    	}else if(ahead.carStatus == CarStatus.STOP){
    		// calculate when you need to decelerate
    		
    	}else if(ahead.carStatus == CarStatus.ACCELERATE){
    		// possibly accelerate to maxSpeed
    		// possibly accelerate to constant speed of car in front
    		// possibly accelerate and then have to decelerate
    	}else if(ahead.carStatus == CarStatus.DECELERATE){
    		// possibly decelerate to a stop
    		// possibly decelerate to a constant speed
    		
    	}
    }
*/   
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
