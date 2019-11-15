import java.net.*;
import java.io.*;
import java.util.concurrent.*;

//OTIS:
//Please do the following
/** 
 * Validate username
 * Check for duplicates
 * Validate message
 * Ensure not too many messages from one user within single timeframe
 * Send messages to every User object except the one sending containing username + ": " + message
*/

public class LincolnServer {
	private int maxUsers = 10;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Socket[] clients = new Socket[maxUsers];
	private String[] usernames = new String[maxUsers];
	private int numClients = 0;
	
	public void start(int port) throws Exception{
		serverSocket = new ServerSocket(port);
		System.out.println("Server started");

		while(true){
			clientSocket = serverSocket.accept();

			if(numClients < maxUsers){
				clients[numClients] = clientSocket;
				System.out.println("Connected with " + clientSocket.getRemoteSocketAddress().toString());
				new ClientHandler(clientSocket).start();
				numClients++;
			} else{
				new PrintWriter(clientSocket.getOutputStream(), true).write("Server cannot accept more clients!");
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

private class ClientHandler extends Thread{
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	public ClientHandler(Socket client){
		clientSocket = client;
	}
	
	public void stopConnection() throws Exception{
        in.close();
        out.close();
        clientSocket.close();
	}
	
	public void run(){

		try{
			System.out.println("Connected with " + clientSocket.getRemoteSocketAddress().toString());
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String inputLine;
			System.out.println("Client now speaking");
			
			while ((inputLine = in.readLine()) != null) {
				
				if ("exit".equals(inputLine)) {
					out.println("Goodbye");
					break;
				}
				
				out.println(inputLine);
				System.out.println(inputLine);
			}

		} catch(Exception E){

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