import java.io.PrintWriter;
import java.net.*;
import java.util.Date;


public class User {
    private Socket s;
    private String username = "";
    private PrintWriter out;
    private Date lastsent;

    public User(Socket s){
        this.s = s;
        this.lastsent = new Date();

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

    public void sent() { 
        //Utility function that updates the last sent time of a message
        this.lastsent = new Date();
    }

    public String getUsername(){return username;}

    public Socket getSocket(){return s;}

    public long getTime(){return lastsent.getTime();}

}