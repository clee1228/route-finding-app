import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;


/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /**
     * Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc.
     */
    private HashMap<Long, Node> graph;
    private Node lastNode;
    private Way way;

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        graph = new HashMap<>();
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> toRemove = new ArrayList<>();
        Collection<Node> v = graph.values();
        for (Node item : v) {
            if (item.linkID.size() == 0) {
                toRemove.add(item.id);
            }
        }

        for (int i = 0; i < toRemove.size(); i++) {
            graph.remove(toRemove.get(i));

        }
    }


    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return graph.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     *
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        return (ArrayList<Long>) graph.get(v).getLinks().clone();
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
<<<<<<< HEAD

        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    double distance(double lonV, double latV, double lonW, double latW) {
=======
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
>>>>>>> 27400cffb6a4bb269b7f9c88796f94fa70800885
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {

        Long firstID = (Long) graph.keySet().toArray()[0];
        Long min = firstID;
        double minDist = distance(lon, lat, lon(firstID), lat(firstID));

        for (Long i : graph.keySet()) {
            Long curr = graph.get(i).id;
            double currDist = distance(lon, lat, lon(curr), lat(curr));
            if (currDist < minDist) {
                min = curr;
                minDist = currDist;
            }
        }
        return min;

    }

    /**
     * Gets the longitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return graph.get(v).lon;
    }

    /**
     * Gets the latitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return graph.get(v).lat;
    }

    /*
    Adds vertices to the graph
     */
    void addNode(long id, double lat, double lon) {
        lastNode = graph.get(id);
        graph.put(id, new Node(id, lat, lon));
    }

    void addWay(long id) {
        this.way = new Way(id);
    }

    void addListOfNodes(Long node) {
        way.listOfNodes.add(node);

    }

    void highwayType(String flag) {
        way.flag = flag;
    }

    void wayName(String name) {
        way.name = name;
    }

    void nodeName(String name) {
        lastNode.nodeName(name);
    }

    void addLinks() {
        int i = 0;
        while (i < way.listOfNodes.size() - 1) {
            long a = way.listOfNodes.get(i);
            long b = way.listOfNodes.get(i + 1);

            graph.get(a).getLinks().add(b);
            graph.get(b).getLinks().add(a);

            graph.get(a).getNodes().add(graph.get(b));
            graph.get(b).getNodes().add(graph.get(a));
            i++;
        }
    }



    String flag() {
        return way.flag;
    }

    HashMap<Long, Node> returnGraph() {
        return graph;
    }

//    double bestDist(Node vertex) {
//        ArrayList<Node> neighbors = vertex.linkNodes;
//        double minDist = 0;
//        for (Node v : neighbors) {
//            double currDist = distance(v.id, vertex.id);
//            if (currDist < minDist) {
//                minDist = currDist;
//            }
//        }
//        return minDist;
//    }


}

