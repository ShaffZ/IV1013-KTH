import java.util.Arrays;
import java.util.Random;


public class MyRandom extends Random {

    long seed; // X0
    int[] S = new int [256];
    byte[] key;

    
    public MyRandom(){
    }

    
    public MyRandom(long seed){
        this.setSeed(seed);
    }


    public void rc4init(){

        for(int i=0; i<256; i++){
            S[i]=i;
        }

        key = (seed + "").getBytes();
        int j = 0;
        int tmp = 0;

        for(int i = 0; i < 256; i++){

            j = (j + S[i] + key[i % key.length]) % 256;
            tmp = S[i];
            S[i] = S[j];
            S[j] = S[tmp];
        }

    }
    
     

    @Override
    public int next(int bit){
        rc4init();

        int i = 0;
        int j = 0;
        int tmp = 0;

        i = (i + 1) % 256;
        j = (j + S[i]) % 256;

        tmp = S[i];
        S[i] = S[j];
        S[j] = S[tmp];

        int r = S[(S[i]+S[j]) % 256];

        return r;
    }

    @Override
    public void setSeed(long seed){ 
        this.seed = seed;
    }
}
