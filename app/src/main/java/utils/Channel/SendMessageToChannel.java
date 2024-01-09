package utils.Channel;

public class SendMessageToChannel {
    private final String channelName;
    private final String message;
    private final String sender;

    public SendMessageToChannel(String channelName, String sender, String message) {
        this.channelName = channelName;
        this.sender = sender;
        this.message = message;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSender() {
        return this.sender;
    }

    @Override
    public String toString() {
        return "Sender: " + this.sender + "->" + "Message: " + this.message;
    }
}
