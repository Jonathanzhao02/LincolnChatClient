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
        in.close();
        out.close();
        clientSocket.close();
    }
	
	public static void main(String[] args) throws Exception{
		LincolnClient client = new LincolnClient();
		client.startConnection("10.186.46.126", 53);
        Scanner input = new Scanner(System.in);
        
        System.out.println("Please enter a username (less than 20 characters)");
        String username = input.nextLine();

        //check for duplicate username as well
        //make serverside checking
        //assign random username
        while(invalidUsername(username)){
            username = input.nextLine();
        }

        new ClientInput(client.clientSocket).start();
        new ClientOutput(client.clientSocket, username).start();
        
        while(client.clientSocket.isConnected()){

        }

        input.close();
		client.stopConnection();
	}
    
    private static Boolean invalidUsername(String s){
        int currentChar;

        if(username.length() > 20){
            return false;
        }

        for(int i = 0; i < username.length(); i++){
            currentChar = username.charAt(i);

            if(currentChar < 32 || currentChar > 126){
                return false;
            }

        }

        return true;
    }

    //Writes to server
    private class ClientOutput extends Thread{
        private Socket s;
        private PrintWriter out;
        private String response;

        public ClientOutput(Socket s){
            this.s = s;
            this.out = new PrintWriter(s.getOutputStream(), true);
        }

        public void run(){

            //constantly send messages to server
            while(s.isConnected()){
                response = input.nextLine();
                //check for valid response (limit length, prevent spam, add delay)
                out.println(response);
            }

        }

    }

    //Reads from server
    private class ClientInput extends Thread{
        private Socket s;
        private BufferedReader in;

        public ClientInput(Socket s){
            this.s = s;
            in = new BufferedReader(s.getInputStream());
        }

        public void run(){

            //constantly read from output stream
            while(s.isConnected()){
                System.out.println(in.readLine());
            }

        }

    }

}