package arsi.dev.kriptofoni;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.domain.Global.Global;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import arsi.dev.kriptofoni.Fragments.AlertsFragment;
import arsi.dev.kriptofoni.Fragments.MainFragment;
import arsi.dev.kriptofoni.Fragments.MoreFragment;
import arsi.dev.kriptofoni.Fragments.NewsFragment;
import arsi.dev.kriptofoni.Fragments.PortfolioFragment;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private final MainFragment mainFragment = new MainFragment(this);
    private final PortfolioFragment portfolioFragment = new PortfolioFragment();
    private final AlertsFragment alertsFragment = new AlertsFragment();
    private final NewsFragment newsFragment = new NewsFragment();
    private final MoreFragment moreFragment = new MoreFragment();
    private Fragment active = mainFragment;
    private FragmentManager fragmentManager;

    private CoinGeckoApi myCoinGeckoApi;
    private CoinGeckoApiClient client;

    private List<CoinSearchModel> coinSearchModels, mostInc24Hours, mostInc7Days,
            coinSearchModelsFromMem, mostInc24HoursFromMem, mostInc7DaysFromMem;
    private ArrayList<String> names;

//    private ArrayList<CoinSearchModel> coinSearchModels, mostInc24Hours, mostInc7Days,
//            coinSearchModelsFromMem, mostInc24HoursFromMem, mostInc7DaysFromMem;
//    private ArrayList<String> names;

    private String currency;
    private int max, totalPageNumber, tasksDone = 0;
    private boolean firstLoad = true;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        coinSearchModels = new ArrayList<>();
