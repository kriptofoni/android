package arsi.dev.kriptofoni;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Adapters.MainCoinsSearchRecyclerAdapter;
import arsi.dev.kriptofoni.Models.CoinSearchModel;

public class CoinSelectActivity extends AppCompatActivity {

    private ArrayList<CoinSearchModel> coins;
    private MainCoinsSearchRecyclerAdapter mainCoinsSearchRecyclerAdapter;
    private RecyclerView recyclerView;
    private ImageView backButton;
    private EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_select);

        coins = new ArrayList<>();

        recyclerView = findViewById(R.id.coin_select_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainCoinsSearchRecyclerAdapter = new MainCoinsSearchRecyclerAdapter(coins, this);
        recyclerView.setAdapter(mainCoinsSearchRecyclerAdapter);

        backButton = findViewById(R.id.coin_select_back_button);
        search = findViewById(R.id.coin_select_search_bar);

        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", 0);

        Gson gson = new Gson();
        String coinSearchModelsJson = sharedPreferences.getString("coinModelsForSearch", "");
        Type type = new TypeToken<List<CoinSearchModel>>() {}.getType();
        if (!coinSearchModelsJson.isEmpty()) coins = gson.fromJson(coinSearchModelsJson, type);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            coins.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(lhs.getMarketCapRank(), rhs.getMarketCapRank());
                }
            });
        }

        mainCoinsSearchRecyclerAdapter.setCoins(coins);

        backButton.setOnClickListener(new View.OnClickListener() {
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
    }

    private void filter(String text) {
        if (!text.isEmpty()) {
            boolean contains = false;
            ArrayList<CoinSearchModel> filteredList = new ArrayList<>();
            for (CoinSearchModel coin : coins) {
                if (coin.getName().toLowerCase(Locale.ENGLISH).startsWith(text.toLowerCase(Locale.ENGLISH))
                || coin.getSymbol().toLowerCase(Locale.ENGLISH).startsWith(text.toLowerCase(Locale.ENGLISH))) {
                    filteredList.add(coin);
                    contains = true;
                }
            }
            if (contains)
                mainCoinsSearchRecyclerAdapter.setCoins(filteredList);
            else
                mainCoinsSearchRecyclerAdapter.setCoins(new ArrayList<>());
        } else {
            mainCoinsSearchRecyclerAdapter.setCoins(coins);
        }
    }
}