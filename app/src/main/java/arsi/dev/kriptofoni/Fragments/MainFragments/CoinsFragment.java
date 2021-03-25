package arsi.dev.kriptofoni.Fragments.MainFragments;

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
import com.litesoftwares.coingecko.constant.Currency;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.Results;
import arsi.dev.kriptofoni.Retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoinsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<CoinModel> coinModels;
    private MainCoinsRecyclerAdapter mainCoinsRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_coins, container, false);

        coinModels = new ArrayList<>();

        recyclerView = view.findViewById(R.id.main_coins_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainCoinsRecyclerAdapter = new MainCoinsRecyclerAdapter(coinModels);
        recyclerView.setAdapter(mainCoinsRecyclerAdapter);

        getCoins();

        return view;
    }

    private void getCoins() {
        coinModels.clear();
        // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
        // We create a simple HTTP Request via Retrofit
        Call<List<Results>> call = RetrofitClient.getInstance().getMyApi().getCoinMarkets(Currency.USD, null, 100, 1, false, "24h,7d");
        call.enqueue(new Callback<List<Results>>() {
            @Override
            public void onResponse(Call<List<Results>> call, Response<List<Results>> response) {
                // Creating a list of Result objects using our response data.
                List<Results> coins = response.body();
                for (int i = 0; i < coins.size(); i++) {
                    // Creating a coin model for each coin in our response data.
                    Results result = coins.get(i);
                    String imageUrl = result.getImage();
                    String name = result.getName();
                    String shortCut = result.getSymbol();
                    double changeIn24Hours = result.getPrice_change_percentage_24h_in_currency();
                    double changeIn7Days = result.getPrice_change_percentage_7d_in_currency();
                    double currentPrice = result.getCurrent_price();
                    CoinModel model = new CoinModel(i + 1, imageUrl, name, shortCut, changeIn24Hours, changeIn7Days, currentPrice);
                    coinModels.add(model);
                    mainCoinsRecyclerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Results>> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }
}
