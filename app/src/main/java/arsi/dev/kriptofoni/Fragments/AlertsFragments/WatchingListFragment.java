package arsi.dev.kriptofoni.Fragments.AlertsFragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Adapters.WatchingListRecyclerAdapter;
import arsi.dev.kriptofoni.CoinSelectActivity;
import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.HomeActivity;
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
    private boolean selectingMode = false, firstInitial = true, hiddenChanged = false;
    private ProgressBar progressBar;
    private List<String> ids, deleteIds;
    private SharedPreferences sharedPreferences;
    private HomeActivity homeActivity;
    private CountryCodePicker countryCodePicker;
    private final int CURRENCY_CHOOSE_CODE = 1, COIN_CHOOSE_CODE = 2;

    public WatchingListFragment() {}
    public WatchingListFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts_watching_list, container, false);

        models = new ArrayList<>();
        deleteIds = new ArrayList<>();
        sharedPreferences = getParentFragment().getActivity().getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "usd");
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
                if (!deleteIds.isEmpty()) {
                    ids.removeAll(deleteIds);

                    removeCoin();
                } else {
                    Toast.makeText(getContext(), "Lütfen silmek istediğiniz kripto paraları seçin", Toast.LENGTH_SHORT).show();
                }

            }
        });

        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CurrencyChooseActivity.class);
                intent.putExtra("converter", true);
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

        String tempCurrencyText = sharedPreferences.getString("currency", "");

        if (firstInitial || hiddenChanged) {

            fetchCoins();

            if (firstInitial)
                firstInitial = false;
            if (hiddenChanged)
                hiddenChanged = false;
        } else if (!tempCurrencyText.equals(currencyText)) {
            currencyText = tempCurrencyText;
            currency.setText(currencyText.toUpperCase(Locale.ENGLISH));

            fetchCoins();
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

    public void setHiddenChanged(boolean hiddenChanged) {
        this.hiddenChanged = hiddenChanged;
    }

    private void fetchCoins() {
        coinIds = sharedPreferences.getString("watchingList", "");
        if (!coinIds.isEmpty())
            ids = new LinkedList<>(Arrays.asList(coinIds.split(",")));
        else
            ids = new LinkedList<>();

        countryCodePicker = new CountryCodePicker();
        String[] codes = countryCodePicker.getCountryCode(currencyText);
        watchingListRecyclerAdapter.setCurrencySymbol(codes[1]);

        if (!coinIds.equals(",") && !coinIds.isEmpty()) {
            getCoinInfo();
        } else {
            progressBar.setVisibility(View.GONE);
            noCoin.setVisibility(View.VISIBLE);
        }
    }

    private void removeCoin() {
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
        deleteIds.clear();
        watchingListRecyclerAdapter.setSelectingMode(selectingMode);

        if (coinIds.isEmpty()) {
            hasCoin.setVisibility(View.GONE);
            noCoin.setVisibility(View.VISIBLE);
        } else {
            add.setVisibility(View.VISIBLE);
            List<WatchingListModel> modelsToRemove = new ArrayList<>();
            for (WatchingListModel model : models) {
                if (!ids.contains(model.getId())) modelsToRemove.add(model);
            }
            models.removeAll(modelsToRemove);
            watchingListRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void getCoinInfo() {
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
                        getCoinInfo();
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                getCoinInfo();
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        getCoinInfo();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                getCoinInfo();
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 101) {
            List<String> removeIds = new ArrayList<>();
            removeIds.add(ids.get(item.getGroupId()));
            ids.removeAll(removeIds);

            removeCoin();

            return true;
        } else
            return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CURRENCY_CHOOSE_CODE && resultCode == CURRENCY_CHOOSE_CODE) {
            if (data != null) {
                noCoin.setVisibility(View.GONE);
                hasCoin.setVisibility(View.GONE);
                add.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                currencyText = data.getStringExtra("currencyId");
                currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
                String[] codes = countryCodePicker.getCountryCode(currencyText);
                watchingListRecyclerAdapter.setCurrencySymbol(codes[1]);
                if (!coinIds.equals(",") && !coinIds.isEmpty()) {
                    getCoinInfo();
                } else {
                    progressBar.setVisibility(View.GONE);
                    noCoin.setVisibility(View.VISIBLE);
                }
                homeActivity.resetMainFragments();
            }
        } else if (requestCode == COIN_CHOOSE_CODE && resultCode == CURRENCY_CHOOSE_CODE) {
            if (data != null) {
                if (!ids.contains(data.getStringExtra("id"))) {
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
                    getCoinInfo();
                } else {
                    Toast.makeText(getContext(), "Seçtiğiniz kripto para zaten izleme listenizde mevcut.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
