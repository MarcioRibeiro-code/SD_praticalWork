package Entity;

public class User {

    // Incremental ID
    private static int LastAssignedID = 0;

    MilitarType militarType;

    private String name;

    private int ID;

    private String profile;

    /**
     * Constructor for input name and entity type only with default profile
     * @param name name of user
     * @param militarType type of entity
     */
    public User(String name, String militarType) {
        this.name = name;
        this.ID = ++LastAssignedID;
        this.profile = null;
        setMilitarType(militarType);
    }

    /**
     * Constructor for input name and profile description
     * @param name name of user
     * @param profile user profile description
     * @param militarType type of entity
     */
    public User(String name, String profile, String militarType) {
        this.name = name;
        this.ID = ++LastAssignedID;
        this.profile = profile;
        setMilitarType(militarType);
    }

    /**
     * Function to change value of MilitarType property receiving a string and convert
     * @param type entity type
     */
    private void setMilitarType(String type) {
        switch (type.toLowerCase()) {
            case "soldier":
                militarType = MilitarType.SOLDIER;
                break;
            case "cape":
                militarType = MilitarType.CAPE;
                break;
            case "seargent":
                militarType = MilitarType.SEARGENT;
                break;
        }
    }

    public MilitarType getMilitarType() {
        return militarType;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }

    public String getProfile() {
        return profile;
    }
}
