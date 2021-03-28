package arsi.dev.kriptofoni.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CryptoIconsRetrofitClient {
    private static CryptoIconsRetrofitClient instance = null;
    private CryptoIconsApi myCryptoIconsApi;

    private CryptoIconsRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(myCryptoIconsApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myCryptoIconsApi = retrofit.create(CryptoIconsApi.class);
    }

    public static synchronized CryptoIconsRetrofitClient getInstance() {
        if (instance == null) {
            instance = new CryptoIconsRetrofitClient();
        }
        return instance;
    }

    public CryptoIconsApi getMyCryptoIconsApi() {
        return myCryptoIconsApi;
    }
}
