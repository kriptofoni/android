package arsi.dev.kriptofoni;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;

import android.content.Context;
import androidx.loader.content.Loader;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Global.Global;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Fragments.AlertsFragment;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.AlertFragment;
import arsi.dev.kriptofoni.Fragments.MainFragment;
import arsi.dev.kriptofoni.Fragments.MoreFragment;
import arsi.dev.kriptofoni.Fragments.NewsFragment;
import arsi.dev.kriptofoni.Fragments.PortfolioFragment;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private BottomNavigationView bottomNavigationView;
    private MainFragment mainFragment;
    private PortfolioFragment portfolioFragment;
    private AlertsFragment alertsFragment;
    private NewsFragment newsFragment;
    private MoreFragment moreFragment;
    private Fragment active;
    private FragmentManager fragmentManager;
    private RelativeLayout splashScreen, mainScreen;

    private CoinGeckoApi myCoinGeckoApi;
    private CoinGeckoApiClient client;

    private static List<CoinSearchModel> coinSearchModels;
    private List<CoinSearchModel> coinSearchModelsFromMem, mostInc24HoursFromMem, mostInc7DaysFromMem;

    private String currency;
    private int max, totalPageNumber;
    private boolean firstLoad = true, fetchAllCoins = false;
    private double lastFetchOfAllCoins;

    private SharedPreferences sharedPreferences;

    private LoaderManager loaderManager;

    public HomeActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loaderManager = LoaderManager.getInstance(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (savedInstanceState != null) {
            System.out.println("savedInstance");
        }

        mainFragment = new MainFragment(this);
        portfolioFragment = new PortfolioFragment();
        alertsFragment = new AlertsFragment(this);
        newsFragment = new NewsFragment();
        moreFragment = new MoreFragment();

        active = mainFragment;

        splashScreen = findViewById(R.id.splash_screen);
        mainScreen = findViewById(R.id.main_screen);

        coinSearchModels = Collections.synchronizedList(new ArrayList<>());
        coinSearchModelsFromMem = Collections.synchronizedList(new ArrayList<>());
        mostInc24HoursFromMem = Collections.synchronizedList(new ArrayList<>());
        mostInc7DaysFromMem = Collections.synchronizedList(new ArrayList<>());

        myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();
        sharedPreferences = getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency", "");
        if (currency.isEmpty()) {
            currency = "usd";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currency", currency);
            editor.apply();
        }
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
        fragmentManager.beginTransaction().add(R.id.fragment_container, alertsFragment,"3").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, portfolioFragment,"2").hide(portfolioFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, mainFragment,"1").commit();
        bottomNavigationView.getMenu().getItem(0).setEnabled(false);

        // Remove splash screen after 3 seconds
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().hide(alertsFragment).commit();
                setScreen();
            }
        };
        handler.postDelayed(runnable, 3000);

    }

    @Override
    public void onBackPressed() {
        if (active == newsFragment) {
            WebView webView = newsFragment.getWebView();
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                moveTaskToBack(true);
            }
        } else {
            moveTaskToBack(true);
        }
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
        // Fetching all coins is required because CoinGeckoAPI doesn't have sorting options
        // by coins' 24 hour price change percent and 7 days price change percent.
        // In order to obtain sorted coins, we fetch all the coins that CoinGeckoApi listed.
        // And sort them according to the desired values.

        if (firstLoad)
            firstLoad = false;
        // Since downloading the data of all coins is a long process,
        // we are launching an AsyncTaskLoader to perform this process.
        loaderManager.initLoader(0, null, this);

        // We use a handler that repeats every 250ms to find out that the
        // download of all coins is complete.
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (coinSearchModels.size() == max && max != 0) {
                    System.out.println("run");
                    // When the data download is complete, we send the data to the required pages.
                    mainFragment.setCoinModelsForSearch(coinSearchModels);
                    mainFragment.setMostIncIn24List(coinSearchModels);
                    mainFragment.setMostDecIn24List(coinSearchModels);
                    mainFragment.setMostIncIn7List(coinSearchModels);
                    mainFragment.setMostDecIn7List(coinSearchModels);

                    // We save the data in memory to prevent the user from waiting with
                    // the download when the application is opened again.
                    mainFragment.writeToMem("coinModelsForSearch", coinSearchModels);
                    mainFragment.writeToMem("mostIncIn24List", coinSearchModels);
                    mainFragment.writeToMem("mostIncIn7List", coinSearchModels);
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

    public Fragment getActive() {
        return active;
    }

    public void setScreen() {
        splashScreen.setVisibility(View.GONE);
        mainScreen.setVisibility(View.VISIBLE);
    }

    public void resetMainFragments() {
        mainFragment.resetFragments();
    }

    public boolean isFetchAllCoins() {
        return fetchAllCoins;
    }

    @Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        return new GetAllCoinsAsyncTaskLoader(this, totalPageNumber, myCoinGeckoApi, currency);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {}

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {}


    private class GetTotalMarketCap extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // At the first opening of the application, if there is data stored in memory,
            // we send this data to the required pages.
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

                firstLoad = false;

                // Don't fetch all coins if already fetched in last 10 mins.
                lastFetchOfAllCoins = sharedPreferences.getFloat("lastFetchOfAllCoins", 0);
                System.out.println(lastFetchOfAllCoins - (System.currentTimeMillis() - 1000 * 60 * 10));
                getAllCoins();
                if (lastFetchOfAllCoins < System.currentTimeMillis() - 1000 * 60 * 10) {
                    fetchAllCoins = true;

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("lastFetchOfAllCoins", System.currentTimeMillis());
                    editor.apply();
                }
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
                            currency = sharedPreferences.getString("currency", "usd");
                            System.out.println(global.getData().getTotalMarketCap().get(currency));
                            mainFragment.setTotalMarketValue(global.getData().getTotalMarketCap().get(currency));
                            max = (int) global.getData().getActiveCryptocurrencies();
                            totalPageNumber = max / 250 + 1;
                        }
                    });
                }
            } catch (CoinGeckoApiException ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new GetTotalMarketCap().execute();
                    }
                });
                cancel(true);
            }
            return null;
        }
    }

    public static class MyThread extends Thread {

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
            for (int i = first; i < last; i++) {
                CoinMarket coin = coins.get(i);
                String name = coin.getName();
                String id = coin.getId();
                String symbol = coin.getSymbol();
                String image = coin.getImage();
                double priceIn24 = coin.getPrice_change_24h();
                double priceChangeIn24h = coin.getPrice_change_percentage_24h_in_currency();
                double priceChangeIn7d = coin.getPrice_change_percentage_7d_in_currency();
                double marketCapRank = coin.getMarket_cap_rank();
                double marketCap = coin.getMarket_cap();
                double currentPrice = coin.getCurrent_price();
                CoinSearchModel model = new CoinSearchModel(marketCapRank, id, name, symbol, marketCap, image, priceChangeIn24h, priceChangeIn7d, (index - 1) * 100 + i + 1, currentPrice, priceIn24);
                search.add(model);
            }
            synchronized (search) {
                coinSearchModels.addAll(search);
            }
//            System.out.println(coinSearchModels.size());
        }
    }
}
