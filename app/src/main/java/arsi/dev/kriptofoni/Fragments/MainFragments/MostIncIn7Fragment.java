package arsi.dev.kriptofoni.Fragments.MainFragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
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

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Adapters.MainCoinsSearchRecyclerAdapter;
import arsi.dev.kriptofoni.Fragments.MainFragment;
import arsi.dev.kriptofoni.HomeActivity;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsApi;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MostIncIn7Fragment extends Fragment {

    private List<CoinSearchModel> allCoinSearchModels, coinModelsForSearch;
    private List<CoinModel> coinModels, allCoins;
    private boolean first = true;
    private RecyclerView recyclerView;
    private MainCoinsRecyclerAdapter mainCoinsRecyclerAdapter;
    private int currentPage = 1;
    private boolean reached = false, onScreen = false, firstRender = false, startDone = false,
            inProgress = false, isInterrupted = false;
    private SortedCoinsApi myCoinGeckoApi;
    private String currency, fetchType;
    private MainCoinsSearchRecyclerAdapter mainCoinsSearchRecyclerAdapter;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar progressBar, bottomProgressBar;

    private HomeActivity homeActivity;

    public MostIncIn7Fragment() {}

    public MostIncIn7Fragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_most_inc_7, container, false);

        if (allCoinSearchModels == null)
            allCoinSearchModels = new ArrayList<>();

        coinModels = new ArrayList<>();
        allCoins = new ArrayList<>();
        coinModelsForSearch = new ArrayList<>();

        myCoinGeckoApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();

        recyclerView = view.findViewById(R.id.main_most_inc_7_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels, this, "7");
        mainCoinsSearchRecyclerAdapter = new MainCoinsSearchRecyclerAdapter(coinModelsForSearch, this);
        recyclerView.setAdapter(mainCoinsRecyclerAdapter);

        progressBar = view.findViewById(R.id.main_most_inc_7_progress_bar);
        bottomProgressBar = view.findViewById(R.id.main_most_inc_7_bottom_progress_bar);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!reached && !inProgress) {
                    if (!recyclerView.canScrollVertically(1) && recyclerView.getAdapter() instanceof MainCoinsRecyclerAdapter) {
                        reached = true;
                        currentPage++;
                        bottomProgressBar.setVisibility(View.VISIBLE);
                        fetchType = "newPage";
                        addIds();
                        recyclerView.scrollToPosition((currentPage - 1) * 50 - 4);
                    }
                }
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (onScreen && startDone && !inProgress) {
                    fetchType = "update";
                    addIds();
                }
                handler.postDelayed(this, 10000);
            }
        };

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency", "usd");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (onScreen) {
            onScreen = false;
            handler.removeCallbacks(runnable);
            // If the page is stopped while a data loading process is in progress,
            // we check whether there is any data fetch process when the page
            // is stopped in order to start the data fetch process
            // from the beginning when the page is opened again.
            if (inProgress) isInterrupted = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (homeActivity != null && homeActivity.getActive() != null && homeActivity.getActive() instanceof MainFragment) {
            onScreen = true;
            handler.postDelayed(runnable, 10000);
            if (isInterrupted) {
                fetchType = "update";
                addIds();
                isInterrupted = false;
            }
        }
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
                recyclerView.scrollToPosition((currentPage - 1 ) * 50 - 4);
            }
        }
    }

    public void setCoinsList(ArrayList<CoinSearchModel> coins) {
        if (recyclerView != null) {
            if (!coins.isEmpty()) {
                recyclerView.setAdapter(mainCoinsSearchRecyclerAdapter);
                mainCoinsSearchRecyclerAdapter.setCoins(coins);
            }  else {
                recyclerView.scrollToPosition((currentPage - 1 ) * 50 - 4);
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
        if (mainCoinsRecyclerAdapter != null)
            mainCoinsRecyclerAdapter.setCurrencySymbol(symbol);
    }

    public void emptyAllCoinModels() {
        allCoins.clear();
        coinModels.clear();
        recyclerView.scrollTo(0, 0);
    }

    public void setCoins(List<CoinSearchModel> coins) {
        if (allCoinSearchModels != null) {
            allCoinSearchModels.clear();
        } else {
            allCoinSearchModels = new ArrayList<>();
        }

        allCoinSearchModels.addAll(coins);

        if (!inProgress) {
            if (!firstRender) {
                fetchType = "initial";
                addIds();
            } else {
                fetchType = "dataReload";
                addIds();
            }
        }

        if (firstRender && !startDone) startDone = true;
        if (!firstRender) firstRender = true;
    }

    private void addIds() {
        StringBuilder stringBuilder = new StringBuilder();
        String s = "";

        int firstIndex, lastIndex;

        if (fetchType.equals("newPage")) {
            firstIndex = (currentPage - 1) * 50;
        } else {
            firstIndex = 0;
        }

        lastIndex = firstIndex + 50;

        if (allCoinSearchModels != null && !allCoinSearchModels.isEmpty()) {
            for (int i = firstIndex; i < lastIndex; i++) {
                stringBuilder.append(this.allCoinSearchModels.get(i).getId() + ",");
            }

            s = stringBuilder.toString();
            inProgress = true;
            new GetCoinInfo().execute(s);
        }
    }

    public void setProgressBarVisibility(int visibility) {
        if (progressBar != null)
            progressBar.setVisibility(visibility);
    }

    private class GetCoinInfo extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String ids = strings[0];

            ArrayList<CoinModel> temp = new ArrayList<>();
            ArrayList<CoinModel> newPage = new ArrayList<>();
            // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
            // We create a simple HTTP Request via Retrofit
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, ids, null, 50, 1, true, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    // Creating a list of Result objects using our response data.
                    List<CoinMarket> coins = response.body();
                    if (coins != null && !coins.isEmpty()) {
                        coinModels.clear();
                        coinModels.addAll(allCoins);
                        allCoins.clear();
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
                            LinkedTreeMap sparkline = (LinkedTreeMap) result.getSparkline_in_7d();
                            ArrayList<Double> prices = (ArrayList<Double>) sparkline.get("price");
                            double pricechangeIn7Days = prices.isEmpty() ? 0 : prices.get(prices.size() - 1) - prices.get(0);
                            CoinModel model = new CoinModel(i, imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, pricechangeIn7Days);
                            if (fetchType.equals("update") || fetchType.equals("dataReload")) {
                                temp.add(model);
                            } else if (fetchType.equals("newPage")) {
                                newPage.add(model);
                            } else {
                                coinModels.add(model);
                            }
                        }

                        if (fetchType.equals("initial")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                coinModels.sort(new Comparator<CoinModel>() {
                                    @Override
                                    public int compare(CoinModel lhs, CoinModel rhs) {
                                        return Double.compare(rhs.getChangeIn7Days(), lhs.getChangeIn7Days());
                                    }
                                });
                            }
                        }

                        if ((fetchType.equals("update") || fetchType.equals("dataReload")) && !coinModels.isEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                temp.sort(new Comparator<CoinModel>() {
                                    @Override
                                    public int compare(CoinModel lhs, CoinModel rhs) {
                                        return Double.compare(rhs.getChangeIn7Days(), lhs.getChangeIn7Days());
                                    }
                                });
                            }
                            for (int i = 0; i < temp.size(); i++) {
                                coinModels.set(i, temp.get(i));
                            }
                        }

                        if (fetchType.equals("newPage") && !coinModels.isEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                newPage.sort(new Comparator<CoinModel>() {
                                    @Override
                                    public int compare(CoinModel lhs, CoinModel rhs) {
                                        return Double.compare(rhs.getChangeIn7Days(), lhs.getChangeIn7Days());
                                    }
                                });
                            }
                            coinModels.addAll(newPage);
                        }

                        for (int i = 0; i < coinModels.size(); i++) {
                            coinModels.get(i).setNumber(i + 1);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fetchType.equals("update") || fetchType.equals("dataReload"))
                                    mainCoinsRecyclerAdapter.notifyItemRangeChanged(0, 50);
                                else
                                    mainCoinsRecyclerAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                bottomProgressBar.setVisibility(View.GONE);
                                if (fetchType.equals("newPage")) recyclerView.scrollToPosition((currentPage - 1) * 50 - 4);
                            }
                        });
                        allCoins.addAll(coinModels);
                        reached = false;
                        if (inProgress) inProgress = false;
                    } else {
                        new GetCoinInfo().execute(strings);
                        cancel(true);
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                    new GetCoinInfo().execute(strings);
                    cancel(true);
                }
            });

            return null;
        }
    }
}
