package gurinderhans.me.whatplaneisthat.Models;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ghans on 6/15/15.
 */
public class Plane {

	protected static final String TAG = Plane.class.getSimpleName();

	// plane's unique identifier (used for fetching plane data from web and searching for plane in mem.)
	public final String keyIdentifier;

	public String shortName;

	@Nullable
	private String fullName;

	@Nullable
	private String airlineName;

	@Nullable
	private LatLng planePos;

	private double rotation;

	private ArrayList<Double> altitude = new ArrayList<>();
	private ArrayList<Double> speed = new ArrayList<>();


	@Nullable
	private Destination destination;

	@Nullable
	private Pair<Drawable, Integer> planeImage;


	public Plane(String keyIdentifier) {
		this.keyIdentifier = keyIdentifier;
	}

	//
	// setters & getters
	//

	@Nullable
	public LatLng getPlanePos() {
		return planePos;
	}

	public void setPlanePos(@Nullable LatLng planePos) {
		this.planePos = planePos;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setPosition(@Nullable LatLng planePos) {
		this.planePos = planePos;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	@Nullable
	public Destination getDestination() {
		return destination;
	}

	public void setDestination(@Nullable Destination destination) {
		this.destination = destination;
	}

	public String getFullName() {
		return fullName != null && !fullName.isEmpty() ? fullName : (!shortName.isEmpty() ? shortName : "No CallSign");
	}

	public void setFullName(@Nullable String fullName) {
		this.fullName = fullName;
	}

	public String getAirlineName() {
		return airlineName != null && !airlineName.isEmpty() ? airlineName : "Unknown Airlines";
	}

	public void setAirlineName(@Nullable String airlineName) {
		this.airlineName = airlineName;
	}

	@Nullable
	public Pair<Drawable, Integer> getPlaneImage() {
		return planeImage;
	}

	public void setPlaneImage(@Nullable Pair<Drawable, Integer> planeImage) {
		this.planeImage = planeImage;
	}

	public double getSpeed() {
		// TODO: index out of bounds exception ?
		return speed.get(speed.size() - 1);
	}

	public void setSpeed(double speed) {
		this.speed.add(speed);
	}

	public ArrayList<Double> getSpeedDataSet() {
		return speed;
	}

	public double getAltitude() {
		// TODO: index out of bounds exception ?
		return altitude.get(altitude.size() - 1);
	}

	public void setAltitude(double altitude) {
		this.altitude.add(altitude);
	}

	public ArrayList<Double> getAltitudeDataSet() {
		return altitude;
	}
}
