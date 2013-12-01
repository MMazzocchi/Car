import java.util.ArrayList;


public class Crosswalk {

	public static Random random;

	private int duration;
	public double currentTime;

	private EventQueue eventList;
	private CarList carList;
	private PedList pedList;

	private Light light;
	private double dontWalkTime;
	private Car carAtLightL;
	private Car carAtLightR;
	private ArrayList<Integer> pedsAtWalk;

	private Statistics stats;

	public Crosswalk(int time, long seed) {
		random = new Random(seed);

		currentTime = 0.0;
		duration = time;

		light = new Light();
		carAtLightL = null;
		carAtLightR = null;

		pedsAtWalk = new ArrayList<Integer>();
		dontWalkTime = 0.0;

		carList = new CarList();
		pedList = new PedList();
		eventList = new EventQueue();

		//Add events to start off the simulation; one car at each end
		eventList.add(new Event(random.Exponential(0), EventType.CAR_SPAWN_L));
		eventList.add(new Event(random.Exponential(1), EventType.CAR_SPAWN_R));

		//One pedestrian at each end
		eventList.add(new Event(random.Exponential(2), EventType.PED_SPAWN_L));
		eventList.add(new Event(random.Exponential(3), EventType.PED_SPAWN_R));

		stats = new Statistics();

		this.start();
	}

	public void start() {

		//While the simulation still has time or cars/pedestrians, keep processing events
		while((carList.size() > 0 && pedList.size() > 0) || currentTime < duration) {
			Event currentEvent = eventList.poll();
			currentTime = currentEvent.getTime();
			processEvent(currentEvent);
		}

		//Print statistics
		stats.setDuration(currentTime);
		stats.printStats();
	}

