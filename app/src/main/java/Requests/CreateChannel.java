package Requests;

import utils.Channel.Channel;

public class CreateChannel {
    private final Channel channel;

    public CreateChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return this.channel;
    }
}
