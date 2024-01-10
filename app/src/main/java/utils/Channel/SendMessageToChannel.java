package utils.Channel;

import utils.Requests.RequestType;

public class SendMessageToChannel {
    private final RequestType type;
    private final String channelName;
    private final String message;
    private final String sender;

    public SendMessageToChannel(RequestType type, String channelName, String sender, String message) {
        this.type = type;
        this.channelName = channelName;
        this.sender = sender;
        this.message = message;
    }

    public RequestType getType() {
        return type;
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
