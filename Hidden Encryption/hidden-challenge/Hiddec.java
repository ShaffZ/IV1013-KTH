import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import java.security.*;


public class Hiddec {
    static byte[] byteKey = null;      // key as byte array
    static byte[] ctr = null;          // value of counter as byte array
    static byte[] inFile = null;       // bytes of input file
    static InputStream inputByte = null;
	static FileOutputStream OutputByte = null;
    static int firstIndex;
    static int secondIndex;
    static Cipher cip = null;
    static byte[] encryptedData = null;
    static byte[] decryptedData = null;
    static byte[] hashedData = null;


    /*
     * Usage: explain how to use the program, then exit with failure status
     */
    private static void usage() {
        System.err.println("Make sure to use this format: Java Hiddec --key=KEY --ctr=CTR --input=INPUT --output=OUTPUT");
        System.exit(1);
    }
    
	/*
	 * Parse arguments on command line
	 */
	private static void parse(String[] args) {
		try {

            if (args.length < 3 && args.length > 4 ){
                usage();
            }

            for (String arg: args){
                String par = arg.split("=")[0];
                String dat = arg.split("=")[1];

                if (arg.split("=").length != 2){
                    System.out.println("Wrong argument was inserted");
                    usage();
                }
                else if (par.equals("--key")){
                    byteKey = decodeHexString(dat);
                    continue;
                }
                else if (par.equals("--ctr")){
                    ctr = decodeHexString(dat);
                    continue;
                }
                else if (par.equals("--input")){
                    inputByte = new FileInputStream(dat);
                    inFile = inputByte.readAllBytes();;
                    continue;
                }
                else if (par.equals("--output")){
                    OutputByte = new FileOutputStream(dat);
                    continue;
                }

            }
            if(byteKey == null || inputByte == null || OutputByte== null){
                System.out.println("Wrong arguments were inserted");
                usage();
            }


		} catch (Exception ex) {
				// Exception handling and displaying relevant error messages
				// tell user how to use the program
                if(ex.getClass() == ArrayIndexOutOfBoundsException.class){
                    System.out.println("Wrong arguemnts inserted");
                    usage();
                }

                if(ex.getClass() == FileNotFoundException.class){
                    System.out.println("The input file was not found!");
                    System.exit(1);
                }
				usage();
		}
	}

