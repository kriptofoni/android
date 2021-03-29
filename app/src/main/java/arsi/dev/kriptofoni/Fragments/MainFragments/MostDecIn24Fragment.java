package arsi.dev.kriptofoni.Fragments.MainFragments;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MostDecIn24Fragment extends Fragment {

    private ArrayList<CoinSearchModel> allCoinSearchModels;
    private ArrayList<CoinModel> coinModels, allCoins;
    private boolean first = true;
    private RecyclerView recyclerView;
    private MainCoinsRecyclerAdapter mainCoinsRecyclerAdapter;
    private int currentPage = 1;
    private boolean reached = false;
    private CoinGeckoApi myCoinGeckoApi;
    private String currency;
    HashMap<String, Integer> coinNumbers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_most_dec_24, container, false);

        allCoinSearchModels = new ArrayList<>();
        coinModels = new ArrayList<>();
        allCoins = new ArrayList<>();
        coinNumbers = new HashMap<>();

        myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();

        recyclerView = view.findViewById(R.id.main_most_dec_24_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels, "24");
        recyclerView.setAdapter(mainCoinsRecyclerAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency", "usd");
    }

    private void getCoins(String ids) {
        coinModels.clear();
        coinModels.addAll(allCoins);
        // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
        // We create a simple HTTP Request via Retrofit
        Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, ids, null, 100, 1, false, "24h,7d");
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
                        CoinModel model = new CoinModel(coinNumbers.get(id), imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap, changeIn7Days);
                        coinModels.add(model);
                        allCoins.add(model);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        coinModels.sort(new Comparator<CoinModel>() {
                            @Override
                            public int compare(CoinModel lhs, CoinModel rhs) {
                                return lhs.getNumber() > rhs.getNumber() ? 1 : lhs.getNumber() < rhs.getNumber() ? -1 : 0;
                            }
                        });
                    }
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

    public void setCoins(ArrayList<CoinSearchModel> coins) {
        coinModels.clear();
        allCoins.clear();
        this.allCoinSearchModels = coins;
        StringBuilder stringBuilder = new StringBuilder();
        String s = "";
        for (int i = (currentPage - 1) * 50; i < (currentPage - 1) * 100 + 50; i++) {
            stringBuilder.append(this.allCoinSearchModels.get(i).getId() + ",");
            allCoinSearchModels.get(i).setNumber(i + 1);
            coinNumbers.put(allCoinSearchModels.get(i).getId(), allCoinSearchModels.get(i).getNumber());
        }

        s = stringBuilder.toString();
        getCoins(s);
    }
}
