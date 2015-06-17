package gurinderhans.me.whatplaneisthat;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    // TODO: Estimate plane location and make it move in "realtime"
    // TODO: Get plane rotation
    // TODO: get user location with rotation
    // TODO: guess which planes "I" might be able to see

    protected static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Handler mHandler = new Handler();
    OkHttpWrapper mWrapper;
    List<Pair<String, Marker>> mPlaneMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        mPlaneMarkers = new ArrayList<>();
        mWrapper = new OkHttpWrapper(this);
        mHandler.postDelayed(fetchData, 0);
    }

    Runnable fetchData = new Runnable() {
        @Override
        public void run() {
            mWrapper.getJson(Constants.BASE_URL + String.format(Constants.OPTIONS_FORMAT,
                            // map bounds
                            "49.3413832283658",
                            "48.96590422141798",
                            "-123.49031433105472",
                            "-122.58386901855408"),
                    new OkHttpWrapper.HttpCallback() {
                        @Override
                        public void onFailure(Response response, Throwable throwable) {
                            //
                        }

                        @Override
                        public void onSuccess(JsonObject jsonData) {
                            for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {

                                if (entry.getValue() instanceof JsonArray) { // this data is about a plane and not other json info

                                    JsonArray dataArr = entry.getValue().getAsJsonArray();

                                    Plane plane = new Plane(dataArr.get(16).getAsString(),
                                            dataArr.get(12).getAsString(),
                                            dataArr.get(13).getAsString(),
                                            dataArr.get(1).getAsDouble(),
                                            dataArr.get(2).getAsDouble());

                                    String planeName = plane.name + Constants.PLANE_NAME_SPLITTER + plane.name2;

                                    LatLngBounds searchBounds = new LatLngBounds(new LatLng(48.96590422141798, -122.58386901855408), new LatLng(49.3413832283658, -123.49031433105472));

                                    int markerIndex = getPlaneMarkerIndex(planeName);

                                    // add to list if not already
                                    if (markerIndex == -1) {
                                        mPlaneMarkers.add(Pair.create(planeName, mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(plane.latitude, plane.longitude))
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
                                                .title(plane.name))));
                                    } else {

                                        if (searchBounds.contains(new LatLng(plane.latitude, plane.longitude))) {
                                            // remove the marker
                                            mPlaneMarkers.remove(markerIndex);
                                        } else {
                                            // update its location
                                            mPlaneMarkers.get(markerIndex).second.setPosition(new LatLng(plane.latitude, plane.longitude));
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFinished() {
                            // loop with delay
                            mHandler.postDelayed(fetchData, 10000);
                        }
                    });
        }
    };

    /**
     * Checks if given plane name is in mPlaneMarkers
     *
     * @param name - plane name
     * @return - index of the plane in the list, -1 if not found
     */
    public int getPlaneMarkerIndex(String name) {

        for (int i = 0; i < mPlaneMarkers.size(); i++) {
            if (mPlaneMarkers.get(i).first.equals(name))
                return i;
        }

        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

}
