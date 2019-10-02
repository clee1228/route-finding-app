import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;


/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private final double ROOT_ULLON = MapServer.ROOT_ULLON;
    private final double ROOT_ULLAT = MapServer.ROOT_ULLAT;
    private final double ROOT_LRLON = MapServer.ROOT_LRLON;
    private final double ROOT_LRLAT = MapServer.ROOT_LRLAT;
    private final double DEGREE = 288200.0;
    private double midLong = ((ROOT_LRLON - ROOT_ULLON) / 2) + ROOT_ULLON;
    private double midLat = ROOT_ULLAT - ((ROOT_ULLAT - ROOT_LRLAT) / 2);
    private double rasterLRLON;
    private double rasterLRLAT;
    private double rasterULLON;
    private double rasterULLAT;

    public Rasterer() {
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */

    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();

        double lrlon = params.get("lrlon");
        double lrlat = params.get("lrlat");
        double ullon = params.get("ullon");
        double ullat = params.get("ullat");
        double width = params.get("w");

        Boolean querySuccess = true;
        //query_success
        if (ullon < ROOT_ULLON || ullat > ROOT_ULLAT || lrlon < ullon
                || lrlat > ullat || lrlon > ROOT_LRLON || lrlat < ROOT_LRLAT) {
            querySuccess = false;
        }

        //finds depth
        int depth = findDepth(lrlon, ullon, width);

        //finds intersecting tiles
        String[][] renderGrid = grid(ullon, ullat, lrlon, lrlat, depth);


        if (renderGrid.length == 1 && renderGrid[0].length == 1) {
            String renderString = renderGrid[0][0];
            String x = between(renderString, "x", "_");
            String y = between(renderString, "y", ".");
            int lon = Integer.parseInt(x);
            int lat = Integer.parseInt(y);
            Map<String, Double> box = calcCoords(lon, lat, depth);
            rasterULLON = box.get("ullong");
            rasterULLAT = box.get("ullat");
            rasterLRLON = box.get("lrlong");
            rasterLRLAT = box.get("lrlat");

        } else {
            //find raster ullon + ullat
            String rasterUL = renderGrid[0][0];
            String ulX = between(rasterUL, "x", "_");
            String ulY = between(rasterUL, "y", ".");
            int ulLON = Integer.parseInt(ulX);
            int ulLAT = Integer.parseInt(ulY);
            Map<String, Double> ul = calcCoords(ulLON, ulLAT, depth);
            rasterULLON = ul.get("ullong");
            rasterULLAT = ul.get("ullat");


            //find raster lrlon + lrlat
            String rasterLR = renderGrid[renderGrid.length - 1][renderGrid[0].length - 1];
            String lrX = between(rasterLR, "x", "_");
            String lrY = between(rasterLR, "y", ".");
            int lrLON = Integer.parseInt(lrX);
            int lrLAT = Integer.parseInt(lrY);
            Map<String, Double> lr = calcCoords(lrLON, lrLAT, depth);
            rasterLRLON = lr.get("lrlong");
            rasterLRLAT = lr.get("lrlat");
        }


        //put different info including which tiles to display
        results.put("raster_ul_lon", rasterULLON);
        results.put("depth", depth);
        results.put("raster_lr_lon", rasterLRLON);
        results.put("raster_lr_lat", rasterLRLAT);
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lat", rasterULLAT);
        results.put("query_success", querySuccess);



        return results;

    }

    public String between(String text, String textFrom, String textTo) {
        String result = "";
        result = text.substring(text.indexOf(textFrom) + textFrom.length(), text.length());
        result = result.substring(0, result.indexOf(textTo));

        return result;
    }


    private String fileName(int d, int x, int y) {
        String fileName =  String.format("d%d_x%d_y%d.png", d, x, y);
        return fileName;
    }


    public String[][] grid(double ullon, double ullat, double lrlon, double lrlat, int depth) {

        int xyVal;
        if (depth < 4) {
            xyVal = (int) (Math.pow(2, depth) - 1);
        } else {
            xyVal = (int) (Math.pow(2, depth) - 2);
        }

        String[] list = new String[(xyVal + 1) * (xyVal + 1)];
        ArrayList<Integer> yvalues = new ArrayList<>();


        int index = 0;
        for (int y = 0; y <= xyVal; y++) {
            for (int x = 0; x <= xyVal; x++) {
                Map<String, Double> coord = calcCoords(x, y, depth);
                if ((coord.get("lrlong") <= ullon || coord.get("lrlat") >= ullat
                        || lrlon <= coord.get("ullong") || lrlat >= coord.get("ullat"))) {
                    continue;
                } else {

                    String insert = fileName(depth, x, y);
                    list[index] = insert;

                    index++;
                    yvalues.add(y);
                }
            }
        }


        ArrayList<Integer> stop = new ArrayList<>();
        if (!yvalues.isEmpty()) {
            int start = yvalues.get(0);
            for (int i = 0; i < yvalues.size(); i++) {
                if (yvalues.get(i) != start) {
                    stop.add(i);
                    start = yvalues.get(i);
                }
            }
        }

        if (stop.size() >= 1) {
            String[][] intersect = new String[stop.size() + 1][stop.get(0)];
            int beg = 0;
            int count = stop.get(0);
            for (int i = 0; i < stop.size() + 1; i++) {
                String[] temp = new String[stop.get(0)];
                temp = Arrays.copyOfRange(list, beg, count);
                intersect[i] = temp;
                beg += stop.get(0); //0, 3, 6
                count += stop.get(0); //3, 5, 9
            }
            return intersect;
        } else {
            String[][] intersect = new String[1][1];
            String[] temp = new String[1];
            temp = Arrays.copyOfRange(list, 0, 1);
            intersect[0] = temp;
            return intersect;
        }
    }

    public Map<String, Double> calcCoords(int x, int y, int depth) {
        Map<String, Double> tileCoords = new HashMap<>();
        double longitude = (ROOT_ULLON - midLong) / Math.pow(2, depth - 1);
        double latitude = (ROOT_ULLAT - midLat) / Math.pow(2, depth - 1);
        double ullong = ROOT_ULLON - (longitude * x);
        double ullat = (ROOT_ULLAT - (latitude * y));
        double lrlong = ROOT_ULLON - (longitude * (x + 1));
        double lrlat = (ROOT_ULLAT - (latitude * y)) - latitude;

        tileCoords.put("ullong", ullong);
        tileCoords.put("ullat", ullat);
        tileCoords.put("lrlong", lrlong);
        tileCoords.put("lrlat", lrlat);

        return tileCoords;
    }


    public int findDepth(double lrLON, double ulLON, double width) {
        int depth = 0;
        double d1LDPP = findLDPP(ROOT_ULLON, ROOT_LRLON, MapServer.TILE_SIZE);
        double lonDPP = findLDPP(ulLON, lrLON, width);
        while (d1LDPP > lonDPP && depth < 7) {
            d1LDPP = d1LDPP / 2;
            depth = depth + 1;
        }

        return depth;
    }

    public double findLDPP(double ulLON, double lrLON, double width) {
        double xDist = lrLON - ulLON;
        double lonDPP = (DEGREE * xDist) / width;
        return lonDPP;
    }

}
