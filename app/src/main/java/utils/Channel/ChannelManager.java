package utils.Channel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChannelManager {
    private Map<String, Channel> channels;
    private Set<String> usedIPs;
    private Set<String> usedNames;

    public ChannelManager() {
        this.channels = new HashMap<>();
        this.usedIPs = new HashSet<>();
        this.usedNames = new HashSet<>();
    }

    public void createChannel(String channelAddress, String channelName) {
        if (usedIPs.contains(channelAddress) || usedNames.contains(channelName)) {
            return;
        }
        Channel channel = new Channel(channelAddress, channelName);
        channels.put(channelName, channel);
        usedIPs.add(channelAddress);
        usedNames.add(channelName);
        System.out.println("Created channel: " + channelName + " (" + channelAddress + ")");
    }

    public void joinChannel(String channelName, UUID userId, MulticastSocket multicastSocket) throws IOException {
        if (channels.containsKey(channelName)) {
            Channel channel = channels.get(channelName);
            channel.join(userId);
            InetAddress group = InetAddress.getByName(channel.getIp());
            multicastSocket.joinGroup(group);
            System.out.println("User " + userId + " joined channel: " + channelName);
        } else {
            System.out.println("Channel not found: " + channelName);
        }
    }

    public void leaveChannel(String channelName, UUID userId, MulticastSocket multicastSocket) throws IOException {
        if (channels.containsKey(channelName)) {
            Channel channel = channels.get(channelName);
            channel.leave(userId);
            InetAddress group = InetAddress.getByName(channel.getIp());
            multicastSocket.leaveGroup(group);
            System.out.println("User " + userId + " left channel: " + channelName);
        } else {
            System.out.println("Channel not found: " + channelName);
        }
    }

    public void listChannels() {
        System.out.println("Available Channels: " + channels.keySet());
    }

    public void listUsersInChannel(String channelAddress) {
        if (channels.containsKey(channelAddress)) {
            Channel channel = channels.get(channelAddress);
            channel.listUsers();
        } else {
            System.out.println("Channel not found: " + channelAddress);
        }
    }

    public Map<String, Channel> getChannels() {
        return this.channels;
    }

    public boolean sendMessageToChannel(String channelName, String userName, String message,
            MulticastSocket multicastSocket) {
        try {

            if (!channels.containsKey(channelName)) {
                System.err.println("Channel not found: " + channelName);
                return false;
            }
            Channel channel = channels.get(channelName);
            return channel.sendMessage(userName, message, multicastSocket);
        } catch (Exception e) {
            System.err.println("Error sending message to channel: " + channelName + " (" + e.getMessage() + ")");
            return false;
        }

    }
}
