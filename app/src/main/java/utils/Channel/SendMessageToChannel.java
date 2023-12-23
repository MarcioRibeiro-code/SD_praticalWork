package utils.Channel;

public class SendMessageToChannel {
    private final String channelName;
    private final String message;

    public SendMessageToChannel(String channelName, String message) {
        this.channelName = channelName;
        this.message = message;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public String getMessage() {
        return this.message;
    }
}
