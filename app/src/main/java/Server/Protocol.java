package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import java.util.logging.Level;
import java.util.logging.Logger;

import Entity.MilitarType;
import Gui.Client;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import Entity.User;
import Requests.CreateChannel;
import Requests.Login;
import Requests.UserLogin;
import Requests.UserRegister;
import utils.Stats;
import utils.Channel.*;
import utils.Requests.Request;
import utils.Requests.RequestType;
import utils.Responses.Response;
import utils.Tasks.ApproveTask;

public class Protocol {

    protected UUID User_ID;
    private final JsonFileHelper jsonFileHelper;
    private final Gson jsonHelper;
    private final Server server;
    private MulticastSocket multicastSocket;
    private static final Logger logger = Logger.getLogger(Protocol.class.getName());


    public Protocol(Server server, MulticastSocket multicastSocket) throws IOException {
        this.server = server;
        this.jsonFileHelper = new JsonFileHelper("files/");
        this.jsonHelper = new Gson();
        this.multicastSocket = multicastSocket;

        new Thread(() -> {
            while (true) {
                try {
                    // EVERY 2 MINUTES
                    Thread.sleep(120000);

                    // Get the stats
                    String statusString = Server.getStats().toString();

                    String statsJson = jsonHelper.toJson(Response.success(RequestType.FEEDBACK,
                            statusString));
                    // Send to all users
                    this.server.getClientHandlers().forEach(clientHandler -> {
                        clientHandler.sendMessage(statsJson);
                    });

                } catch (Exception e) {
                    System.out.println("Error sending stats");
                }
            }

        }).start();
    }

    private List<String> processInbox() {
        if (server.inbox.isEmpty()) {
            System.out.println("INBOX EMPTY");
            return null;
        }
        if (!server.inbox.containsKey(User_ID)) {
            System.out.println("DOENST CONTAIN ID");
            return null;
        }

        List<String> inbox = new ArrayList<>(server.inbox.get(User_ID));

        if (inbox.isEmpty()) {
            return null;
        }


        return inbox;
    }

    protected synchronized String processMessage(String requestMessage) {

        RequestType requestType;
        List<User> militaryList;
        logger.log(Level.INFO, "Received Request from client: {0}", requestMessage);
        try {
            requestType = this.jsonHelper.fromJson(requestMessage, Request.class).getType();
            militaryList = this.jsonFileHelper.deserializeArray("users", User[].class);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while processing the request.", e);
            return this.jsonHelper.toJson(Response.error(null, requestMessage));
        }

        switch (requestType) {
            case LOGIN:
                return userLoginHandler(requestMessage, militaryList);

            case REGISTER:
                return userRegisterHandler(requestMessage, militaryList);

            case CREATE_CHANNEL:
                return createChannelHandler(requestMessage);

            case GET_CHANNELS:
                Collection<Channel> channels = this.server.channelManager.getChannels().values();

                Set<ChannelResponse> channelResponses = channels.stream()
                        .map(channel -> new ChannelResponse(channel.getName()))
                        .collect(Collectors.toSet());

                GetChannelsResponse response = new GetChannelsResponse(new HashSet<>(channelResponses));

                return this.jsonHelper.toJson(Response.success(RequestType.GET_CHANNELS, response));

            case GET_JOINABLE_CHANNELS:
                return getJoinableChannelsHandler(requestMessage, militaryList);

            case GET_JOINED_CHANNELS:
                return getJoinedChannelsHandler(requestMessage);

            case GET_USERS:
                List<UserLogin> users = new ArrayListSync<UserLogin>();
                for (User user : militaryList) {
                    users.add(new UserLogin(user.getUsername(), user.getMilitarType(), user.getID()));
                }

                return this.jsonHelper.toJson(Response.success(RequestType.GET_USERS, users));

            case JOIN_CHANNEL:
                return joinChannelHandler(requestMessage, militaryList);

            case SEND_MESSAGE_TO_CHANNEL:
                return sendMessageToChannelHandler(requestMessage, militaryList);

            case SEND_MESSAGE_TO_USER:
                return sendMessageToUserHandler(requestMessage);

            case REQUEST_TASK:
                return sendTaskHandler(requestMessage, militaryList);

            case APPROVE_TASK:
                return aproveTaskHandler(requestMessage);

            case GET_INBOX:
                List<String> inbox = processInbox();
                if (inbox == null || inbox.isEmpty()) {
                    return this.jsonHelper.toJson(Response.success(RequestType.FEEDBACK, "Inbox vazia"));
                }

                return this.jsonHelper.toJson(Response.success(RequestType.GET_INBOX, inbox));

            default:
                logger.log(Level.SEVERE, "Invalid Request Type: {0}", requestType);
                return this.jsonHelper.toJson(Response.error(requestType, "Invalid Request Type"));
        }
    }

