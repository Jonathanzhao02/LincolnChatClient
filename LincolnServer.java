import java.net.*;
import java.io.*;

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
	private DiscoveryHandler discovery;
	private int port = 53;
	
	private void start() throws Exception{
		serverSocket = new ServerSocket(port);
		discovery = new DiscoveryHandler(port);
		ClientHandler.setUserList(clients);
		discovery.start();
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
		server.start();
	}
	
}