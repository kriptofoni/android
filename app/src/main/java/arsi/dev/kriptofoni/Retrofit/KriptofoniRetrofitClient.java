package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KriptofoniRetrofitClient {
    private static KriptofoniRetrofitClient instance = null;
    private NewsApi newsApi;

    private KriptofoniRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(newsApi.BASE_URL_KRIPTOFONI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        newsApi = retrofit.create(NewsApi.class);
    }

    public static synchronized KriptofoniRetrofitClient getInstance() {
        if (instance == null) {
            instance = new KriptofoniRetrofitClient();
        }
        return instance;
    }

    public NewsApi getNewsApi() {
        return newsApi;
    }
}
