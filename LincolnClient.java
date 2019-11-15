import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class LincolnClient {
    private Socket clientSocket;
 
    private void startConnection(String ip, int port) throws Exception{
		System.out.println("Connecting...");
        clientSocket = new Socket(ip, port);
		System.out.println("Connected");
    }
 
    private void stopConnection() throws Exception{
        clientSocket.close();
    }
	
	public static void main(String[] args) throws Exception{
        Scanner input = new Scanner(System.in);
		LincolnClient client = new LincolnClient();
        //client.startConnection("10.186.46.126", 53);
        client.startConnection("localhost", 53);
        System.out.println("Enter 'exit' to exit");
        System.out.println("Please enter a username (less than 20 characters)");
        String username = "";
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

        ClientInput inputThread = new ClientInput(client.clientSocket);
        ClientOutput outputThread = new ClientOutput(client.clientSocket, input, username);
        inputThread.start();
        outputThread.start();
        
        while(client.clientSocket.isConnected()){

        }

        input.close();
		client.stopConnection();
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

//Writes to server
class ClientOutput extends Thread{
    private Socket s;
    private PrintWriter out;
    private String response;
    private Scanner in;

    public ClientOutput(Socket s, Scanner in, String username){
        this.s = s;
        this.in = in;

        try{
            this.out = new PrintWriter(s.getOutputStream(), true);
            out.write(username);
        } catch(Exception e){
            System.out.println("Writer could not be initialized.");
        }

    }

    public void run(){

        //constantly send messages to server
        while(s.isConnected()){
            response = in.nextLine();
            //check for valid response (limit length, prevent spam, add delay)
            out.println(response);
        }

    }

}

//Reads from server
class ClientInput extends Thread{
    private Socket s;
    private BufferedReader in;

    public ClientInput(Socket s){
        this.s = s;

        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch(Exception e){
            System.out.println("Reader could not be initialized.");
        }

    }

    public void run(){
        String output;

        //constantly read from output stream
        while(s.isConnected()){

            try{
                output = in.readLine();

                if(output != null){
                    System.out.println(output);
                }

            } catch(Exception e){
                System.out.println("Uh oh! Something went wrong.");
            }

        }

    }

}