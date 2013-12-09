
public class Ped {
    public enum Origin {LEFT, RIGHT};
    
    private int id;

    public double speed; // ft/min
    private double position; //feet
    private boolean hasCrossed;
    
    private double waitStart;

    public Ped(int pedId, Origin origin) {
        id = pedId;
        
        //Calculate the speed of this pedestrian and their current position.
        speed = (Crosswalk.random.Uniform(6)*7.0)+6.0;
        if(origin == Origin.LEFT) {
            position = Metrics.BLOCK_LEFT;
        } else {
            speed *= -1;
            position = Metrics.BLOCK_RIGHT;
        }

        speed = speed * 60.0;

        hasCrossed = false;
        waitStart = -1;
    }
    
    //Start recording the wait time.
    public void startWait(double currentTime) {
        waitStart = currentTime;
    }
    
    //Return the amount of time waited up to the current time.
    public double waitTime(double currentTime) {
        if(waitStart == -1) {
            return 0.0;
        } else {
            return currentTime - waitStart;
        }
    }
    
    //Generate an event signifying the arrival of this pedestrian at the crosswalk.
    public Event crossArrival(double currentTime) {
        double stopPoint = Metrics.WALK_CENTER;
        double stopTime = (stopPoint - position)/speed;
        return new PedEvent(currentTime + stopTime, EventType.PED_AT_WALK, id);
    }

    //Generate an event signifying the pedestrian exiting the crosswalk, but on only if they have enough time to do so.
    public Event exitEvent(double currentTime, double dontWalkTime) {
        
        //Calculate the time needed to cross.
        double stopPoint = Metrics.STREET_WIDTH;
        double stopTime = Math.abs((stopPoint)/speed);
        
        //If there's enough time, return an event.
        if(currentTime + stopTime < dontWalkTime) {
            hasCrossed = true;
            return new PedEvent(currentTime + stopTime, EventType.PED_EXIT, id);
        } else {
            return null;
        }
    }

    public boolean hasCrossed() {
        return hasCrossed;
    }
}
