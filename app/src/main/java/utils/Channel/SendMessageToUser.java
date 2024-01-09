package utils.Channel;

public class SendMessageToUser {

    private final String Sender;
    private final String Receiver;

    private final String Message;

    public SendMessageToUser(String sender, String receiver, String message) {
        Sender = sender;
        Receiver = receiver;
        Message = message;
    }


    public String getSender() {
        return Sender;
    }

    public String getReceiver() {
        return Receiver;
    }

    public String getMessage() {
        return Message;
    }
}
