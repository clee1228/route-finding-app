import java.util.ArrayList;

/**
 * Created by caitlinlee on 4/18/18
 */

public class Node implements Comparable<Node> {
    long id;
    double lat;
    double lon;
    String flag;
    String name;
    ArrayList<Long> linkID;
    ArrayList<Node> linkNodes;
    Node parent;
    double svDist;


    public Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        linkID = new ArrayList<>();
        linkNodes = new ArrayList<>();
    }

    public void nodeName(String thisName) {
        name = thisName;
    }

    public ArrayList<Long> getLinks() {
        return linkID;
    }

    public ArrayList<Node> getNodes() {
        return linkNodes;
    }

    @Override
    public boolean equals(Object a) {
        if (a == null) {
            return false;
        } else if (getClass() != a.getClass()) {
            return false;
        }
        Node node = (Node) a;
        return this.id == node.id;
    }


    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(Node node) {
        if (this.svDist == node.svDist) {
            return 0;
        } else if (this.svDist < node.svDist) {
            return -1;
        } else {
            return 1;
        }
    }
}
