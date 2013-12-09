import java.util.HashMap;


public class PedList extends HashMap<Integer, Ped> {

    private int id;
    private Generator gen;
    
    
    public PedList(Generator g) {
        super();
        gen = g;
        id = 0;
    }
    
    //Add a pedestrian on the left side of the block.
    public int addPedL(double time) {
        int newId = id;
        id++;
        Ped p = new Ped(newId, Ped.Origin.LEFT, gen);
        this.put(newId, p);
        
        Crosswalk.tw.printPedSpawn(time, newId, p.speed, 2);
        
        return newId;
    }
    
    //Add a pedestrian on the right side of the block.
    public int addPedR(double time) {
        int newId = id;
        id++;
        Ped p = new Ped(newId, Ped.Origin.RIGHT, gen);
        this.put(newId, p);
        
        Crosswalk.tw.printPedSpawn(time, newId, p.speed, 1);
        
        return newId;
    }
}