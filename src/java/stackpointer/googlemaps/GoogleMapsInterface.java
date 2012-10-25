/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stackpointer.googlemaps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import stackpointer.common.Location;
import stackpointer.stackexchange.Question;
        
/**
 *
 * @author Phil
 */
public class GoogleMapsInterface {
    final static String gMapsUrlBase = "https://maps.googleapis.com/maps/api/";
    final static String gMapsKey = "AIzaSyA2DwdJgZeUssqn561w5tgt7b56oGcYf_o";
    final static String gUseSensor = "false";
    
    /**
     * Takes a string and generates its populated Location
     * @param locString
     * @return a Location populated with the string, lat and lon
     */
    public static Location geocode(String locString)
    {
        Location toReturn = null;
        try {
            URL url = new URL(gMapsUrlBase+"geocode/json?sensor="+gUseSensor+"&address="+URLEncoder.encode(locString, "UTF-8"));
            URLConnection conn = url.openConnection();
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            try
            {
                JSONObject json = new JSONObject(builder.toString());
                JSONArray results = json.getJSONArray("results");
                if(!results.isNull(0))
                {
                    JSONObject jLoc = results.getJSONObject(0).
                            getJSONObject("geometry").getJSONObject("location");
                    toReturn = new Location(locString);
                    toReturn.setLat(jLoc.getDouble("lat"));
                    toReturn.setLon(jLoc.getDouble("lng"));
                }
            }
            catch(JSONException e)
            {
                System.out.println("Error parsing JSON geocoding string: "+e);
            }
        }
        catch (Exception e)
        {
            System.out.println("Error geocoding "+locString+":\n"+e);
        }
        return toReturn;
    }
    
    /**
     * Generate the string needed to connect to google maps javascript includes
     * @return the url for the javascript 'src'
     */
    public static String headerJavascript() {
        
        return gMapsUrlBase+"js?key="+gMapsKey+"&sensor="+gUseSensor;
    }
    
    /**
     * Initializes the google map with our specified options in the mapContainer
     * @param mapContainer
     * @return the javascript to init the map with our options
     */
    public static String setupMap(String mapContainer) {
        StringBuilder toReturn = new StringBuilder();
        //the options
        toReturn.append("var mapOptions = {"+'\n'+
        "  center: new google.maps.LatLng(0, 0),"+'\n'+
        "  zoom: 2,"+'\n'+
        "  mapTypeId: google.maps.MapTypeId.ROADMAP"+'\n'+
        "};"+'\n');
        //the map
        toReturn.append("var map = new google.maps.Map(document.getElementById(\"");
        toReturn.append(mapContainer);
        toReturn.append("\"),"+'\n'+
        "    mapOptions);");
        
        return toReturn.toString();
    }
    
    public static String generateMarkers(Collection<Question> questions)
    {
        StringBuilder toReturn = new StringBuilder();
        
        for (Question q : questions)
        {
            if(q.hasLocation())
            {
                //the location
                toReturn.append("var myLatlng = new google.maps.LatLng(");
                toReturn.append(q.getAskedBy().getLoc().getLat()).append(',').append(q.getAskedBy().getLoc().getLon()).append(");\n");

                //the marker
                toReturn.append("var marker = new google.maps.Marker({"+'\n'+
                "    position: myLatlng,"+'\n'+
                "    map: map,"+'\n'+
                "    title:\"");
                toReturn.append(q.getqTitle());
                toReturn.append("\""+'\n'+
                "});"+'\n');
            }
        }
        
        return toReturn.toString();
    }
}
