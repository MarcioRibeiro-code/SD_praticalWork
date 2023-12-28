package Requests;

import Entity.MilitarType;
import utils.Channel.Channel;

public class CreateChannel {
    private final String channelName;
    private final boolean isPrivate;
    private final MilitarType role;

    public CreateChannel(String channelName, boolean isPrivate, MilitarType role) {
        this.channelName = channelName;
        this.isPrivate = isPrivate;
        this.role = role;
    }

    public String channelName() {
        return this.channelName;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public MilitarType role() {
        return this.role;
    }
}
