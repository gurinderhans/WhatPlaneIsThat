package gurinderhans.me.whatplaneisthat;

/**
 * Created by ghans on 6/16/15.
 */
public class Constants {

    // data URL and options format
    public static String BASE_URL = "http://lhr.data.fr24.com/zones/fcgi/feed.js";
    public static String OPTIONS_FORMAT = "?bounds=%s,%s,%s,%s&faa=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=1&estimated=1&maxage=900&gliders=1&stats=1&";

    // each plane data url
    public static String PLANE_DATA_URL = "http://lhr.data.fr24.com/_external/planedata_json.1.4.php?f=%s&format=2";
    // plane data keys
    public static final String KEY_PLANE_MAP_TRAIL = "trail";
    public static final String KEY_AIRCRAFT_NAME = "aircraft";
    public static final String KEY_AIRLINE_NAME = "airline";
    public static final String KEY_PLANE_FROM = "from_iata";
    public static final String KEY_PLANE_TO = "to_iata";


    public static String PLANE_NAME_SPLITTER = "-||-";

    public static final long REFRESH_INTERVAL = 10000l;

    public static final float MAP_CAMERA_LOCK_MIN_ZOOM = 12f;

}
