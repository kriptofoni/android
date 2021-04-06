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

import java.util.ArrayList;
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
    private int currentPage = 1;
    private boolean reached = false, onScreen = false, startDone = false, firstRender = false, inProgress = false;
    private SortedCoinsApi myCoinGeckoApi;
    private String currency, ids;
    private MainCoinsSearchRecyclerAdapter mainCoinsSearchRecyclerAdapter;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_most_inc_24, container, false);

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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!reached) {
                    if (!recyclerView.canScrollVertically(1) && recyclerView.getAdapter() instanceof MainCoinsRecyclerAdapter) {
                        reached = true;
                        inProgress = true;
                        currentPage++;
                        addIds("initial");
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
                    addIds("update");
                    System.out.println(allCoinSearchModels.get(0).getId());
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
        allCoins = new ArrayList<>();
        coinModels = new ArrayList<>();
        mainCoinsRecyclerAdapter.setCoins(coinModels);
//        new GetCoinInfo().execute(this.ids, "initial");
        getCoinInfo(this.ids, "initial");
        recyclerView.scrollTo(0, 0);
    }

    public void setCoins(List<CoinSearchModel> coins) {
        coinModels.clear();
        allCoins.clear();
        allCoinSearchModels.clear();
        allCoinSearchModels.addAll(coins);
        addIds("initial");
        if (firstRender && !startDone) startDone = true;
        if (!firstRender) firstRender = true;
    }

    private void addIds(String type) {
        StringBuilder stringBuilder = new StringBuilder();
        String s = "";

        int firstIndex = (currentPage - 1) * 50;
        int lastIndex = firstIndex + 50;

        for (int i = firstIndex; i < lastIndex; i++) {
            stringBuilder.append(allCoinSearchModels.get(i).getId() + ",");
        }

        s = stringBuilder.toString();
        if (currentPage == 1)
            setIds(s);
//        new GetCoinInfo().execute(s, type);
        getCoinInfo(s, type);
    }

    private void setIds(String ids) {
        this.ids = ids;
    }

    public void setProgressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }

    private void getCoinInfo(String ids, String type) {
        coinModels.clear();
        coinModels.addAll(allCoins);
        ArrayList<CoinModel> temp = new ArrayList<>();
        // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
        // We create a simple HTTP Request via Retrofit
        Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, ids, null, 50, 1, true, "24h,7d");
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
                        CoinModel model = new CoinModel(i, imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, 0);
                        if (type.equals("update")) {
                            temp.add(model);
                        } else {
                            coinModels.add(model);
                            allCoins.add(model);
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        coinModels.sort(new Comparator<CoinModel>() {
                            @Override
                            public int compare(CoinModel lhs, CoinModel rhs) {
                                return Double.compare(rhs.getChangeIn24Hours(), lhs.getChangeIn24Hours());
                            }
                        });
                    }

                    int firstIndex = (currentPage - 1) * 50;
                    int lastIndex = firstIndex + 50;

                    if (type.equals("update")) {
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

                    for (int i = 0; i < coinModels.size(); i++) {
                        coinModels.get(i).setNumber(i + 1);
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
                getCoinInfo(ids, type);
            }
        });
    }

    private class GetCoinInfo extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String ids = strings[0];
            String type = strings[1];

            coinModels.clear();
            coinModels.addAll(allCoins);
            ArrayList<CoinModel> temp = new ArrayList<>();
            // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
            // We create a simple HTTP Request via Retrofit
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, ids, null, 50, 1, true, "24h,7d");
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
                            CoinModel model = new CoinModel(i, imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days, id, 0);
                            if (type.equals("update")) {
                                temp.add(model);
                            } else {
                                coinModels.add(model);
                                allCoins.add(model);
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            coinModels.sort(new Comparator<CoinModel>() {
                                @Override
                                public int compare(CoinModel lhs, CoinModel rhs) {
                                    return Double.compare(rhs.getChangeIn24Hours(), lhs.getChangeIn24Hours());
                                }
                            });
                        }

                        int firstIndex = (currentPage - 1) * 50;
                        int lastIndex = firstIndex + 50;

                        if (type.equals("update")) {
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

                        for (int i = 0; i < coinModels.size(); i++) {
                            coinModels.get(i).setNumber(i + 1);
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
                    doInBackground(strings);
                }
            });

            return null;
        }
    }
}
