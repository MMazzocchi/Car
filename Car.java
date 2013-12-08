
public class Car {
	public enum CarStatus {CONSTANT, ACCELERATE, DECELERATE, STOP};

	private int id;
	private CarStatus carStatus;
	private Car ahead;
	private Car behind;
	private Car saved;

	private double acceleration; // ft/min^2
	private double tempSpeed; // ft/min
	private double maxSpeed; // ft/min
	private double position; // feet

	private double arrivalTime;
	private double state_time;
	private double optimumExit;
	private double actualExit;
	
	public Car(int carId, double arrivalTime) {	
		
		id = carId;
		this.arrivalTime = arrivalTime;
		state_time = arrivalTime;
		
		carStatus = CarStatus.CONSTANT;

		// Calculate the speed of this car and it's initial position
		maxSpeed = (Crosswalk.random.Uniform(5)*10.0)+25.0;
		position = 0;

		maxSpeed = (maxSpeed * 5280)/60.0;
		tempSpeed = maxSpeed;
		
		acceleration = (Crosswalk.random.Uniform(10)*5.0)+7.0;
	
		acceleration = acceleration * 60 * 60;
//		acceleration = (acceleration * 5280)/(60.0*60.0);
		optimumExit = this.arrivalTime + (Metrics.STREET_LENGTH/maxSpeed);
	}

	public void follow(Car c) {
		ahead = c;
		c.setBehind(this);
		P.p("Following car: " + c);
	}

	public void setBehind(Car c) {
		behind = c;
		P.p(id + " is behind: " + c);
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
		P.p("Strategy distance being calculated: " + distance);
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
		P.p("Strategy speed being calculated: " + speed);
		return speed;
	}

	public boolean canMakeLight(double currentTime){
		
		calcCurrentState(currentTime);
		
		if((position  + strategyDistance(Metrics.WALK_YELLOW))  > (Metrics.WALK_RIGHT + 20.0)){
			P.p(id + " can make light");
			return true;
		}
		P.p(id + " can't make light");
		return false;
	}

	public void calcCurrentState(double currentTime){
		position = position + strategyDistance(currentTime - state_time);
		tempSpeed = strategySpeed(currentTime - state_time);
		state_time = currentTime;
		P.p("calcCurrentState being executed, position: " + position + ", tempSpeed: " + tempSpeed);
	}

	public void reactToLight(Light.LightStatus lightStatus, double currentTime){
		
		calcCurrentState(currentTime);
		
		P.p("Current position: "+position);
		
		P.p("Reacting to lightStatus: " + lightStatus);
		switch(lightStatus){
		case GREEN:
			P.p("lightStatus is green");
			ahead = saved;
			// create event for accelerating
			changeState(currentTime);
			break;
		case YELLOW:
			P.p("lightStatus is yellow");
			// calculate when it will need to start decelerating
//			processSpeed(Metrics.WALK_LEFT, 0, currentTime);

			saved = ahead;
			ahead = Crosswalk.stopped;
			changeState(currentTime);
			
			if(behind != null) 
				behind.changeState(currentTime);
			break;
			default:
				break;
		}
	}   

