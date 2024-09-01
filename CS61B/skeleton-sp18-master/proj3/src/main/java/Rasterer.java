import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    public Rasterer() {
        // YOUR CODE HERE ?


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
     *               {lrlon=-122.20908713544797,
     *                ullon=-122.3027284165759,
     *                w=305.0,
     *                h=300.0,
     *                ullat=37.88708748276975,
     *                lrlat=37.848731523430196}
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

        // initialize, but not grid
        double raster_ul_lon = -122.2998046875; double raster_ul_lat = 37.892195547244356;
        double raster_lr_lon = -122.2119140625; double raster_lr_lat = 37.82280243352756;
        int depth = 7;
        boolean query_success = true;

        // get params
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double h = params.get("h");
        double w = params.get("w");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");

        // calculate expected LonDPP and find the depth
        double exLDPP = (lrlon - ullon) / w;
        double[] LDPPs = {
              0.00034332275390625,
              0.000171661376953125,
              0.0000858306884765625,
              0.00004291534423828125,
              0.000021457672119140625,
              0.000010728836059570312,
              0.000005364418029785156,
              0.000002682209014892578
        };

        for (int i = LDPPs.length - 1; i >= 0 ; i--) {
            if(i == 0 && (exLDPP>=LDPPs[0])){
                depth = 0;
                break; // corner case maybe!!!
            }
            if ((LDPPs[i] <= exLDPP) && (exLDPP < LDPPs[i-1])) {
                depth = i;
                break;
            }
        }

        // initialize the ul and lr points
        int ulX = 0; int ulY = 0; int lrX = (int)(Math.pow(2, depth) - 1); int lrY = (int)(Math.pow(2, depth) - 1);
        // find out raster bounds
        double xLen = Math.abs(122.2119140625 - 122.2998046875);
        double unitX = xLen / Math.pow(2,depth);
        for (int i = 0; i*unitX <= xLen; i++) {
            if ((-122.2998046875 + i*unitX < ullon)&&(ullon < -122.2998046875 + (i+1)*unitX)) {
                raster_ul_lon = -122.2998046875 + i*unitX;
                ulX = i;
                break; // maybe a problem if not check i+1 ? corner case?
            }
        }
        for (int i = 0; i*unitX <= xLen; i++) {
            if ((-122.2998046875 + i*unitX < lrlon)&&(lrlon < -122.2998046875 + (i+1)*unitX) && ((i+1)*unitX <= xLen)) {
                raster_lr_lon = -122.2998046875 + (i+1)*unitX;
                lrX = i; // maybe wrong?
                break;
            }
        }

        double yLen = Math.abs(37.892195547244356 - 37.82280243352756);
        double unitY = yLen / Math.pow(2, depth);
        for (int i = 0; i*unitY <= yLen; i++) {
            if ((37.892195547244356 - (i+1)*unitY < ullat)&&(ullat < 37.892195547244356 - i*unitY)) {
                raster_ul_lat = 37.892195547244356 - i*unitY;
                ulY = i;
                break;
            }
        }

        for (int i = 0; i*unitY <= yLen; i++) {
            if ((37.892195547244356 - (i+1)*unitY < lrlat) && (lrlat < 37.892195547244356 - i*unitY) && ((i+1)*unitY <= yLen)) {
                raster_lr_lat = 37.892195547244356 - (i+1)*unitY;
                lrY = i; // ?
                break;
            }
        }

        // place the grid
        String[][] render_grid = new String[lrY-ulY+1][lrX-ulX+1]; // #row, #col
        for (int j = 0; j < lrY-ulY+1; j++) {
            for (int k = 0; k < lrX-ulX+1; k++) {
                int gx = ulX+k;
                int gy = ulY+j;
                render_grid[j][k] = "d"+depth+"_x"+gx+"_y"+gy+".png";
                System.out.println(render_grid[j][k]);
            }
        }
        // decide if success or not
        boolean requestCrossed = (ullon>=lrlon)||(ullat<=lrlat);
        if (requestCrossed){
            query_success = false;
        }


        // put into hash map and return
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", render_grid); // !!
        results.put("raster_ul_lon", raster_ul_lon);
        results.put("raster_ul_lat", raster_ul_lat);
        results.put("raster_lr_lon", raster_lr_lon);
        results.put("raster_lr_lat", raster_lr_lat);
        results.put("depth", depth);
        results.put("query_success", query_success);
        // some test
//        System.out.println(params); // test
//        System.out.println(ulX);
//        System.out.println(ulY);
//        System.out.println(lrX);
//        System.out.println(lrY);
        return results;
    }

}
