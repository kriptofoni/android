package arsi.dev.kriptofoni.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import arsi.dev.kriptofoni.Adapters.PortfolioRecyclerAdapter;
import arsi.dev.kriptofoni.BuySellActivity;
import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.WatchingListFragment;
import arsi.dev.kriptofoni.Models.LineChartEntryModel;
import arsi.dev.kriptofoni.Models.PortfolioMemoryModel;
import arsi.dev.kriptofoni.Models.PortfolioModel;
import arsi.dev.kriptofoni.Models.WatchingListModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinInfoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsApi;
import arsi.dev.kriptofoni.Retrofit.SortedCoinsRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PortfolioFragment extends Fragment {

    private final int BUY_SELL_ACTIVITY_CODE = 2;

    private TextView principal, totalPrice, totalPriceChange, oneDay, oneWeek,
                        oneMonth, threeMonths, oneYear, all, active;
    private RelativeLayout hasCoin, noCoin;
    private RecyclerView recyclerView;
    private Button noCoinAdd;
    private ImageView selectCoins, addCoin, delete;
    private ProgressBar progressBar, chartProgressBar;
    private LineChart lineChart;

    private PortfolioRecyclerAdapter portfolioRecyclerAdapter;
    private List<PortfolioModel> models;
    private List<PortfolioMemoryModel> memoryModels, removeModels;
    private List<String> deleteIds;
    private List<List<Entry>> yValues = new ArrayList<>();
    private Map<String, Double> quantities, timestamps;
    private Set<String> idList;
    private String coinIds = "", currencyText, currencySymbol;
    private double portfolioValue = 0, totalPrincipal = 0;
    private boolean selectingMode = false, firstInitial = true;
    private int chartColor = Color.BLACK;
    private long from, to;

    private SortedCoinsApi sortedCoinsApi;
    private CoinInfoApi chartInfoApi;
    private SharedPreferences sharedPreferences;

    private NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
    private CountryCodePicker countryCodePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "");

        models = new ArrayList<>();
        deleteIds = new ArrayList<>();
        removeModels = new ArrayList<>();

        sortedCoinsApi = SortedCoinsRetrofitClient.getInstance().getMyCoinGeckoApi();
        chartInfoApi = CoinInfoRetrofitClient.getInstance().getMyCoinGeckoApi();

        recyclerView = view.findViewById(R.id.portfolio_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        portfolioRecyclerAdapter = new PortfolioRecyclerAdapter(models, this);
        recyclerView.setAdapter(portfolioRecyclerAdapter);

        hasCoin = view.findViewById(R.id.portfolio_has_coin);
        noCoin = view.findViewById(R.id.portfolio_no_coin);
        noCoinAdd = view.findViewById(R.id.portfolio_no_coin_add);
        selectCoins = view.findViewById(R.id.portfolio_select_coins);
        progressBar = view.findViewById(R.id.portfolio_progress_bar);
        chartProgressBar = view.findViewById(R.id.portfolio_chart_progress_bar);
        addCoin = view.findViewById(R.id.portfolio_add_coin);
        principal = view.findViewById(R.id.portfolio_total_principal);
        totalPrice = view.findViewById(R.id.portfolio_total_price_text);
        totalPriceChange = view.findViewById(R.id.portfolio_price_change_text);
        delete = view.findViewById(R.id.portfolio_delete);
        lineChart = view.findViewById(R.id.portfolio_chart);
        oneDay = view.findViewById(R.id.portfolio_24h);
        oneWeek = view.findViewById(R.id.portfolio_1w);
        oneMonth = view.findViewById(R.id.portfolio_1m);
        threeMonths = view.findViewById(R.id.portfolio_3m);
        oneYear = view.findViewById(R.id.portfolio_1y);
        all = view.findViewById(R.id.portfolio_all);

        long oneDayInSeconds = 60 * 60 * 24;
        to = System.currentTimeMillis() / 1000;
        oneDay.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
        oneDay.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonTextColor));
        from = to - oneDayInSeconds;

        active = oneDay;

        noCoinAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        addCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        selectCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectingMode = !selectingMode;
                portfolioRecyclerAdapter.setSelectingMode(selectingMode);
                delete.setVisibility(selectingMode ? View.VISIBLE : View.GONE);
                addCoin.setVisibility(selectingMode ? View.GONE : View.VISIBLE);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!deleteIds.isEmpty()) {
                    for (PortfolioMemoryModel model : memoryModels) {
                        if (deleteIds.contains(model.getId())) removeModels.add(model);
                    }

                    memoryModels.removeAll(removeModels);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String json = "";
                    if (!memoryModels.isEmpty()) {
                        json = new Gson().toJson(memoryModels);
                    }

                    editor.putString("portfolio", json);
                    editor.apply();

                    delete.setVisibility(View.GONE);

                    selectingMode = false;
                    portfolioRecyclerAdapter.setSelectingMode(selectingMode);

                    readFromMemory();
                    deleteIds.clear();
                    removeModels.clear();
                } else {
                    Toast.makeText(getContext(), "Lütfen silmek istediğiniz kripto paraları seçin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        oneDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneDay) {
                    oneDay.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
                    oneDay.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24);
                    active.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                    active = oneDay;
                    setChartProgressBarVisible();
                    for (String id : idList) {
                        new GetChartInfo().execute(id);
                    }
                }
            }
        });

        oneWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneWeek) {
                    oneWeek.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
                    oneWeek.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 7);
                    active.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                    active = oneWeek;
                    setChartProgressBarVisible();
                    for (String id : idList) {
                        new GetChartInfo().execute(id);
                    }
                }
            }
        });

        oneMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneMonth) {
                    oneMonth.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
                    oneMonth.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 30);
                    active.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                    active = oneMonth;
                    setChartProgressBarVisible();
                    for (String id : idList) {
                        new GetChartInfo().execute(id);
                    }
                }
            }
        });

        threeMonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != threeMonths) {
                    threeMonths.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
                    threeMonths.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 90);
                    active.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                    active = threeMonths;
                    setChartProgressBarVisible();
                    for (String id : idList) {
                        new GetChartInfo().execute(id);
                    }
                }
            }
        });

        oneYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneYear) {
                    oneYear.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
                    oneYear.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 365);
                    active.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                    active = oneYear;
                    setChartProgressBarVisible();
                    for (String id : idList) {
                        new GetChartInfo().execute(id);
                    }
                }
            }
        });

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != all) {
                    all.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonColor));
                    all.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = 0;
                    active.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                    active = all;
                    setChartProgressBarVisible();
                    for (String id : idList) {
                        new GetChartInfo().execute(id);
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            readFromMemory();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        countryCodePicker = new CountryCodePicker();
//        String[] codes = countryCodePicker.getCountryCode(currencyText);
        currencySymbol = "$";
        portfolioRecyclerAdapter.setCurrencySymbol(currencySymbol);

        if (firstInitial) {
            readFromMemory();
            firstInitial = false;
        }
    }

    private void readFromMemory() {
        totalPrincipal = 0;
        portfolioValue = 0;

        hasCoin.setVisibility(View.GONE);
        addCoin.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String portfolioJson = sharedPreferences.getString("portfolio", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<PortfolioMemoryModel>>() {}.getType();
        if (!portfolioJson.isEmpty()) memoryModels = gson.fromJson(portfolioJson, type);
        else memoryModels = new ArrayList<>();

        quantities = new HashMap<>();
        timestamps = new HashMap<>();

        if (!memoryModels.isEmpty()) {
            for (PortfolioMemoryModel model : memoryModels) {
                if (model.getType().equals("buy"))
                    totalPrincipal += model.getQuantity() * model.getPrice() + model.getFee();
                else
                    totalPrincipal -= model.getQuantity() * model.getPrice() - model.getFee();

                String id = model.getId();
                double quantity = model.getType().equals("buy") ? model.getQuantity() : -1 * model.getQuantity();
                double timestamp = model.getTimestamp();
                if (quantities.get(id) != null) {
                    quantities.put(id, quantities.get(id) + quantity);
                } else {
                    quantities.put(id, quantity);
                }

                if (timestamps.get(id) != null) {
                    if (timestamp < timestamps.get(id))
                        timestamps.put(id, timestamp);
                } else {
                    timestamps.put(id, timestamp);
                }
            }

            portfolioRecyclerAdapter.setTimestamps(timestamps);

            idList = quantities.keySet();

            StringBuilder builder = new StringBuilder();
            for (String id : idList) {
                new GetChartInfo().execute(id);
                builder.append(id).append(",");
            }

            coinIds = builder.toString();

            if (!coinIds.isEmpty()) {
                getCoinInfo();
            }
        } else {
            progressBar.setVisibility(View.GONE);
            hasCoin.setVisibility(View.GONE);
            addCoin.setVisibility(View.GONE);
            noCoin.setVisibility(View.VISIBLE);
        }
    }

    private void setLineChart(List<Entry> yValue) {

        int textColor = ContextCompat.getColor(getContext(), R.color.textColor);

        if (lineChart.getData() != null) {
            lineChart.clearValues();
            lineChart.invalidate();
        }

        // Customizing chart appearance
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(null);
        lineChart.getLegend().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(textColor);
        YAxis yAxisRight = lineChart.getAxisRight();
        lineChart.getAxisLeft().setTextColor(textColor);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String result = "";
                if (active == oneDay) {
                    result = getChartXAxisHourAndMinute(value);
                } else if (active == all) {
                    result = getChartXAxisYears(value);
                } else {
                    result = getChartXAxisDayAndMonth(value);
                }
                return result;
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxisRight.setEnabled(false);

        LineDataSet set = new LineDataSet(yValue, "Fiyatlar");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(0f);
        set.setLineWidth(2f);
        set.setFillAlpha(170);
        set.setDrawFilled(true);
        set.setColor(chartColor);
        set.setFillColor(chartColor);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        LineData data = new LineData(dataSets);

        lineChart.setData(data);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
        setChartProgressBarInvisible();
    }

    private String getChartXAxisHourAndMinute(float timestamp) {
        Date date = new Date((long) timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String result = "";
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (hour < 10) {
            if (minute < 10) {
                result = 0 + "" + hour + ":" + 0 + "" + minute;
            } else {
                result = 0 + "" + hour + ":" + minute;
            }
        } else {
            if (minute < 10)  {
                result = hour + ":" + 0 + "" + minute;
            } else {
                result = hour + ":" + minute;
            }
        }
        return result;
    }

    private String getChartXAxisDayAndMonth(float timestamp) {
        Date date = new Date((long) timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        String result = "";
        if (day < 10) {
            if (month < 10) {
                result = 0 + "" + day + "/" + 0 + "" + month;
            } else {
                result = 0 + "" + day + "/" + month;
            }
        } else {
            if (month < 10) {
                result = day + "/" + 0 + "" + month;
            } else {
                result = day + "/" + month;
            }
        }
        return result;
    }

    private String getChartXAxisYears(float timestamp) {
        Date date = new Date((long) timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    private void setChartProgressBarVisible() {
        lineChart.setVisibility(View.GONE);
        chartProgressBar.setVisibility(View.VISIBLE);
    }

    private void setChartProgressBarInvisible() {
        chartProgressBar.setVisibility(View.GONE);
        lineChart.setVisibility(View.VISIBLE);
    }

    private void intentToCoinChoose() {
        Intent intent = new Intent(getActivity(), BuySellActivity.class);
        intent.putExtra("fromPortfolio", true);
        startActivityForResult(intent, BUY_SELL_ACTIVITY_CODE);
    }

    public void addId(String id) {
        deleteIds.add(id);
    }

    public void removeId(String id) {
        deleteIds.remove(id);
    }

    private void getCoinInfo() {
        Call<List<CoinMarket>> call = sortedCoinsApi.getCoinMarkets("usd", coinIds, "market_cap_desc", 50, 1, false, "24h");
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
                            String icon = coin.getImage();
                            String id = coin.getId();
                            double currentPrice = coin.getCurrent_price();
                            double change24h = coin.getPrice_change_24h();
                            double changePercentage24H = coin.getPrice_change_percentage_24h_in_currency();
                            double quantity = quantities.get(id);
                            double change = change24h * quantity;
                            PortfolioModel model = new PortfolioModel(id, shortCut, icon, quantity * currentPrice, change, changePercentage24H, currentPrice, quantity, false);
                            models.add(model);

                            portfolioValue += quantity * currentPrice;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                noCoin.setVisibility(View.GONE);
                                hasCoin.setVisibility(View.VISIBLE);
                                addCoin.setVisibility(View.VISIBLE);
                                totalPrice.setText(String.format("%s%s", currencySymbol, nf.format(portfolioValue)));
                                double priceDiff = portfolioValue - totalPrincipal;
                                double priceDiffPerc = priceDiff / totalPrincipal * 100;
                                String priceChangeText = String.format("%s%s (%%%s)", currencySymbol, nf.format(priceDiff), nf.format(priceDiffPerc));
                                totalPriceChange.setText(priceChangeText);
                                principal.setText(String.format("Ana para: %s%s", currencySymbol, nf.format(totalPrincipal)));

                                int red = Color.parseColor("#f6465d");
                                int green = Color.parseColor("#2ebd85");
                                int defaultColor = ContextCompat.getColor(getContext(), R.color.textColor);

                                totalPriceChange.setTextColor(priceDiff < 0 ? red : priceDiff > 0 ? green : defaultColor);
                                portfolioRecyclerAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        getCoinInfo();
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                getCoinInfo();
                            }
                        };
                        handler.postDelayed(runnable, 3000);
                    } else {
                        getCoinInfo();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                getCoinInfo();
            }
        });
    }

    private class GetChartInfo extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (yValues.size() == idList.size()) {

                List<Entry> values = new ArrayList<>();
                List<Entry> yValue = new ArrayList<>();
                int max = Integer.MAX_VALUE;

                for (int i = 0; i < yValues.size(); i++) {
                    int size = yValues.get(i).size();
                    if (size < max) {
                        max = size;
                        yValue = yValues.get(i);
                    }
                }

                for (int i = 0; i < yValue.size(); i++) {
                    float total = 0;
                    for (int j = 0; j < yValues.size(); j++) {
                        total += yValues.get(j).get(i).getY();
                    }
                    values.add(new Entry(yValue.get(i).getX(), total));
                }

                float first = values.get(0).getY();
                float last = values.get(values.size() - 1).getY();

                int red = Color.parseColor("#f6465d");
                int green = Color.parseColor("#2ebd85");
                int defaultColor = ContextCompat.getColor(getContext(), R.color.textColor);

                chartColor = first < last ? green : first > last ? red : defaultColor;

                setLineChart(values);
                yValues.clear();
            }
        }

        @Override
        protected Void doInBackground(String... strings) {

            String coinModelId = strings[0];

            List<Entry> yValue = new ArrayList<>();

            Call<JsonObject> call = chartInfoApi.getMarketChart(coinModelId, "usd", String.valueOf(from), String.valueOf(to));
            try {
                Response<JsonObject> response = call.execute();
                if (response.isSuccessful()) {
                    JsonObject result = response.body();
                    if (result != null && !result.isJsonNull() && result.size() != 0) {
                        JsonArray prices = (JsonArray) result.get("prices");
                        if (prices != null && !prices.isJsonNull()) {
                            for (int i = 0; i < prices.size(); i++) {
                                if (i % 4 == 0) {
                                    JsonArray priceValues = (JsonArray) prices.get(i);
                                    if (priceValues != null && !priceValues.isJsonNull()) {
                                        float timestamp = priceValues.get(0).getAsFloat();
                                        float price = priceValues.get(1).getAsFloat();
                                        float quantityCount = 0;
                                        for (int j = 0; j < memoryModels.size(); j++) {
                                            PortfolioMemoryModel model = memoryModels.get(j);
                                            if (model.getId().equals(coinModelId)) {
                                                float modelTimestamp = (float) (model.getTimestamp() - (1000 * 60 * 20));
                                                if (modelTimestamp < timestamp) {
                                                    if (model.getType().equals("buy"))
                                                        quantityCount += model.getQuantity();
                                                    else
                                                        quantityCount -= model.getQuantity();
                                                }
                                            }
                                        }

                                        if (quantityCount > 0) {
                                            yValue.add(new Entry(timestamp, quantityCount * price));
                                        } else {
                                            yValue.add(new Entry(timestamp, 0));
                                        }
                                    }
                                }
                            }

                            yValues.add(yValue);
                        }
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == 429) {
                                Handler handler = new Handler();
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        new GetChartInfo().execute(strings);
                                        cancel(true);
                                    }
                                };
                                handler.postDelayed(runnable, 3000);
                            } else {
                                new GetChartInfo().execute(strings);
                                cancel(true);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new GetChartInfo().execute(strings);
                    }
                });
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BUY_SELL_ACTIVITY_CODE && resultCode == BUY_SELL_ACTIVITY_CODE) {
            noCoin.setVisibility(View.GONE);
            readFromMemory();
        }

    }
}
