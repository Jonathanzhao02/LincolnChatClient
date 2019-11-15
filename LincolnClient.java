import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

//Introduce UI for textfield + other
public class LincolnClient extends Application{
    private Socket clientSocket;
    private String username = "";
 
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage mainStage){

        try{
            startClient();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    private void startConnection(String ip, int port) throws Exception{
		System.out.println("Connecting...");
        clientSocket = new Socket(ip, port);
		System.out.println("Connected");
    }
 
    private void stopConnection() throws Exception{
        clientSocket.close();
    }
	
	private void startClient() throws Exception{
        Scanner input = new Scanner(System.in);
        startConnection("10.186.42.222", 53);
        System.out.println("Enter 'exit' to exit");
        System.out.println("Please enter a username (less than 20 characters)");
        int tries = 0;

        //check for duplicate username as well
        //make serverside checking
        //assign random username
        while(invalidUsername(username)){

            if(tries != 0){
                System.out.println("Invalid username");
            }

            username = input.next();
            tries++;
        }

        Thread inputThread = 
        new Thread(() -> {
            PrintWriter out = null;

            try{
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.write(username);
            } catch(Exception e){
                System.out.println("Writer could not be initialized.");
            }

            String response;

            //constantly send messages to server
            while(clientSocket.isConnected()){
                response = input.nextLine();
                //check for valid response (limit length, prevent spam, add delay)
                out.println(response);
            }

        });

        Thread outputThread = 
        new Thread(() -> {
            BufferedReader in = null;

            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch(Exception e){
                System.out.println("Reader could not be initialized.");
            }

            String output;

            //constantly read from output stream
            while(clientSocket.isConnected()){

                try{
                    output = in.readLine();

                    if(output != null){
                        System.out.println(output);
                    }

                } catch(Exception e){
                    System.out.println("Uh oh! Something went wrong.");
                }

            }

        });

        inputThread.start();
        outputThread.start();
        
        while(clientSocket.isConnected()){

        }

        input.close();
		stopConnection();
	}
    
    private static Boolean invalidUsername(String s){
        int currentChar;

        if(s.length() > 20 || s.length() == 0){
            return true;
        }

        for(int i = 0; i < s.length(); i++){
            currentChar = s.charAt(i);

            if(currentChar < 32 || currentChar > 126){
                return true;
            }

        }

        return false;
    }

}