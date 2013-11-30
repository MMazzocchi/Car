
public class Event {
    
    private double time;
    private EventType type;
    
    public Event(double t, EventType et) {
        time = t;
        type = et;
    }
    
    public double getTime() {
        return time;
    }
    
    public EventType getType() {
        return type;
    }

}
