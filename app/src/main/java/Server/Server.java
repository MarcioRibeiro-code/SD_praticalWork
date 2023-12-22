package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import Entity.MilitarType;

public class Server {
    protected static final int port = 4445;

    protected static final String MAIN_GROUP_IP = "224.0.0.1";
    private static final String CAPE_GROUP_IP = "224.0.0.2";
    private static final String SEARGENT_GROUP_IP = "224.0.0.3";
    protected static final Map<String, String> MILITAR_GROUPS = new HashMap<String, String>() {
        {
            put(MilitarType.CAPE.name(), CAPE_GROUP_IP);
            put(MilitarType.SEARGENT.name(), SEARGENT_GROUP_IP);
        };
    };

    private final ServerSocket serverSocket;
    private final DatagramSocket datagramSocket;
    private final ArrayListSync<ClientHandler> clientHandlers;

    public Server(ServerSocket serverSocket, DatagramSocket datagramSocket) {
        this.serverSocket = serverSocket;
        this.datagramSocket = datagramSocket;
        this.clientHandlers = new ArrayListSync<ClientHandler>();
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

    public void sendMulticastCapitanMessage(String message) {
        new Thread(() -> {
            try {
                byte[] buffer = message.getBytes();
                datagramSocket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(CAPE_GROUP_IP),
                        port));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMulticastSeargentMessage(String message) {
        new Thread(() -> {
            try {
                byte[] buffer = message.getBytes();
                datagramSocket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(SEARGENT_GROUP_IP),
                        port));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(port);
        Server server = new Server(new ServerSocket(2048), datagramSocket);
        server.startServer();
    }
}