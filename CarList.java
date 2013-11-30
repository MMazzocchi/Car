import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

public class CarList {

    private int id;
    private Deque<Car> right;
    private Deque<Car> left;
    private HashMap<Integer, Car> ids;
    
    public CarList() {
        super();
        right = new LinkedList<Car>();
        left = new LinkedList<Car>();
        ids = new HashMap<Integer, Car>();
        id = 0;
    }
    
    // Spawn a car coming from the left end of the street
    public int addCarL(double time) {
        int newId = id;
        id++;
        Car c = new Car(newId, Car.Origin.LEFT, time);
        c.follow(left.getLast());
        left.add(c);
        ids.put(newId, c);
        return newId;
    }
    
    // Spawn a car coming from the right end of the street
    public int addCarR(double time) {
        int newId = id;
        id++;
        Car c = new Car(newId, Car.Origin.RIGHT, time);
        c.follow(right.getLast());
        right.add(c);
        ids.put(newId, c);
        return newId;
    }
    
    public Car findCarAtLightL() {
    	for(Car c : left) {
    		if(c.canMakeLight()) {
    			return c;
    		}
    	}
    	return null;
    }
    
    public Car findCarAtLightR() {
    	for(Car c : right) {
    		if(c.canMakeLight()) {
    			return c;
    		}
    	}
    	return null;
    }
    
    public void remove(int id) {
    	ids.remove(id);
    }
    
    public int size() {
    	return ids.size();
    }
}
