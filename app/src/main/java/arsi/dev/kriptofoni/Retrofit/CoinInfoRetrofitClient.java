package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CoinInfoRetrofitClient {
    private static CoinInfoRetrofitClient instance = null;
    private CoinInfoApi myCoinGeckoApi;

    private CoinInfoRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(myCoinGeckoApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myCoinGeckoApi = retrofit.create(CoinInfoApi.class);
    }

    public static synchronized CoinInfoRetrofitClient getInstance() {
        if (instance == null) {
            instance = new CoinInfoRetrofitClient();
        }
        return instance;
    }

    public CoinInfoApi getMyCoinGeckoApi() {
        return myCoinGeckoApi;
    }
}
