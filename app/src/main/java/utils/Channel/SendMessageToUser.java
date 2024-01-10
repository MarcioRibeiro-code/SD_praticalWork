package utils.Channel;

import java.util.UUID;

public class SendMessageToUser {

    private final String senderUserName; 
    private final UUID Receiver;

    private final String Message;

    public SendMessageToUser(String senderUserName,UUID receiver, String message) {
        this.senderUserName = senderUserName;
        this.Receiver = receiver;
        this.Message = message;
    }



    public UUID getReceiver() {
        return Receiver;
    }

    public String getMessage() {
        return Message;
    }

    public String getSenderUserName() {
        return senderUserName;
    }
}
