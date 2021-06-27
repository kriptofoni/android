package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CoinGeckoRetrofitClient {
//    private static CoinGeckoRetrofitClient instance = null;
    private CoinGeckoApi myCoinGeckoApi;

    public CoinGeckoRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(myCoinGeckoApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myCoinGeckoApi = retrofit.create(CoinGeckoApi.class);
    }

//    public static synchronized CoinGeckoRetrofitClient getInstance() {
//        if (instance == null) {
//            instance = new CoinGeckoRetrofitClient();
//        }
//        return instance;
//    }

    public CoinGeckoApi getMyCoinGeckoApi() {
        return myCoinGeckoApi;
    }
}
