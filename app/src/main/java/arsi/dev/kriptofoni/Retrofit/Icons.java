package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Icons {
    @SerializedName("icons")
    public JsonObject icons;

    public JsonObject getIcons() {
        return icons;
    }
}
