package arsi.dev.kriptofoni.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.google.gson.Gson;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.Fragments.MainFragments.CoinsFragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostDecIn24Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostDecIn7Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostIncIn24Fragment;
import arsi.dev.kriptofoni.Fragments.MainFragments.MostIncIn7Fragment;
import arsi.dev.kriptofoni.HomeActivity;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;

public class MainFragment extends Fragment {

    private final HomeActivity homeActivity;
    private TabLayout tabs;
    private ImageView search, back;
    private TextView totalMarketVal, currency;
    private ArrayList<CoinSearchModel> coinModelsForSearch;
    private RelativeLayout header, searchTab;
    private EditText searchBar;
    private String currencyText;
    private SharedPreferences sharedPreferences;

    private CoinsFragment coinsFragment;
    private MostIncIn24Fragment mostIncIn24Fragment;
    private MostDecIn24Fragment mostDecIn24Fragment;
    private MostIncIn7Fragment mostIncIn7Fragment;
    private MostDecIn7Fragment mostDecIn7Fragment;

    public MainFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

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

        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "usd");

        coinModelsForSearch = new ArrayList<>();

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
                searchBar.setText("");
                searchBar.clearFocus();
                View focusView = getActivity().getCurrentFocus();
                if (focusView != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
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
                Intent intent = new Intent(getActivity(), CurrencyChooseActivity.class);
                startActivityForResult(intent, 0);
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
    }

    private void filter(String text) {
        boolean contains = false;
        ArrayList<CoinSearchModel> filteredList = new ArrayList<>();
        if (!text.isEmpty()) {
            for (CoinSearchModel coin : coinModelsForSearch) {
                if (coin.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH))) {
                    filteredList.add(coin);
                    contains = true;
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

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        coinsFragment = new CoinsFragment();
        mostIncIn24Fragment = new MostIncIn24Fragment();
        mostDecIn24Fragment = new MostDecIn24Fragment();
        mostIncIn7Fragment = new MostIncIn7Fragment();
        mostDecIn7Fragment = new MostDecIn7Fragment();
        adapter.addFragment(coinsFragment, "Coins");
        adapter.addFragment(mostIncIn24Fragment, "Most Inc In 24 Hours");
        adapter.addFragment(mostDecIn24Fragment, "Most Dec In 24 Hours");
        adapter.addFragment(mostIncIn7Fragment, "Most Inc In 7 Days");
        adapter.addFragment(mostDecIn7Fragment, "Most Dec In 7 Days");
        viewPager.setOffscreenPageLimit(5);
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
        try {
            CountryCodePicker countryCodePicker = new CountryCodePicker();
            String[] arr = countryCodePicker.getCountryCode(currencyText);
            coinsFragment.setCurrencySymbol(arr[1]);
            // Formatting number with decimal points
            // Ex. 123456 -> 123.456
            NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
            String text = !arr[1].isEmpty() ? arr[1] + " " + nf.format(totalMarketValue) : nf.format(totalMarketValue);
            totalMarketVal.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCoinModelsForSearch(ArrayList<CoinSearchModel> coinModelsForSearch) {
        this.coinModelsForSearch = coinModelsForSearch;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.coinModelsForSearch.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(lhs.getMarketCapRank(), rhs.getMarketCapRank());
                }
            });
        }
    }

    public void setMostIncIn24List(ArrayList<CoinSearchModel> mostIncIn24List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostIncIn24List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(rhs.getPriceChangeIn24(), lhs.getPriceChangeIn24());
                }
            });
        }
        mostIncIn24Fragment.setCoins(mostIncIn24List);
    }

    public void setMostDecIn24List(ArrayList<CoinSearchModel> mostDecIn24List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostDecIn24List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(lhs.getPriceChangeIn24(), rhs.getPriceChangeIn24());
                }
            });
        }
        mostDecIn24Fragment.setCoins(mostDecIn24List);
    }

    public void setMostIncIn7List(ArrayList<CoinSearchModel> mostIncIn7List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostIncIn7List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(rhs.getPriceChangeIn7(), lhs.getPriceChangeIn7());
                }
            });
        }
        mostIncIn7Fragment.setCoins(mostIncIn7List);
    }

    public void setMostDecIn7List(ArrayList<CoinSearchModel> mostDecIn7List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostDecIn7List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(lhs.getPriceChangeIn7(), rhs.getPriceChangeIn7());
                }
            });
        }
        mostDecIn7Fragment.setCoins(mostDecIn7List);
    }

    public void writeToMem(String id, ArrayList<CoinSearchModel> list) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(list);
        editor.putString(id, json);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            currencyText = sharedPreferences.getString("currency", "usd");
            homeActivity.getTotalMarketCap();
            coinsFragment.setCurrency(currencyText);
            coinsFragment.setCurrentPage(1);
            coinsFragment.emptyAllCoinModels();
        }
    }
}


