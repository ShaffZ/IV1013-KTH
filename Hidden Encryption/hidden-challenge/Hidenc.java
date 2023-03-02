import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.*;
import java.security.*;


public class Hidenc {
    static byte[] byteKey = null;      // key as byte array
    static byte[] ctr = null;          // value of counter as byte array
    static byte[] inFile = null;       // bytes of input file
    static InputStream inputByte = null;
	static FileOutputStream OutputByte = null;
    static int offset = -1;
    static int size = 0;
    static byte[] templateFile = null;
    static InputStream templateByte = null;
    static Cipher cip = null;
    static byte[] hashedData = null;
    static byte[] decBlob = null;
    static byte[] encBlob = null;


    /*
     * Usage: explain how to use the program, then exit with failure status
     */
    private static void usage() {
        System.err.println("Make sure to use this format: Java Hiddec --key=KEY --ctr=CTR --input=INPUT --output=OUTPUT --offset=OFFSET --template=TEMPLATE --size=SIZE");
        System.exit(1);
    }
    
	/*
	 * Parse arguments on command line
	 */
	private static void parse(String[] args) {
		try {

            if (args.length < 4 && args.length > 7 ){
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
                else if (par.equals("--offset")){
                    offset = Integer.parseInt(dat);
                    continue;
                }else if (par.equals("--size")){
                    size = Integer.parseInt(dat);
                    continue;
                }else if (par.equals("--template")){
                    templateByte = new FileInputStream(dat);
                    templateFile = templateByte.readAllBytes();;
                    continue;
                }


            }
            if(byteKey == null || inputByte == null || OutputByte== null){
                System.out.println("Wrong arguments were inserted");
                usage();
            }

            if (size == 0 && templateFile == null){
                System.out.println("One of --size and --template should be given!");
                System.exit(1);
            }
            if (size != 0 && templateFile != null){
                System.out.println("Only one of --size and --template should be given!");
                System.exit(1);
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
                cipher.init(Cipher.ENCRYPT_MODE, decKey);
            }
            else{
                IvParameterSpec ivPar = new IvParameterSpec(ctr);
                cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, decKey, ivPar);
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
    public static byte[] encryptor(byte[] data, Cipher cipher) {


        try {
            return cipher.doFinal(data);
        }
        catch (Exception ex) {
            if(ex.getClass() == IllegalBlockSizeException.class){
                System.out.println("Encryption failed! Data must be multiple of 16!");
                System.exit(1);
            }

            if(ex.getClass() == BadPaddingException.class){
                System.out.println("Encryption failed! Bad padded data!");
                System.exit(1);
            }
        }
        return null;
    }

    static byte[] tempMaker(int size){
        if (templateFile == null){
            templateFile = new byte[size];
            new Random().nextBytes(templateFile);
            return templateFile;
        }
        else{
            return templateFile;
        }
    }


    static byte[] chunkMaker(byte[] hashedKey){
        byte[] hashedData = hasher(inFile);
        decBlob = new byte[2*hashedKey.length + inFile.length + hashedData.length];
        inserter(decBlob, hashedKey, 0);
        inserter(decBlob, inFile, hashedKey.length);
        inserter(decBlob, hashedKey, hashedKey.length + inFile.length);
        inserter(decBlob, hashedData, 2*hashedKey.length + inFile.length);
        return decBlob;
    }


    static void inserter(byte[] main, byte[] in, int start){
        for(int i = start; i < in.length; i++){
            main[i] = in[i-start];
        }
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
        chunkMaker(hashKey);
        Cipher cip = cipherMaker(ctr);
        encBlob = encryptor(decBlob, cip);
        byte[] template = tempMaker(size);

        if(offset == -1){
            int rest = new Random().nextInt(template.length -encBlob.length);
            if(rest % 16 == 0){
                offset = rest;
            }
            else{
                offset = rest - (rest % 16); 
            }
        }
        for (int i = 0; i < encBlob.length; i++) {
            template[i + offset] = encBlob[i];
        }


        try {
            OutputByte.write(template);
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

