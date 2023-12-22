package Requests;

import Entity.User;

public class UserRegister {
    private final User user;

    public UserRegister(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}
