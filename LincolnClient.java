import java.net.*;
import java.io.*;
import java.util.*;

public class LincolnClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
 
    private void startConnection(String ip, int port) throws Exception{
		System.out.println("Connecting...");
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		System.out.println("Connected");
    }
 
    private String sendMessage(String msg) throws Exception{
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }
 
    private void stopConnection() throws Exception{
        in.close();
        out.close();
        clientSocket.close();
    }
	
	public static void main(String[] args) throws Exception{
		LincolnClient client = new LincolnClient();
		client.startConnection("10.186.46.126", 53);
		String response;
        Scanner input = new Scanner(System.in);
        
        System.out.println("Please enter a username (less than 20 characters)");
        String username = input.nextLine();

        //check for duplicate username as well
        while(invalidUsername(username)){
            username = input.nextLine();
        }
        
        //split into input + output threads
		while(client.clientSocket.isConnected()){
            response = client.sendMessage(username + ": " + input.nextLine());

            //check for valid response (limit length, prevent spam, add delay)

			System.out.println("\tServer: " + response);
		}
		
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

}