import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;

//OTIS:
//Please do the following
/** 
 * Maybe debug username problems/add more validation to it? (WORKING?)
 * Improve messaging in some way
 * Allow kicking of users (perhaps using javafx application layout)
 * Possible profanity filter start (WORKING)
 * Ensure not too many messages from one user within single timeframe (DONE)
*/

public class LincolnServer {
	private int maxUsers = 100;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private User[] clients = new User[maxUsers];
	
	private void start(int port) throws Exception{
		serverSocket = new ServerSocket(port);
		ClientHandler.setUserList(clients);
		System.out.println("Server started");

		while(true){
			clientSocket = serverSocket.accept();


			tryAdd(clientSocket);
			// I turned off already connected testing cause it was
			// annoying to test with. We should still reenable it
			// once we start actually running this.
			//if(!alreadyConnected(clientSocket)){
			//	tryAdd(clientSocket);
			//} else{
				
				//try{
				//	new PrintWriter(clientSocket.getOutputStream(), true).println("Already connected on your device!");
				//	clientSocket.close();
				//} catch(Exception e){
				//	
				//}

			//}

		}
		
	}

	private Boolean alreadyConnected(Socket s){
		InetAddress ipv4 = s.getInetAddress();

		for(int i = 0; i < clients.length; i++){

			if(clients[i] != null && ipv4.equals(clients[i].getSocket().getInetAddress())){
				return true;
			}

		}

		return false;
	}

	private void tryAdd(Socket s){
		Boolean added = false;

		try{
			for(int i = 0; i < maxUsers; i++){

				if(clients[i] == null || !clients[i].getSocket().isConnected()){
					clients[i] = new User(s);
					new ClientHandler(clients[i]).start();
					i = maxUsers;
					added = true;
				}

			}

			if(!added){
				new PrintWriter(s.getOutputStream(), true).println("Server cannot accept more clients!");
				s.close();
			}

		} catch(Exception e){

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

	public static void removeUser(User u){

		for(int i = 0; i < userList.length; i++){

			if(userList[i] == u){
				userList[i] = null;
			}

		}

	}
	
	public ClientHandler(User client){
		this.client = client;
		clientSocket = client.getSocket();
	}
	
	public void stopConnection() throws Exception{
        in.close();
        out.close();
		clientSocket.close();
		ClientHandler.removeUser(this.client);
	}

	private Boolean validUsername(String s){
		boolean[] checks = new boolean[4];
		checks[0] = checkWordLength(s,20);
		checks[1] = checkSwears(s);
		checks[2] = checkCharRange(s, 32, 126);
		checks[3] = checkDuplicate(s); //Checks duplicate usernames

		boolean result = true;

		for (int i = 0; i < checks.length; i++) {
			if (!checks[i]) {
				result = false;
				//returns false if any checks fail, true in any other situation
			}
		}

		return result;
	}

	private Boolean checkDuplicate(String s){
		String name;

		for(int i = 0; i < userList.length; i++){

			if(userList[i] != null){
				name = userList[i].getUsername();

				if(s.equals(name)){
					return false; // Inverted this because checks should show if pass (no dup) or fail (dup)
				}

			}

		}

		return true;
	}

	private boolean checkCharRange(String checkString, int minChar, int maxChar) {
		char currentChar;
		//Checks if a string has all characters in a certain range
        for(int i = 0; i < checkString.length(); i++){
            currentChar = checkString.charAt(i);

            if(currentChar < minChar || currentChar > maxChar){
                return false;
            }

		}
		return true;
	}

	private String randomName(){
		Random r = new Random();
		String newName = "";
		int randomint = 5;
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz 1234567890!@#$%^&*()~=-+_{}[]|<>,./?;:";
		int length = alphabet.length();

		while(!validUsername(newName)){
			newName = "";
			randomint = r.nextInt(21); //I fixed the random username code


			for(int i = 0; i < randomint; i++){
				newName += alphabet.charAt(r.nextInt(length));
			}

		}

		return newName;
	}
	
	private Boolean validMessage(String s){
		//Running through all checks
		boolean[] checks = new boolean[3]; //Number of checks = 3
		checks[0] = checkMessageTime(s, client);
		checks[1] = checkWordLength(s, 200);
		checks[2] = checkSwears(s);

		boolean result = true;

		for (int i = 0; i < checks.length; i++) {
			if (!checks[i]) {
				result = false;
				//returns false if any checks fail, true in any other situation
			}
		}

		return result;
	}

	private static boolean checkMessageTime(String checkString, User user) {

		if ( new Date().getTime() - user.getTime() > 500) {
			//Prevents more than 3 messages per second
			//To change this modify 500 to 1000/desired messages per second
			return true;
		}
		else {
			return false;
		}
	}

	private static boolean checkWordLength (String checkString, int maxlength) {
		if(checkString.length() > 0 && checkString.length() < maxlength){
			return true;
		}
		else {
			return false;
		}
	}

	private static boolean checkSwears(String checkString) {
		String[] forbidden = {"fuck","shit","bitch","ass"}; //Feel free to add more

		for (int i = 0; i < forbidden.length; i++) {
			if (checkString.contains(forbidden[i])) {
				return false;
			}
		}
		return true;
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

			while(inputLine.length() == 0 && clientSocket.isConnected() && !clientSocket.isClosed()){
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
			
			while (clientSocket.isConnected() && !clientSocket.isClosed() && (inputLine = in.readLine()) != null) {
				
				if ("exit".equals(inputLine)) {
					out.println("Goodbye");
					break;
				}
				
				if(validMessage(inputLine)){
					client.sent();
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