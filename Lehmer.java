import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Lehmer {

    static final int MODULUS         = 2147483647; /* DON'T CHANGE THIS VALUE                  */
    static final int MULTIPLIER     = 48271;      /* DON'T CHANGE THIS VALUE                  */
    static final int CHECK          = 399268537;  /* DON'T CHANGE THIS VALUE                  */
    static final int STREAMS        = 256;        /* # of streams, DON'T CHANGE THIS VALUE    */
    static final int A256           = 22925;      /* jump multiplier, DON'T CHANGE THIS VALUE */
    static final int DEFAULT        = 123456789;  /* initial seed, use 0 < DEFAULT < MODULUS  */

    long[] seed = new long[STREAMS];              /* current state of each stream   */
    long[] numbersDrawn = new long[STREAMS];       /* numbers drawn from each stream   */
    int stream = 0;                                /* stream index, 0 is the default */
    int initialized = 0;                            /* test for stream initialization */

    // Constructor
    public Lehmer() {
        this.seed[0] = DEFAULT;
    }

    /* ----------------------------------------------------------------
     * Random returns a pseudo-random real number uniformly distributed 
     * between 0.0 and 1.0. 
     * ----------------------------------------------------------------
     */
    public double random() {
        final long Q = MODULUS / MULTIPLIER;
        final long R = MODULUS % MULTIPLIER;
        long t;

        t = MULTIPLIER * (seed[stream] % Q) - R * (seed[stream] / Q);
        if (t > 0) {
            seed[stream] = t;
        } else { 
            seed[stream] = t + MODULUS;
        }
        numbersDrawn[stream]++;
        return ((double) seed[stream] / MODULUS);
    }

    /* ---------------------------------------------------------------------
     * Use this function to set the state of all the random number generator 
     * streams by "planting" a sequence of states (seeds), one per stream, 
     * with all states dictated by the state of the default stream. 
     * The sequence of planted states is separated one from the next by 
     * 8,367,782 calls to Random(). (Note: stream 255 seems to have 13699236 calls)
     * ---------------------------------------------------------------------
     */
    void plantSeeds(long x) {
        final long Q = MODULUS / A256;
        final long R = MODULUS % A256;
        int j;
        int s;

        initialized = 1;
        s = stream;                            /* remember the current stream */
        selectStream(0);                       /* change to stream 0          */
        putSeed(x);                            /* set seed[0]                 */
        stream = s;                            /* reset the current stream    */
        for (j = 1; j < STREAMS; j++) {
            x = A256 * (seed[j - 1] % Q) - R * (seed[j - 1] / Q);
            if (x > 0) {
                seed[j] = x;
            } else {
                seed[j] = x + MODULUS;
            }
        }
    }

    /* ---------------------------------------------------------------
     * Use this function to set the state of the current random number 
     * generator stream according to the following conventions:
     *    if x > 0 then x is the state (unless too large)
     *    if x < 0 then the state is obtained from the system clock
     *    if x = 0 then the state is to be supplied interactively
     * ---------------------------------------------------------------
     */
    void putSeed(long x) {
        char ok = 0;

        if (x > 0) {
            x = x % MODULUS;                       /* correct if x is too large  */
        } else if (x < 0) {
            x = ((long) System.currentTimeMillis()) % MODULUS;
        } else if (x == 0) {                                
            while (ok != 1) {
                System.out.println("\nEnter a positive integer seed (9 digits or less) >> ");

                try {
                    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                    String userInputString = bufferRead.readLine();
                    x = Integer.parseInt(userInputString);
                } catch (java.lang.NumberFormatException e) {
                    ok = 0;
                    System.out.println("\nInvalid Input (too big or non-numerical)\n");
                    continue;
                } catch (IOException e) {
                    ok = 0;
                    System.out.println("\nThere was an issue reading from the input stream\n");
                    continue;
                }

                if((0 < x) && (x < MODULUS)) {
                    ok = 1;
                } else {
                    ok = 0;
                    System.out.println("\nInput out of range ... try again\n");
                }
            }
        }
        seed[stream] = x;
    }

    /* ---------------------------------------------------------------
     * Use this function to get the state of the current random number 
     * generator stream.                                                   
     * ---------------------------------------------------------------
     */
    long getSeed() {
        return seed[stream];
    }


    /* ------------------------------------------------------------------
     * Use this function to set the current random number generator
     * stream -- that stream from which the next random number will come.
     * ------------------------------------------------------------------
     */
    void selectStream(int index) {
        stream = ((int) index) % STREAMS;
        if ((initialized == 0) && (stream != 0)) {   /* protect against        */
            plantSeeds(DEFAULT);                     /* un-initialized streams */
        }
    }
    
    public void checkStreamOverflow() {
        // Check if any streams were exhausted
        for(int i = 0; i < 256; i++) {
            if(numbersDrawn[i] > 8367782) {
                System.out.println("WARNING: Random number stream " + i + " has exceeded 8367782 numbers and will begin to overflow into stream " + (i+1) % 256);
            }
        }
    }

    /* ------------------------------------------------------------------
     * Use this (optional) function to test for a correct implementation.
     * ------------------------------------------------------------------    
     */
    void testRandom() {
        long   i;
        long   x;
        char   ok = 0;  

        selectStream(0);                  /* select the default stream */
        putSeed(1);                       /* and set the state to 1    */
        for(i = 0; i < 10000; i++) {
            random();
        }
        x = getSeed();                      /* get the new state value   */
        if (x == CHECK) {                /* and check for correctness */
            ok = 1;
        }

        selectStream(1);                  /* select stream 1                 */ 
        plantSeeds(1);                    /* set the state of all streams    */
        x = getSeed();                      /* get the state of stream 1       */

        if((ok == 1) && (x == A256)) {          /* x should be the jump multiplier */   
            ok = 1;
        } else {
            ok = 0;
        }

        if (ok == 1) {
            System.out.println("\n The implementation of Lehmer.java is correct.\n\n");
        } else {
            System.out.println("\n ERROR -- the implementation of rngs.c is not correct.\n\n");
        }
    }

    /*
    public static void main(String args[]) {

        Lehmer l = new Lehmer();
        l.plantSeeds(DEFAULT); // Done with initialization
        
        l.selectStream(0);
        double x = l.random(); // x is first random number from stream 0
        l.selectStream(1);
        double y = l.random();
        double z = l.random(); // y is first random number from stream 1, z is second
        l.selectStream(2);
        
        long b = l.getSeed();
        double c = l.random();
        l.putSeed(b);
        double d = l.random();  // c and d hold the same random number
        System.out.println(c + " " + d);

    }
    */
    
}


