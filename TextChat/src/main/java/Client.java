import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private List<Message> incomingMessages;
    private ServerInputHandler messageReader;
    private boolean connected;

    public Client(int port){
        incomingMessages = Collections.synchronizedList(new ArrayList<Message>());
        try{
            socket = new Socket("127.0.0.1",port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            messageReader = new ServerInputHandler(in, incomingMessages);
            connected = true;
        }
        catch(IOException e){
            APINotify("Couldn't connect to server.");
            APINotify("Either the server does not exist or there was an error connecting.");
            APINotify("This window will self-destruct in 10 seconds.");
            connected = false;
        }
    }

    public void APINotify(String message){
        incomingMessages.add(new Message("ClientAPI", message, null));
    }

    public boolean isConnected(){
        return connected;
    }

    public void cleanMessages(){
        while(incomingMessages.size() > 30){
            incomingMessages.remove(0);
        }
    }

    public ArrayList<String> displayMessages(int width, int height){
        cleanMessages();
        ArrayList<String> results = new ArrayList<String>();
        int lines = 0;
        int messageIndex = incomingMessages.size()-1;

        while(messageIndex > -1) {
            String message = incomingMessages.get(messageIndex).toString();
            lines += message.length() / width+1;
            if(lines > height){
                break;
            }
            results.add(message);
            messageIndex--;
        }
        return results;
    }

    public Boolean login(String name){
        Boolean result = false;
        try {
            out.writeObject(name);
            result = (Boolean)in.readObject();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public void message(String text){
        try{
            out.writeObject(text);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void startMessageReader(){
        messageReader.start();
    }

    public void kill(){
        if(connected) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("client socket closed");
            }
        }
    }
}
