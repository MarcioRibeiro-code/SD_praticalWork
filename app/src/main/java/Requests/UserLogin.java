package Requests;

import java.util.UUID;

import Entity.MilitarType;

public class UserLogin {

    private final String username;
    private final UUID uuid;
    private final MilitarType role;

    public UserLogin(String username, MilitarType role, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public MilitarType getRole() {
        return role;
    }

    public UUID getUuid() {
        return uuid;
    }

}
