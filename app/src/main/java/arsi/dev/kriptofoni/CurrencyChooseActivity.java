package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrencyChooseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CurrencyChooseRecyclerAdapter currencyChooseRecyclerAdapter;
    private ImageView back;
    private EditText search;
    private CoinGeckoApiClient client;
    private ArrayList<String> currencies;
    private String currency;
    private SharedPreferences sharedPreferences;
    private CoinGeckoApi myCoinGeckoApi;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_choose);

        sharedPreferences = getSharedPreferences("Preferences", 0);
        myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();

        currencies = new ArrayList<>();

        recyclerView = findViewById(R.id.currency_choose_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyChooseRecyclerAdapter = new CurrencyChooseRecyclerAdapter(currencies, sharedPreferences);
        recyclerView.setAdapter(currencyChooseRecyclerAdapter);

        back = findViewById(R.id.currency_choose_back_button);
        search = findViewById(R.id.currency_choose_search_bar);
        progressBar = findViewById(R.id.currency_choose_progress_bar);

        client = new CoinGeckoApiClientImpl();

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
//        GetCurrencies getCurrencies = new GetCurrencies();
//        getCurrencies.execute();
//        try {
//            if (getCurrencies.get()) {
//                currencyChooseRecyclerAdapter.setCurrencies(currencies);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Call<String[]> call = myCoinGeckoApi.getCurrencies();
        call.enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {
                if (response.isSuccessful()) {
                    String[] responseBody = response.body();
                    currencies.addAll(Arrays.asList(responseBody));
                    currencyChooseRecyclerAdapter.setCurrencies(currencies);
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
    }

    private class GetCurrencies extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            List<String> curr = client.getSupportedVsCurrencies();
            currencies.addAll(curr);

            return true;
        }
    }
}