//        mostInc24Hours = new ArrayList<>();
//        coinSearchModelsFromMem = new ArrayList<>();
//        mostInc24HoursFromMem = new ArrayList<>();
//        mostInc7Days = new ArrayList<>();
//        mostInc7DaysFromMem = new ArrayList<>();
//        names = new ArrayList<>();

        coinSearchModels = Collections.synchronizedList(new ArrayList<>());
        mostInc24Hours = Collections.synchronizedList(new ArrayList<>());
        coinSearchModelsFromMem = Collections.synchronizedList(new ArrayList<>());
        mostInc24HoursFromMem = Collections.synchronizedList(new ArrayList<>());
        mostInc7Days = Collections.synchronizedList(new ArrayList<>());
        mostInc7DaysFromMem = Collections.synchronizedList(new ArrayList<>());

        myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();
        sharedPreferences = getSharedPreferences("Preferences", 0);
        Gson gson = new Gson();

        String coinSearchModelsJson = sharedPreferences.getString("coinModelsForSearch", "");
        String mostInc24HoursJson = sharedPreferences.getString("mostIncIn24List", "");
        String mostInc7DaysJson = sharedPreferences.getString("mostIncIn7List", "");
        Type type = new TypeToken<List<CoinSearchModel>>() {}.getType();
        if (!coinSearchModelsJson.isEmpty()) coinSearchModelsFromMem = gson.fromJson(coinSearchModelsJson, type);
        if (!mostInc24HoursJson.isEmpty()) mostInc24HoursFromMem = gson.fromJson(mostInc24HoursJson, type);
        if (!mostInc7DaysJson.isEmpty()) mostInc7DaysFromMem = gson.fromJson(mostInc7DaysJson, type);

        client = new CoinGeckoApiClientImpl();

        getTotalMarketCap();
        
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container, moreFragment,"5").hide(moreFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, newsFragment,"4").hide(newsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, alertsFragment,"3").hide(alertsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, portfolioFragment,"2").hide(portfolioFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, mainFragment,"1").commit();
        bottomNavigationView.getMenu().getItem(0).setEnabled(false);

    }

    @Override
    protected void onStart() {
        super.onStart();
        currency = sharedPreferences.getString("currency", "usd");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()) {
                        case R.id.nav_main:
                            fragmentManager.beginTransaction().hide(active).show(mainFragment).commit();
                            bottomNavigationView.getMenu().getItem(Integer.parseInt(active.getTag()) - 1).setEnabled(true);
                            active = mainFragment;
                            bottomNavigationView.getMenu().getItem(0).setEnabled(false);
                            return true;
                        case R.id.nav_portfolio:
                            fragmentManager.beginTransaction().hide(active).show(portfolioFragment).commit();
                            bottomNavigationView.getMenu().getItem(Integer.parseInt(active.getTag()) - 1).setEnabled(true);
                            active = portfolioFragment;
                            bottomNavigationView.getMenu().getItem(1).setEnabled(false);
                            return true;
                        case R.id.nav_alerts:
                            fragmentManager.beginTransaction().hide(active).show(alertsFragment).commit();
                            bottomNavigationView.getMenu().getItem(Integer.parseInt(active.getTag()) - 1).setEnabled(true);
                            active = alertsFragment;
                            bottomNavigationView.getMenu().getItem(2).setEnabled(false);
                            return true;
                        case R.id.nav_news:
                            fragmentManager.beginTransaction().hide(active).show(newsFragment).commit();
                            bottomNavigationView.getMenu().getItem(Integer.parseInt(active.getTag()) - 1).setEnabled(true);
                            active = newsFragment;
                            bottomNavigationView.getMenu().getItem(3).setEnabled(false);
                            return true;
                        case R.id.nav_more:
                            fragmentManager.beginTransaction().hide(active).show(moreFragment).commit();
                            bottomNavigationView.getMenu().getItem(Integer.parseInt(active.getTag()) - 1).setEnabled(true);
                            active = moreFragment;
                            bottomNavigationView.getMenu().getItem(4).setEnabled(false);
                            return true;
                    }

                    return false;
                }
            };
    
    private void getAllCoins() {
        firstLoad = false;
        for (int i = 1; i <= totalPageNumber; i++) {
            new GetAllCoins().execute(i);
        }
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (coinSearchModels.size() == max && mostInc24Hours.size() == max && mostInc7Days.size() == max && max != 0) {
                    mainFragment.setCoinModelsForSearch(coinSearchModels);
                    mainFragment.setMostIncIn24List(mostInc24Hours);
                    mainFragment.setMostDecIn24List(mostInc24Hours);
                    mainFragment.setMostIncIn7List(mostInc7Days);
                    mainFragment.setMostDecIn7List(mostInc7Days);

                    mainFragment.writeToMem("coinModelsForSearch", coinSearchModels);
                    mainFragment.writeToMem("mostIncIn24List", mostInc24Hours);
                    mainFragment.writeToMem("mostIncIn7List", mostInc7Days);
                    return;
                }
                handler.postDelayed(this, 250);
            }
        };
        handler.postDelayed(runnable, 250);
    }

    public void getTotalMarketCap() {
        new GetTotalMarketCap().execute();
    }

    private class GetAllCoins extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... integers) {
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,"id_desc", 250, integers[0], true, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    if (response.isSuccessful()) {
                        int index = integers[0];
                        List<CoinMarket> coins = response.body();
                        int lastIndex = coins.size();
                        int difference = lastIndex % 10 == 0 ? lastIndex / 10 : lastIndex / 10 + 1 ;
                        Thread thread1 = new MyThread(coins, 0, difference, index);
                        Thread thread2 = new MyThread(coins, difference, difference * 2, index);
                        Thread thread3 = new MyThread(coins, difference * 2, difference * 3, index);
                        Thread thread4 = new MyThread(coins, difference * 3, difference * 4, index);
                        Thread thread5 = new MyThread(coins, difference * 4, difference * 5, index);
                        Thread thread6 = new MyThread(coins, difference * 5, difference * 6, index);
                        Thread thread7 = new MyThread(coins, difference * 6, difference * 7, index);
                        Thread thread8 = new MyThread(coins, difference * 7, difference * 8, index);
                        Thread thread9 = new MyThread(coins, difference * 8, difference * 9, index);
                        Thread thread10 = new MyThread(coins, difference * 9, lastIndex, index);

                        thread1.start();
                        thread2.start();
                        thread3.start();
                        thread4.start();
                        thread5.start();
                        thread6.start();
                        thread7.start();
                        thread8.start();
                        thread9.start();
                        thread10.start();
//                        for (int i = 0; i < coins.size(); i++) {
//                            CoinMarket coin = coins.get(i);
//                            String name = coin.getName();
//                            String id = coin.getId();
//                            String symbol = coin.getSymbol();
//                            String image = coin.getImage();
//                            double priceChangeIn24h = coin.getPrice_change_percentage_24h_in_currency();
//                            double priceChangeIn7d = coin.getPrice_change_percentage_7d_in_currency();
//                            double marketCapRank = coin.getMarket_cap_rank();
//                            double marketCap = coin.getMarket_cap();
//                            CoinSearchModel model = new CoinSearchModel(marketCapRank, id, name, symbol, marketCap, image);
//                            CoinSearchModel model24H = new CoinSearchModel((index - 1) * 100 + i + 1, id, name, symbol, image, priceChangeIn24h);
//                            CoinSearchModel model7D = new CoinSearchModel(id, name, symbol, image, priceChangeIn7d, (index - 1) * 100 + i + 1);
//                            coinSearchModels.add(model);
//                            mostInc24Hours.add(model24H);
//                            mostInc7Days.add(model7D);
//                        }
//                        System.out.println(coinSearchModels.size() + " , " + mostInc24Hours.size() + " , " + mostInc7Days.size());
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {

                }
            });
            return true;
        }
    }

    private class GetTotalMarketCap extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (firstLoad) {
                if (!coinSearchModelsFromMem.isEmpty())
                    mainFragment.setCoinModelsForSearch(coinSearchModelsFromMem);
                if (!mostInc24HoursFromMem.isEmpty()) {
                    mainFragment.setMostIncIn24List(mostInc24HoursFromMem);
                    mainFragment.setMostDecIn24List(mostInc24HoursFromMem);
                }
                if (!mostInc7DaysFromMem.isEmpty()) {
                    mainFragment.setMostIncIn7List(mostInc7DaysFromMem);
                    mainFragment.setMostDecIn7List(mostInc7DaysFromMem);
                }
                getAllCoins();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Global global = client.getGlobal();
                if (global != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainFragment.setTotalMarketValue(global.getData().getTotalMarketCap().get(currency));
                            max = (int) global.getData().getActiveCryptocurrencies();
                            totalPageNumber = max / 250 + 1;
                        }
                    });
                }
            } catch (CoinGeckoApiException ex) {
                System.out.println(ex.getMessage());
            }
            return null;
        }
    }

    private class MyThread extends Thread {

        private List<CoinMarket> coins;
        private int first, last, index;

        public MyThread(List<CoinMarket> coins, int first, int last, int index) {
            this.coins = coins;
            this.first = first;
            this.last = last;
            this.index = index;
        }

        @Override
        public void run() {
            super.run();
            List<CoinSearchModel> search = Collections.synchronizedList(new ArrayList<>());
            List<CoinSearchModel> most24 = Collections.synchronizedList(new ArrayList<>());
            List<CoinSearchModel> most7 = Collections.synchronizedList(new ArrayList<>());
            for (int i = first; i < last; i++) {
                CoinMarket coin = coins.get(i);
                String name = coin.getName();
                String id = coin.getId();
                String symbol = coin.getSymbol();
                String image = coin.getImage();
                double priceChangeIn24h = coin.getPrice_change_percentage_24h_in_currency();
                double priceChangeIn7d = coin.getPrice_change_percentage_7d_in_currency();
                double marketCapRank = coin.getMarket_cap_rank();
                double marketCap = coin.getMarket_cap();
                CoinSearchModel model = new CoinSearchModel(marketCapRank, id, name, symbol, marketCap, image);
                CoinSearchModel model24H = new CoinSearchModel((index - 1) * 100 + i + 1, id, name, symbol, image, priceChangeIn24h);
                CoinSearchModel model7D = new CoinSearchModel(id, name, symbol, image, priceChangeIn7d, (index - 1) * 100 + i + 1);
                search.add(model);
                most24.add(model24H);
                most7.add(model7D);
            }
            synchronized (search) {
                coinSearchModels.addAll(search);
            }

            synchronized (most24) {
                mostInc24Hours.addAll(most24);
            }

            synchronized (search) {
                mostInc7Days.addAll(most7);
            }

//            System.out.println(coinSearchModels.size() + " , " + mostInc24Hours.size() + " , " + mostInc7Days.size());
        }
    }
}
