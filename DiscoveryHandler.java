import java.io.*;
import java.net.*;

public class DiscoveryHandler extends Thread {
    private DatagramSocket socket;
    private int port;

    public DiscoveryHandler(int port){
        this.port = port;
    }

    @Override
    public void run(){

        try{
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);

            while(true){
                System.out.println("Discovery ready to receive broadcast packets!");

                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                System.out.println("Packet receieved from " + packet.getSocketAddress());
                System.out.println("Packet data: " + new String(packet.getData()));

                String message = new String(packet.getData()).trim();

                if(message.equals("LCC_DISCOVER_REQUEST")){
                    byte[] sendData = "LCC_DISCOVER_RESPONSE".getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);

                    System.out.println("Discovery sent packet " + new String(sendPacket.getData()) + " to " + packet.getSocketAddress());
                } else if(message.equals("LCC_CHECK_CONNECTION")){
                    byte[] sendData = "LCC_CONNECTED".getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);

                    System.out.println("Discovery sent check packet to " + packet.getSocketAddress());
                }

            }

        } catch(IOException e){
            e.printStackTrace();
        }

    }

}