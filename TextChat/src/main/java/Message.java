import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable{
    final private String author;
    final private LocalDateTime time;
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
    final private String message;
    final private String recipient;

    public Message(String author, String message, String recipient){
        this.message = message;
        this.author = author;
        time = LocalDateTime.now();
        this.recipient = recipient;
    }

    public String toString(){
        if(recipient == null) {
            return String.format("(%s) %s: %s", dtf.format(time), author, message);
        }
        else{
            return String.format("(%s) %s (To %s): %s", dtf.format(time), author, recipient, message);
        }
    }

    public String getAuthor(){
        return author;
    }

    public String getRecipient() {
        return recipient;
    }
}
