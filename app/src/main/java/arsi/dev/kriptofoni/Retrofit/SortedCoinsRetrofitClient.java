package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SortedCoinsRetrofitClient {
    private static SortedCoinsRetrofitClient instance = null;
    private SortedCoinsApi myCoinGeckoApi;

    private SortedCoinsRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(myCoinGeckoApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myCoinGeckoApi = retrofit.create(SortedCoinsApi.class);
    }

    public static synchronized SortedCoinsRetrofitClient getInstance() {
        if (instance == null) {
            instance = new SortedCoinsRetrofitClient();
        }
        return instance;
    }

    public SortedCoinsApi getMyCoinGeckoApi() {
        return myCoinGeckoApi;
    }
}
