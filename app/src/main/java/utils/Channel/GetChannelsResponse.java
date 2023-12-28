package utils.Channel;

import java.util.Set;

public class GetChannelsResponse {
    private final Set<ChannelResponse> channels;

    public GetChannelsResponse(Set<ChannelResponse> channels) {
        this.channels = channels;
    }

    public Set<ChannelResponse> getChannels() {
        return this.channels;
    }
}
