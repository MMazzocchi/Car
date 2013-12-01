
public class Car {
	public enum CarStatus {CONSTANT, ACCELERATE, DECELERATE, STOP};

	private int id;
	private CarStatus carStatus;
	private Car ahead;
	private Car behind;

	private double acceleration; // ft/min^2
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
	
	//Return the speed after maintaining the current strategy for the given time
	public double strategySpeed(double time) {
		double speed = 0.0;
		switch(carStatus){
		case ACCELERATE:
			speed = tempSpeed + (acceleration*time);
			break;
		case DECELERATE:
			speed = tempSpeed - (acceleration*time);
			break;
		case STOP:
			speed = 0.0;
			break;
		case CONSTANT:
			speed = tempSpeed;
			break;
		}

		return speed;
	}

	public boolean canMakeLight(){
		if((position  + strategyDistance(Metrics.WALK_YELLOW))  > (Metrics.WALK_RIGHT + 20.0)){
			return true;
		}
		return false;
	}

	public void calcCurrentState(double currentTime){
		position = position + strategyDistance(currentTime - state_time);
		tempSpeed = strategySpeed(currentTime - state_time);
	}

	public void reactToLight(Light.LightStatus lightStatus, double currentTime){
		switch(lightStatus){
		case GREEN:
			// create event for accelerating
			changeState(currentTime);
		case YELLOW:
			// calculate when it will need to start decelerating
			processSpeed(Metrics.WALK_LEFT, 0, currentTime);
		//	behind.changeState(currentTime);
		}
	}   

	public void changeState(double currentTime){

		//Recalculate current position and speed
		calcCurrentState(currentTime);

		//Check if we've reached the end of the street
		if(position >= Metrics.STREET_LENGTH) {
			
			//Generate an exit event
			Event exit = exitEvent(currentTime);
			Crosswalk.eventList.add(exit);
			
			//The car behind us is no longer following. Have it reevaluate
			behind.ahead = null;
			behind.changeState(currentTime);
		} else {
			
			//We're still in the simulation. Check if there's anyone ahead of us.
			if(ahead == null) {
				//Calculate the time it'll take to get up to max speed
				double acc_time = (tempSpeed - maxSpeed)/acceleration;
				if(acc_time >= 0) {
					//We're already at max speed
					carStatus = CarStatus.CONSTANT;
					
					//Generate an event for when we will probably exit the simulation
					double exitTime = (Metrics.STREET_LENGTH - position)/tempSpeed;
					Event e = new CarEvent(currentTime + exitTime, EventType.CAR_REEVALUATE, id);
					Crosswalk.eventList.add(e);
					
				} else {
					//Start accelerating
					carStatus = CarStatus.ACCELERATE;
					
					//Calculate when we might get out of the simulation (if we keep accelerating)
					double dist = Metrics.STREET_LENGTH - position;
					double exitTime = -tempSpeed + (Math.sqrt((tempSpeed*tempSpeed)+(2*acceleration*dist))/acceleration);
					
					//Take whichever happens first; either we hit max speed or we exit
					if(exitTime < acc_time)
						acc_time = exitTime;
					Event e = new CarEvent(currentTime + acc_time, EventType.CAR_REEVALUATE, id);
					Crosswalk.eventList.add(e);
				}

			} else {
				//There is a car ahead of us.
				double stopPoint;
				
				//Calculate a safe stopping distance.
				double extraDist = 0;
				if(ahead.acceleration > acceleration) {
					extraDist = ahead.stopDistance() - stopDistance();
				}
				stopPoint = ahead.getPostion() - (Metrics.MINIMUM_STOP + extraDist);
				
				//Get to the safe stopping distance behind that car, going its speed.
				processSpeed(stopPoint, ahead.tempSpeed, currentTime);
			}

			if(behind != null) {
				behind.changeState(currentTime);
			}
		}
	}

	//Determine the best move if our goal is to:
	//	- reach xf at the most efficient rate possible
	//	- have the speed vf once we reach xf
	public void processSpeed(double xf, double vf, double currentTime) {
		if(xf <= position && vf == 0) {
			//Stop here.
			carStatus = CarStatus.STOP;
			tempSpeed = 0;

		} else if((xf <= position && vf == tempSpeed) || (tempSpeed >= maxSpeed && vf >= maxSpeed)) {
			//If we've reached the speed at the point we wanted, hold this speed.
			//If we've reached our max speed and we want to go faster, hold this speed.
			carStatus = CarStatus.CONSTANT;

		} else {
			//Find acceleration distance
			double d_a = (xf-position) - (((((tempSpeed*tempSpeed)+(vf*vf))/(2*acceleration))+(xf-position))/2.0);
			if(d_a <= 0) {
				//Don't accelerate; start de-accelerating. Find the time it will take.
				double d_d = (xf - position) - d_a;
				double time = (-tempSpeed + Math.sqrt((tempSpeed*tempSpeed)+(2*acceleration*d_d)))/acceleration;

				//Calculate the time when we hit our max speed
				double time_v = (maxSpeed - tempSpeed)/acceleration;

				//Use whichever comes sooner
				if(time_v < time) {
					time = time_v;
				}

				//Set status to de-accelerate.
				carStatus = CarStatus.DECELERATE;

				//Return an event at currentTime + time where we re-evaluate
				Event e = new CarEvent(currentTime + time, EventType.CAR_REEVALUATE, id);
				Crosswalk.eventList.add(e);
			} else {
				//Find the time it will take to accelerate this distance
				double time = (-tempSpeed + Math.sqrt((tempSpeed*tempSpeed)+(2*acceleration*d_a)))/acceleration;

				//Set status to accelerate.
				carStatus = CarStatus.ACCELERATE;

				//Return an event at currentTime + time where we re-evaluate
				Event e = new CarEvent(currentTime + time, EventType.CAR_REEVALUATE, id);
				Crosswalk.eventList.add(e);
			}
		}
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

	//Return the distance needed to stop right now
	public double stopDistance() {
		double time = tempSpeed/acceleration;
		return (-.5*acceleration*time*time) + (tempSpeed*time);
	}

	public double getPostion() {
		return position;
	}
}
