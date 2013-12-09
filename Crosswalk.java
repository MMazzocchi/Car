import java.util.ArrayList;


public class Crosswalk {

	public static Random random;

	private int duration;
	public double currentTime;

	public static EventQueue eventList;
	
	private Generator carGen;
	private Generator pedGen;
	private CarList carList;
	private PedList pedList;

	private Light light;
	private double dontWalkTime;
	private Car carAtLightL;
	private Car carAtLightR;
	private ArrayList<Integer> pedsAtWalk;

	private Statistics stats;
	
	public static Car stopped;
	
	public static TraceWriter tw;

	public Crosswalk(int time, long seed, String p_arr, String c_arr,
			String p_rate, String c_rate, String trace) {
		random = new Random(seed);

		currentTime = 0.0;
		duration = time;

		light = new Light();
		carAtLightL = null;
		carAtLightR = null;

		pedsAtWalk = new ArrayList<Integer>();
		dontWalkTime = 0.0;

		pedGen = new Generator(p_arr, p_rate, 11);
		carGen = new Generator(c_arr, c_rate, 13);

		stopped = new Car(-1, -1, carGen);
		stopped.makeStopped();
		
		carList = new CarList(carGen);
		pedList = new PedList(pedGen);
		eventList = new EventQueue();

		//Add events to start off the simulation; one car at each end
		eventList.add(new Event(random.Exponential(0), EventType.CAR_SPAWN_L));
		eventList.add(new Event(random.Exponential(1), EventType.CAR_SPAWN_R));

		//One pedestrian at each end
		eventList.add(new Event(random.Exponential(2), EventType.PED_SPAWN_L));
		eventList.add(new Event(random.Exponential(3), EventType.PED_SPAWN_R));

		stats = new Statistics();
		tw = new TraceWriter(trace);

		this.start();
	}

	public void start() {

		//While the simulation still has time or cars/pedestrians, keep processing events
		while((carList.size() > 0 && pedList.size() > 0) || currentTime < duration) {
			Event currentEvent = eventList.poll();
			currentTime = currentEvent.getTime();
			processEvent(currentEvent);
			try {
				//Thread.sleep(1000);
			} catch (Exception e) {
				P.p("dasdfasdfasdf");
			}
		}
		
		tw.printEnd(currentTime);
		tw.closeFile();
		
		//Print statistics
		stats.setDuration(currentTime);
		stats.printStats();
	}

