package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CryptoIconsApi {
    String BASE_URL = "https://cryptoicons.org/api/";

    @GET("icon/")
    Call<Icons> getIcons();
}
