package utils.Channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Channel {
    private final String ip;
    private final String name;
    private Set<UUID> users;
    private final int multicastPort = 4445;

    public Channel(String ip, String name) {
        this.ip = ip;
        this.name = name;
        this.users = new HashSet<>();
    }

    public void join(UUID userId) {
        users.add(userId);
        System.out.println("User " + userId + " joined channel " + name);
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

    public void sendMessage(String message) {
        System.out.println("Message sent to channel " + name + ": " + message);
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public boolean sendMessage(String userId, String message, MulticastSocket multicastSocket) {
        String fullMessage = "Message from " + userId + " in channel " + name + ": " + message;
        byte[] buffer = fullMessage.getBytes();

        // Send the message using the multicast socket
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), multicastPort);
            multicastSocket.send(packet);

            System.out.println("Message sent to channel " + name + ": " + fullMessage);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
