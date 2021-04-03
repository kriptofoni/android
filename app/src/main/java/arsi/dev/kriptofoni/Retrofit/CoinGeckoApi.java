package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoinGeckoApi {
    String BASE_URL = "https://api.coingecko.com/api/v3/";
    @GET("coins/markets")
    Call<List<CoinMarket>> getCoinMarkets(@Query("vs_currency") String vsCurrency, @Query("ids") String ids,
                                          @Query("order") String order, @Query("per_page") Integer perPage,
                                          @Query("page") Integer page, @Query("sparkline") boolean sparkline,
                                          @Query("price_change_percentage") String priceChangePercentage);

    @GET("global")
    Call<Global> getGlobal();

    @GET("simple/supported_vs_currencies")
    Call<String[]> getCurrencies();

    @GET("coins/list")
    Call<List<Coin>> getCoins(@Query("include_platform") Boolean includePlatform);

    @GET("coins/{id}")
    Call<JsonObject> getCoinInfo(@Path("id")String id, @Query("localization") String localization,
                                 @Query("tickers") boolean tickers,
                                 @Query("market_data") boolean marketData ,
                                 @Query("community_data") boolean communityData,
                                 @Query("developer_data") boolean developerData,
                                 @Query("spark_line") boolean sparkData);
}
