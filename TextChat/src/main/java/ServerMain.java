import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ServerMain {
    private int port;
    private Map<String, ObjectOutputStream> clients;
    private List<Message> newMessages;
    private boolean running;

    public ServerMain(int port){
        this.port = port;
        clients = new ConcurrentHashMap<String, ObjectOutputStream>();
        newMessages = Collections.synchronizedList(new ArrayList<Message>());
    }

    public void start(){
        running = true;

        ServerSocket server = null;
        ClientOutputHandler updater = new ClientOutputHandler(newMessages, clients);

        try{
            server = new ServerSocket(port);
            System.out.println("Port bound");
        }
        catch(IOException e){
            System.out.println("Port won't bind");
        }

        Socket client;
        updater.start();
        while(running) {
            try {
                client = server.accept();
                new ClientHandler(client, clients, newMessages).start();
                System.out.println("New client connected");

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error connecting to client");
            }
        }
    }

    public static void main(String args[]){
        ServerMain server = new ServerMain(6969);
        server.start();
    }
}