import java.util.LinkedList;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

// EXAMPLE OF HOW THE ALGORITHM WORKS
//            |f u c k - w a d --> f u c k w a d --> f u c Kk w a d --> fuck w a d --> fuck w a d               |
//fuck-wad    |f u c k - w a d --> f u c k- -w a d --> f u c k- -w a d --> fuck- -w a d --> {randomword} -w a d| randomword-wad
//            |f f f f f f f f --> f f f f   f f f --> t t t t   f f f --> t      f f f --> f             f f f|

public class SwearProcessing {
    public static void main(String[] args) {
        Scanner filescanner = generatescanner();

        int filelength = getfilelength();
 
        String resultsstring;
        String[] swears = new String[filelength];

        
        ArrayList<LinkedList<Object>> results;


        for (int i = 0; i < filelength; i++) {
            resultsstring = filescanner.nextLine();
            System.out.println("Read string " + resultsstring);
            results = processstring(resultsstring);
            resultsstring = "";
            for (int j = 0; j < results.get(0).size(); j++) {
                resultsstring += (String) results.get(0).get(j);
            }
            swears[i] = resultsstring;
            System.out.println("Processed swear " + resultsstring);
        }


        try {
            ObjectOutputStream filewriter = new ObjectOutputStream(new FileOutputStream("swears.jobj"));
            filewriter.writeObject(swears);
            // Writes the array of primes to a file
            filewriter.close();
        } catch (IOException e) {

        } finally {
            // Make sure to catch these and do nothing
        }
    }

    private static int getfilelength() {
        Scanner scanner = generatescanner();
        int result = 0;

        while (scanner.hasNextLine()) {
            result++;
            scanner.nextLine();
        }

        return result;
    }
    
    private static Scanner generatescanner() {
        File file = new File("swears.txt");

        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.print("File not found\n");
            return new Scanner(System.in);
        }
    }

    private static ArrayList<LinkedList<Object>> processstring(String string) {
        int arrayindex = 0;

        ArrayList<LinkedList<Object>> result = new ArrayList<LinkedList<Object>>(3);
        // Index 1: Each character of the string as it slowly gets reduced
        // Index 2: The sourcemap of the modified string to the original
        // Index 3: If the specified part of the sourcemap triggered a swear alert
        // This uses a lot of down and up casting

        LinkedList<Object> modifiables = new LinkedList<Object>();
        LinkedList<Object> sourcemap = new LinkedList<Object>();
        LinkedList<Object> swears = new LinkedList<Object>();

        result.add(modifiables);
        result.add(sourcemap);
        result.add(swears);

        // IMPORTANT - must load sourcemap before converting to lowercase
        char[] stringaschars = string.toCharArray();
        for (int i = 0; i < stringaschars.length; i++) {
            sourcemap.add(Character.toString(stringaschars[i]));
        }

        // Pre-processing
        string = string.toLowerCase();

        stringaschars = string.toCharArray();

        for (int i = 0; i < stringaschars.length; i++) {
            modifiables.add(Character.toString(stringaschars[i]));
        }

        for (int i = 0; i < stringaschars.length; i++) {
            swears.add(false);
            // True if being used in the swear word processor (just for semantics)
        }

        // Process the string and change numbers to letters, or any other substitutions
        // 0:O 1:I 2:Z 3:e 4:A 5:S 6:G 7:T 8:b 9:g 
        for (int i = 0; i < modifiables.size(); i++) {
            switch((String) modifiables.get(i)) {
                case "0":
                    modifiables.set(i,"o");
                    break;
                case "1":
                    modifiables.set(i,"i");
                    break;
                case "2":
                    modifiables.set(i,"z");
                    break;
                case "3":
                    modifiables.set(i,"e");
                    break;
                case "4":
                    modifiables.set(i,"a");
                    break;
                case "5":
                    modifiables.set(i,"s");
                    break;
                case "6":
                    modifiables.set(i,"g");
                    break;
                case "7":
                    modifiables.set(i,"t");
                    break;
                case "8":
                    modifiables.set(i,"b");
                    break;
                case "9":
                    modifiables.set(i,"g");
                    break;
            }
        }

        //Process the string and remove any duplicate characters
        while (arrayindex < modifiables.size() - 1) {
            // If same character found, consume all characters
            if (((String) modifiables.get(arrayindex)).charAt(0) == ((String) modifiables.get(arrayindex + 1)).charAt(0)) {

                
                while (((String) modifiables.get(arrayindex)).charAt(0) == ((String) modifiables.get(arrayindex + 1)).charAt(0)) {
                    merge(result, arrayindex, arrayindex + 1);
                    if (arrayindex < modifiables.size()) {
                        break;
                    }
                }


                modifiables.set(arrayindex,((String) modifiables.get(arrayindex)).substring(0,1));
            }
            arrayindex++;
        }


        // Process the string and remove any non-letters (merge them using algorithm in the sourcemap)
        // set the non-letters modifiable map to "", but maintain their sourcemap
        for (int i = 0; i < modifiables.size(); i++) {
            if (!(96 < (int) ((String) modifiables.get(i)).charAt(0) && (int) ((String) modifiables.get(i)).charAt(0) < 123)) {
                modifiables.set(i,"");
            }
            //Get the modifiable as a character and see if it is a letter
        }        

        
        // Process swears (only for filter)

        // Replace swears (only for filter)


        return result;
    }

    private static void merge(ArrayList<LinkedList<Object>> result, int index1, int index2) {
        // Most of the time these should be sequential but who knows

        // Merge modifiable entry from index2 to index1 (append)
        result.get(0).set(index1, (String) result.get(0).get(index1) + (String) result.get(0).get(index2));
        // Merge sourcemap entry from index2 to index1 (append)
        result.get(1).set(index1, (String) result.get(1).get(index1) + (String) result.get(1).get(index2));
        // Merge swear entry using or (if either is true, make both true)
        if ((Boolean) result.get(2).get(index2)) {
            result.get(2).set(index1, true);
        }
        // Delete index2
        for (int i = 0; i < 3; i++) {
            result.get(i).remove(index2);
        }
    }

}