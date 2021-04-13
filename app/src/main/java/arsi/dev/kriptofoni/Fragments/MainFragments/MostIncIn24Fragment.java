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

import org.intellij.lang.annotations.JdkConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Adapters.MainCoinsSearchRecyclerAdapter;
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
import retrofit2.Retrofit;

public class MostIncIn24Fragment extends Fragment {

    private List<CoinSearchModel> allCoinSearchModels, coinModelsForSearch;
    private List<CoinModel> coinModels, allCoins;
    private RecyclerView recyclerView;
    private MainCoinsRecyclerAdapter mainCoinsRecyclerAdapter;
    private int currentPage = 1, firstVisibleItemPos = 0;
    private boolean reached = false, onScreen = false, startDone = false, firstRender = false,
            inProgress = false, firstOnResume = false, isInterrupted = false;
    private SortedCoinsApi myCoinGeckoApi;
    private String currency, fetchType;
    private MainCoinsSearchRecyclerAdapter mainCoinsSearchRecyclerAdapter;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar progressBar, bottomProgressBar;

    public MostIncIn24Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_most_inc_24, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency", "usd");

        allCoinSearchModels = new ArrayList<>();
        coinModels = new ArrayList<>();
        allCoins = new ArrayList<>();
        coinModelsForSearch = new ArrayList<>();

        myCoinGeckoApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();

        recyclerView = view.findViewById(R.id.main_most_inc_24_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels, this, "24");
        mainCoinsSearchRecyclerAdapter = new MainCoinsSearchRecyclerAdapter(coinModelsForSearch, this);
        recyclerView.setAdapter(mainCoinsRecyclerAdapter);

        progressBar = view.findViewById(R.id.main_most_inc_24_progress_bar);
        bottomProgressBar = view.findViewById(R.id.main_most_inc_24_bottom_progress_bar);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                firstVisibleItemPos = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (!reached && !inProgress) {
                    if (!recyclerView.canScrollVertically(1) && recyclerView.getAdapter() instanceof MainCoinsRecyclerAdapter) {
                        reached = true;
                        currentPage++;
                        bottomProgressBar.setVisibility(View.VISIBLE);
                        addIds();
                        fetchType = "newPage";
                    }
                }
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (onScreen && startDone && !inProgress) {
                    addIds();
                    fetchType = "update";
                }

                handler.postDelayed(this, 10000);
            }
        };

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        onScreen = false;
        handler.removeCallbacks(runnable);
        // If the page is stopped while a data loading process is in progress,
        // we check whether there is any data fetch process when the page
        // is stopped in order to start the data fetch process
        // from the beginning when the page is opened again.
        if (inProgress) isInterrupted = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        onScreen = true;
        handler.postDelayed(runnable, 10000);
        if (!firstOnResume) {
            progressBar.setVisibility(View.VISIBLE);
            firstOnResume = true;
            addIds();
            fetchType = "initial";
        }
        if (isInterrupted) {
            addIds();
            isInterrupted = false;
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
            } else {
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
        mainCoinsRecyclerAdapter.setCurrencySymbol(symbol);
    }

    public void emptyAllCoinModels() {
        allCoins.clear();
        coinModels.clear();
        recyclerView.scrollTo(0, 0);
    }

    public void setCoins(List<CoinSearchModel> coins) {
        allCoinSearchModels.clear();
        allCoinSearchModels.addAll(coins);
        if (onScreen && !inProgress) {
            if (!firstRender) {
                addIds();
                fetchType = "initial";
            } else {
                addIds();
                fetchType = "dataReload";
            }
        }

        if (firstRender && !startDone) startDone = true;
        if (!firstRender) firstRender = true;
    }

    private void addIds() {
        StringBuilder stringBuilder = new StringBuilder();
        String s = "";

        int firstIndex = (currentPage - 1) * 50;
        int lastIndex = firstIndex + 50;

        if (allCoinSearchModels != null && !allCoinSearchModels.isEmpty()) {
            for (int i = firstIndex; i < lastIndex; i++) {
                stringBuilder.append(allCoinSearchModels.get(i).getId() + ",");
                System.out.println(allCoinSearchModels.get(i).getId() + " , " + allCoinSearchModels.get(i).getPriceChangeIn24());
            }

            s = stringBuilder.toString();
            inProgress = true;
            new GetCoinInfo().execute(s);
        }
    }

    public void setProgressBarVisibility(int visibility) {
        if (!firstOnResume)
        progressBar.setVisibility(visibility);
    }

    public void setFirstOnResume(boolean firstOnResume) {
        this.firstOnResume = firstOnResume;
    }

    private class GetCoinInfo extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String ids = strings[0];
            System.out.println(fetchType);

            coinModels.clear();
            coinModels.addAll(allCoins);
            allCoins.clear();
            ArrayList<CoinModel> temp = new ArrayList<>();
            ArrayList<CoinModel> newPage = new ArrayList<>();
            ArrayList<String> idList = new ArrayList<>();
            // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
            // We create a simple HTTP Request via Retrofit
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, ids, null, 50, 1, true, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    // Creating a list of Result objects using our response data.
                    List<CoinMarket> coins = response.body();
                    if (coins != null && !coins.isEmpty()) {
                        for (int i = 0; i < coins.size(); i++) {
                            // Creating a coin model for each coin in our response data.
                            CoinMarket result = coins.get(i);
                            String imageUrl = result.getImage();
                            String name = result.getName();
                            String shortCut = result.getSymbol();
                            String id = result.getId();
                            idList.add(id);
                            double changeIn24Hours = result.getPrice_change_percentage_24h_in_currency();
                            double priceChangeIn24Hours = result.getPrice_change_24h();
                            double currentPrice = result.getCurrent_price();
                            double marketCap = result.getMarket_cap();
                            double changeIn7Days = result.getPrice_change_percentage_7d_in_currency();
                            CoinModel model = new CoinModel(i, imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, 0);
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
                                        return Double.compare(rhs.getChangeIn24Hours(), lhs.getChangeIn24Hours());
                                    }
                                });
                            }
                        }

                        int firstIndex = (currentPage - 1) * 50;

                        if ((fetchType.equals("update") || fetchType.equals("dataReload")) && !coinModels.isEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                temp.sort(new Comparator<CoinModel>() {
                                    @Override
                                    public int compare(CoinModel lhs, CoinModel rhs) {
                                        return Double.compare(rhs.getChangeIn24Hours(), lhs.getChangeIn24Hours());
                                    }
                                });
                            }
                            for (int i = 0; i < temp.size(); i++) {
                                coinModels.set(firstIndex + i, temp.get(i));
                            }
                        }

                        if (fetchType.equals("newPage") && !coinModels.isEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                newPage.sort(new Comparator<CoinModel>() {
                                    @Override
                                    public int compare(CoinModel lhs, CoinModel rhs) {
                                        return Double.compare(rhs.getChangeIn24Hours(), lhs.getChangeIn24Hours());
                                    }
                                });
                            }
                            coinModels.addAll(newPage);
                        }

                        for (int i = 0; i < coinModels.size(); i++) {
                            coinModels.get(i).setNumber(i + 1);
                        }

                        System.out.println(coinModels.size());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fetchType.equals("update") || fetchType.equals("dataReload"))
                                    mainCoinsRecyclerAdapter.notifyItemRangeChanged(firstIndex, 50);
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