	// Process this event
	public void processEvent(Event event) {
		P.p("Processing event at time "+currentTime);
		
		int id;

		switch(event.getType()) {
		
		case CAR_REEVALUATE:
			id = ((CarEvent)event).getId();
			P.p("Reevaluating car "+id+" at time"+currentTime);
			carList.get(id).changeState(currentTime);
			break;

		//Spawn a car on the left
		case CAR_SPAWN_L:			
			if(currentTime < duration) {

				id = carList.addCarL(currentTime); //Create a new car
				P.p("Car "+id+" spawned on the left at time "+currentTime);

				eventList.add(new Event(random.Exponential(0) + currentTime, EventType.CAR_SPAWN_L)); // Have another car come
				stats.addCar();
			}
			break;

			//Spawn a car on the right
		case CAR_SPAWN_R:
			if(currentTime < duration) {
				id = carList.addCarR(currentTime); //Create a new car
				P.p("Car "+id+" spawned on the right at time "+currentTime);
				
				eventList.add(new Event(random.Exponential(1) + currentTime, EventType.CAR_SPAWN_R)); // Have another car come
				stats.addCar();
			}
			break;

			//Spawn a pedestrian on the left
		case PED_SPAWN_L:
			if(currentTime < duration) {
				id = pedList.addPedL(currentTime); // Create pedestrian
				P.p("Ped "+id+" spawned on left at "+currentTime);

				eventList.add(pedList.get(id).crossArrival(currentTime)); //Add an event for when they arrive at the crosswalk

				eventList.add(new Event(random.Exponential(2) + currentTime, EventType.PED_SPAWN_L)); // Have another pedestrian enter
				stats.addPed();				
			}
			break;

			//Spawn a pedestrian on the right
		case PED_SPAWN_R:
			if(currentTime < duration) {
				id = pedList.addPedR(currentTime); // Create Pedestrian
				P.p("Ped "+id+" spawned on right at "+currentTime);

				eventList.add(pedList.get(id).crossArrival(currentTime)); //Add an event for when they arrive at the crosswalk

				eventList.add(new Event(random.Exponential(3) + currentTime, EventType.PED_SPAWN_R)); // Have another pedestrian enter soon
				stats.addPed();
			}
			break;

			//A car has reached the end of the street; it exits the simulation
		case CAR_EXIT:
			id = ((CarEvent)event).getId();
			P.p("Car "+id+" exited at "+currentTime);
			carList.remove(id, currentTime);
			Car c = carList.get(id);
			stats.addCarWaitTime(c.getWait());
			
	        Crosswalk.tw.printCarExit(currentTime, id);
	        
			break;

			//Process a pedestrian arriving at the light
		case PED_AT_WALK:
			id = ((PedEvent)event).getId();
			P.p("Ped "+id+" arrived at walk at "+currentTime);
			
	        Crosswalk.tw.printPedSpeedChange(currentTime, id, 0.0);

			//If the "Walk" sign is showing, attempt to cross the street (this could fail, since we might not have enough time)
			if(light.getWalkStatus() == Light.WalkStatus.WALK) {
				Event newEvent = pedList.get(id).exitEvent(currentTime, dontWalkTime);
				if(newEvent != null) {
					P.p("Ped "+id+" began crossing right away.");
					eventList.add(newEvent);
					
			        Crosswalk.tw.printPedStartCross(currentTime, id);
				} else {
					pedsAtWalk.add(id);
					pedList.get(id).startWait(currentTime);
					P.p("Ped "+id+" couldn't make it, decided to wait.");
				}
			} else {
				//If the "Don't Walk" sign is showing, decide whether or not to press the button.
				pedsAtWalk.add(id);
				pedList.get(id).startWait(currentTime);

				P.p("Ped "+id+" began waiting.");
				
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
			P.p("Ped "+id+" exited the simulation.");
			stats.addPedWaitTime(pedList.get(id).waitTime(currentTime));
			pedList.remove(id);
			
	        Crosswalk.tw.printPedExit(currentTime, id);
			break;

			//Process an attempted button press.
		case BUTTON_PRESS:

			//Make sure the pedestrian is actually still waiting; they could've already crossed.
			id = ((PedEvent)event).getId();
			if(pedList.get(id) != null) {
				if(!pedList.get(id).hasCrossed()) {
					P.p("Ped "+id+" pressed the button.");
					Event newEvent = light.pressButton(currentTime);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			break;

		//Process a light change.
		case LIGHT_CHANGE:
			P.p("The light has changed at "+currentTime);
			Event newEvent = light.change(currentTime);
			if(newEvent != null) {
				eventList.add(newEvent);
			}
		
			switch(light.getLightStatus()) {
			
			case GREEN:
				//Signal to the car waiting at the light that it is green
				if(carAtLightR != null) {
					P.p("Telling the car "+carAtLightR.getId()+" on right to react to the green.");
					carAtLightR.reactToLight(light.getLightStatus(), event.getTime());
					carAtLightR = null;
				}
				
				if(carAtLightL != null) {
					P.p("Telling the car "+carAtLightL.getId()+" on the left to react to the green.");
					carAtLightL.reactToLight(light.getLightStatus(), event.getTime());
					carAtLightL = null;
				}
				
		        Crosswalk.tw.printLightChange(currentTime, 3);
				
				break;
			case YELLOW:
				
				//Find the first car from either side that will have to stop
				carAtLightL = carList.findCarAtLightL(currentTime);
				carAtLightR = carList.findCarAtLightR(currentTime);

				//Tell those cars to react to the light
				if(carAtLightL != null) {
					P.p("Found car on left "+carAtLightL.getId()+", telling it to react to the yellow.");
					carAtLightL.reactToLight(light.getLightStatus(), event.getTime());
				}
				
				if(carAtLightR != null) {
					P.p("Found car on right "+carAtLightR.getId()+", telling it to react to the yellow.");
					carAtLightR.reactToLight(light.getLightStatus(), event.getTime());
				}
				
		        Crosswalk.tw.printLightChange(currentTime, 2);
				
				break;
			case RED:
				P.p("Telling all the pedestrians to walk at time "+currentTime);
				
				//If the crosswalk sign just became "Walk", have all the pedestrians waiting attempt to cross.
				dontWalkTime = currentTime + Metrics.WALK_RED; //Calculate the time when the crosswalk will flip back to "Don't Walk"
				for(int i=0; i<pedsAtWalk.size(); i++) {
					int pedId = pedsAtWalk.get(i);
					newEvent = pedList.get(pedId).exitEvent(currentTime, dontWalkTime);
					if(newEvent != null) {
						//If this pedestrian made it across the crosswalk, add an event for their exit and remove them.
						eventList.add(newEvent);
						pedsAtWalk.remove(i);
						P.p("Ped "+pedId+" walked.");
						i--;
						
				        Crosswalk.tw.printPedStartCross(currentTime, pedId);
					} else {
						P.p("Ped "+pedId+" didn't walk.");
					}
				}
				
		        Crosswalk.tw.printLightChange(currentTime, 1);

				break;
			}

			break;
		default:
			P.p("ERROR: UNPROCESSED EVENT AT TIME "+currentTime+" OF TYPE "+event.getType());
			System.exit(-1);
			break;
		}
	}
}
