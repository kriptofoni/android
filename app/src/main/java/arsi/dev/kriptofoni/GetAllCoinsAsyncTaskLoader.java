package arsi.dev.kriptofoni;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.net.SocketTimeoutException;
import java.util.List;

import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAllCoinsAsyncTaskLoader extends AsyncTaskLoader<String> {

    private String currency;
    private int totalPageNumber;
    private boolean firstLoad = true;
    private HomeActivity homeActivity;

    public GetAllCoinsAsyncTaskLoader(@NonNull Context context, int totalPageNumber, String currency, HomeActivity homeActivity) {
        super(context);
        this.currency = currency;
        this.totalPageNumber = totalPageNumber;
        this.homeActivity = homeActivity;
    }

    private void loadData (int index) {

        CoinGeckoApi myCoinGeckoApi = new CoinGeckoRetrofitClient().getMyCoinGeckoApi();

        Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,"id_desc", 250, index, true, "24h,7d");
        call.enqueue(new Callback<List<CoinMarket>>() {
            @Override
            public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                System.out.println("loaded");
                if (response.isSuccessful()) {
                    List<CoinMarket> coins = response.body();
                    if (coins != null) {
                        int lastIndex = coins.size();
                        int difference = lastIndex % 10 == 0 ? lastIndex / 10 : lastIndex / 10 + 1;
                        // We create 10 different threads to speed up model creation and listing.
                        if (index != totalPageNumber) {
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
                        } else {
                            Thread thread = new HomeActivity.MyThread(coins, 0, lastIndex, index);
                            thread.start();
                        }
                    } else {
                        loadData(index);
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                loadData(index);
                            }
                        };
                        handler.postDelayed(runnable, 3000);
                    } else {
                        loadData(index);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                if (t instanceof SocketTimeoutException) {
                    System.out.println(t.getMessage());
                    loadData(index);
                }
            }
        });
    }

    @Nullable
    @Override
    public String loadInBackground() {
        for (int i = 1; i <= totalPageNumber ; i++) {
            loadData(i);
        }

        return "Task Done";
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (firstLoad) {
            forceLoad();
            firstLoad = false;
        }
    }
}
