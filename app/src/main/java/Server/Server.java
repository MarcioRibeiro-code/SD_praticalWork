package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.Stats;

import java.net.MulticastSocket;

import Entity.MilitarType;
import utils.Channel.ChannelManager;

public class Server {
    protected static final int port = 4445;

    // protected static final String MAIN_GROUP_IP = "224.0.1."; ++
    // private static final String CAPE_GROUP_IP = "224.0.2."; ++
    // private static final String SEARGENT_GROUP_IP = "224.0.03."; ++
    private final ServerSocket serverSocket;
    protected final ChannelManager channelManager;
    private final ArrayListSync<ClientHandler> clientHandlers;
    protected final MulticastSocket multicastSocket;
    protected ConcurrentHashMap<UUID, ArrayListSync<String>> inbox;

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final Stats stats = new Stats();

    public Server(ServerSocket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayListSync<ClientHandler>();
        this.channelManager = new ChannelManager();
        channelManager.createChannel("main", false, null);
        channelManager.createChannel(MilitarType.CAPE.getTypeString(), true, MilitarType.CAPE);
        channelManager.createChannel(MilitarType.SEARGENT.getTypeString(), true, MilitarType.SEARGENT);
        Server.getStats().incrementNumberOfChannels();
        Server.getStats().incrementNumberOfChannels();
        Server.getStats().incrementNumberOfChannels();
        this.multicastSocket = new MulticastSocket(port);
        this.inbox = new ConcurrentHashMap<>();
    }

    public void startServer() {
        System.out.println("Central node is online!");
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                logger.log(Level.INFO, "New client connected! -> {0}", clientInfo);

                new Thread(new ClientHandler(clientHandlers, socket, this)).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred while accepting a new client connection.", e);
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred while closing the server socket.", e);
        }
    }

    /**
     * Getter to return list clientHandlers
     *
     * @return
     */
    public ArrayListSync<ClientHandler> getClientHandlers() {
        return clientHandlers;
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

    public static Stats getStats() {
        return stats;
    }
}