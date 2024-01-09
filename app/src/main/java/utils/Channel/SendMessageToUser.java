package utils.Channel;

import java.util.UUID;

public class SendMessageToUser {
    private final UUID Receiver;

    private final String Message;

    public SendMessageToUser(UUID receiver, String message) {
        Receiver = receiver;
        Message = message;
    }



    public UUID getReceiver() {
        return Receiver;
    }

    public String getMessage() {
        return Message;
    }
}
