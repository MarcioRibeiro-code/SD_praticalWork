package LocalNode;


import java.util.ArrayList;

/**
 * Class store info about a localNode Login(basictly of a use, user is a localNode basictly)
 */
public class LocalNodeLogin {

    private LocalNode localNode;
    private ArrayList<String> ListIpsToJoin;

    public LocalNodeLogin(LocalNode localNode, ArrayList<String> listIpsToJoin) {
        this.localNode = localNode;
        ListIpsToJoin = listIpsToJoin;
    }

    public LocalNode getLocalNode() {
        return localNode;
    }

    public ArrayList<String> getListIpsToJoin() {
        return ListIpsToJoin;
    }
}
