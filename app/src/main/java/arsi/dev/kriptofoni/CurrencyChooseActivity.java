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

import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Exchanges.Exchanges;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import arsi.dev.kriptofoni.Adapters.CurrencyChooseRecyclerAdapter;

public class CurrencyChooseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CurrencyChooseRecyclerAdapter currencyChooseRecyclerAdapter;
    private ImageView back;
    private EditText search;
    private CoinGeckoApiClient client;
    private ArrayList<String> currencies;
    private String currency;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_choose);

        sharedPreferences = getSharedPreferences("Preferences", 0);

        currencies = new ArrayList<>();

        recyclerView = findViewById(R.id.currency_choose_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        back = findViewById(R.id.currency_choose_back_button);
        search = findViewById(R.id.currency_choose_search_bar);

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
        GetCurrencies getCurrencies = new GetCurrencies();
        getCurrencies.execute();
        try {
            if (getCurrencies.get()) {
                currencyChooseRecyclerAdapter = new CurrencyChooseRecyclerAdapter(currencies, sharedPreferences);
                recyclerView.setAdapter(currencyChooseRecyclerAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetCurrencies extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                List<String> curr = client.getSupportedVsCurrencies();
                currencies.addAll(curr);
            } catch (CoinGeckoApiException ex) {
                this.cancel(true);
                getCurrencies();
            }
            return true;
        }
    }
}
