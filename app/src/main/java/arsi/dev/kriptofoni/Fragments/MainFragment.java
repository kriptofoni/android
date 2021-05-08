package arsi.dev.kriptofoni.Fragments;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

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

    private HomeActivity homeActivity;
    private final int BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT = 1, CURRENCY_CHOOSE_REQUEST_CODE = 1;
    private TabLayout tabs;
    private ImageView search, back;
    private TextView totalMarketVal, currency;
    private List<CoinSearchModel> coinModelsForSearch;
    private RelativeLayout searchTab;
    private ConstraintLayout header;
    private EditText searchBar;
    private String currencyText;
    private SharedPreferences sharedPreferences;

    private CoinsFragment coinsFragment;
    private MostIncIn24Fragment mostIncIn24Fragment;
    private MostDecIn24Fragment mostDecIn24Fragment;
    private MostIncIn7Fragment mostIncIn7Fragment;
    private MostDecIn7Fragment mostDecIn7Fragment;

    private ViewPager viewPager;

    public MainFragment() {}

    public MainFragment (HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        viewPager = view.findViewById(R.id.main_viewpager);
        setupViewPager();

        tabs = view.findViewById(R.id.main_tabs);
        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
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

        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                if (hasFocus) {
                    imm.showSoftInput(searchBar, 0);
                } else {
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                }
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Making header which holds total market cap etc. invisible and
                // making search tab visible
                header.setVisibility(View.INVISIBLE);
                searchTab.setVisibility(View.VISIBLE);
                searchBar.requestFocus();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Making search tab invisible and
                // making header which holds total market cap etc. visible
                searchBar.setText("");
                searchBar.clearFocus();
                searchTab.setVisibility(View.INVISIBLE);
                header.setVisibility(View.VISIBLE);
                // When we close the search bar we need to reset our fragments'
                // List<CoinModel>. So that fragments can turn it's initial state.
                coinsFragment.setCoinsList(new ArrayList<>());
                mostIncIn24Fragment.setCoinsList(new ArrayList<>());
                mostDecIn24Fragment.setCoinsList(new ArrayList<>());
                mostIncIn7Fragment.setCoinsList(new ArrayList<>());
                mostDecIn7Fragment.setCoinsList(new ArrayList<>());
            }
        });

        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CurrencyChooseActivity.class);
                startActivityForResult(intent, CURRENCY_CHOOSE_REQUEST_CODE);
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        Fragment active;
        switch (viewPager.getCurrentItem()) {
            case 0:
                active = coinsFragment;
                break;
            case 1:
                active = mostIncIn24Fragment;
                break;
            case 2:
                active = mostDecIn24Fragment;
                break;
            case 3:
                active = mostIncIn7Fragment;
                break;
            case 4:
                active = mostDecIn7Fragment;
                break;
            default:
                active = null;
        }

        if (hidden) {
            active.onPause();
        } else {
            active.onResume();
        }
    }

    private void filter(String text) {
        boolean contains = false;
        ArrayList<CoinSearchModel> filteredList = new ArrayList<>();
        if (!text.isEmpty()) {
            for (CoinSearchModel coin : coinModelsForSearch) {
                if (coin.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH))
                || coin.getSymbol().toLowerCase(Locale.ENGLISH).startsWith(text.toLowerCase(Locale.ENGLISH))) {
                    filteredList.add(coin);
                    contains = true;
                }
            }
            // Passing all fragments filteredList to render searched items.
            coinsFragment.setCoinsList(filteredList, contains);
            mostIncIn24Fragment.setCoinsList(filteredList, contains);
            mostDecIn24Fragment.setCoinsList(filteredList, contains);
            mostIncIn7Fragment.setCoinsList(filteredList, contains);
            mostDecIn7Fragment.setCoinsList(filteredList, contains);
        } else {
            // Passing all fragments an empty ArrayList
            // to allow page to turn back it's initial state.
            coinsFragment.setCoinsList(new ArrayList<>());
            mostIncIn24Fragment.setCoinsList(new ArrayList<>());
            mostDecIn24Fragment.setCoinsList(new ArrayList<>());
            mostIncIn7Fragment.setCoinsList(new ArrayList<>());
            mostDecIn7Fragment.setCoinsList(new ArrayList<>());
        }
    }

    private void setupViewPager() {
        Adapter adapter = new Adapter(getChildFragmentManager(), BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT);
        coinsFragment = new CoinsFragment(homeActivity);
        mostIncIn24Fragment = new MostIncIn24Fragment(homeActivity);
        mostDecIn24Fragment = new MostDecIn24Fragment(homeActivity);
        mostIncIn7Fragment = new MostIncIn7Fragment(homeActivity);
        mostDecIn7Fragment = new MostDecIn7Fragment(homeActivity);
        adapter.addFragment(coinsFragment, "Kripto Paralar");
        adapter.addFragment(mostIncIn24Fragment, "24 Saatin Yükselenleri");
        adapter.addFragment(mostDecIn24Fragment, "24 Saatin Düşenleri");
        adapter.addFragment(mostIncIn7Fragment, "7 Günün Yükselenleri");
        adapter.addFragment(mostDecIn7Fragment, "7 Günün Düşenleri");
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        public Adapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            tabs.setTabTextColors(ContextCompat.getColor(getContext(), R.color.textColor), ContextCompat.getColor(getContext(), R.color.buttonColor));
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
        System.out.println(totalMarketValue);
        CountryCodePicker countryCodePicker = new CountryCodePicker();
        String[] arr = countryCodePicker.getCountryCode(currencyText);
        coinsFragment.setCurrencySymbol(arr[1]);
        mostIncIn24Fragment.setCurrencySymbol(arr[1]);
        mostDecIn24Fragment.setCurrencySymbol(arr[1]);
        mostIncIn7Fragment.setCurrencySymbol(arr[1]);
        mostDecIn7Fragment.setCurrencySymbol(arr[1]);
        // Formatting number with decimal points
        // Ex. 123456 -> 123.456
        NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
        String text = !arr[1].isEmpty() ? arr[1] + " " + nf.format(totalMarketValue) : nf.format(totalMarketValue);
        System.out.println(text);
        totalMarketVal.setText(text);
    }

    public void setCoinModelsForSearch(List<CoinSearchModel> coinModelsForSearch) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            coinModelsForSearch.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(lhs.getMarketCapRank(), rhs.getMarketCapRank());
                }
            });
        }
        this.coinModelsForSearch.clear();
        this.coinModelsForSearch.addAll(coinModelsForSearch);
    }

    public void setMostIncIn24List(List<CoinSearchModel> mostIncIn24List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostIncIn24List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(rhs.getPriceChangeIn24(), lhs.getPriceChangeIn24());
                }
            });
        }
        mostIncIn24Fragment.setCoins(mostIncIn24List);
        mostIncIn24Fragment.setProgressBarVisibility(View.VISIBLE);
    }

    public void setMostDecIn24List(List<CoinSearchModel> mostDecIn24List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostDecIn24List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(lhs.getPriceChangeIn24(), rhs.getPriceChangeIn24());
                }
            });
        }
        mostDecIn24Fragment.setCoins(mostDecIn24List);
        mostDecIn24Fragment.setProgressBarVisibility(View.VISIBLE);
    }

    public void setMostIncIn7List(List<CoinSearchModel> mostIncIn7List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostIncIn7List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(rhs.getPriceChangeIn7(), lhs.getPriceChangeIn7());
                }
            });
        }
        mostIncIn7Fragment.setCoins(mostIncIn7List);
        mostIncIn7Fragment.setProgressBarVisibility(View.VISIBLE);
    }

    public void setMostDecIn7List(List<CoinSearchModel> mostDecIn7List) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mostDecIn7List.sort(new Comparator<CoinSearchModel>() {
                @Override
                public int compare(CoinSearchModel lhs, CoinSearchModel rhs) {
                    return Double.compare(lhs.getPriceChangeIn7(), rhs.getPriceChangeIn7());
                }
            });
        }
        mostDecIn7Fragment.setCoins(mostDecIn7List);
        mostDecIn7Fragment.setProgressBarVisibility(View.VISIBLE);
    }

    public void writeToMem(String id, List<CoinSearchModel> list) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(list);
        editor.putString(id, json);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CURRENCY_CHOOSE_REQUEST_CODE && resultCode == CURRENCY_CHOOSE_REQUEST_CODE) {
            currencyText = sharedPreferences.getString("currency", "usd");
            homeActivity.getTotalMarketCap();
            // Refreshing coinsFragment
            coinsFragment.setCurrency(currencyText);
            coinsFragment.setCurrentPage(1);
            coinsFragment.emptyAllCoinModels();
            coinsFragment.setFirstOnResume(false);
            // Refreshing mostIncIn24HFragment
            mostIncIn24Fragment.setCurrency(currencyText);
            mostIncIn24Fragment.setCurrentPage(1);
            mostIncIn24Fragment.emptyAllCoinModels();
            mostIncIn24Fragment.setFirstOnResume(false);
            // Refreshing mostDecIn24HFragment
            mostDecIn24Fragment.setCurrency(currencyText);
            mostDecIn24Fragment.setCurrentPage(1);
            mostDecIn24Fragment.emptyAllCoinModels();
            mostDecIn24Fragment.setFirstOnResume(false);
            // Refreshing mostIncIn7HFragment
            mostIncIn7Fragment.setCurrency(currencyText);
            mostIncIn7Fragment.setCurrentPage(1);
            mostIncIn7Fragment.emptyAllCoinModels();
            mostIncIn7Fragment.setFirstOnResume(false);
            // Refreshing mostDecIn7HFragment
            mostDecIn7Fragment.setCurrency(currencyText);
            mostDecIn7Fragment.setCurrentPage(1);
            mostDecIn7Fragment.emptyAllCoinModels();
            mostDecIn7Fragment.setFirstOnResume(false);
        }
    }
}


