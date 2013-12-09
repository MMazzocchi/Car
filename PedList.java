import java.util.HashMap;


public class PedList extends HashMap<Integer, Ped> {

    private int id;
    
    public PedList() {
        super();
        id = 0;
    }
    
    //Add a pedestrian on the left side of the block.
    public int addPedL(double time) {
        int newId = id;
        id++;
        Ped p = new Ped(newId, Ped.Origin.LEFT);
        this.put(newId, p);
        
        Crosswalk.tw.printPedSpawn(time, newId, p.speed, 2);
        
        return newId;
    }
    
    //Add a pedestrian on the right side of the block.
    public int addPedR(double time) {
        int newId = id;
        id++;
        Ped p = new Ped(newId, Ped.Origin.RIGHT);
        this.put(newId, p);
        
        Crosswalk.tw.printPedSpawn(time, newId, p.speed, 1);
        
        return newId;
    }
}