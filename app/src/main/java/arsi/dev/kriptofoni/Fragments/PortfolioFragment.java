package arsi.dev.kriptofoni.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import arsi.dev.kriptofoni.Adapters.PortfolioRecyclerAdapter;
import arsi.dev.kriptofoni.BuySellActivity;
import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.WatchingListFragment;
import arsi.dev.kriptofoni.Models.PortfolioMemoryModel;
import arsi.dev.kriptofoni.Models.PortfolioModel;
import arsi.dev.kriptofoni.Models.WatchingListModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsApi;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PortfolioFragment extends Fragment {

    private final int CURRENCY_CHOOSE_ACTIVITY_CODE = 1, BUY_SELL_ACTIVITY_CODE = 2;

    private TextView currency, principal, totalPrice, totalPriceChange;
    private RelativeLayout hasCoin, noCoin;
    private RecyclerView recyclerView;
    private Button noCoinAdd;
    private ImageView selectCoins, addCoin, delete;
    private ProgressBar progressBar;

    private PortfolioRecyclerAdapter portfolioRecyclerAdapter;
    private List<PortfolioModel> models;
    private List<PortfolioMemoryModel> memoryModels, removeModels;
    private List<String> deleteIds;
    private Map<String, Double> quantities;
    private String coinIds = "", currencyText, currencySymbol;
    private double portfolioValue = 0, portfolioChange = 0, totalPrincipal = 0;
    private boolean selectingMode = false;

    private SortedCoinsApi sortedCoinsApi;
    private SharedPreferences sharedPreferences;

    private NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
    private CountryCodePicker countryCodePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "");

        models = new ArrayList<>();
        deleteIds = new ArrayList<>();
        removeModels = new ArrayList<>();

        sortedCoinsApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();

        recyclerView = view.findViewById(R.id.portfolio_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        portfolioRecyclerAdapter = new PortfolioRecyclerAdapter(models, this);
        recyclerView.setAdapter(portfolioRecyclerAdapter);

        hasCoin = view.findViewById(R.id.portfolio_has_coin);
        noCoin = view.findViewById(R.id.portfolio_no_coin);
        noCoinAdd = view.findViewById(R.id.portfolio_no_coin_add);
        currency = view.findViewById(R.id.portfolio_currency);
        selectCoins = view.findViewById(R.id.portfolio_select_coins);
        progressBar = view.findViewById(R.id.portfolio_progress_bar);
        addCoin = view.findViewById(R.id.portfolio_add_coin);
        principal = view.findViewById(R.id.portfolio_total_principal);
        totalPrice = view.findViewById(R.id.portfolio_total_price_text);
        totalPriceChange = view.findViewById(R.id.portfolio_price_change_text);
        delete = view.findViewById(R.id.portfolio_delete);

        noCoinAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        addCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CurrencyChooseActivity.class);
                startActivityForResult(intent, CURRENCY_CHOOSE_ACTIVITY_CODE);
            }
        });

        selectCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectingMode = !selectingMode;
                portfolioRecyclerAdapter.setSelectingMode(selectingMode);
                delete.setVisibility(selectingMode ? View.VISIBLE : View.GONE);
                addCoin.setVisibility(selectingMode ? View.GONE : View.VISIBLE);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (PortfolioMemoryModel model : memoryModels) {
                    if (deleteIds.contains(model.getId())) removeModels.add(model);
                }

                memoryModels.removeAll(removeModels);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                String json = "";
                if (!memoryModels.isEmpty()) {
                    json = new Gson().toJson(memoryModels);
                }

                editor.putString("portfolio", json);
                editor.apply();

                readFromMemory();
                deleteIds.clear();
                removeModels.clear();

                delete.setVisibility(View.GONE);
                selectingMode = false;
                portfolioRecyclerAdapter.setSelectingMode(selectingMode);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        countryCodePicker = new CountryCodePicker();
        String[] codes = countryCodePicker.getCountryCode(currencyText);
        currencySymbol = codes[1];
        portfolioRecyclerAdapter.setCurrencySymbol(currencySymbol);

        readFromMemory();
    }

    private void readFromMemory() {
        totalPrincipal = 0;
        portfolioValue = 0;

        String portfolioJson = sharedPreferences.getString("portfolio", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<PortfolioMemoryModel>>() {}.getType();
        if (!portfolioJson.isEmpty()) memoryModels = gson.fromJson(portfolioJson, type);
        else memoryModels = new ArrayList<>();

        quantities = new HashMap<>();

        if (!memoryModels.isEmpty()) {
            for (PortfolioMemoryModel model : memoryModels) {
                totalPrincipal += model.getQuantity() * model.getPrice() + model.getFee();
                double quantity = model.getType().equals("buy") ? model.getQuantity() : -1 * model.getQuantity();
                if (quantities.get(model.getId()) != null) {
                    quantities.put(model.getId(), quantities.get(model.getId()) + quantity);
                } else {
                    quantities.put(model.getId(), quantity);
                }
            }

            Set<String> idList = quantities.keySet();
            StringBuilder builder = new StringBuilder();
            for (String id : idList) {
                builder.append(id).append(",");
            }

            coinIds = builder.toString();

            if (!coinIds.isEmpty()) {
                new GetCoinInfo().execute();
            }
        } else {
            progressBar.setVisibility(View.GONE);
            hasCoin.setVisibility(View.GONE);
            addCoin.setVisibility(View.GONE);
            noCoin.setVisibility(View.VISIBLE);
        }
    }

    private void intentToCoinChoose() {
        Intent intent = new Intent(getActivity(), BuySellActivity.class);
        intent.putExtra("fromPortfolio", true);
        startActivityForResult(intent, BUY_SELL_ACTIVITY_CODE);
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
                                String shortCut = coin.getSymbol();
                                String icon = coin.getImage();
                                String id = coin.getId();
                                double currentPrice = coin.getCurrent_price();
                                double change24h = coin.getPrice_change_24h();
                                double changePercentage24H = coin.getPrice_change_percentage_24h_in_currency();
                                double quantity = quantities.get(id);
                                double change = change24h * quantity;
                                PortfolioModel model = new PortfolioModel(id, shortCut, icon, quantity * currentPrice, change, changePercentage24H, currentPrice, quantity, false);
                                models.add(model);

                                portfolioValue += quantity * currentPrice;
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    noCoin.setVisibility(View.GONE);
                                    hasCoin.setVisibility(View.VISIBLE);
                                    addCoin.setVisibility(View.VISIBLE);
                                    totalPrice.setText(String.format("%s%s", currencySymbol, nf.format(portfolioValue)));
                                    double priceDiff = portfolioValue - totalPrincipal;
                                    double priceDiffPerc = priceDiff / totalPrincipal * 100;
                                    String priceChangeText = String.format("%s%s (%%%s)", currencySymbol, nf.format(priceDiff), nf.format(priceDiffPerc));
                                    totalPriceChange.setText(priceChangeText);
                                    principal.setText(String.format("Total Principal: %s%s", currencySymbol, nf.format(totalPrincipal)));
                                    totalPriceChange.setTextColor(priceDiff < 0 ? Color.RED : priceDiff > 0 ? Color.GREEN : Color.BLACK);
                                    portfolioRecyclerAdapter.notifyDataSetChanged();
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

        if (requestCode == CURRENCY_CHOOSE_ACTIVITY_CODE && resultCode == CURRENCY_CHOOSE_ACTIVITY_CODE) {
            if (data != null) {
                currencyText = data.getStringExtra("currency");
                if (currencyText != null) {
                    portfolioRecyclerAdapter.setCurrencySymbol(countryCodePicker.getCountryCode(currencyText)[1]);
                    new GetCoinInfo().execute();
                }
            }
        } else if (requestCode == BUY_SELL_ACTIVITY_CODE && resultCode == CURRENCY_CHOOSE_ACTIVITY_CODE) {
            readFromMemory();
        }
    }
}
