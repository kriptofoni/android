package arsi.dev.kriptofoni;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CoinGeckoApi myCoinGeckoApi;
    private ArrayList<CoinSearchModel> coinSearchModels;
    private final MainFragment mainFragment = new MainFragment(this);
    private final PortfolioFragment portfolioFragment = new PortfolioFragment();
    private final AlertsFragment alertsFragment = new AlertsFragment();
    private final NewsFragment newsFragment = new NewsFragment();
    private final MoreFragment moreFragment = new MoreFragment();
    private Fragment active = mainFragment;
    private FragmentManager fragmentManager;
    private CoinGeckoApiClient client;
    private String currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();
        
        coinSearchModels = new ArrayList<>();

        client = new CoinGeckoApiClientImpl();

        getTotalMarketCap();
        getAllCoins();
        
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
        for (int i = 1; i <= 6544 / 250 + 1; i++) {
            new GetAllCoins().execute(i);
        }
    }

    public void getTotalMarketCap() {
        new GetTotalMarketCap().execute();
    }

    private class GetAllCoins extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... integers) {
//
//            List<CoinMarkets> coins = client.getCoinMarkets("usd", null, null, 250, integers[0], false, null);
//            for (int i = 0; i < coins.size(); i++) {
//                CoinMarkets coin = coins.get(i);
//                String name = coin.getName();
//                String id = coin.getId();
//                String symbol = coin.getSymbol();
//                String image = coin.getImage();
//                CoinSearchModel model = new CoinSearchModel((integers[0] - 1) * 100 + i + 1, id, name, symbol, image);
//                coinSearchModels.add(model);
//            }
//
//            mainFragment.setCoinModelsForSearch(coinSearchModels);

            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets("usd", null, 250, integers[0], false, null);
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
                            CoinSearchModel model = new CoinSearchModel((integers[0] - 1) * 100 + i + 1, id, name, symbol, image);
                            coinSearchModels.add(model);
                        }
                        mainFragment.setCoinModelsForSearch(coinSearchModels);
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
