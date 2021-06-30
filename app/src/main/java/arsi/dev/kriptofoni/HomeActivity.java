package arsi.dev.kriptofoni;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import androidx.loader.content.Loader;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
import arsi.dev.kriptofoni.Retrofit.CoinInfoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.CurrenciesApi;
import arsi.dev.kriptofoni.Retrofit.CurrenciesRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.Global;
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

    private CurrenciesApi myCoinGeckoGlobalApi;

    private static List<CoinSearchModel> coinSearchModels;
    private List<CoinSearchModel> coinSearchModelsFromMem, mostInc24HoursFromMem, mostInc7DaysFromMem;

    private String currency;
    private int max, totalPageNumber;
    private boolean firstLoad = true, fetchAllCoins = false, emptyMemory = false, firstOpen;
    private double lastFetchOfAllCoins;

    private SharedPreferences sharedPreferences;

    private LoaderManager loaderManager;

    public HomeActivity() {}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                redirect(bundle);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }

        loaderManager = LoaderManager.getInstance(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        sharedPreferences = getSharedPreferences("Preferences", 0);

        // If user has set alarm but service is not currently running, run the service.
        // This situation occurs when user doesn't allow Kriptofoni to run background services
        // which can be done from phone's settings.
        if (!sharedPreferences.getString("alarmModels", "").equals("") && !isMyServiceRunning(NotificationBackgroundService.class)) {
            Intent intent = new Intent(getApplication(), NotificationBackgroundService.class);
            intent.putExtra("fromAlarm", true);
            startService(intent);
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

        myCoinGeckoGlobalApi = CurrenciesRetrofitClient.getInstance().getMyCoinGeckoApi();

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
        else emptyMemory = true;
        
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.fragment_container, moreFragment,"5").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, newsFragment,"4").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, alertsFragment,"3").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, portfolioFragment,"2").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, mainFragment,"1").commit();

        bottomNavigationView.getMenu().getItem(0).setEnabled(false);

        // Remove splash screen after 3 seconds
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().hide(moreFragment).commit();
                fragmentManager.beginTransaction().hide(newsFragment).commit();
                fragmentManager.beginTransaction().hide(alertsFragment).commit();
                fragmentManager.beginTransaction().hide(portfolioFragment).commit();
                setScreen();
            }
        };
        handler.postDelayed(runnable, 3000);

        getTotalMarketCap();
    }

    private void checkIfBackgroundServiceRunning() {
        if (isMyServiceRunning(NotificationBackgroundService.class)) {
            Intent stopIntent = new Intent(this, NotificationBackgroundService.class);
            stopIntent.putExtra("killService", true);
            stopService(stopIntent);

            Intent startIntent= new Intent(this, NotificationBackgroundService.class);
            startIntent.putExtra("fromAlarm", true);
            startService(startIntent);
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (active == newsFragment) {
            WebView webView = newsFragment.getWebView();
            if (webView != null) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    newsFragment.removeWebView();
                }
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
//                System.out.println(coinSearchModels.size());
                if (coinSearchModels.size() >= max - 5 && max != 0) {
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

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("lastFetchOfAllCoins", System.currentTimeMillis());
                    editor.apply();

                    loaderManager.destroyLoader(0);
                    return;
                }
                handler.postDelayed(this, 250);
            }
        };
        handler.postDelayed(runnable, 250);
    }

    public void getTotalMarketCap() {
        Call<Global> call = myCoinGeckoGlobalApi.getGlobal();
        call.enqueue(new Callback<Global>() {
            @Override
            public void onResponse(Call<Global> call, Response<Global> response) {
                if (response.isSuccessful()) {
                    Global body = response.body();
                    JsonObject data = body.getData();
                    if (data != null) {
                        currency = sharedPreferences.getString("currency", "usd");
                        JsonObject marketCaps = (JsonObject) data.get("total_market_cap");
                        double totalMarketCap = marketCaps.get(currency).getAsDouble();
                        mainFragment.setTotalMarketValue(totalMarketCap);
                        max = data.get("active_cryptocurrencies").getAsInt();
                        totalPageNumber = max / 250 + 1;

                        if (firstLoad) {
                            firstLoad = false;

                            // Don't fetch all coins if already fetched in last 10 mins.
                            lastFetchOfAllCoins = sharedPreferences.getFloat("lastFetchOfAllCoins", 0);
                            System.out.println(lastFetchOfAllCoins - (System.currentTimeMillis() - 1000 * 60 * 10));
                            if (lastFetchOfAllCoins < System.currentTimeMillis() - 1000 * 60 * 10) {
                                getAllCoins();
                                fetchAllCoins = true;
                                // Saving last selected currency in order to use in main tabs.
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("savedCurrency", currency);
                                editor.apply();
                            }
                        }
                    } else {
                        getTotalMarketCap();
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                getTotalMarketCap();
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        getTotalMarketCap();
                    }
                }
            }

            @Override
            public void onFailure(Call<Global> call, Throwable t) {
                System.out.println(t.getMessage());
                getTotalMarketCap();
            }
        });
    }

    public Fragment getActive() {
        return active;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void setScreen() {
        splashScreen.setVisibility(View.GONE);
        mainScreen.setVisibility(View.VISIBLE);
        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                redirect(bundle);
            }
        }
    }

    private void redirect(Bundle bundle) {
        // If user opens app from a notification redirect to crypto currency detail page
        boolean fromNotification = bundle.getBoolean("fromNotification", false);
        System.out.println(fromNotification);
        if (fromNotification) {
            Intent intent1 = new Intent(HomeActivity.this, CryptoCurrencyDetailActivity.class);
            intent1.putExtra("id", bundle.getString("coinId"));
            startActivity(intent1);
        }
    }

    public void renderMemory() {
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
    }

    public void resetMainFragments() {
        mainFragment.resetFragments();
    }

    public boolean isFetchAllCoins() {
        return fetchAllCoins;
    }

    public boolean isEmptyMemory() {
        return emptyMemory;
    }

    @Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        return new GetAllCoinsAsyncTaskLoader(this, totalPageNumber, currency, this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {}

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {}

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
