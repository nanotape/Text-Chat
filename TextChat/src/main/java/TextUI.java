import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;

public class TextUI{
    private Terminal terminal;
    private Screen screen;
    private TextGraphics graphics;
    private final int width = 80;
    private final int height = 24;
    private final int msgWidth = width-2;

    public TextUI(){
        try {
            terminal = new DefaultTerminalFactory().createTerminal();
            screen = new TerminalScreen(terminal);
            graphics = screen.newTextGraphics();
            screen.setCursorPosition(null);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    //Puts a message on the terminal at a specific location with regards to the width of the screen
    //specified at initialization.
    public void renderMessage(TerminalPosition pos, String message){
        int lines = (message.length()/msgWidth);
        do{
            String tmp = message.substring(lines*msgWidth);
            graphics.putString(pos.withRelativeRow(lines), tmp);
            message = message.substring(0,lines*msgWidth);
            lines--;
        }
        while(lines > -1);
    }

    public void renderMessageList(ArrayList<String> messages, TerminalPosition start){
        int line = height-5;
        for(String message: messages){
            line -= message.length()/msgWidth+1;
            renderMessage(start.withRelativeRow(line), message);
        }
    }

    public void drawBox(TerminalPosition pos, TerminalSize dims){
        //draws the horizontal and vertical sides of the box
        //also fills the box with empty space to make way for content
        graphics.drawLine(pos.withRelativeRow(1), pos.withRelativeRow(dims.getRows()), Symbols.BOLD_SINGLE_LINE_VERTICAL);
        graphics.drawLine(pos.withRelative(dims.getColumns()-1,1), pos.withRelative(dims.getColumns()-1, dims.getRows()), Symbols.BOLD_SINGLE_LINE_VERTICAL);
        graphics.drawLine(pos.withRelative(1,0), pos.withRelativeColumn(dims.getColumns()-2), Symbols.BOLD_SINGLE_LINE_HORIZONTAL);
        graphics.drawLine(pos.withRelative(1,dims.getRows()+1), pos.withRelative(dims.getColumns()-2, dims.getRows()+1), Symbols.BOLD_SINGLE_LINE_HORIZONTAL);
        graphics.fillRectangle(pos.withRelative(1,1), dims.withRelativeColumns(-2), ' ');
        //draws the corners of the box
        graphics.setCharacter(pos,Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
        graphics.setCharacter(pos.withRelativeColumn(dims.getColumns()-1), Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
        graphics.setCharacter(pos.withRelativeRow(dims.getRows()+1), Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
        graphics.setCharacter(pos.withRelative(dims.getColumns()-1,dims.getRows()+1), Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);
    }

    public void run(){
        Client client = new Client(6969);

        if(client.isConnected()){
            client.APINotify("Please enter your username.");
        }

        try{
            boolean loggedIn = false;
            screen.startScreen();

            String messageBuffer = "";
            TerminalPosition inputPos = new TerminalPosition(0,height-3);
            TerminalSize inputDims = new TerminalSize(width,1);
            TerminalPosition messagesPos = new TerminalPosition(0,0);
            TerminalSize messagesDims = new TerminalSize(width, height-5);
            screen.setCursorPosition(null);
            TextGraphics graphics = screen.newTextGraphics();

            //the main loop of the text interface
            while(true){
                //checks if the user has resized the terminal and changes things accordingly
                screen.doResizeIfNecessary();

                KeyStroke input = screen.pollInput();
                if(input != null) {
                    if (input.getKeyType() == KeyType.Escape || input.getKeyType() == KeyType.EOF){
                        break;
                    }
                    else if(input.getKeyType() == KeyType.Character){
                        messageBuffer += input.getCharacter();
                    }
                    else if(input.getKeyType() == KeyType.Backspace && messageBuffer.length() > 0){
                        messageBuffer = messageBuffer.substring(0,messageBuffer.length()-1);
                    }
                    //Checks if the user is pressing enter and that the message actually has content without exceeding a certain limit
                    else if(input.getKeyType() == KeyType.Enter && messageBuffer.length() > 0 && messageBuffer.length() < 384){
                        //If the client is not logged in, then all input will be
                        if(!loggedIn){
                            if(!client.login(messageBuffer)){
                                client.APINotify("Username is already in use.");
                            }
                            else{
                                loggedIn = true;
                                client.startMessageReader();
                            }
                        }
                        else{
                            client.message(messageBuffer);
                        }
                        messageBuffer = "";
                    }
                }

                //draws the bos where messages will be typed in
                graphics.fillRectangle(inputPos, new TerminalSize(messageBuffer.length() + 1 , 1), ' ');
                drawBox(inputPos, inputDims);
                graphics.putString(inputPos.withRelative(1,1), "Message: ");

                //Renders the message that the user is going to send to the screen.
                //If it ends up being too large then a substring is taken from it to be rendered so that it looks
                //as if the text is really just moving off to the left.
                String displayMessage;
                if(messageBuffer.length() > width-12){
                    displayMessage = messageBuffer.substring(messageBuffer.length()-width+12);
                }
                else {
                    displayMessage = messageBuffer;
                }
                graphics.putString(inputPos.withRelative(10, 1), displayMessage);

                //draw the message box and the messages inside
                drawBox(messagesPos, messagesDims);
                ArrayList<String> messages = client.displayMessages(msgWidth, height-5);
                renderMessageList(messages, messagesPos.withRelative(1,1));

                //writes all changes to the terminal
                screen.refresh();
                Thread.yield();

                //Puts the update speed of the loop to 30 fps so that it isn't busy and needlessly using cpu.
                try {
                    Thread.sleep(33);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }

                //checks to see if the client is actually connected
                //If not, then the app will print a message to the screen and close automatically after 10 seconds
                if(!client.isConnected()){
                    try{
                        Thread.sleep(10000);
                        break;
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            client.kill();
            if(screen != null){
                try{
                    screen.stopScreen();
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}