package gurinderhans.me.whatplaneisthat.Controllers;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gurinderhans.me.whatplaneisthat.Models.Destination;
import gurinderhans.me.whatplaneisthat.Models.Plane;
import gurinderhans.me.whatplaneisthat.Tools;

import static gurinderhans.me.whatplaneisthat.Tools.getJsonDoubleFromArr;
import static gurinderhans.me.whatplaneisthat.Tools.getJsonStringFromArr;

/**
 * Created by ghans on 15-08-12.
 */
public class PlaneController {

	PlaneController() {
	}

	public static List<Plane> createPlanes(JSONObject data) {

		List<Plane> planes = new ArrayList<>();

		Iterator<String> iterator = data.keys();

		while (iterator.hasNext()) {
			String planeKey = iterator.next();

			try {
				planes.add(_createPlane(planeKey, data.getJSONArray(planeKey)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return planes;
	}

	private static Plane _createPlane(String key, JSONArray data) {

		Plane plane = new Plane(key);

		plane.setShortName(getJsonStringFromArr(data, 16));
		plane.setRotation(Tools.getJsonDoubleFromArr(data, 3));
		plane.setAltitude(getJsonDoubleFromArr(data, 4));
		plane.setSpeed(getJsonDoubleFromArr(data, 5));

		try {
			plane.setPosition(new LatLng(data.getDouble(1), data.getDouble(2)));
		} catch (JSONException e) {
			// unable to set plane position, oh well!
			e.printStackTrace();
		}

		plane.setDestination(
				new Destination(getJsonStringFromArr(data, 11), getJsonStringFromArr(data, 12))
		);

		return plane;
	}
}





/*
Iterator<String> jsonIterator = response.keys();

while (jsonIterator.hasNext()) {
		String key = jsonIterator.next();

		try { // it's plane data if we can convert to a JSONArray

		JSONArray planeDataArr = response.getJSONArray(key);

		Plane tmpPlane = planeBuilder.build();

		if (!planeDataArr.isNull(4))
		tmpPlane.setAltitude((float) planeDataArr.getDouble(4));

		if (!planeDataArr.isNull(5))
		tmpPlane.setSpeed((float) planeDataArr.getDouble(5));

		int markerIndex = Tools.getPlaneMarkerIndex(mPlaneMarkers, key);

		// add plane to markers list if not
		if (markerIndex == -1) {
		Marker planeMarker = mMap.addMarker(new MarkerOptions().position(tmpPlane.getPlanePos())
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
		.rotation(tmpPlane.getRotation())
		.flat(true));
		mPlaneMarkers.add(Pair.create(tmpPlane, planeMarker));
		} else {

						*/
/* directly update plane and marker objects *//*


		mPlaneMarkers.get(markerIndex).first.setPosition(tmpPlane.getPlanePos());
		mPlaneMarkers.get(markerIndex).first.setRotation(tmpPlane.getRotation());

		mPlaneMarkers.get(markerIndex).first.setAltitude(tmpPlane.getAltitude());
		mPlaneMarkers.get(markerIndex).first.setSpeed(tmpPlane.getSpeed());

		// TODO: update graph table(s) here


		// only set destination if it's null, otherwise other destination
		// variables are set to empty, ex. `toFullCity` as this information
		// isn't available at this time
		if (mPlaneMarkers.get(markerIndex).first.getDestination() == null)
		mPlaneMarkers.get(markerIndex).first.setDestination(tmpPlane.getDestination());

		mPlaneMarkers.get(markerIndex).second.setPosition(tmpPlane.getPlanePos());
		mPlaneMarkers.get(markerIndex).second.setRotation(tmpPlane.getRotation());
		}

		} catch (JSONException e) {
		// this can be ignored because only time this exception occurs is when we try
		// to convert a json node to an node array but it's not. That is used as a
		// detection to check if the node value contains plane data or not
		}

		}*/
