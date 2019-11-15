import java.io.PrintWriter;
import java.net.*;

public class User {
    private Socket s;
    private String username = "";
    private PrintWriter out;

    public User(Socket s){
        this.s = s;

        try{
            out = new PrintWriter(s.getOutputStream(), true);
        } catch(Exception E){
            System.out.println("Writer could not be initialized.");
        }

    }

    public void setUsername(String name){
        this.username = name;
    }

    public void sendMessage(String s){
        out.println(s);
    }

    public String getUsername(){return username;}

    public Socket getSocket(){return s;}

}