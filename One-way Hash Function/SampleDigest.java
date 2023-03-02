import java.security.*;

// ska behöva vänta tills kompleteringen
public class SampleDigest {

    private static void usage() {
        System.err.println("Usage: StreamCipher <key> <infile> <outfile>");
        System.exit(1);
    }

    
    private static void parse(String[] args) {
		try {

            if (args.length != 2 ){
                usage();
            }

            //key = Long.parseLong(args[0]);
            inputByte = new FileInputStream(args[1]);
            OutputByte = new FileOutputStream(args[2]);


		} catch (FileNotFoundException | NumberFormatException ex) {
				// Exception handling and displaying relevant error messages
				// tell user how to use the program

                if(ex.getClass() == NumberFormatException.class ){
                    System.out.println("The key is not valid!! Please use a valid integer key");
                }

                else if(ex.getClass() == FileNotFoundException.class){
                    System.out.println("The input file was not found!");
                }
				usage();
		}
	}



    public static void main(String[] args) {

        parse(args);
        String digestAlgorithm = "SHA-256";
        String textEncoding = "UTF-8";
        String inputText = "Test message 1";
        String inputText2 = "Test message 2";

        try {

            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            byte[] inputBytes = inputText.getBytes(textEncoding);
            md.update(inputBytes);
            byte[] digest = md.digest();
            printDigest(inputText, md.getAlgorithm(), digest);
            byte[] inputBytes2 = inputText2.getBytes(textEncoding);
            md.update(inputBytes2);
            byte[] digest2 = md.digest();
            printDigest(inputText2, md.getAlgorithm(), digest2);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algorithm \"" + digestAlgorithm  + " \" is not available");
        } catch (Exception e) {
            System.out.println("Exception "+e);
        }
    }
    public static void printDigest(String inputText, String algorithm, byte[] 
digest) {
        System.out.println("Digest for the file \"" + inputText +"\", using " + 
algorithm + " is:");
        for (int i=0; i<digest.length; i++)
            System.out.format("%02x", digest[i]&0xff);
        System.out.println();
    }
}