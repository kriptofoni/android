package arsi.dev.kriptofoni.Fragments.AlertsFragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.MainCoinsRecyclerAdapter;
import arsi.dev.kriptofoni.Adapters.WatchingListRecyclerAdapter;
import arsi.dev.kriptofoni.CoinSelectActivity;
import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
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
    private ImageView selectCoins, add, delete;
    private Button noCoinAdd;
    private WatchingListRecyclerAdapter watchingListRecyclerAdapter;
    private List<WatchingListModel> models;
    private SortedCoinsApi sortedCoinsApi;
    private String currencyText, coinIds;
    private boolean selectingMode = false;
    private ProgressBar progressBar;
    private List<String> ids, deleteIds;
    private SharedPreferences sharedPreferences;
    private final int CURRENCY_CHOOSE_CODE = 1, COIN_CHOOSE_CODE = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts_watching_list, container, false);

        models = new ArrayList<>();
        deleteIds = new ArrayList<>();
        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        sortedCoinsApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();

        noCoin = view.findViewById(R.id.alerts_watching_list_no_coin);
        hasCoin = view.findViewById(R.id.alerts_watching_list_coins);
        currency = view.findViewById(R.id.alerts_watching_list_currency);
        selectCoins = view.findViewById(R.id.alerts_watching_list_select_coins);
        add = view.findViewById(R.id.alerts_watching_list_add_coin);
        noCoinAdd = view.findViewById(R.id.alerts_watching_list_no_coin_add_button);
        progressBar = view.findViewById(R.id.alerts_watching_list_progress_bar);
        delete = view.findViewById(R.id.alerts_watching_list_delete);

        recyclerView = view.findViewById(R.id.alerts_watching_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        watchingListRecyclerAdapter = new WatchingListRecyclerAdapter(models, this);
        recyclerView.setAdapter(watchingListRecyclerAdapter);

        selectCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!models.isEmpty()) {
                    selectingMode = !selectingMode;
                    watchingListRecyclerAdapter.setSelectingMode(selectingMode);

                    if (selectingMode) {
                        add.setVisibility(View.GONE);
                        delete.setVisibility(View.VISIBLE);
                    } else {
                        delete.setVisibility(View.GONE);
                        add.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (String id : deleteIds) {
                    ids.remove(id);
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (ids.isEmpty()) {
                    coinIds = "";
                } else {
                    StringBuilder res = new StringBuilder();
                    for (String id : ids) {
                        if (!id.isEmpty())
                            res.append(id).append(",");
                    }
                    coinIds = res.toString();
                }

                editor.putString("watchingList", coinIds);
                editor.apply();

                delete.setVisibility(View.GONE);
                selectingMode = false;
                watchingListRecyclerAdapter.setSelectingMode(selectingMode);

                if (coinIds.isEmpty()) {
                    hasCoin.setVisibility(View.GONE);
                    noCoin.setVisibility(View.VISIBLE);
                } else {
                    add.setVisibility(View.VISIBLE);
                    for (WatchingListModel model : models) {
                        if (!ids.contains(model.getId())) models.remove(model);
                    }
                    watchingListRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });

        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CurrencyChooseActivity.class);
                startActivityForResult(intent, CURRENCY_CHOOSE_CODE);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        noCoinAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        currencyText = sharedPreferences.getString("currency", "");
        coinIds = sharedPreferences.getString("watchingList", "");
        if (!coinIds.isEmpty())
            ids = new LinkedList<>(Arrays.asList(coinIds.split(",")));
        else
            ids = new LinkedList<>();

        CountryCodePicker countryCodePicker = new CountryCodePicker();
        String[] codes = countryCodePicker.getCountryCode(currencyText);
        watchingListRecyclerAdapter.setCurrencySymbol(codes[1]);

        if (!coinIds.equals(",") && !coinIds.isEmpty()) {
            new GetCoinInfo().execute();
        } else {
            progressBar.setVisibility(View.GONE);
            noCoin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void intentToCoinChoose() {
        Intent intent = new Intent(getActivity(), CoinSelectActivity.class);
        startActivityForResult(intent, COIN_CHOOSE_CODE);
    }

    public void addId(String id) {
        deleteIds.add(id);
    }

    public void removeId(String id) {
        deleteIds.remove(id);
    }

    private class GetCoinInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            models.clear();
            Call<List<CoinMarket>> call = sortedCoinsApi.getCoinMarkets(currencyText, coinIds, "market_cap_desc", 50, 1, false, "24h");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    if (response.isSuccessful()) {
                        List<CoinMarket> coins = response.body();
                        if (coins != null && !coins.isEmpty()) {
                            models.clear();
                            for (int i = 0; i < coins.size(); i++) {
                                CoinMarket coin = coins.get(i);
                                String id = coin.getId();
                                String name = coin.getName();
                                String icon = coin.getImage();
                                double priceChangeIn24Hours = coin.getPrice_change_percentage_24h_in_currency();
                                double price = coin.getCurrent_price();
                                WatchingListModel model = new WatchingListModel(id, name, icon, priceChangeIn24Hours, price, i + 1, false);
                                models.add(model);
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    noCoin.setVisibility(View.GONE);
                                    hasCoin.setVisibility(View.VISIBLE);
                                    add.setVisibility(View.VISIBLE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CURRENCY_CHOOSE_CODE && resultCode == CURRENCY_CHOOSE_CODE) {
            if (data != null) {
                currencyText = data.getStringExtra("currency");
                new GetCoinInfo().execute();
            }
        } else if (requestCode == COIN_CHOOSE_CODE && resultCode == CURRENCY_CHOOSE_CODE) {
            if (data != null) {
                ids.add(data.getStringExtra("id"));
                noCoin.setVisibility(View.GONE);
                hasCoin.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                add.setVisibility(View.VISIBLE);

                StringBuilder stringBuilder = new StringBuilder();
                for (String id : ids) {
                    if (!id.isEmpty())
                        stringBuilder.append(id).append(",");
                }

                coinIds = stringBuilder.toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("watchingList", coinIds);
                editor.apply();
                new GetCoinInfo().execute();
            }
        }
    }
}
