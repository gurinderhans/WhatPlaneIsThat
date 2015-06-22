package gurinderhans.me.whatplaneisthat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ghans on 6/16/15.
 */
public class OkHttpWrapper {

    private OkHttpClient client;
    private Request.Builder builder;
    private Handler mainHandler;

    public OkHttpWrapper(Context c) {
        client = new OkHttpClient();
        builder = new Request.Builder();
        mainHandler = new Handler(c.getMainLooper());
    }

    public void getJson(String url, final HttpCallback cb) {

        Request request = builder.url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, final IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cb.onFinished();
                        cb.onFailure(null, e);
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {

                // convert raw data to json
                final JsonObject jsonObject = Tools.stringToJsonObject(response.body());

                // send the callback on the main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        cb.onFinished();

                        if (response.isSuccessful()) {
                            cb.onSuccess(jsonObject);
                            return;
                        }

                        cb.onFailure(response, null);
                    }
                });
            }
        });
    }

    public void downloadImage(String url, final HttpCallback cb) {
        Request req = builder.url(url).build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, final IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cb.onFinished();
                        cb.onFailure(null, e);
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                // convert raw data to json
                InputStream inputStream = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Drawable img = new BitmapDrawable(null, bitmap);
                final Pair<Drawable, Integer> planeImageAndAvgColor = Pair.create(img, Tools.getBitmapColor(bitmap));

                // send the callback on the main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        cb.onFinished();

                        if (response.isSuccessful()) {
                            cb.onSuccess(planeImageAndAvgColor);
                            return;
                        }

                        cb.onFailure(response, null);
                    }
                });
            }
        });
    }

    public interface HttpCallback {

        /**
         * called when the server response was not 2xx or when an exception was thrown in the process
         *
         * @param response  - in case of server error (4xx, 5xx) this contains the server response
         *                  in case of IO exception this is null
         * @param throwable - contains the exception. in case of server error (4xx, 5xx) this is null
         */
        void onFailure(Response response, Throwable throwable);

        /**
         * contains the server response
         *
         * @param data
         */
        void onSuccess(Object data);

        /**
         * custom method just to manage the handler loop
         */
        void onFinished();
    }
}
