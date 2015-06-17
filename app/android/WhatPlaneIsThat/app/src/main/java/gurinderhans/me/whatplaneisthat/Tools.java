package gurinderhans.me.whatplaneisthat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.ResponseBody;

/**
 * Created by ghans on 6/15/15.
 */
public class Tools {

    static final Gson gson = new Gson();

    private Tools() {
        //
    }

    public static JsonObject stringToJsonObject(ResponseBody data) {

        try {
            JsonElement json = gson.fromJson(data.string(), JsonElement.class);
            return json.getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JsonObject();
    }

}
