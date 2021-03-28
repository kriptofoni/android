package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.annotations.SerializedName;

public class Coin {
    @SerializedName("id")
    private String id;
    @SerializedName("symbol")
    private String symbol;
    @SerializedName("name")
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }
}