    private String aproveTaskHandler(String requestMessage) {
        try {
            ApproveTask approveTaskRequest = this.jsonHelper.<Request<ApproveTask>>fromJson(requestMessage,
                    new TypeToken<Request<ApproveTask>>() {
                    }.getType()).getData();

            String task = approveTaskRequest.getTask();
            String username = approveTaskRequest.getApprovedUsername();
            MilitarType militarType = approveTaskRequest.getApprovedUserRole();

            String message = "Task ->" + task + "\nApproved by: " + username + " " + militarType;

            this.server.channelManager.sendMessageToChannel(RequestType.APPROVE_TASK, "main", username, message,
                    this.multicastSocket);

            Server.getStats().incrementTotalOfApprovedTasks();
            return jsonHelper.toJson(Response.success(RequestType.FEEDBACK,
                    "Task Approved: " + task));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while approving the task", e);
            return this.jsonHelper.toJson(Response.error(RequestType.APPROVE_TASK,
                    "Some error occurred in Approve Task"));
        }

    }

    private String sendTaskHandler(String requestMessage, List<User> militaryList) {
        try {
            SendMessageToChannel sendMsgRequest = this.jsonHelper
                    .<Request<SendMessageToChannel>>fromJson(requestMessage,
                            new TypeToken<Request<SendMessageToChannel>>() {
                            }.getType())
                    .getData();

            String channelName = sendMsgRequest.getChannelName();
            String sender = sendMsgRequest.getSender();
            String message = sendMsgRequest.getMessage();
            RequestType type = sendMsgRequest.getType();

            Channel channel = this.server.channelManager.getChannels()
                    .get(sendMsgRequest.getChannelName());
            boolean channelExists = channel != null;

            if (!channelExists) {
                logger.log(Level.SEVERE, "Channel doesn't exists");
                return this.jsonHelper
                        .toJson(Response.error(RequestType.REQUEST_TASK, "Channel doesn't exists"));
            }

            User user = militaryList.stream()
                    .filter(military -> military.getUsername().equals(sender)).findFirst()
                    .orElse(null);

            if (user == null) {
                return this.jsonHelper.toJson(Response.error(RequestType.REQUEST_TASK, "User doesn't exists"));
            }
            if (!channel.isUserInChannel(user.getID())) {
                return this.jsonHelper.toJson(Response.error(RequestType.REQUEST_TASK, "User must enter channel" + channel));
            }

            this.server.channelManager.sendMessageToChannel(type, channelName, sender, message, this.multicastSocket);
            logger.log(Level.INFO, "Task sent");
            Server.getStats().incrementTotalOfRequestedTasks();
            return jsonHelper.toJson(Response.success(RequestType.FEEDBACK,
                    "Task sent to Approval " + channelName));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while sending task.", e);
            return this.jsonHelper.toJson(Response.error(RequestType.REQUEST_TASK,
                    "Some error occurred in Send Task"));
        }
    }

