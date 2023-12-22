package Requests;

import java.util.ArrayList;

import Entity.User;

public class UserLogin {

    private final User user;
    private final ArrayList<String> listIpsToJoin;

    public UserLogin(User user, ArrayList<String> listIpsToJoin) {
        this.user = user;
        this.listIpsToJoin = listIpsToJoin;
    }

    public User getUser() {
        return this.user;
    }

    public ArrayList<String> getListIpsToJoin() {
        return this.listIpsToJoin;
    }

}
