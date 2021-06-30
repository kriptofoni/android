package arsi.dev.kriptofoni.Retrofit;

import com.google.gson.JsonArray;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {
    String BASE_URL_UZMAN_COIN = "https://uzmancoin.com/wp-json/wp/v2/";
    String BASE_URL_KOIN_BULTENI = "https://koinbulteni.com/wp-json/wp/v2/";
    String BASE_URL_KRIPTOFONI = "https://www.kriptofoni.com/wp-json/wp/v2/";


    @GET("posts?_embed")
    Call<JsonArray> getPosts();
}
