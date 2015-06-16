package gurinderhans.me.whatplaneisthat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by ghans on 6/15/15.
 */
public class Tools {

    static final Gson gson = new Gson();

    private Tools() {
        //
    }

    public static JsonObject stringToJsonObject(String data) {

        JsonElement json = gson.fromJson(data, JsonElement.class);

        return json.getAsJsonObject();
    }
}
