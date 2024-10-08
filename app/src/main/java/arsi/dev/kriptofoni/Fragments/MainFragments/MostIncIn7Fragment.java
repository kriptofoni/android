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
    private SharedPreferences sharedPreferences;

    private HomeActivity homeActivity;

    public MostIncIn7Fragment() {}

    public MostIncIn7Fragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_most_inc_7, container, false);

        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency", "usd");

        if (allCoinSearchModels == null)
            allCoinSearchModels = new ArrayList<>();

        if (coinModels == null) coinModels = new ArrayList<>();
        if (allCoins == null) allCoins = new ArrayList<>();
        coinModelsForSearch = new ArrayList<>();

        recyclerView = view.findViewById(R.id.main_most_inc_7_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels, this, "7", currency);
        mainCoinsSearchRecyclerAdapter = new MainCoinsSearchRecyclerAdapter(coinModelsForSearch, this);
        recyclerView.setAdapter(mainCoinsRecyclerAdapter);

        progressBar = view.findViewById(R.id.main_most_inc_7_progress_bar);
        bottomProgressBar = view.findViewById(R.id.main_most_inc_7_bottom_progress_bar);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && recyclerView.getAdapter() instanceof MainCoinsRecyclerAdapter) {
                    if (!reached && !inProgress) {
                        String savedCurrency = sharedPreferences.getString("savedCurrency", "usd");
                        reached = true;
                        currentPage++;
                        fetchType = "newPage";

                        // If user change app's currency make API call for next page.
                        if (!savedCurrency.equals(currency)) {
                            bottomProgressBar.setVisibility(View.VISIBLE);
                            addIds();
                        }
                        // If user doesn't change app's currency don't make API call.
                        // Render saved values.
                        else {
                            renderCoins();
                            bottomProgressBar.setVisibility(View.VISIBLE);
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    bottomProgressBar.setVisibility(View.GONE);
                                }
                            };
                            handler.postDelayed(runnable, 250);
                        }
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
    public void onPause() {
        super.onPause();
        if (onScreen) {
            onScreen = false;
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (homeActivity != null && homeActivity.getActive() != null && homeActivity.getActive() instanceof MainFragment) {
            onScreen = true;
            if (!sharedPreferences.getString("savedCurrency", "usd").equals(currency)) {
                fetchType = "update";
                addIds();
            }
            handler.postDelayed(runnable, 10000);
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
        progressBar.setVisibility(View.VISIBLE);
        // If there is already a fetching process wait for this process to finish.
        if (inProgress) {
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!inProgress) {
                        allCoins = new ArrayList<>();
                        coinModels = new ArrayList<>();
                        recyclerView.scrollTo(0, 0);
                        mainCoinsRecyclerAdapter.setCoins(coinModels);
                        mainCoinsRecyclerAdapter.notifyDataSetChanged();
                        fetchType = "initial";
                        addIds();
                        return;
                    }
                    handler.postDelayed(this, 250);
                }
            };
            handler.postDelayed(runnable, 250);
        } else {
            allCoins = new ArrayList<>();
            coinModels = new ArrayList<>();
            recyclerView.scrollTo(0, 0);
            mainCoinsRecyclerAdapter.setCoins(coinModels);
            mainCoinsRecyclerAdapter.notifyDataSetChanged();
            fetchType = "initial";
            addIds();
        }
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
            } else {
                fetchType = "dataReload";
            }

            renderCoins();
        }

        if (firstRender && !startDone) startDone = true;
        if (!firstRender) {
            firstRender = true;
            if (!homeActivity.isFetchAllCoins()) startDone = true;
            if (homeActivity.isEmptyMemory()) startDone = true;
        }
    }

    private void renderCoins() {

        int firstIndex, lastIndex;

        if (fetchType.equals("newPage")) {
            firstIndex = (currentPage - 1) * 50;
            lastIndex = firstIndex + 50;
        } else if (fetchType.equals("dataReload")) {
            firstIndex = 0;
            lastIndex = currentPage * 50;
        } else {
            firstIndex = 0;
            lastIndex = firstIndex + 50;
        }

        if (coinModels == null) coinModels = new ArrayList<>();
        if (allCoins == null) allCoins = new ArrayList<>();

        coinModels.clear();
        coinModels.addAll(allCoins);
        allCoins.clear();

        ArrayList<CoinModel> temp = new ArrayList<>();

        for (int i = firstIndex; i < lastIndex; i++) {
            CoinSearchModel searchModel = allCoinSearchModels.get(i);

            String imageUrl = searchModel.getImage();
            String name = searchModel.getName();
            String shortCut = searchModel.getSymbol();
            String id = searchModel.getId();
            double changeIn24Hours = searchModel.getPriceChangeIn24();
            double priceChangeIn24Hours = searchModel.getPriceIn24();
            double currentPrice = searchModel.getCurrentPrice();
            double marketCap = searchModel.getMarketCap();
            double changeIn7Days = searchModel.getPriceChangeIn7();

            double firstPrice = currentPrice / (changeIn7Days / 100 + 1);

            CoinModel model = new CoinModel(i, imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, currentPrice - firstPrice);
            if (fetchType.equals("dataReload"))
                temp.add(model);
            else {
                coinModels.add(model);
            }
        }

        if (fetchType.equals("dataReload") && !coinModels.isEmpty()) {
            for (int i = 0; i < temp.size(); i++) {
                coinModels.set(i, temp.get(i));
            }
            mainCoinsRecyclerAdapter.notifyItemRangeChanged(firstIndex, lastIndex - firstIndex);
        } else {
            mainCoinsRecyclerAdapter.notifyDataSetChanged();
        }

        progressBar.setVisibility(View.GONE);
        bottomProgressBar.setVisibility(View.GONE);
        if (reached) reached = false;
        allCoins.addAll(coinModels);
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
            if (!inProgress)
                getCoinInfo(s);
            inProgress = true;
        }
    }

    public void setProgressBarVisibility(int visibility) {
        if (progressBar != null)
            progressBar.setVisibility(visibility);
    }

    public void setMyCoinGeckoApi(SortedCoinsApi myCoinGeckoApi) {
        this.myCoinGeckoApi = myCoinGeckoApi;
    }

    private void getCoinInfo(String ids) {
        ArrayList<CoinModel> temp = new ArrayList<>();
        ArrayList<CoinModel> newPage = new ArrayList<>();
        // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
        // We create a simple HTTP Request via Retrofit
        Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, ids, null, 50, 1, true, "24h,7d");
        call.enqueue(new Callback<List<CoinMarket>>() {
            @Override
            public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                // Creating a list of Result objects using our response data.
                if (response.isSuccessful()) {
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

                            double firstPrice = currentPrice / (changeIn7Days / 100 + 1);

                            CoinModel model = new CoinModel(i, imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, currentPrice - firstPrice);
                            if (fetchType.equals("update")) {
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

                        if ((fetchType.equals("update")) && !coinModels.isEmpty()) {
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
                                if (fetchType.equals("update"))
                                    mainCoinsRecyclerAdapter.notifyItemRangeChanged(0, 50);
                                else
                                    mainCoinsRecyclerAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                bottomProgressBar.setVisibility(View.GONE);
//                                if (fetchType.equals("newPage")) recyclerView.scrollToPosition((currentPage - 1) * 50 - 4);
                            }
                        });
                        allCoins.addAll(coinModels);
                        reached = false;
                        if (inProgress) inProgress = false;
                    } else {
                        getCoinInfo(ids);
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                getCoinInfo(ids);
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        getCoinInfo(ids);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                getCoinInfo(ids);
            }
        });
    }
}