    //Code from geeks to geeks https://www.geeksforgeeks.org/java-program-to-convert-hex-string-to-byte-array/
    public static byte[] decodeHexString(String s) {
        
        int len = s.length();
        byte[] byteArr = new byte[len / 2];
       
        for (int i = 0; i < len; i += 2) {
             // using left shift operator on every character
             byteArr[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return byteArr;
    }

    /**
     * Creates a cipher object of type AES ctr or AES ecb
     */
    public static Cipher cipherMaker(byte[] ctr) {
        Cipher cipher = null;
        Key decKey = new SecretKeySpec(byteKey,"AES");
        try{
            if(ctr == null){          
                cipher = Cipher.getInstance("AES/ECB/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, decKey);
            }
            else{
                IvParameterSpec ivPar = new IvParameterSpec(ctr);
                cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, decKey, ivPar);
            }
        }catch(Exception ex){

            System.out.println("Error while creating a cipher object!!");
            System.exit(1);
        }
        return cipher;
    }

    /**
     * Updates the cipher with data to be decrypted
     */
    public static byte[] decryptor(byte[] chunk, Cipher cipher) {
        return cipher.update(chunk); 
    }


    
    /**
     * indexer to find matching blocks with hashed key
     */
    public static int indexer(List<byte[]> chunks, byte[] hashKey, int start, Cipher cip) {
        int index = start;
        for(int i = start/hashKey.length; i < chunks.size(); i++ ){
            byte[] tmp = decryptor(chunks.get(i), cip);
            for(int j = 0 ; j < hashKey.length; j++){
                if(chunks.get(j).length != hashKey.length || tmp[j] != hashKey[j]){
                    index += hashKey.length;
                    break;
                }
                if(j == hashKey.length-1){
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     * the ctr indexer resets the counter with each itteration
     */
    public static int ctrIndexer(List<byte[]> chunks, byte[] hashKey, int start) {
        int index = start;

        for(int i = start/hashKey.length; i < chunks.size(); i++ ){
            cip = cipherMaker(ctr); // resetting counter every loop
            byte[] tmp = decryptor(chunks.get(i), cip);
            for(int j = 0 ; j < hashKey.length; j++){
                if(chunks.get(j).length != hashKey.length || tmp[j] != hashKey[j]){
                    index += hashKey.length;
                    break;
                }
                if(j == hashKey.length-1){
                    return index;
                }
            }
        }
        return -1;
    }


    public static boolean verifyData(byte[] myHashedData, int index) {

        decryptor(Arrays.copyOfRange(inFile, index - byteKey.length, index),cip); // increasing the counter for ctr mode
        byte[] encAlreadyHashed = Arrays.copyOfRange(inFile, index, index + byteKey.length); //Extract block after second H(K)
        byte[] decAlreadyHashed = decryptor(encAlreadyHashed, cip); // decrypt block after second H(K)

        for(int i = 0 ; i < myHashedData.length; i++){
            if(decAlreadyHashed.length != myHashedData.length || decAlreadyHashed[i] != myHashedData[i]){
                return false;
            }
        }
        return true;
    }

    /**
     * splitting data into chunks
     */
    public static List<byte[]> chunker(byte[] file, int size) {
        int begining = 0;
        List<byte[]> chunks = new ArrayList<byte[]>();
        while (begining < file.length) {
            int end = Math.min(file.length, begining + size);
            chunks.add(Arrays.copyOfRange(file, begining, end));
            begining += size;
        }
        return chunks;
    }


    /**
     * Hashing function copied from SampleDigest.java provided by the teachers of the course 
     */
    public static byte[] hasher(byte[] data) {

        String digestAlgorithm = "MD5";
        MessageDigest md;
        byte[] digest = null;

        try {
            md = MessageDigest.getInstance(digestAlgorithm);
            md.update(data);
            digest = md.digest();
        }

        catch (Exception e) {
            System.out.println("Exception "+e);
        }
        return digest;
    }

    /*
     * Main program. Parse arguments on command line and run the cipher program
     */
    public static void main( String[] args) {

        parse(args);
        byte[] hashKey = hasher(byteKey);
        List<byte[]> chunks = chunker(inFile, hashKey.length);

        if(ctr == null){
            cip = cipherMaker(ctr);
            firstIndex =indexer(chunks, hashKey,0, cip);
            secondIndex =indexer(chunks, hashKey,firstIndex + hashKey.length, cip);
        }
        else{
            firstIndex =ctrIndexer(chunks, hashKey,0);
            secondIndex =indexer(chunks, hashKey,firstIndex + hashKey.length, cip);
        }

        if (firstIndex == -1 || secondIndex == -1){
            System.out.println("hashed key not found in data!");
            System.exit(1);
        }

        encryptedData = Arrays.copyOfRange(inFile, firstIndex + hashKey.length, secondIndex); // copying data after index
        ctrIndexer(chunks, hashKey,0); // resetting the counter becasue it was incremented during index search
        decryptedData = decryptor(encryptedData, cip); //decrypting extracted data
        hashedData = hasher(decryptedData);

        if(!verifyData(hashedData, secondIndex + byteKey.length)){ 
            System.out.println("Decrypted data not matching data cheksum!!");      
        }
        try {
            OutputByte.write(decryptedData);
        } catch (IOException e) {
            System.out.println("Error while writing data to file!!");
            System.exit(1);
        }

		try {
            inputByte.close();
            OutputByte.close();
            System.exit(0);

		} catch(IOException ex) {
			System.err.println("Error while closing data stream!!");
			System.exit(1);
        }

	}
}

