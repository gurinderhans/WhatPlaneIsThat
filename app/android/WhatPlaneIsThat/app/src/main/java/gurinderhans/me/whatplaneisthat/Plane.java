package gurinderhans.me.whatplaneisthat;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

    String name, landingAt;
    double latitude, longitude;

    public Plane(String name, String landingAt, double lat, double lng) {
        this.name = name;
        this.landingAt = landingAt;

        this.latitude = lat;
        this.longitude = lng;
    }
}
