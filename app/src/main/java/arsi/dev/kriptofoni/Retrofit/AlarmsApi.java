package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AlarmsApi {
    String BASE_URL = "https://api.coingecko.com/api/v3/";

    @GET("coins/markets")
    Call<List<CoinMarket>> getCoinMarkets(@Query("vs_currency") String vsCurrency, @Query("ids") String ids,
                                                @Query("order") String order, @Query("per_page") Integer perPage,
                                                @Query("page") Integer page, @Query("sparkline") boolean sparkline,
                                                @Query("price_change_percentage") String priceChangePercentage);

}
