package utils.Channel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Entity.MilitarType;
import utils.Requests.RequestType;

public class ChannelManager {


    private Map<String, Channel> channels;
    private Set<String> usedIPs;
    private Set<String> usedNames;
    private static final String prefix = "224.0.0.";
    private static int counter = 1;

    public ChannelManager() {
        this.channels = new HashMap<>();
        this.usedIPs = new HashSet<>();
        this.usedNames = new HashSet<>();
    }

    public String createChannel(String channelName, Boolean isprivate, MilitarType militarType) throws IOException {
        String channelAddress = prefix + (counter++);
        if (usedIPs.contains(channelAddress) || usedNames.contains(channelName)) {
            throw new IOException("Channel already exists");
        }
        Channel channel = new Channel(channelAddress, channelName, isprivate, militarType);
        channels.put(channelName, channel);
        usedIPs.add(channelAddress);
        usedNames.add(channelName);
        return channelAddress;
    }

    public String joinChannel(String channelName, UUID userId, MulticastSocket multicastSocket) throws IOException {
        if (channels.containsKey(channelName)) {
            Channel channel = channels.get(channelName);

            channel.join(userId);
            //InetAddress group = InetAddress.getByName(channel.getIp());
            //multicastSocket.joinGroup(group);
            System.out.println("User " + userId + " joined channel: " + channelName);
            return channel.getIp();
        } else {
            System.out.println("Channel not found: " + channelName);
        }
        return null;
    }

    public void leaveChannel(String channelName, UUID userId, MulticastSocket multicastSocket) throws IOException {
        if (channels.containsKey(channelName)) {
            Channel channel = channels.get(channelName);
            channel.leave(userId);
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

    // Function that takes a channel name and User Object and returns true if the
    // user is in the channel
    public boolean isUserInChannel(String channelName, UUID userId) {
        if (channels.containsKey(channelName)) {
            Channel channel = channels.get(channelName);
            return channel.isUserInChannel(userId);
        } else {
            System.out.println("Channel not found: " + channelName);
            return false;
        }
    }

    public Map<String, Channel> getChannels() {
        return this.channels;
    }

    public void sendMessageToChannel(RequestType type, String channelName, String userName, String message,
                                     MulticastSocket multicastSocket) {
        try {

            if (!channels.containsKey(channelName)) {
                System.err.println("Channel not found: " + channelName);
                throw new Exception("Channel not found");
            }
            Channel channel = channels.get(channelName);
            channel.sendMessage(type, userName, message, multicastSocket);
        } catch (Exception e) {
            System.err.println("Error sending message to channel: " + channelName + " (" + e.getMessage() + ")");
            throw new RuntimeException(e);
        }

    }
}
