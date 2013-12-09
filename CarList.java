import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

public class CarList {

	private int id;
	private Deque<Car> right;
	private Deque<Car> left;
	private HashMap<Integer, Car> ids;
	private Generator gen;

	public CarList(Generator g) {
		gen = g;
		right = new LinkedList<Car>();
		left = new LinkedList<Car>();
		ids = new HashMap<Integer, Car>();
		id = 0;
	}

	// Spawn a car coming from the left end of the street
	public int addCarL(double time) {
		int newId = id;
		id++;
		Car c = new Car(newId, time, gen);
		if(left.size() != 0) {
			c.follow(left.getLast());
			left.getLast().changeState(time);
		} else {
			c.changeState(time);
		}

		left.add(c);
		ids.put(newId, c);
		
		Crosswalk.tw.printCarSpawn(time, newId, c.tempSpeed, 2);
		
		return newId;
	}

	// Spawn a car coming from the right end of the street
	public int addCarR(double time) {
		int newId = id;
		id++;
		Car c = new Car(newId, time, gen);
		if(right.size() != 0) {
			c.follow(right.getLast());
			right.getLast().changeState(time);
		} else {
			c.changeState(time);
		}

		right.add(c);
		ids.put(newId, c);
		
		Crosswalk.tw.printCarSpawn(time, newId, c.tempSpeed, 1);
		
		return newId;
	}

	public Car getFirstCarL() {
		return left.getFirst();
	}

	public Car getFirstCarR() {
		return right.getFirst();
	}

	public Car findCarAtLightL(double currentTime) {
		for(Car c : left) {
			if(!c.canMakeLight(currentTime)) {
				return c;
			}
		}
		return null;
	}

	public Car findCarAtLightR(double currentTime) {
		for(Car c : right) {
			if(!c.canMakeLight(currentTime)) {
				return c;
			}
		}
		return null;
	}

	public Car get(int id) {
		return ids.get(id);
	}

	public void remove(int id, double time) {
		P.p("Car "+id+" requested removal");
		if(right.contains(this.get(id))) {
			right.remove(this.get(id));
		} else if(left.contains(this.get(id))) {
			left.remove(this.get(id));
		}
		
		Crosswalk.tw.printCarExit(time, id);
	}

	public int size() {
		P.p("right size: "+ right.size());
		P.p("left size: "+left.size());
		return right.size() + left.size();
	}
}
