
public class Light {

    public enum LightStatus {RED, YELLOW, GREEN};
    public enum WalkStatus {DONT_WALK, WALK};

    private LightStatus lightStatus;
    private WalkStatus walkStatus;

    private boolean buttonPressed;
    private double lastGreen; //Signifies the time this light last changed to green.

    public Light() {
        lightStatus = LightStatus.GREEN;
        walkStatus = WalkStatus.DONT_WALK;
        lastGreen = 0.0;
    }

    //Generate a light change event triggered by a button press.
    public Event pressButton(double currentTime) {
        
        //If the light is green and no one has pressed the button yet, generate the event.
        if((lightStatus == LightStatus.GREEN) && (!buttonPressed)) {
            buttonPressed = true;
            
            //Calculate when to change the light at based on the last green.
            double eventTime = currentTime + 1.0/60.0;
            double time2 = lastGreen + 14.0/60.0;
            if(time2 > eventTime) {
                eventTime = time2;
            }
            return new Event(eventTime, EventType.LIGHT_CHANGE);
        }
        return null;
    }

    //Generate an event triggered by a light change.
    public Event change(double currentTime) {
        switch(lightStatus) {
        
        //If the light is green, change it to yellow and return an event for it's change to red.
        case GREEN:
            lightStatus = LightStatus.YELLOW;
            return new Event(currentTime + Metrics.WALK_YELLOW, EventType.LIGHT_CHANGE);
            
        //If the light is yellow, change it to red and return an event for it's change to green.
        case YELLOW:
            lightStatus = LightStatus.RED;
            walkStatus = WalkStatus.WALK;
            return new Event(currentTime + Metrics.WALK_RED, EventType.LIGHT_CHANGE);
            
        //If the light is red, change it to green and don't generate an event; the light will change only for pedestrians.
        case RED:
            lightStatus = LightStatus.GREEN;
            walkStatus = WalkStatus.DONT_WALK;
            buttonPressed = false;
            return null;
        default:
            return null;
        }
    }

    public LightStatus getLightStatus() {
        return lightStatus;
    }

    public WalkStatus getWalkStatus() {
        return walkStatus;
    }

}
