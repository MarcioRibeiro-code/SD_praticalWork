package Entity;

import java.util.Objects;
import java.util.UUID;

public class User {

    MilitarType militarType;

    private String name;
    private String password;
    private String username;

    private final UUID ID;

    private String profile;

    /**
     * Constructor for input name and entity type only with default profile
     * 
     * @param name        name of user
     * @param militarType type of entity
     */
    public User(String name, String militarType, String password, String username) {
        this.name = name;

        this.profile = null;
        this.password = password;
        this.username = username;
        this.militarType = MilitarType.fromString(militarType);
        this.ID = UUID.randomUUID();
    }

    /**
     * Constructor for input name and profile description
     * 
     * @param name        name of user
     * @param profile     user profile description
     * @param militarType type of entity
     */
    public User(String name, String profile, String militarType, String password, String username) {
        this.name = name;
        this.profile = profile;
        this.militarType = MilitarType.fromString(militarType);
        this.password = password;
        this.username = username;
        this.ID = UUID.randomUUID();
    }

    public MilitarType getMilitarType() {
        return militarType;
    }

    public String getName() {
        return name;
    }

    public UUID getID() {
        return ID;
    }

    public String getProfile() {
        return profile;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

}
