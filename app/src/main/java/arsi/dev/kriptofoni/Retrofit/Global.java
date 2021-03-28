package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Global {
    @SerializedName("data")
    private JsonObject data;

    public JsonObject getData() {
        return data;
    }
}
