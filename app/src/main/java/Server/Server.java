package Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import Entity.MilitarType;
import utils.Channel.ChannelManager;

public class Server {
    protected static final int port = 4445;

    protected static final String MAIN_GROUP_IP = "224.0.0.1";
    private static final String CAPE_GROUP_IP = "224.0.0.2";
    private static final String SEARGENT_GROUP_IP = "224.0.0.3";
    private final ServerSocket serverSocket;
    protected final ChannelManager channelManager;
    private final ArrayListSync<ClientHandler> clientHandlers;

    public Server(ServerSocket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayListSync<ClientHandler>();
        this.channelManager = new ChannelManager();
        channelManager.createChannel(MAIN_GROUP_IP, "main");
        channelManager.createChannel(CAPE_GROUP_IP, MilitarType.CAPE.getTypeString());
        channelManager.createChannel(SEARGENT_GROUP_IP, MilitarType.SEARGENT.getTypeString());
    }

    public void startServer() {
        System.out.println("Central node is online!");
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected! -> " + socket.getInetAddress().getHostAddress() + ":"
                        + socket.getPort());

                new Thread(new ClientHandler(clientHandlers, socket, this)).start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * public void sendMulticastCapitanMessage(String message) {
     * new Thread(() -> {
     * try {
     * byte[] buffer = message.getBytes();
     * datagramSocket.send(new DatagramPacket(buffer, buffer.length,
     * InetAddress.getByName(CAPE_GROUP_IP),
     * port));
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * }).start();
     * }
     * 
     * public void sendMulticastSeargentMessage(String message) {
     * new Thread(() -> {
     * try {
     * byte[] buffer = message.getBytes();
     * datagramSocket.send(new DatagramPacket(buffer, buffer.length,
     * InetAddress.getByName(SEARGENT_GROUP_IP),
     * port));
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * }).start();
     * }
     */

    public static void main(String[] args) throws IOException {
        Server server = new Server(new ServerSocket(2048));
        server.startServer();
    }
}