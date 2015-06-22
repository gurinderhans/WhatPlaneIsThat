package gurinderhans.me.whatplaneisthat;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.squareup.okhttp.ResponseBody;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    private String name, landingAt, name2;
    final String plane_key;
    final String idName;

    // plane viewable data
    private String aircraftName, airlineName;
    private Pair<String, String> destinationTo, destinationFrom;
    private LatLng posTo, posFrom;
    private LatLng planePos;
    private JsonArray planeTrail;

    private Pair<Drawable, Integer> planeImage;
    private String planeImageUrl;

    private long planeArrivalTime, planeDepartureTime;

    private float rotation, altitude, speed;

    private boolean isCached = false;

    // FIXME: rewrite constructor
    public Plane(String name, String landingAt, String name2, String key, float rotation, float altitude, float speed, double lat, double lng) {
        this.name = name;
        this.landingAt = landingAt;
        this.name2 = name2;
        this.idName = name + Constants.PLANE_NAME_SPLITTER + name2;
        this.plane_key = key;

        this.altitude = altitude;
        this.speed = speed;

        // set plane position and rotation
        setPlanePos(new LatLng(lat, lng));
        setRotation(rotation);

    }


    /**
     * @param isCached - boolean value if plane has been cached in memory or not
     */
    public void setIsCached(boolean isCached) {
        this.isCached = isCached;
    }

    /**
     * @return - boolean value if plane is cached or not
     */
    public boolean isCached() {
        return isCached;
    }

    /**
     * @return - the plane name
     */
    public String getPlaneName() {

        if (name != null && !name.isEmpty())
            return name;

        if (name2 != null && !name2.isEmpty())
            return name2;

        return "No Callsign";
    }

    /**
     * Set plane's image url and image
     *
     * @param url   - HTTP url of the image
     * @param image - a pair of drawable and an image color (average color of image)
     */
    public void setPlaneImage(String url, Pair<Drawable, Integer> image) {
        this.planeImageUrl = url;
        this.planeImage = image;
    }

    /**
     * @return - the HTTP url of the image
     */
    public String getPlaneImageUrl() {
        if (planeImageUrl != null && !planeImageUrl.isEmpty())
            return planeImageUrl;
        return "N/a";
    }

    /**
     * @return - the tuple containing image drawable and average image color
     */
    public Pair<Drawable, Integer> getPlaneImage() {
        return planeImage;
    }

    /**
     * @param planeArrivalTime - the time in unix timestamp
     */
    public void setPlaneArrivalTime(long planeArrivalTime) {
        this.planeArrivalTime = planeArrivalTime;
    }

    /**
     * @return - the plane arrival time
     */
    public String getPlaneArrivalTime() {
        if (planeArrivalTime == -1l) return "N/a";

        Date date = new Date(planeArrivalTime * 1000l);
        return new SimpleDateFormat("h:mma").format(date).toLowerCase();
    }

    /**
     * @param planeTrail - the plain trail with JsonArray
     */
    public void setPlaneTrail(JsonArray planeTrail) {
        this.planeTrail = planeTrail;
    }

    /**
     * Get plane's path trail
     *
     * @return - the json array containing plane's trail
     */
    public JsonArray getPlaneTrail() {
        return planeTrail;
    }

    /**
     * Sets the plane's current rotation
     *
     * @param rotation - the plane rotation
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     * Returns the current plane rotations
     *
     * @return - the plane rotation
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Set plane position
     *
     * @param planePos - position coordinates
     */
    public void setPlanePos(LatLng planePos) {
        this.planePos = planePos;
    }

    /**
     * Gets the current plane position
     *
     * @return - the plane position
     */
    public LatLng getPlanePos() {
        return planePos;
    }

    /**
     * Set aircraft name
     *
     * @param name - craft name
     */
    public void setAircraftName(String name) {
        this.aircraftName = name;
    }

    /**
     * Return the aircraft name
     *
     * @return - aircraft name
     */
    public String getAircraftName() {
        if (aircraftName != null && !aircraftName.isEmpty())
            return aircraftName;
        return "Unknown Plane Name";
    }

    /**
     * Set aircraft's airlines name
     *
     * @param airlines - airline name
     */
    public void setAirlineName(String airlines) {
        this.airlineName = airlines;
    }

    /**
     * Returns the plane airline name
     *
     * @return - airline name
     */
    public String getAirlineName() {
        if (airlineName != null && !airlineName.isEmpty())
            return airlineName;
        return "Unknown Airlines";
    }

    /**
     * Set plane destination TO
     *
     * @param shortName - destination abbrv.
     * @param fullName  - destination full name
     */
    public void setPlaneDestinationTo(String shortName, String fullName, LatLng pos) {
        this.destinationTo = Pair.create(shortName, fullName);
        this.posTo = pos;
    }

    /**
     * Set plane destination FROM
     *
     * @param shortName - destination abbrv.
     * @param fullName  - destination full name
     */
    public void setPlaneDestinationFrom(String shortName, String fullName, LatLng pos) {
        this.destinationFrom = Pair.create(shortName, fullName);
        this.posFrom = pos;
    }

    /**
     * Returns the destination name TO
     *
     * @param fullName - if true the full destination name will be returned
     * @return - destination name
     */
    public String getDestinationNameTo(boolean fullName) {
        if (destinationTo != null) {
            if (fullName && !destinationTo.second.isEmpty())
                return destinationTo.second;
            if (!destinationTo.first.isEmpty())
                return destinationTo.first;
        }
        return "N/a";
    }

    /**
     * Returns the destination name FROM
     *
     * @param fullName - if true the full destination name will be returned
     * @return - destination name
     */
    public String getDestinationNameFrom(boolean fullName) {
        if (destinationFrom != null) {
            if (fullName && !destinationFrom.second.isEmpty())
                return destinationFrom.second;
            if (!destinationFrom.first.isEmpty())
                return destinationFrom.first;
        }
        return "N/a";
    }


    /**
     * Returns the plane destination TO
     *
     * @return - LatLng object for the destination
     */
    public LatLng getDestinationPosTo() {
        return posTo;
    }

    /**
     * Returns the plane destination FROM
     *
     * @return - LatLng object for the destination
     */
    public LatLng getDestinationPosFrom() {
        return posFrom;
    }


}
