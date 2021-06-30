package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UzmanCoinRetrofitClient {
    private static UzmanCoinRetrofitClient instance = null;
    private NewsApi newsApi;

    private UzmanCoinRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(newsApi.BASE_URL_UZMAN_COIN)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        newsApi = retrofit.create(NewsApi.class);
    }

    public static synchronized UzmanCoinRetrofitClient getInstance() {
        if (instance == null) {
            instance = new UzmanCoinRetrofitClient();
        }
        return instance;
    }

    public NewsApi getNewsApi() {
        return newsApi;
    }
}
