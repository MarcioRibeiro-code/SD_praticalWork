package Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import Entity.User;
import Requests.CreateChannel;
import Requests.Login;
import Requests.UserLogin;
import Requests.UserRegister;
import utils.Channel.JoinChannel;
import utils.Channel.SendMessageToChannel;
import utils.Requests.Request;
import utils.Requests.RequestType;
import utils.Responses.Response;
import utils.Responses.ResponseStatus;

public class Protocol {

    private final JsonFileHelper jsonFileHelper;
    private final Gson jsonHelper;
    private final ClientHandler clientHandler;
    private final Server server;
    private MulticastSocket multicastSocket;
    private DatagramSocket datagramSocket;

    public Protocol(ClientHandler clientHandler, Server server) throws IOException {
        this.clientHandler = clientHandler;
        this.server = server;
        this.jsonFileHelper = new JsonFileHelper("files/");
        this.jsonHelper = new Gson();
        this.multicastSocket = new MulticastSocket(Server.port);
        this.datagramSocket = new DatagramSocket(Server.port);
    }

    protected synchronized String processMessage(String requestMessage) {

        RequestType requestType;
        List<User> militaryList;
        System.out.println(requestMessage);
        try {
            requestType = jsonHelper.fromJson(requestMessage, Request.class).getType();
            militaryList = this.jsonFileHelper.deserializeArray("users", User[].class);
        } catch (Exception e) {
            return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
        }

        switch (requestType) {
            case LOGIN:
                return userLoginHandler(requestMessage, militaryList);

            case REGISTER:
                return userRegisterHandler(requestMessage, militaryList);

            case CREATE_CHANNEL:
                return createChannelHandler(requestMessage);

            case JOIN_CHANNEL:
                return joinChannelHandler(requestMessage);

            case SEND_MESSAGE_TO_CHANNEL:
                return sendMessageToChannelHandler(requestMessage, militaryList);
            
            default:
                return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
        }
    }

    private String sendMessageToChannelHandler(String requestMessage, List<User> militaryList) {
        try {
            SendMessageToChannel sendMessageToChannel = this.jsonHelper
                    .<Request<SendMessageToChannel>>fromJson(requestMessage,
                            new TypeToken<Request<SendMessageToChannel>>() {
                            }.getType())
                    .getData();

            boolean channelExists = this.server.channelManager.getChannels()
                    .get(sendMessageToChannel.getChannelName()) != null;

            if (!channelExists) {
                return this.jsonHelper
                        .toJson(new Response<>(ResponseStatus.ERROR,
                                RequestType.SEND_MESSAGE_TO_CHANNEL,
                                "Channel doesn't exists"));
            }

            this.server.channelManager.sendMessageToChannel(sendMessageToChannel.getChannelName(),
                    this.clientHandler.username, sendMessageToChannel.getMessage(), this.multicastSocket);

            return this.jsonHelper.toJson(new Response<>(ResponseStatus.SUCCESS,
                    RequestType.SEND_MESSAGE_TO_CHANNEL,
                    sendMessageToChannel.getChannelName()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
        }
    }

    private String joinChannelHandler(String requestMessage) {

        try {
            JoinChannel joinChannel = this.jsonHelper.<Request<JoinChannel>>fromJson(requestMessage,
                    new TypeToken<Request<JoinChannel>>() {
                    }.getType()).getData();

            boolean channelExists = this.server.channelManager.getChannels()
                    .get(joinChannel.getChannelName()) != null;

            if (!channelExists) {
                return this.jsonHelper
                        .toJson(new Response<>(ResponseStatus.ERROR,
                                RequestType.JOIN_CHANNEL,
                                "Channel doesn't exists"));
            }
            this.server.channelManager.joinChannel(joinChannel.getChannelName(), joinChannel.getUserID(),
                    this.multicastSocket);

            return this.jsonHelper.toJson(new Response<>(ResponseStatus.SUCCESS,
                    RequestType.JOIN_CHANNEL,
                    joinChannel.getChannelName()));
        } catch (Exception e) {
            return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
        }

    }

    private String createChannelHandler(String requestMessage) {
        synchronized (server.channelManager) {
            try {
                CreateChannel createChannel = this.jsonHelper.<Request<CreateChannel>>fromJson(requestMessage,
                        new TypeToken<Request<CreateChannel>>() {
                        }.getType()).getData();

                boolean channelExists = this.server.channelManager.getChannels()
                        .get(createChannel.getChannel().getName()) != null;

                if (channelExists) {
                    return this.jsonHelper
                            .toJson(new Response<>(ResponseStatus.ERROR,
                                    RequestType.CREATE_CHANNEL,
                                    "Channel already exists"));
                }

                this.server.channelManager.createChannel(createChannel.getChannel().getIp(),
                        createChannel.getChannel().getName());
                // TODO: SEE THIS, I'M NOT SURE IF IT'S RIGHT
                this.jsonFileHelper.serialize("channels",
                        new HashSet<>(this.server.channelManager.getChannels().values()));

                return this.jsonHelper.toJson(new Response<>(ResponseStatus.SUCCESS, RequestType.CREATE_CHANNEL,
                        createChannel.getChannel()));
            } catch (Exception e) {
                return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
            }
        }
    }

    private String userRegisterHandler(String requestMessage, List<User> militaryList) {
        try {
            UserRegister userRegister = this.jsonHelper.<Request<UserRegister>>fromJson(requestMessage,
                    new TypeToken<Request<UserRegister>>() {
                    }.getType()).getData();

            boolean userExists = militaryList.stream()
                    .anyMatch(user -> user.getUsername().equals(userRegister.getUser().getUsername()));

            if (userExists) {
                return this.jsonHelper
                        .toJson(new Response<>(ResponseStatus.ERROR,
                                RequestType.REGISTER,
                                "User already exists"));
            }

            // String name, String militarType, String password, String username
            User newUser = userRegister.getUser();

            militaryList.add(newUser);

            // TODO: SEE THIS, I'M NOT SURE IF IT'S RIGHT
            this.jsonFileHelper.serialize("users", new HashSet<>(militaryList));

            return this.jsonHelper.toJson(new Response<>(ResponseStatus.SUCCESS, RequestType.REGISTER, newUser));

        } catch (Exception e) {
            return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
        }
    }

    private String userLoginHandler(String requestMessage, List<User> militaryList) {
        try {

            Login login = this.jsonHelper.<Request<Login>>fromJson(requestMessage, new TypeToken<Request<Login>>() {
            }.getType()).getData();

            User userdb = militaryList.stream().filter(user -> user.getPassword().equals(login.getPassword())
                    && user.getName().equals(login.getUsername())).findFirst().orElse(null);

            if (userdb == null) {
                return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid credentials"));
            }

            server.channelManager.joinChannel("main", userdb.getID(),
                    this.multicastSocket);

            server.channelManager.joinChannel(userdb.getMilitarType().getTypeString(), userdb.getID(),
                    this.multicastSocket);

            UserLogin userLogin = new UserLogin(userdb);
            this.clientHandler.username = userdb.getUsername();

            return this.jsonHelper.toJson(new Response<>(ResponseStatus.SUCCESS, RequestType.LOGIN, userLogin));

        } catch (Exception e) {
            return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
        }
    }

}
