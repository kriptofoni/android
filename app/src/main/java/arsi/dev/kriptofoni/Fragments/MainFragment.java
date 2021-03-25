package arsi.dev.kriptofoni.Fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.constant.Currency;
import com.litesoftwares.coingecko.domain.Coins.CoinList;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import arsi.dev.kriptofoni.Fragments.MainFragments.CoinsFragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostDecIn24Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostDecIn7Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostIncIn24Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostIncIn7Fragment;
import arsi.dev.kriptofoni.R;

public class MainFragment extends Fragment {

    private TabLayout tabs;
    private ImageView search;
    private TextView totalMarketVal, currency;
    private CoinGeckoApiClient client;

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

        // Initialize CoinGeckoApiClient
        client = new CoinGeckoApiClientImpl();

        // Api requests can't be made in main thread.
        // In order to make api request, we create an AsyncTask.
        GetTotalMarketValue getTotalMarketValue = new GetTotalMarketValue();
        getTotalMarketValue.execute(client);
        try {
            // Formatting number with decimal points
            // Ex. 123456 -> 123.456
            NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
            String text = "$ " + nf.format(getTotalMarketValue.get());
            totalMarketVal.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new CoinsFragment(), "Coins");
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

    private class GetTotalMarketValue extends AsyncTask<CoinGeckoApiClient, Void, Long> {

        private List<CoinMarkets> coins;

        @Override
        protected Long doInBackground(CoinGeckoApiClient... coinGeckoApiClients) {
            long totalMarketValue = 0;
            coins = (coinGeckoApiClients[0].getCoinMarkets(Currency.USD, null, null,250,null,false,null));
            for (CoinMarkets coin : coins) {
                totalMarketValue += coin.getMarketCap();
            }
            return totalMarketValue;
        }
    }

}


