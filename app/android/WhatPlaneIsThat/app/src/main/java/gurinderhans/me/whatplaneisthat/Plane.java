package gurinderhans.me.whatplaneisthat;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    String name, landingAt, name2;
    float rotation, altitude, speed;
    double latitude, longitude;

    public Plane(String name, String landingAt, String name2, float rotation, float altitude, float speed, double lat, double lng) {
        this.name = name;
        this.landingAt = landingAt;
        this.name2 = name2;

        this.rotation = rotation;
        this.altitude = altitude;
        this.speed = speed;

        this.latitude = lat;
        this.longitude = lng;
    }
}
