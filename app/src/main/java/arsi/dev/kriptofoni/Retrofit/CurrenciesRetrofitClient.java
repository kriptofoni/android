package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrenciesRetrofitClient {
    private static CurrenciesRetrofitClient instance = null;
    private CurrenciesApi myCoinGeckoApi;

    private CurrenciesRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(myCoinGeckoApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myCoinGeckoApi = retrofit.create(CurrenciesApi.class);
    }

    public static synchronized CurrenciesRetrofitClient getInstance() {
        if (instance == null) {
            instance = new CurrenciesRetrofitClient();
        }
        return instance;
    }

    public CurrenciesApi getMyCoinGeckoApi() {
        return myCoinGeckoApi;
    }
}
