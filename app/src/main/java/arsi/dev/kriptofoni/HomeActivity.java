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
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.domain.Global.Global;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

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

public class HomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<String>> {

    private BottomNavigationView bottomNavigationView;
    private CoinGeckoApi myCoinGeckoApi;
    private ArrayList<CoinSearchModel> coinSearchModels, mostInc24Hours;
    private final MainFragment mainFragment = new MainFragment(this);
    private final PortfolioFragment portfolioFragment = new PortfolioFragment();
    private final AlertsFragment alertsFragment = new AlertsFragment();
    private final NewsFragment newsFragment = new NewsFragment();
    private final MoreFragment moreFragment = new MoreFragment();
    private Fragment active = mainFragment;
    private FragmentManager fragmentManager;
    private CoinGeckoApiClient client;
    private String currency;
    private int max, remainder;
    private ArrayList<String> names;
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();
        
        coinSearchModels = new ArrayList<>();
        mostInc24Hours = new ArrayList<>();
        names = new ArrayList<>();

        this.loaderManager = LoaderManager.getInstance(this);
        LoaderManager.LoaderCallbacks<List<String>> loaderCallbacks = this;
        Bundle args = new Bundle();
        Loader<List<String>> loader = this.loaderManager.initLoader(0, args, loaderCallbacks);
        loader.forceLoad();

        client = new CoinGeckoApiClientImpl();

        getTotalMarketCap();
//        getAllCoins();
        
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
        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency", "usd");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    int selectedId = 0;

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
        coinSearchModels.clear();
        max = 6548 / 250 + 1;
        for (int i = 1; i <= max; i++) {
            final int index = i;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    new GetAllCoins().execute(index);
                }
            };
            thread.start();
        }
    }

    public void getTotalMarketCap() {
        new GetTotalMarketCap().execute();
    }

    @NonNull
    @Override
    public Loader<List<String>> onCreateLoader(int id, @Nullable Bundle args) {
        return new GetAllCoinss(this, currency, myCoinGeckoApi, mainFragment);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<String>> loader, List<String> data) {
        loaderManager.destroyLoader(0);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<String>> loader) {

    }

    private class GetAllCoins extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... integers) {

//            List<CoinMarkets> coins = client.getCoinMarkets(currency, null, "id_asc", 250, integers[0], false, null);
//            for (int i = 0; i < coins.size(); i++) {
//                CoinMarkets coin = coins.get(i);
//                String name = coin.getName();
//                String id = coin.getId();
//                String symbol = coin.getSymbol();
//                String image = coin.getImage();
//                double priceChangeIn24h = coin.getPriceChangePercentage24h();
//                CoinSearchModel model = new CoinSearchModel((integers[0] - 1) * 100 + i + 1, id, name, symbol, image);
//                CoinSearchModel model24H = new CoinSearchModel((integers[0] - 1) * 100 + i + 1, id, name, symbol, image, priceChangeIn24h);
//                coinSearchModels.add(model);
//                mostInc24Hours.add(model24H);
//            }
//
//            mainFragment.setCoinModelsForSearch(coinSearchModels);
//            mainFragment.setMostIncIn24List(mostInc24Hours);

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
                            coinSearchModels.add(model);
                            mostInc24Hours.add(model24H);
                            if (!names.contains(name)) names.add(name);
                        }
                        mainFragment.setCoinModelsForSearch(coinSearchModels);
                        mainFragment.setMostIncIn24List(mostInc24Hours);
                        System.out.println(names.size());
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
        protected Void doInBackground(Void... voids) {

            try {
                Global global = client.getGlobal();
                if (global != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainFragment.setTotalMarketValue(global.getData().getTotalMarketCap().get(currency));
                        }
                    });
                }
            } catch (CoinGeckoApiException ex) {

            }


//            Call<Global> call = myCoinGeckoApi.getGlobal();
//            call.enqueue(new Callback<Global>() {
//                @Override
//                public void onResponse(Call<Global> call, Response<Global> response) {
//                    if (response.isSuccessful()) {
//                        Global global = response.body();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mainFragment.setTotalMarketValue(Double.parseDouble(global.getData().getAsJsonObject("total_market_cap").get(currency).toString()));
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Global> call, Throwable t) {
//
//                }
//            });
            return null;
        }
    }
}
