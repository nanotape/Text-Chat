import java.io.ObjectInputStream;
import java.util.List;

/*
This class acts as a separate thread for the client which will handle input
since the readObject() method is blocking and would freeze the execution of other things like client input.
*/
public class ServerInputHandler extends Thread{
    private List<Message> incomingMessages;
    private ObjectInputStream in;

    //Takes a reference to the client's message queue and input stream so that it can add messages accordingly
    public ServerInputHandler(ObjectInputStream in, List<Message> incomingMessages){
        this.incomingMessages = incomingMessages;
        this.in = in;
    }

    public void run(){
        Message incoming;
        try{
            //If a nes message comes in then it will be added to the message queue
            while((incoming = (Message)in.readObject()) != null){
                incomingMessages.add(incoming);
            }
        }
        catch(Exception e){
            System.out.println("Input handler exception");
        }
        System.out.println("Input handler closed");
    }
}
