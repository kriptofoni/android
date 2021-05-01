package arsi.dev.kriptofoni.Fragments;

import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.PortfolioRecyclerAdapter;
import arsi.dev.kriptofoni.BuySellActivity;
import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.WatchingListFragment;
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

    private final int BUY_SELL_ACTIVITY_CODE = 1, CURRENCY_CHOOSE_ACTIVITY_CODE = 2;

    private TextView currency;
    private RelativeLayout hasCoin, noCoin;
    private RecyclerView recyclerView;
    private Button noCoinAdd;
    private ImageView selectCoins;

    private PortfolioRecyclerAdapter portfolioRecyclerAdapter;
    private List<PortfolioModel> models;
    private List<String> ids;
    private String coinIds, currencyText;

    private SortedCoinsApi sortedCoinsApi;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "");

        models = new ArrayList<>();

        sortedCoinsApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();

        portfolioRecyclerAdapter = new PortfolioRecyclerAdapter(models, this);

        hasCoin = view.findViewById(R.id.portfolio_has_coin);
        noCoin = view.findViewById(R.id.portfolio_no_coin);
        noCoinAdd = view.findViewById(R.id.portfolio_no_coin_add);
        currency = view.findViewById(R.id.portfolio_currency);
        selectCoins = view.findViewById(R.id.portfolio_select_coins);

        noCoinAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BuySellActivity.class);
                startActivityForResult(intent, BUY_SELL_ACTIVITY_CODE);
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

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String portfolioJson = sharedPreferences.getString("portfolio", "");
//        if (!coinIds.isEmpty()) {
//            ids = new LinkedList<>(Arrays.asList(coinIds.split(",")));
//        } else {
//            ids = new LinkedList<>();
//        }

        CountryCodePicker countryCodePicker = new CountryCodePicker();
        String[] codes = countryCodePicker.getCountryCode(currencyText);
        portfolioRecyclerAdapter.setCurrencySymbol(codes[1]);

//        if (!coinIds.equals(",") && !coinIds.isEmpty()) {
//            noCoin.setVisibility(View.GONE);
//            hasCoin.setVisibility(View.VISIBLE);
////            add.setVisibility(View.VISIBLE);
////            new WatchingListFragment.GetCoinInfo().execute();
//        } else {
////            progressBar.setVisibility(View.GONE);
//            noCoin.setVisibility(View.VISIBLE);
//        }
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
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

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
