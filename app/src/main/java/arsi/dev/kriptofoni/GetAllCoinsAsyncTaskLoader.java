package arsi.dev.kriptofoni;

import android.content.AsyncTaskLoader;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAllCoinsAsyncTaskLoader extends AsyncTaskLoader<String> {

    private int totalPageNumber;
    private CoinGeckoApi myCoinGeckoApi;
    private String currency;

    public GetAllCoinsAsyncTaskLoader(@NonNull Context context, int totalPageNumber, CoinGeckoApi myCoinGeckoApi, String currency) {
        super(context);
        this.totalPageNumber = totalPageNumber;
        this.myCoinGeckoApi = myCoinGeckoApi;
        this.currency = currency;
    }

    @Nullable
    @Override
    public String loadInBackground() {
        for (int i = 1; i <= totalPageNumber; i++) {
            final int index = i;
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,"id_desc", 250, index, true, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    if (response.isSuccessful()) {
                        List<CoinMarket> coins = response.body();
                        int lastIndex = coins.size();
                        int difference = lastIndex % 10 == 0 ? lastIndex / 10 : lastIndex / 10 + 1;
                        // We create 10 different threads to speed up model creation and listing.
                        for (int j = 0; j < 10; j++) {
                            Thread thread;
                            if  (j == 0) {
                                thread = new HomeActivity.MyThread(coins, 0, difference, index);
                            } else if (j == 9) {
                                thread = new HomeActivity.MyThread(coins, difference * j, lastIndex, index);
                            } else {
                                thread = new HomeActivity.MyThread(coins, difference * j, difference * (j + 1), index);
                            }
                            thread.start();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                    loadInBackground();
                }
            });
        }
        return "Task Result";
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }
}