	// Process this event
	public void processEvent(Event event) {
		int id;

		switch(event.getType()) {

		//Spawn a car on the left
		case CAR_SPAWN_L:
			if(currentTime < duration) {
				id = carList.addCarL(currentTime); //Create a new car

				eventList.add(new Event(random.Exponential(0) + currentTime, EventType.CAR_SPAWN_L)); // Have another car come
			}
			break;

			//Spawn a car on the right
		case CAR_SPAWN_R:
			if(currentTime < duration) {
				id = carList.addCarR(currentTime); //Create a new car
				
				eventList.add(new Event(random.Exponential(1) + currentTime, EventType.CAR_SPAWN_R)); // Have another car come
				stats.addCar();
			}
			break;

			//Spawn a pedestrian on the left
		case PED_SPAWN_L:
			if(currentTime < duration) {
				id = pedList.addPedL(); // Create pedestrian
				eventList.add(pedList.get(id).crossArrival(currentTime)); //Add an event for when they arrive at the crosswalk

				eventList.add(new Event(random.Exponential(2) + currentTime, EventType.PED_SPAWN_L)); // Have another pedestrian enter
			}
			stats.addPed();
			break;

			//Spawn a pedestrian on the right
		case PED_SPAWN_R:
			if(currentTime < duration) {
				id = pedList.addPedR(); // Create Pedestrian
				eventList.add(pedList.get(id).crossArrival(currentTime)); //Add an event for when they arrive at the crosswalk

				eventList.add(new Event(random.Exponential(3) + currentTime, EventType.PED_SPAWN_R)); // Have another pedestrian enter soon
				stats.addPed();
			}
			break;
			/*
        //Process a car arriving at the light
        case CAR_AT_LIGHT:
            id = ((CarEvent)event).getId();

            //If the light is green, drive straight through it.
            if(light.getLightStatus() == Light.LightStatus.GREEN) {
                eventList.add(carList.get(id).exitEvent(currentTime));
                stats.addCarWaitTime(carList.get(id).waitTime(currentTime));
            } else {
            //If the light is not green, stop here.
                carList.get(id).startWait(currentTime);
                carsAtLight.add(id);
            }
            break;
			 */

			//A car has reached the end of the street; it exits the simulation
		case CAR_EXIT:
			id = ((CarEvent)event).getId();
			carList.remove(id);
			break;

			//Process a pedestrian arriving at the light
		case PED_AT_WALK:
			id = ((PedEvent)event).getId();

			//If the "Walk" sign is showing, attempt to cross the street (this could fail, since we might not have enough time)
			if(light.getWalkStatus() == Light.WalkStatus.WALK) {
				Event newEvent = pedList.get(id).exitEvent(currentTime, dontWalkTime);
				if(newEvent != null) {
					eventList.add(newEvent);
				}
			} else {
				//If the "Don't Walk" sign is showing, decide whether or not to press the button.
				pedsAtWalk.add(id);
				pedList.get(id).startWait(currentTime);

				//Calculate the probability of pressing the button.
				int pedCount = pedsAtWalk.size();
				double p;
				if(pedCount == 1) {
					p = 2.0/3.0;
				} else {
					p = 1.0/(double)pedCount;
				}

				//Use a Bernoulli flip to simulate whether or not the button was pressed.
				if(random.Bernoulli(p, 4)) {
					//If we did press the button, add a new event for right now signifying it.
					eventList.add(new PedEvent(currentTime, EventType.BUTTON_PRESS, id));
				} else {
					//If we did not press the button, press it one minute from now.
					eventList.add(new PedEvent(currentTime+1.0, EventType.BUTTON_PRESS, id));
				}
			}
			break;

			//A pedestrian has reached the end of the crosswalk; they exit the simulation.
		case PED_EXIT:
			id = ((PedEvent)event).getId();
			stats.addPedWaitTime(pedList.get(id).waitTime(currentTime));
			pedList.remove(id);
			break;

			//Process an attempted button press.
		case BUTTON_PRESS:

			//Make sure the pedestrian is actually still waiting; they could've already crossed.
			id = ((PedEvent)event).getId();
			if(pedList.get(id) != null) {
				if(!pedList.get(id).hasCrossed()) {
					Event newEvent = light.pressButton(currentTime);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			break;

			//Process a light change.
		case LIGHT_CHANGE:
			Event newEvent = light.change(currentTime);
			if(newEvent != null) {
				eventList.add(newEvent);
			}

			switch(light.getLightStatus()) {
			
			case GREEN:
				//Signal to the car waiting at the light that it is green
				if(carAtLightR != null)
					carAtLightR.reactToLight(light.getLightStatus(), event.getTime());
				
				if(carAtLightL != null)
					carAtLightL.reactToLight(light.getLightStatus(), event.getTime());
				
				break;
			case YELLOW:
				
				//Find the first car from either side that will have to stop
				carAtLightL = carList.findCarAtLightL();
				carAtLightR = carList.findCarAtLightR();

				//Tell those cars to react to the light
				if(carAtLightL != null)
					carAtLightL.reactToLight(light.getLightStatus(), event.getTime());
				
				if(carAtLightR != null)
					carAtLightR.reactToLight(light.getLightStatus(), event.getTime());
				
				break;
			case RED:
				//If the crosswalk sign just became "Walk", have all the pedestrians waiting attempt to cross.
				dontWalkTime = currentTime + Metrics.WALK_RED; //Calculate the time when the crosswalk will flip back to "Don't Walk"
				for(int i=0; i<pedsAtWalk.size(); i++) {
					int pedId = pedsAtWalk.get(i);
					newEvent = pedList.get(pedId).exitEvent(currentTime, dontWalkTime);
					if(newEvent != null) {
						//If this pedestrian made it across the crosswalk, add an event for their exit and remove them.
						eventList.add(newEvent);
						pedsAtWalk.remove(i);
						i--;
					}
				}
				break;
			}

/*			if(light.getLightStatus() == Light.LightStatus.GREEN) {
				for(int carId : carsAtLight) {
					stats.addCarWaitTime(carList.get(carId).waitTime(currentTime));
					eventList.add(carList.get(carId).exitEvent(currentTime));
				}
				carsAtLight.clear();
			}
			*/
			break;
		default:
			System.out.println("ERROR: UNPROCESSED EVENT AT TIME "+currentTime+" OF TYPE "+event.getType());
			System.exit(-1);
			break;
		}
	}
}
