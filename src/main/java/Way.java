import java.util.ArrayList;

/**
 * Created by caitlinlee on 4/18/18
 */

public class Way {
    long wayid;
    String name;
    ArrayList<Long> listOfNodes;
    String flag;

    public Way(long id) {
        this.wayid = id;
        this.listOfNodes = new ArrayList<>();
    }
}


