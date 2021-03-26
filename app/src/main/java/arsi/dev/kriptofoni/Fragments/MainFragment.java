package arsi.dev.kriptofoni.Fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.litesoftwares.coingecko.constant.Currency;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Fragments.MainFragments.CoinsFragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostDecIn24Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostDecIn7Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostIncIn24Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostIncIn7Fragment;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.Api;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {

    private TabLayout tabs;
    private ImageView search, back;
    private TextView totalMarketVal, currency;
    private double totalMarketValue;
    private ArrayList<CoinModel> coinModelsForSearch;
    private CoinsFragment coinsFragment;
    private Api myApi;
    private RelativeLayout header, searchTab;
    private EditText searchBar;
    private final int TOTAL_COIN_NUMBER = 6543, COIN_PER_GROUP = 250;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ViewPager viewPager = view.findViewById(R.id.main_viewpager);
        setupViewPager(viewPager);

        tabs = view.findViewById(R.id.main_tabs);
        tabs.setSelectedTabIndicatorColor(Color.parseColor("#f2a900"));
        tabs.setupWithViewPager(viewPager);

        search = view.findViewById(R.id.main_search);
        totalMarketVal = view.findViewById(R.id.main_total_market_value);
        currency = view.findViewById(R.id.main_currency);
        header = view.findViewById(R.id.main_header);
        searchTab = view.findViewById(R.id.main_seach_tab);
        back = view.findViewById(R.id.main_seach_back_button);
        searchBar = view.findViewById(R.id.main_search_bar);

        coinModelsForSearch = new ArrayList<>();
        myApi = RetrofitClient.getInstance().getMyApi();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Making header which holds total market cap etc. invisible and
                // making search tab visible
                header.setVisibility(View.INVISIBLE);
                searchTab.setVisibility(View.VISIBLE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Making search tab invisible and
                // making header which holds total market cap etc. visible
                searchTab.setVisibility(View.INVISIBLE);
                header.setVisibility(View.VISIBLE);
                // When we close the search bar we need to reset our coinsFragment's
                // ArrayList<CoinModel>. So that coinsFragment page can turn it's initial state.
                coinsFragment.setCoinsList(new ArrayList<>());
            }
        });

        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
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

        getAllCoins();

        return view;
    }

    private void filter(String text) {
        boolean contains = false;
        ArrayList<CoinModel> filteredList = new ArrayList<>();
        if (!text.isEmpty()) {
            for (CoinModel coin : coinModelsForSearch) {
                if (coin.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH))) {
                    filteredList.add(coin);
                    contains = true;
                    // When we filter coins their order breaks down somehow.
                    // To avoid this issue we sort back coinModels by their market caps.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        filteredList.sort(new Comparator<CoinModel>() {
                            @Override
                            public int compare(CoinModel lhs, CoinModel rhs) {
                                return lhs.getMarketCap() > rhs.getMarketCap() ? -1 : lhs.getMarketCap() < rhs.getMarketCap() ? 1 : 0;
                            }
                        });
                    }
                }
            }
            // Passing coinsFragment filteredList to render searched items.
            coinsFragment.setCoinsList(filteredList, contains);
        } else {
            // Passing coinsFragment an empty ArrayList
            // to allow page to turn back it's initial state.
            coinsFragment.setCoinsList(new ArrayList<>());
        }
    }

    private void getAllCoins() {
        coinModelsForSearch.clear();
        // We can't get all of the coins' market information at once. We have to do this
        // process with seperated groups. First we calculate how many groups it will take
        // to fetch all of the coins. Then we start a for loop to fetch all groups.
        int max = TOTAL_COIN_NUMBER / COIN_PER_GROUP + 1;
        for (int i = 1; i <= max; i++) {
            // In order to make fetching process more optimized, we prefer to use AsyncTask.
            new GetAllCoins().execute(i);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        coinsFragment = new CoinsFragment();
        adapter.addFragment(coinsFragment, "Coins");
        adapter.addFragment(new MostIncIn24Fragment(), "Most Inc In 24 Hours");
        adapter.addFragment(new MostDecIn24Fragment(), "Most Dec In 24 Hours");
        adapter.addFragment(new MostIncIn7Fragment(), "Most Inc In 7 Days");
        adapter.addFragment(new MostDecIn7Fragment(), "Most Dec In 7 Days");
        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        public Adapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            tabs.setTabTextColors(Color.parseColor("#797676"), Color.parseColor("#f2a900"));
            return fragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }
    }

    public void setTotalMarketValue(double totalMarketValue) {
        this.totalMarketValue = totalMarketValue;
        try {
            // Formatting number with decimal points
            // Ex. 123456 -> 123.456
            NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
            String text = "$ " + nf.format(totalMarketValue);
            totalMarketVal.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetAllCoins extends AsyncTask<Integer, Void, List<CoinMarket>> {

        @Override
        protected List<CoinMarket> doInBackground(Integer... integers) {
            // Since we can't get weekly price change percentage via CoinGeckoAPİClient,
            // We create a simple HTTP Request via Retrofit
            Call<List<CoinMarket>> call = myApi.getCoinMarkets(Currency.USD, null, 250, integers[0], false, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    // Creating a list of Result objects using our response data.
                    List<CoinMarket> coins = response.body();
                    if (coins != null) {
                        for (int j = 0; j < coins.size(); j++) {
                            // Creating a coin model for each coin in our response data.
                            CoinMarket result = coins.get(j);
                            String imageUrl = result.getImage();
                            String name = result.getName();
                            String shortCut = result.getSymbol();
                            double changeIn24Hours = result.getPrice_change_percentage_24h_in_currency();
                            double priceChangeIn24Hours = result.getPrice_change_24h();
                            double currentPrice = result.getCurrent_price();
                            double marketCap = result.getMarket_cap();
                            totalMarketValue += marketCap;
                            CoinModel model = new CoinModel((integers[0] - 1) * 250 + (j + 1), imageUrl, name, shortCut, changeIn24Hours, priceChangeIn24Hours, currentPrice, marketCap);
                            coinModelsForSearch.add(model);
                        }
                        setTotalMarketValue(totalMarketValue);
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                    System.out.println(t.getMessage());
                }
            });
            return null;
        }
    }

}


