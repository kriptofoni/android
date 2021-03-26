package arsi.dev.kriptofoni.Fragments.MainFragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.constant.Currency;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.Api;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoinsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<CoinModel> coinModels, coinModelsForSearch;
    private MainCoinsRecyclerAdapter mainCoinsRecyclerAdapter;
    private ImageView previous, next;
    private TextView page;
    private int currentPage = 1, max = 6543 / 100 + 1;
    private Api myApi;
    private CoinGeckoApiClient client;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_coins, container, false);

        coinModels = new ArrayList<>();
        coinModelsForSearch = new ArrayList<>();

        recyclerView = view.findViewById(R.id.main_coins_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        previous = view.findViewById(R.id.main_coins_previous_page);
        next = view.findViewById(R.id.main_coins_next_page);
        page = view.findViewById(R.id.main_coins_page);

        myApi = RetrofitClient.getInstance().getMyApi();

        client = new CoinGeckoApiClientImpl();

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPage - 1 > 0) {
                    currentPage--;
                    getCoinsAsync();
                    mainCoinsRecyclerAdapter.notifyDataSetChanged();
                    String text = currentPage + " / " + max;
                    page.setText(text);
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPage + 1 < max) {
                    currentPage++;
                    getCoinsAsync();
                    mainCoinsRecyclerAdapter.notifyDataSetChanged();
                    String text = currentPage + " / " + max;
                    page.setText(text);
                }
            }
        });

        getCoinsAsync();

        return view;
    }

    public void setCoinsList(ArrayList<CoinModel> coins, boolean contains) {
        if (!coins.isEmpty()) {
            mainCoinsRecyclerAdapter.setCoins(coins);
        } else if (!contains) {
            mainCoinsRecyclerAdapter.setCoins(new ArrayList<>());
        } else {
            mainCoinsRecyclerAdapter.setCoins(coinModels);
        }
    }

    public void setCoinsList(ArrayList<CoinModel> coins) {
        if (!coins.isEmpty()) {
            mainCoinsRecyclerAdapter.setCoins(coins);
        }  else {
            mainCoinsRecyclerAdapter.setCoins(coinModels);
        }
    }

    private void getCoinsAsync() {
        // CoinGeckoApiClient requests can't be made in main thread.
        // In order to make api request, we create an AsyncTask.
        final GetCoins getCoins = new GetCoins();
        getCoins.execute();
        try {
            if (getCoins.get()) {
                mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels);
                recyclerView.setAdapter(mainCoinsRecyclerAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCoins() {
        // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
        // We create a simple HTTP Request via Retrofit
        Call<List<CoinMarket>> call = myApi.getCoinMarkets(Currency.USD, null, 100, currentPage, false, "24h,7d");
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
                        mainCoinsRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }

    private class GetCoins extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            coinModels.clear();
            List<CoinMarkets> coins = client.getCoinMarkets(Currency.USD, null, null, 100, currentPage, false, null);
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
            }
            return true;
        }
    }

}
