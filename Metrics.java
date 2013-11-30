
public class Metrics {
    public static final double LAMBDA = 4.0; //4 cars/minute = .25 minutes per car
    public static final double STREET_LENGTH = 330*7; //330 ft/block * 7 blocks
    public static final double WALK_RIGHT = (STREET_LENGTH/2.0)+12; //Position of the right side of the crosswalk
    public static final double WALK_CENTER = STREET_LENGTH/2.0; //Position of the center of the crosswalk
    public static final double WALK_LEFT = (STREET_LENGTH/2.0)-12; //Position of the left of the crosswalk
    public static final double BLOCK_LEFT = WALK_CENTER - (330.0/2.0) + 23;
    public static final double BLOCK_RIGHT = WALK_CENTER + (330.0/2.0) - 23;
    public static final double STREET_WIDTH = 46.0;
    public static final double WALK_YELLOW = 8.0/60.0;
    public static final double WALK_RED = 12.0/60.0;
}
