package gurinderhans.me.whatplaneisthat;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    protected static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Handler mHandler = new Handler();
    OkHttpWrapper mWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        mWrapper = new OkHttpWrapper(this);

        mHandler.postDelayed(fetchData, 0);

    }


    Runnable fetchData = new Runnable() {
        @Override
        public void run() {

            mWrapper.get(Constants.BASE_URL + String.format(Constants.OPTIONS_FORMAT,
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
                        public void onSuccess(Response response) {
                            JsonObject jsonObject = Tools.stringToJsonObject(response.body());

                            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                                if (entry.getValue() instanceof JsonArray) {

                                    JsonArray dataArr = entry.getValue().getAsJsonArray();
                                    Plane plane = new Plane(dataArr.get(16).getAsString(),
                                            dataArr.get(12).getAsString(),
                                            dataArr.get(1).getAsDouble(),
                                            dataArr.get(2).getAsDouble());

                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(plane.latitude, plane.longitude))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
                                            .title(plane.name));
                                }
                            }
                        }

                        @Override
                        public void onFinished() {
                            // clear map
                            mMap.clear();

                            // loop with delay
                            mHandler.postDelayed(fetchData, 10000);
                        }
                    });

        }
    };

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
