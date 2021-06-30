package arsi.dev.kriptofoni.Fragments;

import android.annotation.SuppressLint;
import android.app.Service;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Adapters.NewsRecyclerAdapter;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.AlertFragment;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.WatchingListFragment;
import arsi.dev.kriptofoni.Fragments.NewsFragments.BitcoinFragment;
import arsi.dev.kriptofoni.Fragments.NewsFragments.NewestFragment;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.Models.NewsModel;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.KoinBulteniRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.KriptofoniRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.NewsApi;
import arsi.dev.kriptofoni.Retrofit.UzmanCoinRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {

    private final int BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT = 1;

    private NewsApi uzmanCoinApi, koinBulteniApi, kriptofoniApi;
    private List<NewsModel> models, bitcoinModels;

    private ConstraintLayout constraintLayout;
    private WebView webView;
    private ImageView search, back;
    private EditText searchText;
    private RelativeLayout header, searchBar;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private NewestFragment newestFragment;
    private BitcoinFragment bitcoinFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        uzmanCoinApi = UzmanCoinRetrofitClient.getInstance().getNewsApi();
        koinBulteniApi = KoinBulteniRetrofitClient.getInstance().getNewsApi();
        kriptofoniApi = KriptofoniRetrofitClient.getInstance().getNewsApi();

        models = new ArrayList<>();
        bitcoinModels = new ArrayList<>();

        constraintLayout = view.findViewById(R.id.news_constraint_layout);
        search = view.findViewById(R.id.news_search_icon);
        back = view.findViewById(R.id.news_back_icon);
        searchText = view.findViewById(R.id.news_search_edit_text);
        header = view.findViewById(R.id.news_header_title);
        searchBar = view.findViewById(R.id.news_search_title);
        viewPager = view.findViewById(R.id.news_viewpager);
        setUpViewPager();

        tabLayout = view.findViewById(R.id.news_tabs);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
        tabLayout.setupWithViewPager(viewPager);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchBar();
                searchText.requestFocus();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSearchBar();
                searchText.setText("");
                searchText.clearFocus();
            }
        });

        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                if (hasFocus) {
                    imm.showSoftInput(searchText, 0);
                } else {
                    imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                }
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
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

        getNews("Uzman Coin");
        getNews("Koin Bülteni");
        getNews("Kriptofoni");

        return view;
    }

    private void setUpViewPager() {
        Adapter adapter = new Adapter(getChildFragmentManager(), BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT);

        newestFragment = new NewestFragment(this);
        bitcoinFragment = new BitcoinFragment(this);

        adapter.addFragment(newestFragment, "En Yeniler");
        adapter.addFragment(bitcoinFragment, "Bitcoin Haberleri");

        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {

        private List<Fragment> alertFragmentsList = new ArrayList<>();
        private List<String> alertFragmentsTitlesList = new ArrayList<>();

        public Adapter(FragmentManager fragmentManager, int behaviour){
            super(fragmentManager, behaviour);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            tabLayout.setTabTextColors(ContextCompat.getColor(getContext(), R.color.textColor), ContextCompat.getColor(getContext(), R.color.buttonColor));
            return alertFragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return alertFragmentsList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return alertFragmentsTitlesList.get(position);
        }

        public void addFragment(Fragment fragment, String title){
            alertFragmentsList.add(fragment);
            alertFragmentsTitlesList.add(title);
        }
    }

    private void filter(String text) {
        List<NewsModel> filteredList = new ArrayList<>();
        if (!text.isEmpty()) {
            for (NewsModel model : models) {
                if (Html.fromHtml(model.getTitle(), Html.FROM_HTML_MODE_COMPACT).toString().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH))) {
                    filteredList.add(model);
                }
            }
            newestFragment.setModels(filteredList);
            bitcoinFragment.setModels(filteredList);
        } else {
            newestFragment.setModels(models);
            bitcoinFragment.setModels(bitcoinModels);
        }
    }

    private void getNews(String site) {
        models.clear();
        bitcoinModels.clear();

        Call<JsonArray> call = null;
        String source = "";
        switch (site) {
            case "Uzman Coin":
                call = uzmanCoinApi.getPosts();
                break;
            case "Koin Bülteni":
                call = koinBulteniApi.getPosts();
                break;
            case "Kriptofoni":
                call = kriptofoniApi.getPosts();
                break;
        }

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                JsonArray body = response.body();
                if (response.isSuccessful()) {
                    if (body != null) {
                        for (int i = 0; i < body.size(); i++) {
                            JsonObject currentNew = body.get(i).getAsJsonObject();
                            String title = currentNew.get("title").getAsJsonObject().get("rendered").getAsString();
                            String link = currentNew.get("link").getAsString();
                            LocalDateTime date = LocalDateTime.parse(currentNew.get("date").getAsString());

                            JsonPrimitive thumbNailObject = (JsonPrimitive) currentNew.get("_embedded").getAsJsonObject().get("wp:featuredmedia").getAsJsonArray().get(0).getAsJsonObject().get("source_url");
                            String thumbNail = "";

                            if (thumbNailObject != null && !thumbNailObject.isJsonNull()) {
                                thumbNail = thumbNailObject.getAsString();
                            }

                            NewsModel model = new NewsModel(title, thumbNail, site, link, date);
                            models.add(model);

                        }

                        models.sort(new Comparator<NewsModel>() {
                            @Override
                            public int compare(NewsModel lhs, NewsModel rhs) {
                                return Long.compare(rhs.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), lhs.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            }
                        });

                        for (NewsModel model : models) {
                            if (Html.fromHtml(model.getTitle(), Html.FROM_HTML_MODE_COMPACT).toString().toLowerCase(Locale.ENGLISH).contains("bitcoin"))
                                bitcoinModels.add(model);
                        }

                        newestFragment.setModels(models);
                        bitcoinFragment.setModels(bitcoinModels);
                    } else {
                        getNews(site);
                    }
                } else {
                    getNews(site);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                getNews(site);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void createWebView(String link) {
        webView = new WebView(getActivity());
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(link);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        constraintLayout.addView(webView);
    }

    public WebView getWebView() {
        return webView;
    }

    public void removeWebView() {
        constraintLayout.removeView(webView);
        webView.destroy();
        webView = null;
    }

    private void openSearchBar() {
        header.setVisibility(View.INVISIBLE);
        searchBar.setVisibility(View.VISIBLE);
    }

    private void closeSearchBar() {
        searchBar.setVisibility(View.GONE);
        header.setVisibility(View.VISIBLE);
    }
}