	public void changeState(double currentTime){
		P.p(id + " changing state");

		//Recalculate current position and speed
		calcCurrentState(currentTime);

		//Check if we've reached the end of the street
		if(position >= Metrics.STREET_LENGTH) {
			P.p(id + " should be exiting");
			//Generate an exit event
			Event exit = exitEvent(currentTime);
			Crosswalk.eventList.add(exit);
			
			//The car behind us is no longer following us.
			if(behind != null)
				behind.ahead = null;
		} else {
			
			//We're still in the simulation. Check if there's anyone ahead of us.
			if(ahead == null) {
				P.p("No one is ahead of " + id);
				//Calculate the time it'll take to get up to max speed
				double acc_time = (tempSpeed - maxSpeed)/acceleration;
				if(acc_time <= 0) {
					P.p(id + " is already at max speed");
					//We're already at max speed
					carStatus = CarStatus.CONSTANT;
					
					//Generate an event for when we will probably exit the simulation
					double exitTime = (Metrics.STREET_LENGTH - position)/tempSpeed;
					Event e = new CarEvent(currentTime + exitTime, EventType.CAR_REEVALUATE, id);
					Crosswalk.eventList.add(e);
					
				} else {
					P.p(id + "is accelerating");
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
				P.p("Someone is ahead of " + id);
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
		}
		
		if(behind != null) {
			behind.changeState(currentTime);
		}
	}

	//Determine the best move if our goal is to:
	//	- reach xf at the most efficient rate possible
	//	- have the speed vf once we reach xf
	public void processSpeed(double xf, double vf, double currentTime) {
		P.p("In processSpeed");
		P.p("Current State:");
		P.p("    Position: "+position);
		P.p("    Speed: "+tempSpeed);
		P.p("    Status: "+carStatus);
		P.p("");
		P.p("Attempting to reach:");
		P.p("    Position: "+xf);
		P.p("    Speed: "+vf);
		if(xf <= position && vf == 0) {
			P.p("Stop here");
			//Stop here.
			carStatus = CarStatus.STOP;
			tempSpeed = 0;

		} else if((xf <= position && vf == tempSpeed) || (tempSpeed >= maxSpeed && vf >= maxSpeed)) {
			P.p("Maintain constant");
			//If we've reached the speed at the point we wanted, hold this speed.
			//If we've reached our max speed and we want to go faster, hold this speed.
			carStatus = CarStatus.CONSTANT;

		} else {
			//Find acceleration distance
			double d_tot = xf - position;
			double vf_2 = vf*vf;
			double vi_2 = tempSpeed*tempSpeed;
			double a = acceleration;
			double d_a = (((vf_2 - vi_2)/(2.0*a))+d_tot)/2.0;
			
			
			P.p("Acceleration distance: "+d_a);
			if(d_a <= 0) {
				//Don't accelerate; start de-accelerating. Find the time it will take.
				double d_d = (xf - position) - d_a;
				P.p("Deceleration distance "+d_d);
				double time = (-tempSpeed + Math.sqrt((tempSpeed*tempSpeed)+(2*acceleration*d_d)))/acceleration;

				//Set status to de-accelerate.
				carStatus = CarStatus.DECELERATE;
				P.p("Decelerating");
				
				P.p("Time til stop: "+ time);

				//Return an event at currentTime + time where we re-evaluate
				Event e = new CarEvent(currentTime + time, EventType.CAR_REEVALUATE, getId());
				Crosswalk.eventList.add(e);
			} else {
				//Find the time it will take to accelerate this distance
				double time = (-tempSpeed + Math.sqrt((tempSpeed*tempSpeed)+(2*acceleration*d_a)))/acceleration;

				//Calculate the time when we hit our max speed
				double time_v = (maxSpeed - tempSpeed)/acceleration;

				//Use whichever comes sooner
				if(time_v < time) {
					time = time_v;
				}

				if(time != 0) {
					//Set status to accelerate.
					carStatus = CarStatus.ACCELERATE;
					P.p("Accelerating");

				} else {
					P.p("Keeping constant speed (at max speed)");
					carStatus = CarStatus.CONSTANT;
					double decelPt = xf - stopDistance();
					time = (decelPt - position)/tempSpeed;
					P.p("DecelPt: "+decelPt);
					P.p("time: "+time);
				}
				
				if(time < .000001)
					time = .000001;
				
				//Return an event at currentTime + time where we re-evaluate
				Event e = new CarEvent(currentTime + time, EventType.CAR_REEVALUATE, getId());
				Crosswalk.eventList.add(e);
			}
		}
	}

	//Return an event signifying when this car exits the simulation
	public Event exitEvent(double currentTime) {
		P.p(id + " triggered exitEvent");
		actualExit = currentTime;
		return new CarEvent(currentTime, EventType.CAR_EXIT, id);
	}

	//Return the distance needed to stop right now
	public double stopDistance() {
		double time = tempSpeed/acceleration;
		return (-.5*acceleration*time*time) + (tempSpeed*time);
	}

	public double getPostion() {
		return position;
	}

	public int getId() {
		return id;
	}

	public CarStatus getState() {
		return carStatus;
	}
	
	public double getWait() {
		return (actualExit - optimumExit);
	}
	
	public void makeStopped() {
		position = Metrics.WALK_LEFT+20;
		carStatus = CarStatus.STOP;
		tempSpeed = 0.0;
	}
}
