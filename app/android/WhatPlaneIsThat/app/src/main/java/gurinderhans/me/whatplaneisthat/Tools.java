package gurinderhans.me.whatplaneisthat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Rect;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.larvalabs.svgandroid.SVGBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import gurinderhans.me.whatplaneisthat.Models.Plane;

/**
 * Created by ghans on 6/15/15.
 */
public class Tools {

	private Tools() {
		//
	}

	public static Bitmap getSVGBitmap(Context c, int rId, int width, int height) {

		Picture picture = new SVGBuilder().readFromResource(c.getResources(), rId).build().getPicture();

		int w = (width < 1) ? picture.getWidth() : width;
		int h = (height < 1) ? picture.getWidth() : height;

		Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		canvas.drawPicture(picture, new Rect(0, 0, w, h));

		return bmp;
	}

	public static int getBitmapColor(Bitmap bitmap) {

		int redColors = 0;
		int greenColors = 0;
		int blueColors = 0;
		int pixelCount = 0;

		for (int y = 0; y < bitmap.getHeight(); y++) {
			for (int x = 0; x < bitmap.getWidth(); x++) {
				int c = bitmap.getPixel(x, y);
				pixelCount++;
				redColors += Color.red(c);
				greenColors += Color.green(c);
				blueColors += Color.blue(c);
			}
		}
		// calculate average of bitmap r,g,b values
		int red = (redColors / pixelCount);
		int green = (greenColors / pixelCount);
		int blue = (blueColors / pixelCount);

		return Color.rgb(red, green, blue);
	}


	/**
	 * @param jsonObject - json object containing data
	 * @param key        - key for the value
	 * @return
	 */
	public static String getJsonString(JSONObject jsonObject, String key) {
		try {
			return jsonObject.getString(key);
		} catch (JSONException je) {
			return null;
		}
	}

	public static String getJsonStringFromArr(JSONArray array, int index) {
		try {
			return array.getString(index);
		} catch (JSONException je) {
			return null;
		}
	}

	public static double getJsonDoubleFromArr(JSONArray array, int index) {
		try {
			return array.getDouble(index);
		} catch (JSONException e) {
			return Double.MIN_VALUE;
		}
	}

	public static long getJsonLong(JSONObject data, String key) {
		try {
			return data.getLong(key);
		} catch (JSONException e) {
			return Long.MIN_VALUE;
		}
	}

	/**
	 * Checks if given plane with key `name` is contained in `markersList`
	 *
	 * @param allPlanes - list of planes to search through
	 * @param key       - plane key, uniquely identifies each plane
	 * @return - index of the plane in the list, -1 if not found
	 */

	public static int getPlaneIndex(List<Plane> allPlanes, String key) {
		for (int i = 0; i < allPlanes.size(); i++)
			if (allPlanes.get(i).keyIdentifier.equals(key))
				return i;

		return -1;
	}

	/**
	 * Checks if given plane name is in mPlaneMarkers
	 *
	 * @param markersList - list of all markers
	 * @param markerId    - plane marker id
	 * @return - index of the plane in the list, -1 if not found
	 */
	public static int getPlaneMarkerIdIndex(List<Marker> markersList, String markerId) {
		for (int i = 0; i < markersList.size(); i++) {
			if (markersList.get(i).getId().equals(markerId))
				return i;
		}
		return Constants.INVALID_ARRAY_INDEX;
	}

	/**
	 * Calculates LatLng of some point at a distance from given latitude, longitude at an angle
	 *
	 * @param location - given location
	 * @param bearing  - give bearing / angle (in degrees)
	 * @param distance - distance in Km
	 * @return - new LatLng that is distance away from current point at some angle
	 */
	public static LatLng boundingBox(LatLng location, double bearing, double distance) {

		float radius = 6378.1f;
		double latitude = location.latitude;
		double longitude = location.longitude;

		// new latitude
		double nLat = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(latitude)) * Math.cos(distance / radius) + Math.cos(Math.toRadians(latitude)) * Math.sin(distance / radius) * Math.cos(Math.toRadians(bearing))));
		double nLng = Math.toDegrees(Math.toRadians(longitude) + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(distance / radius) * Math.cos(Math.toRadians(latitude)), Math.cos(distance / radius) - Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(nLat))));

		return new LatLng(nLat, nLng);
	}
}
