package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlarmsRetrofitClient {
    private static AlarmsRetrofitClient instance = null;
    private AlarmsApi myCoinGeckoApi;

    private AlarmsRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(myCoinGeckoApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myCoinGeckoApi = retrofit.create(AlarmsApi.class);
    }

    public static synchronized AlarmsRetrofitClient getInstance() {
        if (instance == null) {
            instance = new AlarmsRetrofitClient();
        }
        return instance;
    }

    public AlarmsApi getMyCoinGeckoApi() {
        return myCoinGeckoApi;
    }
}
