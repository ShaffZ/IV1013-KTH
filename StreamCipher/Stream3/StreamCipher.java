import java.io.*;
import java.util.Random;


public class StreamCipher {
	static Long key = null;			     // Key
	static FileInputStream inputByte = null;			     // input ether as plaintext or ciphertext
	static FileOutputStream OutputByte = null;			     // output ether as plaintext or ciphertext

    /*
     * Usage: explain how to use the program, then exit with failure status
     */
    private static void usage() {
        System.err.println("Usage: StreamCipher <key> <infile> <outfile>");
        System.exit(1);
    }

	/*
	 * Parse arguments on command line
	 */
	private static void parse(String[] args) {
		try {

            if (args.length != 3 ){
                usage();
            }

            key = Long.parseLong(args[0]);
            inputByte = new FileInputStream(args[1]);
            OutputByte = new FileOutputStream(args[2]);


		} catch (FileNotFoundException | NumberFormatException ex) {
				// Exception handling and displaying relevant error messages
				// tell user how to use the program

                if(ex.getClass() == NumberFormatException.class ){
                    System.out.println("The key is not valid!! Please use a valid integer key");
                    System.exit(1);

                }

                else if(ex.getClass() == FileNotFoundException.class){
                    System.out.println("The input file was not found!");
                    System.exit(1);
                }
				usage();
		}
	}

    /*
     * Main program. Parse arguments on command line and run the cipher program
     */
    public static void main( String[] args) {

        parse(args);
        MyRandom prng = new MyRandom(key);
        int buf = 0;

		try {

            while((buf = inputByte.read()) != -1){
                OutputByte.write(buf ^ prng.next(8));
            }

            inputByte.close();
            OutputByte.close();
            System.exit(0);

		} catch(IOException ex) {
			System.err.println(ex);
			System.exit(1);
        }

	}
}

