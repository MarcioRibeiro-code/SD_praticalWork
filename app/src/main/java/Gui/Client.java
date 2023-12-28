package Gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import Gui.Frames.InitialFrame;

import java.net.Socket;
import java.net.MulticastSocket;

import java.net.DatagramPacket;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.channels.NotYetConnectedException;
//import Gui.InitialFrame;

import Requests.UserLogin;
import utils.Channel.GetChannelsResponse;
import utils.Requests.RequestType;
import utils.Responses.Response;
import utils.Responses.ResponseStatus;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class Client {
    private final MulticastSocket multicastSocket;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    // IMPORTANT: WHERE SHOULD I INSERT ROLE??
    // Needed to make a rbac system

    // TODO: MISSING IMPORT IN BUILD.GRADLE
    private final InitialFrame initialFrame;
    private UserMenuFrame userMenuFrame;

    public Client(MulticastSocket multicastSocket, String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.multicastSocket = multicastSocket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.initialFrame = new InitialFrame(this);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotYetConnectedException();
        }
    }

    // TODO: SEE IF THIS IS NEEDED
    /*
     * public void joinGroups(ArrayList<String> ips) throws IOException {
     * System.out.println("Joining groups...");
     * for (String ip : ips) {
     * multicastSocket.joinGroup(InetAddress.getByName(ip));
     * }
     * }
     */

    public void sendMessage(String message) {
        new Thread(() -> {
            if (message != null) {
                try {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    closeEverything(socket, reader, writer);
                }
            }
        }).start();
    }

    private void receiveMessagesMulticast() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    processResponse(message);

                    System.out.println("Received message: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void receiveMessages() {
        new Thread(() -> {
            while (true) {
                try {
                    String responseMessage = reader.readLine();

                    if (responseMessage == null)
                        closeEverything(socket, reader, writer);

                    processResponse(responseMessage);

                    System.out.println(responseMessage);

                } catch (IOException e) {
                    closeEverything(socket, reader, writer);
                    break;
                }
            }
        }).start();
    }

    private void closeEverything(Socket socket2, BufferedReader reader2, BufferedWriter writer2) {
        try {
            if (reader2 != null) {
                reader2.close();
            }

            if (writer2 != null) {
                writer2.close();
            }

            if (socket2 != null) {
                socket2.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void processResponse(String responseMessage) {
        Gson jsonHelper = new GsonBuilder().serializeNulls().create();

        Response<Object> response = jsonHelper.<Response<Object>>fromJson(responseMessage, Response.class);
        if (response == null) {
            return;
        }

        RequestType type = response.getType();
        switch (type) {
            case LOGIN:
                Response<UserLogin> loginResponse = jsonHelper.<Response<UserLogin>>fromJson(responseMessage,
                        new TypeToken<Response<UserLogin>>() {
                        }.getType());

                if (!checkStatus(initialFrame, loginResponse)) {
                    return;
                }

                UserLogin userLogin = loginResponse.getData();
                System.out.println(userLogin.getUuid());

                initialFrame.showUserMenuFrame(userLogin);
                break;

            case REGISTER:
                Response<String> registerResponse = jsonHelper.<Response<String>>fromJson(responseMessage,
                        new TypeToken<Response<String>>() {
                        }.getType());

                if (!checkStatus(initialFrame, registerResponse)) {
                    return;
                }

                String message = registerResponse.getData();

                showMessageDialog(initialFrame, message);
                break;

            /*
             * case GET_CHANNELS:
             * Response<GetChannelsResponse> getChannelsResponse =
             * jsonHelper.<Response<GetChannelsResponse>>fromJson(
             * responseMessage,
             * new TypeToken<Response<GetChannelsResponse>>() {
             * }.getType());
             * 
             * if (!checkStatus(userMenuFrame, getChannelsResponse)) {
             * return;
             * }
             * 
             * Set<ChannelResponse> channels = getChannelsResponse.getData().getChannels();
             * System.out.println(channels.toString());
             * 
             * userMenuFrame.updateChannels(channels.stream()
             * .map(ChannelResponse::toString)
             * .toArray(String[]::new));
             * break;
             */

            case JOIN_CHANNEL:
                Response<String> joinChannelResponse = jsonHelper.<Response<String>>fromJson(responseMessage,
                        new TypeToken<Response<String>>() {
                        }.getType());

                if (!checkStatus(userMenuFrame, joinChannelResponse)) {
                    return;
                }

                System.out.println(joinChannelResponse.getData());

                showMessageDialog(userMenuFrame, joinChannelResponse.getData());
                break;

            case GET_JOINED_CHANNELS:
                Response<GetChannelsResponse> getJoinedChannelsResponse = jsonHelper
                        .<Response<GetChannelsResponse>>fromJson(
                                responseMessage,
                                new TypeToken<Response<GetChannelsResponse>>() {
                                }.getType());

                if (!checkStatus(userMenuFrame, getJoinedChannelsResponse)) {
                    return;
                }
                userMenuFrame.updateJoinedChannels(getJoinedChannelsResponse.getData().getChannels());
                break;

            case GET_JOINABLE_CHANNELS:
                Response<GetChannelsResponse> getJoinableChannelsResponse = jsonHelper
                        .<Response<GetChannelsResponse>>fromJson(
                                responseMessage,
                                new TypeToken<Response<GetChannelsResponse>>() {
                                }.getType());

                if (!checkStatus(userMenuFrame, getJoinableChannelsResponse)) {
                    return;
                }

                userMenuFrame.updateJoinableChannels(getJoinableChannelsResponse.getData().getChannels());
                break;

            default:
                break;

        }
    }

    private boolean checkStatus(Component parent, Response<?> response) {
        if (response.getStatus() == ResponseStatus.ERROR) {
            showMessageDialog(parent, response.getMessage(), "Error:" + response.getType(), ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public UserMenuFrame getUserMenuFrame() {
        return userMenuFrame;
    }

    public static void main(String[] args) {
        try {
            // TODO: MAKE A FILE WITH THE CONFIGURATIONS (MULTICAST PORTS, SERVER PORT,
            // SERVER IP, ETC) and change files affected
            MulticastSocket multicastSocket = new MulticastSocket(4445);
            Client client = new Client(multicastSocket, "localhost", 2048);
            client.receiveMessages();
            client.receiveMessagesMulticast();
        } catch (NotYetConnectedException | IOException e) {
            showMessageDialog(null, "Unable to connect to the server.", "", ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void setUserMenuFrame(UserMenuFrame userMenuFrame) {
        this.userMenuFrame = userMenuFrame;
    }
}
