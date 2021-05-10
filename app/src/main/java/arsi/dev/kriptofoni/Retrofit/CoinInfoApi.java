package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoinInfoApi {
    String BASE_URL = "https://api.coingecko.com/api/v3/";

    @GET("coins/{id}")
    Call<JsonObject> getCoinInfo(@Path("id")String id, @Query("localization") String localization,
                                 @Query("tickers") boolean tickers,
                                 @Query("market_data") boolean marketData ,
                                 @Query("community_data") boolean communityData,
                                 @Query("developer_data") boolean developerData,
                                 @Query("spark_line") boolean sparkData);

    @GET("simple/price")
    Call<JsonObject> getCoinSimple(@Query("ids") String ids, @Query("vs_currencies") String vsCurrencies,
                                   @Query("include_24hr_vol") String include24HrVol);

    @GET("coins/{id}/market_chart/range")
    Call<JsonObject> getMarketChart(@Path("id") String id, @Query("vs_currency") String vsCurrency,
                                    @Query("from") String from, @Query("to") String to);

    @GET("coins/{id}/ohlc")
    Call<JsonArray> getOHLC(@Path("id") String id, @Query ("vs_currency") String vsCurrency,
                            @Query("days") Integer days);

    @GET("coins/{id}/ohlc")
    Call<JsonArray> getOHLC(@Path("id") String id, @Query ("vs_currency") String vsCurrency,
                             @Query("days") String days);
}
