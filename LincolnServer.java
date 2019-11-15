import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;

//OTIS:
//Please do the following
/** 
 * Maybe debug username problems/add more validation to it?
 * Improve messaging
 * Slap application layout over this
 * Allow kicking of users
 * Possible profanity filter start
 * Ensure not too many messages from one user within single timeframe
*/

public class LincolnServer {
	private int maxUsers = 10;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private User[] clients = new User[maxUsers];
	private int numClients = 0;
	
	public void start(int port) throws Exception{
		serverSocket = new ServerSocket(port);
		ClientHandler.setUserList(clients);
		System.out.println("Server started");

		while(true){
			clientSocket = serverSocket.accept();

			if(numClients < maxUsers){
				clients[numClients] = new User(clientSocket);
				new ClientHandler(clients[numClients]).start();
				numClients++;
			} else{
				new PrintWriter(clientSocket.getOutputStream(), true).println("Server cannot accept more clients!");
				clientSocket.close();
			}

		}
		
	}
	
	public void stop() throws Exception{
        serverSocket.close();
    }
	
	public static void main(String[] args) throws Exception{
		LincolnServer server = new LincolnServer();
		server.start(53);
	}
	
}

class ClientHandler extends Thread {
	private static User[] userList;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private User client;

	public static void setUserList(User[] list){
		userList = list;
	}
	
	public ClientHandler(User client){
		this.client = client;
		clientSocket = client.getSocket();
	}
	
	public void stopConnection() throws Exception{
        in.close();
        out.close();
        clientSocket.close();
	}

	private Boolean validUsername(String s){
		int currentChar;

        if(s.length() > 20 || s.length() == 0){
            return false;
        }

        for(int i = 0; i < s.length(); i++){
            currentChar = s.charAt(i);

            if(currentChar < 32 || currentChar > 126){
                return false;
            }

        }

        return !checkDuplicate(s);
	}

	private Boolean checkDuplicate(String s){
		String name;

		for(int i = 0; i < userList.length; i++){

			if(userList[i] != null){
				name = userList[i].getUsername();

				if(s.equals(name)){
					return true;
				}

			}

		}

		return false;
	}

	private String randomName(){
		Random r = new Random();
		String newName = "";
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz 1234567890!@#$%^&*()~=-+_{}[]|<>,./?;:";
		int length = alphabet.length();

		while(checkDuplicate(newName) && !validUsername(newName)){
			newName = "";

			for(int i = 0; i < r.nextInt(21); i++){
				newName += alphabet.charAt(r.nextInt(length));
			}

		}

		return newName;
	}
	
	private Boolean validMessage(String s){

		if(s.length() > 0 && s.length() < 200){
			return true;
		} else{
			return false;
		}

	}

	private void sendToAll(String s){

		for(int i = 0; i < userList.length; i++){

			if(userList[i] != null && userList[i] != this.client){
				userList[i].sendMessage(s);
			}

		}

	}

	public void run(){

		try{
			System.out.println("Connected with " + clientSocket.getRemoteSocketAddress().toString());
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String inputLine = "";

			while(inputLine.length() == 0){
				inputLine = in.readLine();
			}

			System.out.println("Username input: " + inputLine);

			if(validUsername(inputLine)){
				client.setUsername(inputLine);
				client.sendMessage("Username accepted. Start chatting!");
				System.out.println("Assigned valid name");
			} else{
				client.setUsername(randomName());
				client.sendMessage("Username not accepted. Random username " + client.getUsername() + " assigned. Start chatting!");
				System.out.println("Assigned random name");
			}

			System.out.println("Client now speaking");
			
			while ((inputLine = in.readLine()) != null) {
				
				if ("exit".equals(inputLine)) {
					out.println("Goodbye");
					break;
				}
				
				if(validMessage(inputLine)){
					sendToAll(client.getUsername() + ": " + inputLine);
				} else{
					out.println("Message not delivered");
					System.out.println("Message not delivered");
				}

				System.out.println(client.getUsername() + ": " + inputLine);
			}

		} catch(Exception E){
			E.printStackTrace();
		} finally{

			if(clientSocket != null){
				System.out.println("Disconnecting with " + clientSocket.getRemoteSocketAddress().toString());

				try{
					stopConnection();
				} catch(Exception E){

				}

				System.out.println("Disconnected");
			}

		}

	}

}