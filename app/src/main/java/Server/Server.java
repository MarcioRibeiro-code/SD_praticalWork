package Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    protected static final int port = 4445;

    protected static final String MAIN_GROUP_IP = "224.0.0.1";
    protected static final String CAPE_GROUP_IP = "224.0.0.2";
    protected static final String SEARGENT_GROUP_IP = "224.0.0.3";
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
                System.out.println("New client connected! -> " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

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

    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(port);
        Server server = new Server(new ServerSocket(2048), datagramSocket);
        server.startServer();
    }
}
