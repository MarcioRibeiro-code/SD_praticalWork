package utils.Channel;

import java.util.UUID;

public class JoinChannel {

    private final String channelName;
    private final UUID userID;

    public JoinChannel(String channelName, UUID userID) {
        this.channelName = channelName;
        this.userID = userID;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public UUID getUserID() {
        return this.userID;
    }
}
