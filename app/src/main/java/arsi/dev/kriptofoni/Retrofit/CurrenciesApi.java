package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CurrenciesApi {
    String BASE_URL = "https://api.coingecko.com/api/v3/";

    @GET("simple/supported_vs_currencies")
    Call<String[]> getCurrencies();

    @GET("global")
    Call<Global> getGlobal();
}
