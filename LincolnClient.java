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
        while(clientSocket.isConnected() && !clientSocket.isClosed()){

            try{
                output = in.readLine();

                if(output != null){
                    output(output);
                }

            } catch(Exception e){
                //System.out.println("Uh oh! Something went wrong.");
            }

        }

    });
 
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage mainStage){
        this.mainStage = mainStage;
        createScene();

        try{
            startClient();
        } catch(Exception e){
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

    private void startConnection(String ip, int port) throws Exception{
        output("Note: you MUST be connected to pps-wifi-guest to connect!");
        output("Connecting...");
        
        Platform.runLater(() -> {
            try {
                clientSocket = new Socket(ip, port);
                output("Connected to " + clientSocket.getInetAddress());
                setupClient();
            } catch(Exception e){
                output("Could not connect. Retrying...");

                Platform.runLater(() -> {
                    try{
                        startClient();
                    } catch(Exception E){
    
                    }
                });

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

                    if(clientSocket.isConnected() && !clientSocket.isClosed() && response.length() > 0){
                        out.println(response);
                        output(username + ": " + response);
                    }

                    userInput.clear();
                });

            }

            userInput.clear();
        });
    }
	
	private void startClient() throws Exception{
        clearOutput();
        startConnection("10.186.66.95", 53);
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