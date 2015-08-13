package gurinderhans.me.whatplaneisthat.Models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ghans on 6/24/15.
 */
public class Destination {

	private String toShort;
	private String fromShort;
	private String toFullCity;
	private String fromFullCity;
	private String toAirport;
	private String fromAirport;

	private LatLng destTo;
	private LatLng destFrom;

	private long departureTime;
	private long arrivalTime;

	public Destination(String fromShort, String toShort) {
		this.fromShort = fromShort;
		this.toShort = toShort;
	}

	public String getToShort() {
		return toShort;
	}

	public void setToShort(String toShort) {
		this.toShort = toShort;
	}

	public String getFromShort() {
		return fromShort;
	}

	public void setFromShort(String fromShort) {
		this.fromShort = fromShort;
	}

	public String getToFullCity() {
		return toFullCity;
	}

	public void setToFullCity(String toFullCity) {
		this.toFullCity = toFullCity;
	}

	public String getFromFullCity() {
		return fromFullCity;
	}

	public void setFromFullCity(String fromFullCity) {
		this.fromFullCity = fromFullCity;
	}

	public String getToAirport() {
		return toAirport;
	}

	public void setToAirport(String toFullAirport) {
		this.toAirport = toFullAirport;
	}

	public String getFromAirport() {
		return fromAirport;
	}

	public void setFromAirport(String fromAirport) {
		this.fromAirport = fromAirport;
	}

	public LatLng getDestTo() {
		return destTo;
	}

	public void setDestTo(LatLng destTo) {
		this.destTo = destTo;
	}

	public LatLng getDestFrom() {
		return destFrom;
	}

	public void setDestFrom(LatLng destFrom) {
		this.destFrom = destFrom;
	}



	public String getArrivalTime() {
		if (arrivalTime != Long.MIN_VALUE)
			return new SimpleDateFormat("h:mma", Locale.getDefault()).format(new Date(arrivalTime * 1000l)).toLowerCase();

		return "N/a";
	}

	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getDepartureTime() {
		if (departureTime != Long.MIN_VALUE)
			return new SimpleDateFormat("h:mma", Locale.getDefault()).format(new Date(arrivalTime * 1000l)).toLowerCase();

		return "N/a";
	}

	public void setDepartureTime(long departureTime) {
		this.departureTime = departureTime;
	}
}
