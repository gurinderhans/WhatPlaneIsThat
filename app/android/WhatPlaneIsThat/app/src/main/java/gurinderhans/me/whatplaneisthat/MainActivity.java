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
import com.google.gson.Gson;
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

    public static final String TAG = MainActivity.class.getSimpleName();

    String BASE_URL = "http://lhr.data.fr24.com/zones/fcgi/feed.js";
    String OPTIONS = String.format("?bounds=%s,%s,%s,%s&faa=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=1&estimated=1&maxage=900&gliders=1&stats=1&", "49.3413832283658", "48.96590422141798", "-123.49031433105472", "-122.58386901855408");


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    OkHttpClient client = new OkHttpClient();
    final Gson gson = new Gson();

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        mHandler.postDelayed(fetchData, 0);
    }

    Runnable fetchData = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "called fetch data runnable");
            try {
                mMap.clear();
                fetchPlaneData(BASE_URL + OPTIONS, new Callback() {

                    Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (response.isSuccessful()) {

                            JsonObject jsonObject = Tools.stringToJsonObject(response.body().string());

                            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                                if (entry.getValue() instanceof JsonArray) {

                                    final JsonArray dataArr = entry.getValue().getAsJsonArray();

                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Plane plane = new Plane(dataArr.get(16).getAsString(), dataArr.get(12).getAsString(),
                                                    dataArr.get(1).getAsDouble(), dataArr.get(2).getAsDouble());

                                            mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(plane.latitude, plane.longitude))
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plane_icon))
                                                    .title(plane.name));
                                        }
                                    });
                                }
                            }

                        }
                    }

                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                        Log.i(TAG, "request failed");
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            mHandler.postDelayed(this, 10000);
        }
    };

    Call fetchPlaneData(String url, Callback callback) throws IOException {
        Request request = new Request.Builder().url(url).build();

        Call call = client.newCall(request);
        call.enqueue(callback);

        return call;
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
