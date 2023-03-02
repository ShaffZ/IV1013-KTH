import java.util.Random;


public class MyRandom extends Random {

    long seed; // X0
    long prime = 0x23407BF97L; // the prime m
    long a = 12348723L; // primative root for the prime
    long b = 76135L; //picking b as a random integer to avoid getting
                //zeroes as psuedorandom if key was zero

    
    public MyRandom(){
    }

    
    public MyRandom(long seed){
        this.setSeed(seed);
    }
    
     
    /**
     *  we & with 1 shifted bit number of times -1 
     *  in order to get rid of the non valid
     *  bytes therefore we can use only func next()
     *  to generate psuedorandom numbers instead of 
     *  nextInt()
     */
    @Override
    public int next(int bit){

        long r = (((a * seed) + b) % prime);
        this.seed = r;
    
        return (int) (r & (1 << bit)-1);
    }

    @Override
    public void setSeed(long seed){ 
        this.seed = seed;
    }
}
