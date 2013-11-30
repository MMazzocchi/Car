import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

public class CarList extends HashMap<Integer, Car> {

    private int id;
    private Deque<Car> right;
    private Deque<Car> left;
    
    public CarList() {
        super();
        right = new LinkedList<Car>();
        left = new LinkedList<Car>();
        id = 0;
    }
    
    // Spawn a car coming from the left end of the street
    public int addCarL(double time) {
        int newId = id;
        id++;
        Car c = new Car(newId, Car.Origin.LEFT, time);
        c.follow(left.getLast());
        left.add(c);
        this.put(newId, c);
        return newId;
    }
    
    // Spawn a car coming from the right end of the street
    public int addCarR(double time) {
        int newId = id;
        id++;
        Car c = new Car(newId, Car.Origin.RIGHT, time);
        c.follow(right.getLast());
        right.add(c);
        this.put(newId, c);
        return newId;
    }
}