    private String getJoinedChannelsHandler(String requestMessage) {
        try {

            String userID = this.jsonHelper.<Request<String>>fromJson(requestMessage,
                    new TypeToken<Request<String>>() {
                    }.getType()).getData();

            if (userID == null) {
                logger.log(Level.SEVERE, "UserID is null");
                return this.jsonHelper.toJson(Response.error(RequestType.GET_JOINED_CHANNELS, "UserID must be set"));
            }

            Set<ChannelResponse> joinedChannels = this.getJoinedChannels(UUID.fromString(userID)).stream()
                    .map(channel -> new ChannelResponse(channel.getName())).collect(Collectors.toSet());

            return this.jsonHelper.toJson(Response.success(RequestType.GET_JOINED_CHANNELS,
                    new GetChannelsResponse(joinedChannels)));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while getting joined channels.", e);
            return this.jsonHelper.toJson(Response.error(RequestType.GET_JOINED_CHANNELS,
                    "Some error occurred in Get Joined Channels"));
        }
    }

    private Set<Channel> getJoinedChannels(UUID userID) {
        return this.server.channelManager.getChannels().values().stream()
                .filter(channel -> this.server.channelManager.isUserInChannel(channel.getName(), userID))
                .collect(Collectors.toSet());
    }

    private String getJoinableChannelsHandler(String requestMessage, List<User> militaryList) {
        try {
            UUID userID = UUID.fromString(this.jsonHelper.<Request<String>>fromJson(requestMessage,
                    new TypeToken<Request<String>>() {
                    }.getType()).getData());

            if (userID == null) {
                logger.log(Level.SEVERE, "UserID is null");
                return this.jsonHelper.toJson(Response.error(RequestType.GET_JOINABLE_CHANNELS, "UserID must be set"));
            }

            User user = militaryList.stream()
                    .filter(military -> military.getID().equals(userID)).findFirst()
                    .orElse(null);

            if (user == null) {
                logger.log(Level.SEVERE, "User doesn't exists");
                return this.jsonHelper.toJson(Response.error(RequestType.GET_JOINABLE_CHANNELS, "User doesn't exists"));
            }

            Set<ChannelResponse> joinableChannels = this.server.channelManager.getChannels().values().stream()
                    .filter(channel -> channel.getAuthorization().isPrivate()
                            && channel.getAuthorization().getRole().equals(user.getMilitarType()))
                    .map(channel -> new ChannelResponse(channel.getName())).collect(Collectors.toSet());

            joinableChannels.addAll(this.server.channelManager.getChannels().values().stream()
                    .filter(channel -> !channel.getAuthorization().isPrivate())
                    .map(channel -> new ChannelResponse(channel.getName())).collect(Collectors.toSet()));

            // NOW REMOVE THE CHANNELS THAT THE USER IS ALREADY IN
            Set<ChannelResponse> joinedChannels = this.getJoinedChannels(userID).stream()
                    .map(channel -> new ChannelResponse(channel.getName())).collect(Collectors.toSet());

            System.out.println(joinableChannels.removeAll(joinedChannels));

            return this.jsonHelper.toJson(Response.success(RequestType.GET_JOINABLE_CHANNELS,
                    new GetChannelsResponse(joinableChannels)));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while getting joinable channels.", e);
            return this.jsonHelper.toJson(Response.error(RequestType.GET_JOINABLE_CHANNELS,
                    "Some error occurred in Get Joinable Channels"));
        }
    }

