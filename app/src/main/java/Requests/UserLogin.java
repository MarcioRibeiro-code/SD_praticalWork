package Requests;

import java.util.ArrayList;

import Entity.User;

public class UserLogin {

    private final User user;

    public UserLogin(User user) {
        this.user = user;

    }

    public User getUser() {
        return this.user;
    }

}
