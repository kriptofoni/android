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

    private ArrayList<CoinSearchModel> coinSearchModels, mostInc24Hours, mostInc7Days,
            coinSearchModelsFromMem, mostInc24HoursFromMem, mostInc7DaysFromMem;
    private ArrayList<String> names;

    private String currency;
    private int max, totalPageNumber, tasksDone = 0;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        coinSearchModels = new ArrayList<>();
        mostInc24Hours = new ArrayList<>();
        coinSearchModelsFromMem = new ArrayList<>();
        mostInc24HoursFromMem = new ArrayList<>();
        mostInc7Days = new ArrayList<>();
        mostInc7DaysFromMem = new ArrayList<>();
        names = new ArrayList<>();

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
        for (int i = 1; i <= totalPageNumber; i++) {
            Thread thread = new MyThread(i);
            thread.start();
        }
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (coinSearchModels.size() == max) {
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
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,"id_asc", 250, integers[0], false, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    if (response.isSuccessful()) {
                        List<CoinMarket> coins = response.body();
                        for (int i = 0; i < coins.size(); i++) {
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
                            CoinSearchModel model24H = new CoinSearchModel((integers[0] - 1) * 100 + i + 1, id, name, symbol, image, priceChangeIn24h);
                            CoinSearchModel model7D = new CoinSearchModel(id, name, symbol, image, priceChangeIn7d, (integers[0] - 1) * 100 + i + 1);
                            coinSearchModels.add(model);
                            mostInc24Hours.add(model24H);
                            mostInc7Days.add(model7D);
                        }
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

        private int index;

        public MyThread(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            super.run();
            new GetAllCoins().execute(index);
        }
    }
}