    private String sendMessageToUserHandler(String RequestMessage) {
        try {
            SendMessageToUser sendMessageToUser = this.jsonHelper
                    .<Request<SendMessageToUser>>fromJson(RequestMessage,
                            new TypeToken<Request<SendMessageToUser>>() {
                            }.getType())
                    .getData();

            UUID receiver = sendMessageToUser.getReceiver();
            String Message = sendMessageToUser.getMessage();
            Message = sendMessageToUser.getSenderUserName() + ": " + Message;

            List<ClientHandler> listClientHandlers = this.server.getClientHandlers();

            for (ClientHandler temp : listClientHandlers) {
                if (temp.protocol.User_ID != null) {
                    if (temp.protocol.User_ID.equals(receiver)) {
                        String temp2 = this.jsonHelper.toJson(Response.success(RequestType.GET_DIRECT_MESSAGE,
                                new ReceivedMessages(Message)));
                        temp.sendMessage(temp2);
                        // temp.sendMessage(Message);
                        return this.jsonHelper.toJson(Response.success(RequestType.FEEDBACK, "Message sent to user"));
                    }
                }
            }

            ConcurrentHashMap<UUID, ArrayListSync<String>> list = server.inbox;

            if (list.containsKey(receiver)) {
                ArrayListSync<String> userMessages = list.get(receiver);

                userMessages.add(RequestMessage);

                list.put(receiver, userMessages);
            } else {
                ArrayListSync<String> userInbox = new ArrayListSync<String>();
                userInbox.add(RequestMessage);
                list.put(receiver, userInbox);
            }

            return this.jsonHelper
                    .toJson(Response.success(RequestType.FEEDBACK, "Message sent to user and went to inbox"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String sendMessageToChannelHandler(String requestMessage, List<User> militaryList) {
        try {
            SendMessageToChannel sendMsgRequest = this.jsonHelper
                    .<Request<SendMessageToChannel>>fromJson(requestMessage,
                            new TypeToken<Request<SendMessageToChannel>>() {
                            }.getType())
                    .getData();

            String channelName = sendMsgRequest.getChannelName();
            String sender = sendMsgRequest.getSender();
            String message = sendMsgRequest.getMessage();
            message = sender + ": " + message;
            RequestType type = sendMsgRequest.getType();

            boolean channelExists = this.server.channelManager.getChannels()
                    .get(sendMsgRequest.getChannelName()) != null;

            if (!channelExists) {
                logger.log(Level.SEVERE, "Channel doesn't exists");
                return this.jsonHelper
                        .toJson(Response.error(RequestType.SEND_MESSAGE_TO_CHANNEL, "Channel doesn't exists"));
            }

            // Get the user from the list of users, and check if the user is in the channel
            User senderUser = militaryList.stream()
                    .filter(user -> user.getUsername().equals(sendMsgRequest.getSender())).findFirst()
                    .orElse(null);

            if (senderUser == null) {
                logger.log(Level.SEVERE, "User doesn't exists");
                return this.jsonHelper.toJson(Response.error(RequestType.SEND_MESSAGE_TO_CHANNEL,
                        "User doesn't exists"));
            }

            if (!this.server.channelManager.isUserInChannel(channelName, senderUser.getID())) {
                logger.log(Level.SEVERE, "Sender user is not in the channel");
                return jsonHelper.toJson(
                        Response.error(RequestType.SEND_MESSAGE_TO_CHANNEL, "Sender user is not in the channel"));
            }

            this.server.channelManager.sendMessageToChannel(type, channelName, sender, message, this.multicastSocket);
            logger.log(Level.INFO, "Message sent to channel {0}", channelName);
            return jsonHelper.toJson(Response.success(RequestType.FEEDBACK,
                    "Message sent to channel " + channelName));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while sending message to channel.", e);
            return this.jsonHelper.toJson(Response.error(RequestType.SEND_MESSAGE_TO_CHANNEL,
                    "Some error occurred in Send Message to Channel"));
        }
    }

    private String joinChannelHandler(String requestMessage, List<User> militaryList) {

        try {
            JoinChannel joinChannel = this.jsonHelper.<Request<JoinChannel>>fromJson(requestMessage,
                    new TypeToken<Request<JoinChannel>>() {
                    }.getType()).getData();

            Channel channel = this.server.channelManager.getChannels()
                    .get(joinChannel.getChannelName());

            User user = militaryList.stream()
                    .filter(military -> military.getID().equals(joinChannel.getUserID())).findFirst()
                    .orElse(null);

            if (user == null) {
                return this.jsonHelper.toJson(Response.error(RequestType.JOIN_CHANNEL, "User doesn't exists"));
            }
            if (channel == null) {
                return this.jsonHelper
                        .toJson(Response.error(RequestType.JOIN_CHANNEL, "Channel doesn't exists"));
            }

            // IF THE CHANNEL IS PRIVATE, check if the user is allowed to join by comparing
            // the role

            if (channel.getAuthorization().isPrivate()) {
                if (!channel.getAuthorization().getRole().equals(user.getMilitarType())) {
                    return this.jsonHelper.toJson(Response.error(RequestType.JOIN_CHANNEL,
                            "User is not allowed to join this channel"));
                }
            }
            String address = this.server.channelManager.joinChannel(joinChannel.getChannelName(),
                    joinChannel.getUserID(),
                    this.multicastSocket);

            return this.jsonHelper.toJson(Response.success(RequestType.JOIN_CHANNEL,
                    address));
        } catch (Exception e) {
            return this.jsonHelper.toJson(Response.error(RequestType.JOIN_CHANNEL,
                    "Some error occurred in Join Channel : " + e.getMessage()));
        }

    }

    private String createChannelHandler(String requestMessage) {
        synchronized (server.channelManager) {
            try {
                CreateChannel createChannel = this.jsonHelper.<Request<CreateChannel>>fromJson(requestMessage,
                        new TypeToken<Request<CreateChannel>>() {
                        }.getType()).getData();

                boolean channelExists = this.server.channelManager.getChannels()
                        .get(createChannel.channelName()) != null;

                if (channelExists) {
                    logger.log(Level.SEVERE, "Channel already exists");
                    return this.jsonHelper
                            .toJson(Response.error(RequestType.CREATE_CHANNEL, "Channel already exists"));
                }

                String address = this.server.channelManager.createChannel(createChannel.channelName(),
                        createChannel.isPrivate(), createChannel.role());
                InetAddress group = InetAddress.getByName(address);
                this.multicastSocket.joinGroup(group);
                // TODO: SEE THIS, I'M NOT SURE IF IT'S RIGHT
                this.jsonFileHelper.serialize("channels",
                        new HashSet<>(this.server.channelManager.getChannels().values()));

                logger.log(Level.INFO, "Channel {0} created", createChannel.channelName());
                Server.getStats().incrementNumberOfChannels();
                return this.jsonHelper.toJson(Response.success(RequestType.CREATE_CHANNEL,
                        "Channel " + createChannel.channelName() + " created"));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "An error occurred while creating channel.", e);
                return this.jsonHelper.toJson(Response.error(RequestType.CREATE_CHANNEL,
                        "Some error occurred in Create Channel"));
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
                logger.log(Level.SEVERE, "User already exists");
                return this.jsonHelper
                        .toJson(Response.error(RequestType.REGISTER, "User already exists"));
            }

            // String name, String militarType, String password, String username
            User newUser = userRegister.getUser();

            ArrayList<User> copyList = new ArrayListSync<User>(militaryList);

            copyList.add(newUser);

            // TODO: SEE THIS, I'M NOT SURE IF IT'S RIGHT
            this.jsonFileHelper.serialize("users", new HashSet<>(copyList));
            logger.log(Level.INFO, "User {0} registered", userRegister.getUser().getUsername());
            return this.jsonHelper.toJson(Response.success(RequestType.REGISTER,
                    "User " + userRegister.getUser().getUsername() + " registered"));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return this.jsonHelper.toJson(Response.error(RequestType.REGISTER, "Error occurred in User Register"));
        }
    }

    private String userLoginHandler(String requestMessage, List<User> militaryList) {
        try {

            Login login = this.jsonHelper.<Request<Login>>fromJson(requestMessage, new TypeToken<Request<Login>>() {
            }.getType()).getData();

            User userdb = militaryList.stream().filter(user -> user.getPassword().equals(login.getPassword())
                    && user.getUsername().equals(login.getUsername())).findFirst().orElse(null);

            if (userdb == null) {
                logger.log(Level.SEVERE, "Invalid username or password");
                return this.jsonHelper.toJson(Response.error(RequestType.LOGIN, "Invalid username or password"));
            }

            UserLogin userLogin = new UserLogin(userdb.getUsername(), userdb.getMilitarType(), userdb.getID());

            this.User_ID = userdb.getID();

            logger.log(Level.INFO, "User {0} logged in", userdb.getUsername());
            Server.getStats().incrementNumberOfConnectedUsers();
            return this.jsonHelper.toJson(Response.success(RequestType.LOGIN, userLogin));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while logging in.", e);
            return this.jsonHelper.toJson(Response.error(RequestType.LOGIN, "Error occurred in User Login"));
        }
    }

}
