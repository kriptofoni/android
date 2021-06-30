package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KoinBulteniRetrofitClient {
    private static KoinBulteniRetrofitClient instance = null;
    private NewsApi newsApi;

    private KoinBulteniRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(newsApi.BASE_URL_KOIN_BULTENI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        newsApi = retrofit.create(NewsApi.class);
    }

    public static synchronized KoinBulteniRetrofitClient getInstance() {
        if (instance == null) {
            instance = new KoinBulteniRetrofitClient();
        }
        return instance;
    }

    public NewsApi getNewsApi() {
        return newsApi;
    }
}
