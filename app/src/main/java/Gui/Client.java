package Gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import Gui.Frames.InitialFrame;

import java.net.*;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.channels.NotYetConnectedException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import Gui.InitialFrame;

import Requests.UserLogin;
import utils.Channel.ChannelResponse;
import utils.Channel.GetChannelsResponse;
import utils.Channel.ReceivedMessages;
import utils.Channel.SendMessageToChannel;
import utils.Channel.SendMessageToUser;
import utils.Requests.Request;
import utils.Requests.RequestType;
import utils.Responses.Response;
import utils.Responses.ResponseStatus;

import javax.swing.*;

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
                    System.out.println(message);
                    processResponse(message);
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

    void processResponse(String responseMessage) {
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

            case FEEDBACK:
                Response<String> feedbackResponse = jsonHelper.<Response<String>>fromJson(responseMessage,
                        new TypeToken<Response<String>>() {
                        }.getType());

                showMessageDialog(userMenuFrame, feedbackResponse.getData());
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

                try {
                    InetAddress group = InetAddress.getByName(joinChannelResponse.getData());
                    this.multicastSocket.joinGroup(group);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

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

            case GET_USERS:
                Response<List<UserLogin>> getUsers = jsonHelper.<Response<List<UserLogin>>>fromJson(
                        responseMessage,
                        new TypeToken<Response<List<UserLogin>>>() {
                        }.getType());

                userMenuFrame.updateUsers(getUsers.getData());
                break;

            case GET_DIRECT_MESSAGE:
                Response<ReceivedMessages> messageResponse = jsonHelper.<Response<ReceivedMessages>>fromJson(
                        responseMessage,
                        new TypeToken<Response<ReceivedMessages>>() {

                        }.getType());

                userMenuFrame.updateDirectMessages(messageResponse.getData());
                break;

            case SEND_MESSAGE_TO_CHANNEL:
                Response<SendMessageToChannel> sendMessageToChannelResponse = jsonHelper
                        .<Response<SendMessageToChannel>>fromJson(
                                responseMessage,
                                new TypeToken<Response<SendMessageToChannel>>() {
                                }.getType());

                if (!checkStatus(userMenuFrame, sendMessageToChannelResponse)) {
                    return;
                }

                userMenuFrame.updateChannelText(sendMessageToChannelResponse.getData());
                break;

            case REQUEST_TASK:
                Response<SendMessageToChannel> taskResponse = jsonHelper.<Response<SendMessageToChannel>>fromJson(
                        responseMessage,
                        new TypeToken<Response<SendMessageToChannel>>() {
                        }.getType());

                System.out.println("Task RESPONSE: " + taskResponse.getData());
                userMenuFrame.updateTaskApprovable(taskResponse.getData());
                break;

            case APPROVE_TASK:
                Response<SendMessageToChannel> approvedTaskResponse = jsonHelper
                        .<Response<SendMessageToChannel>>fromJson(
                                responseMessage,
                                new TypeToken<Response<SendMessageToChannel>>() {
                                }.getType());

                if (!checkStatus(userMenuFrame, approvedTaskResponse)) {
                    return;
                }

                String approvedTaskMessage = approvedTaskResponse.getData().getMessage();

                // Define the regular expression pattern
                String pattern = "Task ->(.*?)\\n";

                // Create a Pattern object
                Pattern r = Pattern.compile(pattern);

                // Create a Matcher Object

                Matcher m = r.matcher(approvedTaskMessage);

                if (m.find()) {
                    // Extract the task value
                    String task = m.group(1);
                    userMenuFrame.approveTask(task);
                    JOptionPane.showMessageDialog(userMenuFrame, approvedTaskMessage);
                } else {
                    JOptionPane.showMessageDialog(userMenuFrame, "Error approving task");
                }
                break;

            case GET_INBOX:
                Response<List<String>> inboxResponse = jsonHelper.<Response<List<String>>>fromJson(
                        responseMessage,
                        new TypeToken<Response<List<String>>>() {
                        }.getType());

                //Convert to list of SendMessageToUser
                for (String inboxmessage : inboxResponse.getData()) {
                    SendMessageToUser sendMessageToUser = jsonHelper.<Response<SendMessageToUser>>fromJson(inboxmessage,
                            new TypeToken<Response<SendMessageToUser>>() {
                            }.getType()).getData();

                    System.out.println(sendMessageToUser.toString());
                    String inboxMessage = sendMessageToUser.getSenderUserName() + ": "
                            + sendMessageToUser.getMessage();

                    userMenuFrame.updateInbox(inboxMessage);
                }

              /*  if (!checkStatus(userMenuFrame, inboxResponse)) {
                    return;
                }

                for(SendMessageToUser sendMessageToUser : inboxResponse.getData()){
                    String inboxMessage = sendMessageToUser.getSenderUserName() + ": "
                            + sendMessageToUser.getMessage();

                    userMenuFrame.updateInbox(inboxMessage);
                }*/


                break;

            default:
                System.err.println("REQUEST NOT YET SUPPORTED -> " + type);
                break;

        }
    }
}
