import java.net.*;
import java.io.*;
import java.util.List;
import java.util.Map;

public class ClientHandler extends Thread{
    private Socket socket;
    private Map<String, ObjectOutputStream> clients;
    private List<Message> newMessages;
    private String clientName;

    //socket objects created specifically for this handler
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket client, Map<String,ObjectOutputStream> clients, List<Message> newMessages){
        this.clients = clients;
        socket = client;
        this.newMessages = newMessages;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        }
        catch(Exception e){
            System.out.println("Couldn't create client handler");
        }
    }


    /*I realize that these two functions are repetitive,
    but hey it makes it easier for me to code.*/

    //This one sends a private message to the client being handled
    public void sendServerMessage(String text){
        newMessages.add(new Message("Server", text, clientName));
    }

    //Whereas this one sends a message to everybody
    //This is mainly used to let everyone know if someone has connected/disconnected
    public void sendAnnouncement(String text){
        newMessages.add(new Message("Server", text, null));
    }

    public void processCommand(String input){
        String parts[] = input.substring(3).split(" ");
        String command = parts[0];

        switch(command){
            case "msg":
                if(parts.length < 2){
                    sendServerMessage("Command Usage: ///msg [username] [content]");
                }
                else if(!clients.containsKey(parts[1])){
                    sendServerMessage("User \"" + parts[1] + "\" does not exist.");
                }
                else if(clientName.equals(parts[1])){
                    sendServerMessage("Why are you trying to talk to yourself?");
                }
                else{
                    int chunk = parts[1].length() + 8;
                    newMessages.add(new Message(clientName, input.substring(chunk), parts[1]));
                }
                break;
            case "users":
                sendServerMessage("Online users: " + clients.keySet().toString());
                break;
            case "help":
                if(parts.length < 2){
                    sendServerMessage("Available commands: quit, msg, users. You can use ///help with any of these commands as an argument to get information about them.");
                }
                else{
                    switch(parts[1]){
                        case "msg":
                            sendServerMessage("Used to privately message other users. ex. ///msg [username] [content]");
                            break;
                        case "users":
                            sendServerMessage("Lists off all the users in the chatroom");
                            break;
                        default:
                            sendServerMessage("Help for command \"" + parts[1] + "\" does not exist.");
                            break;
                    }
                }
                break;
            default:
                sendServerMessage("Command \""+ command + "\" does not exist.");
                break;
        }
    }

    /*handles the client logging in and makes suer that they set an
    available username*/
    public boolean loginHandler(){
        try {
            String name;
            while((name = (String)in.readObject()) != null){
                name = name.replace(" ", "_");
                if(name.length() < 1 || clients.containsKey(name)) {
                    System.out.println(name);
                    out.writeObject(false);
                }
                else{
                    clients.put(name, out);
                    out.writeObject(true);
                    clientName = name;
                    System.out.println("new user registered");
                    sendAnnouncement(clientName + " has logged in.");
                    break;
                }
            }
        }
        catch(Exception e){
            System.out.println("login failed");
            System.out.println("Client Disconnected");
            return false;
        }
        return true;
    }

    public void messageHandler(){
        try{
            String message;
            while((message = (String)in.readObject()) != null){
                if(message.startsWith("///")){
                    processCommand(message);
                }
                else{
                    newMessages.add(new Message(clientName, message, null));
                }
            }
        }
        catch(Exception e){
            System.out.println("message handling failed");
            System.out.println("Client disconnected");
        }
    }

    public void run() {
        if(loginHandler()) {
            System.out.println("now handling messages");
            messageHandler();
        }
        else{
            System.out.println("Client decided to not login");
            return;
        }
        try{
            clients.remove(clientName);
            socket.close();
        }
        catch(Exception e){
            System.out.println("Error closing connection");
        }
        System.out.println("Client handler for " + clientName +" has terminated");
        clients.remove(clientName);
        sendAnnouncement(clientName + " has logged out.");
    }
}
