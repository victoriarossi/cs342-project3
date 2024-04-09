import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    private String userID; // ID of user sending the message
    private String messageContent; // content of the message
    private MessageType messageType; // type of message (BROADCAST = ALL USERS OR PRIVATE = SINGLE USER)

    private ArrayList listOfUsers;

    // Constructor to initialize message object with userID, message content, and type of message
    public Message(String userID, String messageContent, MessageType messageType) {
        this.userID = userID;
        this.messageContent = messageContent;
        this.messageType = messageType;
    }

    // getter to retrieve userID of the message
    public String getUserID() {
        return userID;
    }

    // setter to set userID of the message
    public void setUserID(String userID) {
        this.userID = userID;
    }

    // getter to retrieve message content of the message
    public String getMessageContent() {
        return messageContent;
    }

    // setter to set message content of the message
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    // getter to retrieve message type of the message
    public MessageType getMessageType() {
        return messageType;
    }

    // setter to set message type of the message
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    // enum defining possible types of messages
    enum MessageType {
        BROADCAST, // Message for all clients
        PRIVATE // Message for 1 specific client
    }

    public String toString(){
        return "This is a message";
    }
}
