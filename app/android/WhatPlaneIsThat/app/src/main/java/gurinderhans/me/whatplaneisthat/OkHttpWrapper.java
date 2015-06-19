package gurinderhans.me.whatplaneisthat;

import android.content.Context;
import android.os.Handler;

import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

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
                // send the callback on the main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        cb.onFinished();

                        if (response.isSuccessful()) {
                            cb.onSuccess(Tools.stringToJsonObject(response.body()));
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
         * @param jsonData
         */
        void onSuccess(Object jsonData);

        /**
         * custom method just to manage the handler loop
         */
        void onFinished();
    }
}
