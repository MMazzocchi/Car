
public class CarEvent extends Event{

    private int id;
    
    public CarEvent(double t, EventType et, int carId) {
        super(t, et);
        id = carId;
    }
    
    public int getId() {
        return id;
    }
}
