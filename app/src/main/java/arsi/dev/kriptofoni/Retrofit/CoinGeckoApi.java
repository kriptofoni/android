package arsi.dev.kriptofoni.Retrofit;

import java.lang.reflect.Parameter;
import java.util.List;

import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CoinGeckoApi {
    String BASE_URL = "https://api.coingecko.com/api/v3/";
    @GET("coins/markets")
    Call<List<CoinMarket>> getCoinMarkets(@Query("vs_currency") String vsCurrency,
                                          @Query("order") String order, @Query("per_page") Integer perPage,
                                          @Query("page") Integer page, @Query("sparkline") boolean sparkline,
                                          @Query("price_change_percentage") String priceChangePercentage);

    @GET("global")
    Call<Global> getGlobal();

    @GET("simple/supported_vs_currencies")
    Call<String[]> getCurrencies();

    @GET("coins/list")
    Call<List<Coin>> getCoins(@Query("include_platform") Boolean includePlatform);
}
