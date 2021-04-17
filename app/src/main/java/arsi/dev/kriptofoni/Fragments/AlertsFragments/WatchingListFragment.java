package arsi.dev.kriptofoni.Fragments.AlertsFragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Adapters.WatchingListRecyclerAdapter;
import arsi.dev.kriptofoni.Models.WatchingListModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsApi;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchingListFragment extends Fragment{

    private RelativeLayout noCoin, hasCoin;
    private RecyclerView recyclerView;
    private TextView currency;
    private ImageView selectCoins, add;
    private Button noCoinAdd;
    private WatchingListRecyclerAdapter watchingListRecyclerAdapter;
    private List<WatchingListModel> models;
    private SortedCoinsApi sortedCoinsApi;
    private String currencyText, coinIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts_watching_list, container, false);

        models = new ArrayList<>();
        sortedCoinsApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();

        noCoin = view.findViewById(R.id.alerts_watching_list_no_coin);
        hasCoin = view.findViewById(R.id.alerts_watching_list_coins);
        currency = view.findViewById(R.id.alerts_watching_list_currency);
        selectCoins = view.findViewById(R.id.alerts_watching_list_select_coins);
        add = view.findViewById(R.id.alerts_watching_list_add_coin);
        noCoinAdd = view.findViewById(R.id.alerts_watching_list_no_coin_add_button);

        recyclerView = view.findViewById(R.id.alerts_watching_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        watchingListRecyclerAdapter = new WatchingListRecyclerAdapter(models, this);
        recyclerView.setAdapter(watchingListRecyclerAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "");
        coinIds = sharedPreferences.getString("watchingList", "");

        CountryCodePicker countryCodePicker = new CountryCodePicker();
        String[] codes = countryCodePicker.getCountryCode(currencyText);
        watchingListRecyclerAdapter.setCurrencySymbol(codes[1]);

        if (!coinIds.isEmpty()) {
            noCoin.setVisibility(View.GONE);
            hasCoin.setVisibility(View.VISIBLE);
            add.setVisibility(View.VISIBLE);
        }

        new GetCoinInfo().execute();
    }

    private class GetCoinInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Call<List<CoinMarket>> call = sortedCoinsApi.getCoinMarkets(currencyText, coinIds, "market_cap_desc", 50, 1, false, "24h");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    if (response.isSuccessful()) {
                        List<CoinMarket> coins = response.body();
                        if (coins != null && !coins.isEmpty()) {
                            models.clear();
                            for (CoinMarket coin : coins) {
                                String id = coin.getId();
                                String name = coin.getName();
                                String icon = coin.getImage();
                                double priceChangeIn24Hours = coin.getPrice_change_percentage_24h_in_currency();
                                double price = coin.getCurrent_price();
                                WatchingListModel model = new WatchingListModel(id, name, icon, priceChangeIn24Hours, price);
                                models.add(model);
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    watchingListRecyclerAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            new GetCoinInfo().execute();
                            cancel(true);
                        }
                    } else {
                        new GetCoinInfo().execute();
                        cancel(true);
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                    new GetCoinInfo().execute();
                    cancel(true);
                }
            });
            return null;
        }
    }
}
