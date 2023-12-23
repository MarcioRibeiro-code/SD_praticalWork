package LocalNode;

import java.util.List;

public class LocalNode {
    private final String name;
    private final String password;
    private final String id;

    //Make sense store Channels there? I will store in LocalNodeLogin
    //private final List<String> channels;




    public LocalNode(String name, String password, String id, List<String> channels) {
        this.name = name;
        this.password = password;
        this.id = id;
        //this.channels = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

}
