
public class Car {
    public enum CarStatus {CONSTANT, ACCELERATE, DECELERATE, STOP};
    
    private int id;
    private CarStatus carStatus;
    private Car ahead;
    private Car behind;
    
    private double acceleration; // ft/min
    private double tempSpeed; // ft/min
    private double maxSpeed; // ft/min
    private double position; // feet
    
    private double arrivalTime;
    private double state_time;
    private double optimumExit;
    
    public Car(int carId, double arrivalTime) {
        id = carId;
        this.arrivalTime = arrivalTime;
        carStatus = CarStatus.CONSTANT;
        
        // Calculate the speed of this car and it's initial position
        maxSpeed = (Crosswalk.random.Uniform(5)*10.0)+25.0;
            position = 0;
        
        maxSpeed = (maxSpeed * 5280)/60.0;
        tempSpeed = maxSpeed;
        acceleration = (acceleration * 5280)/60.0;
        optimumExit = this.arrivalTime + (Metrics.STREET_LENGTH/maxSpeed);
    }
    
    public void follow(Car c) {
        ahead = c;
        c.setBehind(this);
    }
    
    public void setBehind(Car c) {
    	behind = c;
    }
    
    public double strategyDistance(double time){
    	double distance = 0.0;
    	switch(carStatus){
	    	case ACCELERATE:
	    		distance = 0.5*(acceleration)*(time*time) + tempSpeed*time;
	    		break;
	    	case DECELERATE:
	    		distance = 0.5*(-acceleration)*(time*time) + tempSpeed*time;
	    		break;
	    	case STOP:
	    		distance = 0.0;
	    		break;
	    	case CONSTANT:
	    		distance = tempSpeed*time;
	    		break;
    	}
    	
    	return distance;
    }
    
    public boolean canMakeLight(){
    	if((position  + strategyDistance(Metrics.WALK_YELLOW))  > (Metrics.WALK_RIGHT + 20.0)){
    		return true;
    	}
    	return false;
    }
    
    
    public void calcPosition(double currentTime){
    	position = position + strategyDistance(currentTime - state_time);
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
   
    public Event changeState(CarStatus status, double currentTime){
    	
    	switch(ahead.carStatus) {
    	case CONSTANT:
    		// if you will catch up and have to slow down
    		// 
    		// if you wont have to do anything
    		
    	case STOP:
    		//The car ahead of us is stopped.
    		double stopPoint = ahead.getPostion() - Metrics.MINIMUM_STOP;
    		Event nextEvent = processSpeed(stopPoint, 0, currentTime);
    		return nextEvent;
    		
    	case ACCELERATE:
    		// possibly accelerate to maxSpeed
    		// possibly accelerate to constant speed of car in front
    		// possibly accelerate and then have to decelerate
    	case DECELERATE: 
    		// possibly decelerate to a stop
    		// possibly decelerate to a constant speed
    		
    	}
    	
    	return null;
    }

    //Determine the best move if our goal is to:
    //	- reach xf at the most efficient rate possible
    //	- have the speed vf once we reach xf
    public Event processSpeed(double xf, double vf, double currentTime) {
    	if(xf <= position && vf == 0) {
    		//Stop here.
    		carStatus = CarStatus.STOP;

    	} else {
    		//Find acceleration distance
    		double d_a = (xf-position) - (((((tempSpeed*tempSpeed)+(vf*vf))/(2*acceleration))+(xf-position))/2.0);
    		if(d_a <= 0) {
    			//Don't accelerate; start de-accelerating. Find the time it will take.
    			double d_d = (xf - position) - d_a;
    			double time = (-tempSpeed + Math.sqrt((tempSpeed*tempSpeed)+(2*acceleration*d_d)))/acceleration;

    			//Set status to de-accelerate.
    			carStatus = CarStatus.DECELERATE;

    			//Return an event at currentTime + time where we re-evaluate
    			return new CarEvent(currentTime + time, EventType.CAR_REEVALUATE, id);

    		} else {
    			//Find the time it will take to accelerate this distance
    			double time = (-tempSpeed + Math.sqrt((tempSpeed*tempSpeed)+(2*acceleration*d_a)))/acceleration;

    			//Set status to accelerate.
    			carStatus = CarStatus.ACCELERATE;

    			//Return an event at currentTime + time where we re-evaluate
    			return new CarEvent(currentTime + time, EventType.CAR_REEVALUATE, id);
    		}
    	}
    	return null;
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
    
    public double getPostion() {
    	return position;
    }
}
