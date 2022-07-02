import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class ClientOutputHandler extends Thread{
    private List<Message> newMessages;
    private Map<String, ObjectOutputStream> clients;

    public ClientOutputHandler(List<Message> newMessages, Map<String, ObjectOutputStream> clients){
        this.newMessages = newMessages;
        this.clients = clients;
    }

    public void run(){
        while(true){
            try{
                if(newMessages.size() > 0) {
                    Message outbound = newMessages.remove(0);
                    if(outbound.getRecipient() != null){
                        System.out.println("Sending private message");
                        if(outbound.getAuthor() != "Server") {
                            clients.get(outbound.getAuthor()).writeObject(outbound);
                        }
                        clients.get(outbound.getRecipient()).writeObject(outbound);
                    }
                    else {
                        for (String name : clients.keySet()) {
                            System.out.println("Sending message to " + name);
                            clients.get(name).writeObject(outbound);
                        }
                    }
                }
            }
            catch(Exception e){
                System.out.println("fucked up sending messages");
            }
        }
    }
}
