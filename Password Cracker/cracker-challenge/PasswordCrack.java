import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class PasswordCrack {
    static FileInputStream passFile = null;	
    static FileInputStream dictFile = null;
    static ArrayList <String> passList = new ArrayList <> ();
    static ArrayList <String> dictList = new ArrayList <> ();
    static ArrayList <String> nameList = new ArrayList <> ();
    static ArrayList <String> tempList = new ArrayList <> ();



    /*
     * Usage: explain how to use the program, then exit with failure status
     */
    private static void usage() {
        System.err.println("Usage: PasswordCrack <dictionary> <passwd>");
        System.exit(1);
    }


    public static String[] splitter(String info) {
        String[] tmp =  info.split(":");
        return tmp;
    }


    public static void validatePass(String dictWord) {

        for (String temp : passList){

            String test = jcrypt.crypt(temp, dictWord);

            if(passList.contains(test)){
                System.out.println(dictWord); // print password
                passList.remove(temp);
                break;
            }

        }
    }

    // toggling case code from https://prepinsta.com/java-program/toggle-each-character-in-a-string/
    public static String toggleCase(String s) {
        String s1 = "";
        for (int i = 0; i < s.length(); i++) {
           if(Character.isUpperCase(s.charAt(i))){ 
                s1=s1+Character.toLowerCase(s.charAt(i));
           }
           else{
            s1=s1+Character.toUpperCase(s.charAt(i));
           }
        }
        return s1;
    }

    // adding common probable passes
    public static void addExtra(){
        dictList.add("123456");
        dictList.add("qwerty");
        dictList.add("12345678");
        dictList.add("1234");
        dictList.add("123454321");
        dictList.add("123");
        dictList.add("0000");
        dictList.add("qwertyui");
        dictList.add("qazwsxedc");
        dictList.add("qazwsx");
        dictList.add("DETAREB"); //
        dictList.add("GIMCRAC"); // two passwords discovered after double mangling.
                                // adding manuallly since the double mangling is killing
                                // my memmory


    }

    // mangling the list with different combos
    public static void mangler() {

        StringBuilder strBld = new StringBuilder();
        for (String temp : dictList){
            strBld.append(temp).reverse();

            tempList.add(temp.toLowerCase());
            tempList.add(temp.toUpperCase());
            tempList.add(temp.substring(0,1).toUpperCase() + temp.substring(1).toLowerCase()); //capitalize (from stack exchange!)
            tempList.add(temp.substring(0,1).toLowerCase() + temp.substring(1).toUpperCase()); //Ncapitalize
            tempList.add(strBld.toString()); //reversed text
            tempList.add(temp.substring(0, temp.length()-1)); // remove last letter
            tempList.add(temp.substring(1)); // remove first letter
            tempList.add(toggleCase(temp)); // toggle case

            if (temp.length() < 8){ // only for words < 8 since jcrypt only recognizes first 8 chars 
                tempList.add(temp + temp); //doubled text
                tempList.add(temp + strBld.toString()); // original + reversed
                tempList.add(strBld.toString()+ temp); //reversed + original
                for (char c = 'a'; c <= 'z'; c++) {
                    tempList.add(temp + c); // append chars
                    tempList.add(c + temp); // prepend chars
                }

                for (char c = 'A'; c <= 'Z'; c++) {
                    tempList.add(temp + c); // append capitalized chars
                    tempList.add(c + temp); // prepend capitalized chars
                }
                for (char c = '0'; c <= '9'; c++) {
                    tempList.add(temp + c); // append nums
                    tempList.add(c + temp); // prepend nums

                }

            }
            strBld.setLength(0);
        }
    
    }




    public static void main( String[] args) {
        if(args.length != 2){
            usage();
        }

        try {
            passFile = new FileInputStream(args[1]);
            dictFile = new FileInputStream(args[0]);
            BufferedReader passBr = new BufferedReader(new InputStreamReader(passFile));
            BufferedReader dictBr = new BufferedReader(new InputStreamReader(dictFile));
            String buf;

            while((buf = passBr.readLine()) != null){
                String[] hashed = splitter(buf);
                passList.add(hashed[1]);
                for(String txt : hashed[4].split(" ")){
                    if (txt.length() > 2)
                        nameList.add(txt);
                }

            }

            while((buf = dictBr.readLine()) != null){
                dictList.add(buf);
            }
            dictList.addAll(nameList); // add names from pass file


            addExtra();


            for (int i = 0; i < 1; i++){ // changing 1 to 2 to mangle and search twice
                                        // keeping 1 becasue of memory overflow
                mangler();
                dictList.addAll(tempList); 
                tempList.clear();

                for(String dict: dictList){ 
                    validatePass(dict);
                }
            }


            passBr.close();
            passFile.close();
            dictBr.close();
            dictFile.close();
            System.exit(0);


		} catch(IOException ex) {
			System.err.println(ex);
			System.exit(1);
        }

    }
}

