package arsi.dev.kriptofoni.Fragments.MainFragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoinsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<CoinModel> coinModels, allCoinModels;
    private ArrayList<CoinSearchModel> coinModelsForSearch;
    private MainCoinsRecyclerAdapter mainCoinsRecyclerAdapter;
    private MainCoinsSearchRecyclerAdapter mainCoinsSearchRecyclerAdapter;
    private CoinGeckoApi myCoinGeckoApi;
    private int currentPage = 1;
    private CoinGeckoApiClient client;
    private String currency;
    private boolean reached = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_coins, container, false);

        coinModels = new ArrayList<>();
        coinModelsForSearch = new ArrayList<>();
        allCoinModels = new ArrayList<>();

        recyclerView = view.findViewById(R.id.main_coins_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels, this);
        recyclerView.setAdapter(mainCoinsRecyclerAdapter);
        mainCoinsSearchRecyclerAdapter = new MainCoinsSearchRecyclerAdapter(coinModelsForSearch);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency" ,"usd");

        myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();

        client = new CoinGeckoApiClientImpl();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!reached) {
                    if (!recyclerView.canScrollVertically(1) && recyclerView.getAdapter() instanceof MainCoinsRecyclerAdapter) {
                        reached = true;
                        currentPage++;
                        getCoinsAsync();
                        recyclerView.scrollToPosition((currentPage - 1) * 100 - 4);
                    }
                }
            }
        });

        getCoinsAsync();

        return view;
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

    public void getCoinsAsync() {
        // CoinGeckoApiClient requests can't be made in main thread.
        // In order to make api request, we create an AsyncTask.
//        final GetCoins getCoins = new GetCoins();
//        getCoins.execute();
//        try {
//            if (getCoins.get()) {
//                mainCoinsRecyclerAdapter.setCoins(coinModels);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        getCoins();
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
        System.out.println("run");
        allCoinModels = new ArrayList<>();
        coinModels = new ArrayList<>();
        mainCoinsRecyclerAdapter.setCoins(coinModels);
        getCoins();
        recyclerView.scrollTo(0, 0);
    }

    private void getCoins() {
        coinModels.clear();
        coinModels.addAll(allCoinModels);
        // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
        // We create a simple HTTP Request via Retrofit
        Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,null, 100, currentPage, false, "24h,7d");
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
                        double changeIn24Hours = result.getPrice_change_percentage_24h_in_currency();
                        double priceChangeIn24Hours = result.getPrice_change_24h();
                        double currentPrice = result.getCurrent_price();
                        double marketCap = result.getMarket_cap();
                        CoinModel model = new CoinModel((currentPage - 1) * 100 + (i + 1), imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap);
                        coinModels.add(model);
                        allCoinModels.add(model);
                    }
                    System.out.println("178 " + allCoinModels.size() + " , " + coinModels.size());
                    mainCoinsRecyclerAdapter.notifyDataSetChanged();
                    reached = false;
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }

    private class GetCoins extends AsyncTask<Void, Void, Boolean>  {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                List<CoinMarkets> coins = client.getCoinMarkets(currency, null, null, 100, currentPage, false, null);
                for (int i = 0; i < coins.size(); i++) {
                    CoinMarkets coin = coins.get(i);
                    String imgUrl = coin.getImage();
                    String name = coin.getName();
                    String shortCut = coin.getSymbol();
                    double changeIn24Hours = coin.getPriceChangePercentage24h();
                    double priceChange = coin.getPriceChange24h();
                    double currentPrice = coin.getCurrentPrice();
                    CoinModel model = new CoinModel((currentPage - 1) * 100 + (i + 1), imgUrl, name, shortCut, changeIn24Hours, priceChange, currentPrice);
                    coinModels.add(model);
                    allCoinModels.add(model);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainCoinsRecyclerAdapter.notifyDataSetChanged();
                    }
                });
                return true;
            } catch (CoinGeckoApiException ex) {
                return false;
            }
        }
    }
}
