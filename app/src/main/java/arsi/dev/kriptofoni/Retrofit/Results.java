package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Results {
    @SerializedName("id")
    private String id;
    @SerializedName("symbol")
    private String symbol;
    @SerializedName("name")
    private String name;
    @SerializedName("image")
    private String image;
    @SerializedName("current_price")
    private double current_price;
    @SerializedName("market_cap")
    private double market_cap;
    @SerializedName("market_cap_rank")
    private double market_cap_rank;
    @SerializedName("fully_diluted_valuation")
    private double fully_diluted_valuation;
    @SerializedName("total_volume")
    private double total_volume;
    @SerializedName("high_24h")
    private double high_24h;
    @SerializedName("low_24h")
    private double low_24h;
    @SerializedName("price_change_24h")
    private double price_change_24h;
    @SerializedName("price_change_percentage_24h")
    private double price_change_percentage_24h;
    @SerializedName("market_cap_change_24h")
    private double market_cap_change_24h;
    @SerializedName("market_cap_change_percentage_24h")
    private double market_cap_change_percentage_24h;
    @SerializedName("circulating_supply")
    private double circulating_supply;
    @SerializedName("total_supply")
    private double total_supply;
    @SerializedName("max_supply")
    private double max_supply;
    @SerializedName("ath")
    private double ath;
    @SerializedName("ath_change_percentage")
    private double ath_change_percentage;
    @SerializedName("ath_date")
    private String ath_date;
    @SerializedName("atl")
    private double atl;
    @SerializedName("atl_change_percentage")
    private double atl_change_percentage;
    @SerializedName("atl_date")
    private String atl_date;
    @SerializedName("roi")
    private Object roi;
    @SerializedName("last_updated")
    private String last_updated;
    @SerializedName("price_change_percentage_24h_in_currency")
    private double price_change_percentage_24h_in_currency;
    @SerializedName("price_change_percentage_7d_in_currency")
    private double price_change_percentage_7d_in_currency;

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public double getCurrent_price() {
        return current_price;
    }

    public double getMarket_cap() {
        return market_cap;
    }

    public double getMarket_cap_rank() {
        return market_cap_rank;
    }

    public double getFully_diluted_valuation() {
        return fully_diluted_valuation;
    }

    public double getTotal_volume() {
        return total_volume;
    }

    public double getHigh_24h() {
        return high_24h;
    }

    public double getLow_24h() {
        return low_24h;
    }

    public double getPrice_change_24h() {
        return price_change_24h;
    }

    public double getPrice_change_percentage_24h() {
        return price_change_percentage_24h;
    }

    public double getMarket_cap_change_24h() {
        return market_cap_change_24h;
    }

    public double getMarket_cap_change_percentage_24h() {
        return market_cap_change_percentage_24h;
    }

    public double getCirculating_supply() {
        return circulating_supply;
    }

    public double getTotal_supply() {
        return total_supply;
    }

    public double getMax_supply() {
        return max_supply;
    }

    public double getAth() {
        return ath;
    }

    public double getAth_change_percentage() {
        return ath_change_percentage;
    }

    public String getAth_date() {
        return ath_date;
    }

    public double getAtl() {
        return atl;
    }

    public double getAtl_change_percentage() {
        return atl_change_percentage;
    }

    public String getAtl_date() {
        return atl_date;
    }

    public Object getRoi() {
        return roi;
    }

    public String getLast_updated() {
        return last_updated;
    }


    public double getPrice_change_percentage_24h_in_currency() {
        return price_change_percentage_24h_in_currency;
    }

    public double getPrice_change_percentage_7d_in_currency() {
        return price_change_percentage_7d_in_currency;
    }
}
