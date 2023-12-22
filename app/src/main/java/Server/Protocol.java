package Server;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import Entity.User;
import Requests.Login;
import Requests.UserLogin;
import Requests.UserRegister;
import utils.Requests.Request;
import utils.Requests.RequestType;
import utils.Responses.Response;
import utils.Responses.ResponseStatus;

public class Protocol {

    private final JsonFileHelper jsonFileHelper;
    private final Gson jsonHelper;
    private final ClientHandler clientHandler;
    private final Server server;

    public Protocol(ClientHandler clientHandler, Server server) throws IOException {
        this.clientHandler = clientHandler;
        this.server = server;
        this.jsonFileHelper = new JsonFileHelper("files/");
        this.jsonHelper = new Gson();
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

            default:
                return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
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

            ArrayList<String> ipsToJoin = new ArrayList<String>();
            ipsToJoin.add(0, Server.MAIN_GROUP_IP);
            ipsToJoin.add(1, Server.MILITAR_GROUPS.get(userdb.getMilitarType().name()));

            UserLogin userLogin = new UserLogin(userdb, ipsToJoin);
            this.clientHandler.username = userdb.getUsername();

            return this.jsonHelper.toJson(new Response<>(ResponseStatus.SUCCESS, RequestType.LOGIN, userLogin));

        } catch (Exception e) {
            return this.jsonHelper.toJson(new Response<>(ResponseStatus.ERROR, "Invalid request"));
        }
    }

}
