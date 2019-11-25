import java.util.Scanner;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;


public class WordProcessing {
    public static void main(String[] args) {
        Scanner filescanner = generatescanner();

        int filelength = getfilelength();
 
        String resultsstring;
        String[] swears = new String[filelength];

        for (int i = 0; i < filelength; i++) {
            resultsstring = filescanner.nextLine();
            System.out.println("Read string " + resultsstring);
            swears[i] = resultsstring;
            System.out.println("Processed swear " + resultsstring);
        }


        try {
            ObjectOutputStream filewriter = new ObjectOutputStream(new FileOutputStream("words.jobj"));
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
        File file = new File("words.txt");

        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.print("File not found\n");
            return new Scanner(System.in);
        }
    }


}