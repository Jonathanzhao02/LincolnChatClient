import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;
class SwearTester {
    public static void main(String[] args) {
        String[] swears;
        String[] words;
        Scanner input = new Scanner(System.in);
        try {
            ObjectInputStream filereader = new ObjectInputStream(new FileInputStream("swears.jobj"));

            swears = (String[]) filereader.readObject();
            // Reads the array of primes from a file

            filereader.close();
        } catch (IOException e) {
            swears = new String[0];
        } catch (ClassNotFoundException e) {
            swears = new String[0];
            // Make sure to catch these and do nothing
        }
        try {
            ObjectInputStream filereader = new ObjectInputStream(new FileInputStream("words.jobj"));

            words = (String[]) filereader.readObject();
            // Reads the array of primes from a file

            filereader.close();
        } catch (IOException e) {
            words = new String[0];
        } catch (ClassNotFoundException e) {
            words = new String[0];
            // Make sure to catch these and do nothing
        }


        while (true) {
            print(swearFilter(input.nextLine(), swears, words));
        }
    }

    private static String swearFilter(String string, String[] swearlist, String[] wordlist) {
        int arrayindex = 0;
        String tempstring = "";
        String tempstring2 = "";
        LinkedList<String> swearscontained = new LinkedList<String>();
        int startindex = 0;
        int endindex = 0;

        Random random = new Random();

        ArrayList<LinkedList<Object>> result = new ArrayList<LinkedList<Object>>(3);
        String resultstring = "";
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
                    if (arrayindex + 2 > modifiables.size()) {
                        break; //I'm sorry Mr. Tinling
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
        for (int i = 0; i < modifiables.size(); i++) {
            tempstring += (String) modifiables.get(i);
        }
        for (int i = 0; i < swearlist.length; i++) {
            if (tempstring.contains(swearlist[i])) {
                swearscontained.add(swearlist[i]);
            }
        }
        tempstring = "";
        for (int i = 0; i < modifiables.size(); i++) {
            tempstring += (String) modifiables.get(i);
            if (!((String) modifiables.get(i)).equals("")) {
                for (int j = 0; j < swearscontained.size(); j++) {
                    if (tempstring.endsWith(swearscontained.get(j))) {
                        tempstring2 = "";
                        endindex = i + 1;
                        startindex = endindex;
                        while (!(swearscontained.get(j).equals(tempstring2))) {
                            startindex--;
                            tempstring2 = (String) modifiables.get(startindex) + tempstring2;
                        }
                        for (int k = startindex; k < endindex; k++) {
                            swears.set(k, true);
                        }
                    }
                }
            }
        }

        // Replace swears (only for filter)
        arrayindex = 0;
        while (arrayindex < swears.size() - 1) {
            // If same character found, consume all characters
            if ((Boolean) swears.get(arrayindex) && (Boolean) swears.get(arrayindex + 1)) {

                
                while  ((Boolean) swears.get(arrayindex) && (Boolean) swears.get(arrayindex + 1)){
                    merge(result, arrayindex, arrayindex + 1);
                    if (arrayindex + 2 > swears.size()) {
                        break; //I'm sorry Mr. Tinling
                    }
                }


                modifiables.set(arrayindex,((String) modifiables.get(arrayindex)).substring(0,1));
            }
            arrayindex++;
        }

        for (int i = 0; i < swears.size(); i++) {
            if ((Boolean) swears.get(i)) {
                sourcemap.set(i, wordlist[random.nextInt(wordlist.length)]);
            }
        }

        tempstring = "";
        for (int i = 0; i < sourcemap.size(); i++) {
            tempstring += (String) sourcemap.get(i);
        }

        return tempstring;
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

    private static void print(String string) {
        System.out.println(string);
    }
}