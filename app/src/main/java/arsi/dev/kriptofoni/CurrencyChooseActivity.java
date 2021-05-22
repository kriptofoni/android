package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Exchanges.Exchanges;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Adapters.CurrencyChooseRecyclerAdapter;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CurrenciesApi;
import arsi.dev.kriptofoni.Retrofit.CurrenciesRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrencyChooseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CurrencyChooseRecyclerAdapter currencyChooseRecyclerAdapter;
    private ImageView back;
    private EditText search;
    private ArrayList<String> currencies;
    private String currency, currenciesStr;
    private SharedPreferences sharedPreferences;
    private CurrenciesApi myCoinGeckoApi;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_choose);

        sharedPreferences = getSharedPreferences("Preferences", 0);
        currenciesStr = sharedPreferences.getString("currencies", "");
        myCoinGeckoApi = CurrenciesRetrofitClient.getInstance().getMyCoinGeckoApi();

        currencies = new ArrayList<>();

        recyclerView = findViewById(R.id.currency_choose_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyChooseRecyclerAdapter = new CurrencyChooseRecyclerAdapter(currencies, sharedPreferences, this);
        recyclerView.setAdapter(currencyChooseRecyclerAdapter);

        Intent intent = getIntent();
        currencyChooseRecyclerAdapter.setConverter(intent.getBooleanExtra("converter", false));

        back = findViewById(R.id.currency_choose_back_button);
        search = findViewById(R.id.currency_choose_search_bar);
        progressBar = findViewById(R.id.currency_choose_progress_bar);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

        getCurrencies();
    }

    private void filter(String text) {
        boolean contains = false;
        ArrayList<String> filteredList = new ArrayList<>();
        if (!text.isEmpty()) {
            for (String currency : currencies) {
                if (currency.toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH))) {
                    contains = true;
                    filteredList.add(currency);
                }
            }
            currencyChooseRecyclerAdapter.setCurrencies(filteredList, contains);
        } else {
            currencyChooseRecyclerAdapter.setCurrencies(currencies);
        }
    }

    private void getCurrencies() {
        if (currenciesStr.isEmpty()) {
            Call<String[]> call = myCoinGeckoApi.getCurrencies();
            call.enqueue(new Callback<String[]>() {
                @Override
                public void onResponse(Call<String[]> call, Response<String[]> response) {
                    if (response.isSuccessful()) {
                        String[] responseBody = response.body();
                        currencies.addAll(Arrays.asList(responseBody));
                        currencyChooseRecyclerAdapter.setCurrencies(currencies);

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < currencies.size(); i++) {
                            if (i == currencies.size() - 1) {
                                stringBuilder.append(currencies.get(i));
                            } else {
                                stringBuilder.append(currencies.get(i)).append(",");
                            }
                        }

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("currencies", stringBuilder.toString());
                        editor.apply();

                        progressBar.setVisibility(View.GONE);
                    } else {
                        System.out.println("133 " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<String[]> call, Throwable t) {
                    System.out.println(t.getMessage());
                }
            });
        } else {
            String[] currenciesArray = currenciesStr.split(",");
            currencies.addAll(Arrays.asList(currenciesArray));
            currencyChooseRecyclerAdapter.setCurrencies(currencies);
            progressBar.setVisibility(View.GONE);
        }
    }
}
