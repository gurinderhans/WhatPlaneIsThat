package gurinderhans.me.whatplaneisthat;

/**
 * Created by ghans on 6/16/15.
 */
public class Constants {

    // data URL and options format
    public static final String BASE_URL = "http://lhr.data.fr24.com/zones/fcgi/feed.js";
    public static final String OPTIONS_FORMAT = "?bounds=%s,%s,%s,%s&faa=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=1&estimated=1&maxage=900&gliders=1&stats=1&";

    public static final int SEARCH_RADIUS = 1000;

    public static final String PLANE_NAME_SPLITTER = "-||-";
    public static final long REFRESH_INTERVAL = 10000l;
    public static final float MAP_CAMERA_LOCK_MIN_ZOOM = 12f;

    // each plane data url
    public static final String PLANE_DATA_URL = "http://lhr.data.fr24.com/_external/planedata_json.1.4.php?f=%s&format=2";
    // plane data keys
    public static final String KEY_PLANE_MAP_TRAIL = "trail";
    public static final String KEY_AIRCRAFT_NAME = "aircraft";
    public static final String KEY_AIRLINE_NAME = "airline";

    //
    // plane FROM -> TO destination
    //

    // short and full versions of the name
    public static final String KEY_PLANE_SHORT_FROM = "from_iata";
    public static final String KEY_PLANE_SHORT_TO = "to_iata";
    public static final String KEY_PLANE_FROM = "from_city";
    public static final String KEY_PLANE_TO = "to_city";
    // exact position (lat, lng)
    public static final String KEY_PLANE_POS_FROM = "from_pos";
    public static final String KEY_PLANE_POS_TO = "to_pos";

    public static final String KEY_PLANE_DEPARTURE_TIME = "departure";
    public static final String KEY_PLANE_ARRIVAL_TIME = "arrival";

    public static final String KEY_PLANE_IMAGE_URL = "image_large";


}
