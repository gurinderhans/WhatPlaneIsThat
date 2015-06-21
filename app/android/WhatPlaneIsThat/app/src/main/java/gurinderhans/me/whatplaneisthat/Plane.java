package gurinderhans.me.whatplaneisthat;

import android.util.Pair;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    String name, landingAt, name2, idName;
    String plane_key;

    // plane viewable data
    private String aircraftName, airlineName;
    private Pair<String, String> destinationTo, destinationFrom;

    float rotation, altitude, speed;
    double latitude, longitude;

    public Plane(String name, String landingAt, String name2, String key, float rotation, float altitude, float speed, double lat, double lng) {
        this.name = name;
        this.landingAt = landingAt;
        this.name2 = name2;
        this.idName = name + Constants.PLANE_NAME_SPLITTER + name2;
        this.plane_key = key;

        this.rotation = rotation;
        this.altitude = altitude;
        this.speed = speed;

        this.latitude = lat;
        this.longitude = lng;
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
        return aircraftName;
    }


    /**
     * Set aircraft's airlines name
     *
     * @param airlines - airline name
     */
    public void setPlaneAirlines(String airlines) {
        this.airlineName = airlines;
    }

    /**
     * Returns the plane airline name
     *
     * @return - airline name
     */
    public String getAirlineName() {
        return airlineName;
    }

    /**
     * Set plane destination TO
     *
     * @param shortName - destination abbrv.
     * @param fullName  - destination full name
     */
    public void setPlaneDestinationTo(String shortName, String fullName) {
        destinationTo = Pair.create(shortName, fullName);
    }

    /**
     * Set plane destination FROM
     *
     * @param shortName - destination abbrv.
     * @param fullName  - destination full name
     */
    public void setPlaneDestinationFrom(String shortName, String fullName) {
        destinationFrom = Pair.create(shortName, fullName);
    }

    /**
     * Returns the destination TO name
     *
     * @param fullName - if true the full destination name will be returned
     * @return - destination name
     */
    public String getDestinationTo(boolean fullName) {
        if (fullName) return destinationTo.second;
        return destinationTo.first;
    }

    /**
     * Returns the destination FROM name
     *
     * @param fullName - if true the full destination name will be returned
     * @return - destination name
     */
    public String getDestinationFrom(boolean fullName) {
        if (fullName) return destinationFrom.second;
        return destinationFrom.first;
    }


}
