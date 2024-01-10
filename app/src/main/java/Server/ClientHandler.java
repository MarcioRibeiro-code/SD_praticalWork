package Server;

import utils.Channel.Channel;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    protected Socket socket;
    protected BufferedReader bufferedReader;
    protected BufferedWriter bufferedWriter;
    protected Protocol protocol;
    protected ArrayListSync<ClientHandler> clientHandlers;
    protected Server server;

    /*
     * Have a List of LocalNode to store all LocalNodes, that being said the list of
     * users
     */
    /*
     * Can we use localNodeLogin to store the role of a user?
     * Since we have restricted access to functions, we can use this class to store
     * the role of a user, and use it to restrict access to functions
     */

    public ClientHandler(ArrayListSync<ClientHandler> clientHandlers, Socket socket, Server server) throws IOException {
        try {
            this.socket = socket;
            this.server = server;
            this.clientHandlers = clientHandlers;
            this.protocol = new Protocol(server, server.multicastSocket);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientHandlers.add(this);
        } catch (IOException e) {
            System.err.println("Error creating client handler: " + e.getMessage());
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        receiveMessage();
    }

    private void receiveMessage() {

        new Thread(() -> {
            while (socket.isConnected() && !socket.isClosed()) {
                try {

                    String messageString = bufferedReader.readLine();

                    if (messageString == null) {
                        System.out.println("Client disconnected! -> " + socket.getInetAddress().getHostAddress() + ":"
                                + socket.getPort());
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        return;
                    }

                    System.out.println("Message received from " + socket.getInetAddress().getHostAddress() + ":"
                            + socket.getPort() + " -> " + messageString);

                    String responseMessage = protocol.processMessage(messageString);
                    System.out.println("Response message: " + responseMessage);

                    sendMessage(responseMessage);
                } catch (Exception e) {
                    System.err.println("Error receiving message: " + e.getMessage());
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();

    }

    protected void sendMessage(String sendMessage) {
        try {
            bufferedWriter.write(sendMessage);
            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
        }
    }

    /**
     * Function to close BufferedWriter and BufferedReader of a Socket
     *
     * @param socket         conection established with a specific client
     * @param bufferedReader reader of a specific client
     * @param bufferedWriter writer of a specific client
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null)
                bufferedReader.close();

            if (bufferedWriter != null)
                bufferedWriter.close();

            if (socket != null)
                socket.close();


            for (Channel channel : this.server.channelManager.getChannels().values()) {
                if (channel.isUserInChannel(this.protocol.User_ID)) {
                    channel.leave(this.protocol.User_ID);
                }
            }
            clientHandlers.remove(this);

            //Set referentes to null
            this.bufferedReader = null;
            this.bufferedWriter = null;
            this.socket = null;
            this.protocol = null;
            this.server = null;

            Server.getStats().decrementNumberOfConnectedUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
