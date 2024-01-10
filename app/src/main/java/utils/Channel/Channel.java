package utils.Channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;

import Entity.MilitarType;
import utils.Requests.RequestType;
import utils.Responses.Response;

public class Channel {
    private final String ip;
    private final String name;
    private final ChannelAuthorization authorization;
    private Set<UUID> users;
    private final int multicastPort = 4445;
    private Gson gson = new Gson();

    public Channel(String ip, String name, boolean isPrivate, MilitarType role) {
        this.ip = ip;
        this.name = name;
        this.users = new HashSet<>();
        this.authorization = new ChannelAuthorization(isPrivate, role);
        this.gson = new Gson();
    }

    public void join(UUID userId) {
        users.add(userId);
    }

    public void leave(UUID userId) {
        users.remove(userId);
        System.out.println("User " + userId + " left channel " + name);
    }

    public void listUsers() {
        System.out.println("Users in channel " + name + ":");
        for (UUID user : users) {
            System.out.println(user.toString());
        }
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public ChannelAuthorization getAuthorization() {
        return authorization;
    }

    public void sendMessage(RequestType type, String userId, String message, MulticastSocket multicastSocket) {
        SendMessageToChannel sendMessageToChannel = new SendMessageToChannel(type, name, userId, message);

        String fullMessage = gson.toJson(Response.success(type, sendMessageToChannel));
        byte[] buffer = fullMessage.getBytes();

        // Send the message using the multicast socket
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), multicastPort);
            multicastSocket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending message to channel " + name);
        }
    }

    public boolean isUserInChannel(UUID userId) {
        return users.contains(userId);
    }

    @Override
    public String toString() {
        return "Channel: " + name + ", IP: " + ip + "\n";
    }

    public class ChannelAuthorization {
        private final boolean isPrivate;
        private final MilitarType role;

        public ChannelAuthorization(boolean isPrivate, MilitarType role) {
            this.isPrivate = isPrivate;
            this.role = role;
        }

        public boolean isPrivate() {
            return isPrivate;
        }

        public MilitarType getRole() {
            return role;
        }
    }
}
