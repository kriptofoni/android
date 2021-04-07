package arsi.dev.kriptofoni.Fragments.MainFragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Adapters.MainCoinsSearchRecyclerAdapter;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsApi;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoinsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<CoinModel> coinModels, allCoinModels;
    private List<CoinSearchModel> coinModelsForSearch;
    private MainCoinsRecyclerAdapter mainCoinsRecyclerAdapter;
    private MainCoinsSearchRecyclerAdapter mainCoinsSearchRecyclerAdapter;
    private SortedCoinsApi myCoinGeckoApi;
    private int currentPage = 1;
    private String currency;
    private boolean reached = false, onScreen = false, firstRender = false, inProgress = false;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar progressBar, bottomProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_coins, container, false);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (onScreen && firstRender && !inProgress) {
                    new GetCoinInfo().execute("update");
//                    getCoinInfo("update");
                    handler.postDelayed(this, 10000);
                }
            }
        };

        coinModels = new ArrayList<>();
        coinModelsForSearch = new ArrayList<>();
        allCoinModels = new ArrayList<>();

        progressBar = view.findViewById(R.id.main_coins_progress_bar);
        bottomProgressBar = view.findViewById(R.id.main_coins_bottom_progress_bar);

        recyclerView = view.findViewById(R.id.main_coins_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels, this, "24");
        recyclerView.setAdapter(mainCoinsRecyclerAdapter);
        mainCoinsSearchRecyclerAdapter = new MainCoinsSearchRecyclerAdapter(coinModelsForSearch, this);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency" ,"usd");

        myCoinGeckoApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!reached) {
                    if (!recyclerView.canScrollVertically(1) && recyclerView.getAdapter() instanceof MainCoinsRecyclerAdapter) {
                        reached = true;
                        inProgress = true;
                        currentPage++;
                        bottomProgressBar.setVisibility(View.VISIBLE);
                        new GetCoinInfo().execute("initial");
//                        getCoinInfo("initial");
                        recyclerView.scrollToPosition((currentPage - 1) * 100 - 4);
                    }
                }
            }
        });

        new GetCoinInfo().execute("initial");
//        getCoinInfo("initial");
        if (!firstRender) firstRender = true;

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        onScreen = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        onScreen = true;
        handler.postDelayed(runnable, 10000);
    }

    public void setCoinsList(ArrayList<CoinSearchModel> coins, boolean contains) {
        if (recyclerView != null) {
            if (!coins.isEmpty()) {
                recyclerView.setAdapter(mainCoinsSearchRecyclerAdapter);
                mainCoinsSearchRecyclerAdapter.setCoins(coins);
            } else if (!contains) {
                mainCoinsSearchRecyclerAdapter.setCoins(new ArrayList<>());
            } else {
                recyclerView.setAdapter(mainCoinsRecyclerAdapter);
                recyclerView.scrollToPosition((currentPage - 1 ) * 100 - 4);
            }
        }
    }

    public void setCoinsList(ArrayList<CoinSearchModel> coins) {
        if (recyclerView != null) {
            if (!coins.isEmpty()) {
                recyclerView.setAdapter(mainCoinsSearchRecyclerAdapter);
                mainCoinsSearchRecyclerAdapter.setCoins(coins);
            }  else {
                recyclerView.scrollToPosition((currentPage - 1 ) * 100 - 4);
                recyclerView.setAdapter(mainCoinsRecyclerAdapter);
            }
        }
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setCurrencySymbol(String symbol) {
        mainCoinsRecyclerAdapter.setCurrencySymbol(symbol);
    }

    public void emptyAllCoinModels() {
        progressBar.setVisibility(View.VISIBLE);
        allCoinModels = new ArrayList<>();
        coinModels = new ArrayList<>();
        mainCoinsRecyclerAdapter.setCoins(coinModels);
        new GetCoinInfo().execute("initial");
//        getCoinInfo("initial");
        recyclerView.scrollTo(0, 0);
    }

    private void getCoinInfo(String type) {
        coinModels.clear();
        coinModels.addAll(allCoinModels);
        ArrayList<CoinModel> temp = new ArrayList<>();
        // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
        // We create a simple HTTP Request via Retrofit
        Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,null, 100, currentPage, true, "24h,7d");
        call.enqueue(new Callback<List<CoinMarket>>() {
            @Override
            public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                // Creating a list of Result objects using our response data.
                List<CoinMarket> coins = response.body();
                if (coins != null) {
                    for (int i = 0; i < coins.size(); i++) {
                        // Creating a coin model for each coin in our response data.
                        CoinMarket result = coins.get(i);
                        String imageUrl = result.getImage();
                        String name = result.getName();
                        String shortCut = result.getSymbol();
                        String id = result.getId();
                        double changeIn24Hours = result.getPrice_change_percentage_24h_in_currency();
                        double priceChangeIn24Hours = result.getPrice_change_24h();
                        double currentPrice = result.getCurrent_price();
                        double marketCap = result.getMarket_cap();
                        double changeIn7Days = result.getPrice_change_percentage_7d_in_currency();
                        CoinModel model = new CoinModel((currentPage - 1) * 100 + (i + 1), imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, 0);
                        if (type.equals("update")) {
                            temp.add(model);
                        } else {
                            coinModels.add(model);
                            allCoinModels.add(model);
                        }
                    }

                    int firstIndex = (currentPage - 1) * 100;

                    if (type.equals("update") && !coinModels.isEmpty()) {
                        for (int i = 0; i < temp.size(); i++) {
                            coinModels.set(firstIndex + i, temp.get(i));
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainCoinsRecyclerAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    reached = false;
                    if (inProgress) inProgress = false;
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                getCoinInfo(type);
            }
        });
    }

    private class GetCoinInfo extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String type = strings[0];
            coinModels.clear();
            coinModels.addAll(allCoinModels);
            ArrayList<CoinModel> temp = new ArrayList<>();
            // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
            // We create a simple HTTP Request via Retrofit
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,null, 100, currentPage, true, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    // Creating a list of Result objects using our response data.
                    List<CoinMarket> coins = response.body();
                    if (coins != null) {
                        for (int i = 0; i < coins.size(); i++) {
                            // Creating a coin model for each coin in our response data.
                            CoinMarket result = coins.get(i);
                            String imageUrl = result.getImage();
                            String name = result.getName();
                            String shortCut = result.getSymbol();
                            String id = result.getId();
                            double changeIn24Hours = result.getPrice_change_percentage_24h_in_currency();
                            double priceChangeIn24Hours = result.getPrice_change_24h();
                            double currentPrice = result.getCurrent_price();
                            double marketCap = result.getMarket_cap();
                            double changeIn7Days = result.getPrice_change_percentage_7d_in_currency();
                            CoinModel model = new CoinModel((currentPage - 1) * 100 + (i + 1), imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, 0);
                            if (type.equals("update")) {
                                temp.add(model);
                            } else {
                                coinModels.add(model);
                                allCoinModels.add(model);
                            }
                        }

                        int firstIndex = (currentPage - 1) * 100;

                        if (type.equals("update") && !coinModels.isEmpty()) {
                            for (int i = 0; i < temp.size(); i++) {
                                coinModels.set(firstIndex + i, temp.get(i));
                            }
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainCoinsRecyclerAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                bottomProgressBar.setVisibility(View.GONE);
                            }
                        });

                        reached = false;
                        if (inProgress) inProgress = false;
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                    doInBackground(strings);
                }
            });
            return null;
        }
    }

}
