package utils.Channel;

import java.util.UUID;

public class SendMessageToUser {

    private final UUID Sender;
    private final UUID Receiver;

    private final String Message;

    public SendMessageToUser(UUID sender, UUID receiver, String message) {
        Sender = sender;
        Receiver = receiver;
        Message = message;
    }


    public UUID getSender() {
        return Sender;
    }

    public UUID getReceiver() {
        return Receiver;
    }

    public String getMessage() {
        return Message;
    }
}
