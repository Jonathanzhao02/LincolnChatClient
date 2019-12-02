import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LincolnClient extends Application{
    private Socket clientSocket;
    private PrintWriter out = null;
    private String username = "";

    private BorderPane mainPane = new BorderPane();
    private TextField userInput = new TextField();
    private TextArea userOutput = new TextArea();
    private Button clearBtn = new Button("Clear");

    private Scene mainScene;
    private Stage mainStage;

    private LinkedBlockingQueue<InetAddress> ip = new LinkedBlockingQueue<InetAddress>();
    private volatile Boolean connected = false;

    private Thread discoveryThread =
    new Thread(() -> {

        try{
            DatagramSocket discSocket = new DatagramSocket();
            discSocket.setBroadcast(true);

            byte[] sendData = "LCC_DISCOVER_REQUEST".getBytes();

            try{
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                discSocket.send(sendPacket);
                //System.out.println("Sent packet to 255.255.255.255 (DEFAULT)");
            } catch(Exception e){e.printStackTrace();}

            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

            while(interfaces.hasMoreElements()){
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if(networkInterface.isLoopback()){
                    continue;
                }

                for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()){
                    InetAddress broadcast = interfaceAddress.getBroadcast();

                    if(broadcast == null){
                        continue;
                    }

                    try{
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 1337);
                        discSocket.send(sendPacket);
                    } catch(Exception e){
                        //e.printStackTrace();
                    }

                    //System.out.println("Sent packet to " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }

            }

            //System.out.println("Now waiting for reply");
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            discSocket.receive(receivePacket);
            //System.out.println("Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
            String message = new String(receivePacket.getData()).trim();

            if(message.equals("LCC_DISCOVER_RESPONSE")){
                //System.out.println("Found server!");
                ip.add(receivePacket.getAddress());
                discSocket.setSoTimeout(1000);

                while(true){
                    Thread.sleep(5000);
                    connected = checkConnection(discSocket, receivePacket.getAddress());
                }

            } else{
                System.out.println("Received " + message);
            }

            discSocket.close();
        } catch(Exception e){
            e.printStackTrace();
        }

    });

    private Thread outputThread = 
    new Thread(() -> {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch(Exception e){
            System.out.println("Reader could not be initialized.");
        }

        String output;

        //constantly read from output stream
        while(clientSocket.isConnected() && !clientSocket.isClosed() && connected){

            try{
                output = in.readLine();

                if(output != null){
                    output(output);
                }

            } catch(Exception e){
                //System.out.println("Uh oh! Something went wrong.");
            }

        }

        output("Lost connectin with server, please restart");
    });

    private Boolean checkConnection(DatagramSocket s, InetAddress i){
        try{
            byte[] sendMsg = "LCC_CHECK_CONNECTION".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendMsg, sendMsg.length, i, 1337);
            s.send(sendPacket);

            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            s.receive(receivePacket);
            
            String message = new String(receivePacket.getData()).trim();

            if(message.equals("LCC_CONNECTED")){
                return true;
            } else{
                return false;
            }
        
        } catch(SocketTimeoutException e){
            return false;
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }

    }
 
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage mainStage){
        this.mainStage = mainStage;
        createScene();
        //output("Please select a server to connect to.");
        discoveryThread.start();

        try{
            InetAddress serverIp = ip.poll(2000, TimeUnit.MILLISECONDS);    //Perhaps place on separate thread to prevent client from freezing on startup
            startConnection(serverIp, 1337);
        } catch(Exception e){
            output("Failed to connect");
            e.printStackTrace();
        }

    }

    private void createScene(){
        HBox controlPane = new HBox(userInput, clearBtn);
        userOutput.setPrefWidth(500);
        userOutput.setPrefHeight(400);
        userOutput.setEditable(false);
        userInput.setPrefHeight(20);
        userInput.setPrefWidth(400);
        clearBtn.setPrefHeight(20);
        clearBtn.setPrefWidth(100);
        clearBtn.setOnAction(e -> {
            clearOutput();
        });

        userOutput.setWrapText(true);
        //mainPane.setTop(serverPane);
        mainPane.setCenter(userOutput);
        mainPane.setBottom(controlPane);
        mainScene = new Scene(mainPane, 500, 500);
        mainStage.setScene(mainScene);
        mainStage.show();
    }

    public void stop(){

        try{
            stopConnection();
            outputThread.interrupt();
        } catch(Exception e){
            
        }

    }

    private void output(String message){
        userOutput.appendText("\n" + message);
    }

    private void clearOutput(){
        userOutput.clear();
    }

    private void startConnection(InetAddress servIp, int port){
        clearOutput();
        output("Connecting...");
        
        Platform.runLater(() -> {
            try {
                clientSocket = new Socket(servIp, port);
                output("Connected to " + clientSocket.getInetAddress());
                setupClient();
            } catch(Exception e){
                output("Could not connect.");
            }
        });
    }
 
    private void stopConnection() throws Exception{
        clientSocket.close();
    }

    private void setupClient(){
        output("Please enter a username (less than 20 characters)");

        userInput.setOnAction(e -> {

            if(invalidUsername(userInput.getText())){
                output("Invalid username");
            } else{
                clearOutput();
                username = userInput.getText();
                mainStage.setTitle(username + "'s Chatroom");

                try{
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(username);
                } catch(Exception exception){
                    System.out.println("Writer could not be initialized.");
                }

                outputThread.start();

                userInput.setOnAction(E -> {
                    String response = userInput.getText();

                    if(clientSocket.isConnected() && !clientSocket.isClosed() && response.length() > 0 && connected){
                        out.println(response);
                        output(username + ": " + response);
                    }

                    userInput.clear();
                });

            }

            userInput.clear();
        });
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
