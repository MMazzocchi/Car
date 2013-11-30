import java.util.Comparator;
import java.util.PriorityQueue;


public class EventQueue extends PriorityQueue<Event> {
    
    public static class EventComparator implements Comparator<Event> {
        
        public int compare(Event arg0, Event arg1) {
            double time = arg0.getTime() - arg1.getTime();
            if(time == 0) {
                return 0;
            }
            if(time < 0) {
                return -1;
            }
            if(time > 0) {
                return 1;
            }
            return 0;
        }
    }
    
    public EventQueue() {
        super(100, new EventComparator());
    }
}
