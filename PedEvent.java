
public class PedEvent extends Event{

    private int id;
    
    public PedEvent(double t, EventType et, int pedId) {
        super(t, et);
        id = pedId;
    }
    
    public int getId() {
        return id;
    }
}