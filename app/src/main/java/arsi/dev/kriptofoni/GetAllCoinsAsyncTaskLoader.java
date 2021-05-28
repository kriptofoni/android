package arsi.dev.kriptofoni;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAllCoinsAsyncTaskLoader extends AsyncTaskLoader<String> {

    private CoinGeckoApi myCoinGeckoApi;
    private String currency;
    private int totalPageNumber;

    public GetAllCoinsAsyncTaskLoader(@NonNull Context context, int totalPageNumber, CoinGeckoApi myCoinGeckoApi, String currency) {
        super(context);
        this.myCoinGeckoApi = myCoinGeckoApi;
        this.currency = currency;
        this.totalPageNumber = totalPageNumber;
    }

    private void loadData (int index) {
        Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,"id_desc", 250, index, true, "24h,7d");
        call.enqueue(new Callback<List<CoinMarket>>() {
            @Override
            public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
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
                        if (response.code() == 429) {
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    loadData(index);
                                }
                            };
                            handler.postDelayed(runnable, 3000);
                        }
                        loadData(index);
                    }
                } else {
                    loadData(index);
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                if (t instanceof SocketTimeoutException) {
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
//        List<Observable<List<CoinMarket>>> requests = new ArrayList<>();
//        for (int i = 1; i <= totalPageNumber; i++) {
//            requests.add(myCoinGeckoApi.getCoinMarkets(currency, null,"id_desc", 250, i, true, "24h,7d"));
//        }
//
//        Observable.zip(
//                requests,
//                new Function<Object[], Object>() {
//                    @Override
//                    public Object apply(Object[] objects) throws Exception {
//                        List<List<CoinMarket>> response = new ArrayList<>();
//                        for (Object o : objects) {
//                            response.add((List<CoinMarket>) o);
//                        }
//                        return response;
//                    }
//                })
//                .subscribe(
//                        new Consumer<Object>() {
//                            @Override
//                            public void accept(Object o) throws Exception {
//                                List<List<CoinMarket>> coins = (List<List<CoinMarket>>) o;
//                                System.out.println("245 " + coins.size());
//                                for (int i = 0; i < coins.size(); i++) {
//                                    System.out.println(coins.get(i).size());
//                                }
//                            }
//                        },
//                        new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//                                System.out.println("251 " + throwable.toString());
//                            }
//                        }
//                );
        return "Task Done";
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }
}
