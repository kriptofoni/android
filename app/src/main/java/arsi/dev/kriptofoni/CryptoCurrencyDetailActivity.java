package arsi.dev.kriptofoni;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Models.CandleStickChartEntryModel;
import arsi.dev.kriptofoni.Models.LineChartEntryModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CryptoCurrencyDetailActivity extends AppCompatActivity{

    private final int CURRENCY_CHOOSE_CODE = 1;
    private String currencyText, coinModelId, chartType, twitterScreenName, webLink,
            redditLink, coinShortCut, time;
    private TextView value, oneHourChange, twentyFourHoursChange, sevenDaysChange,
            marketValue, twentyFourHoursMarketVolume, circulatingSupply, totalSupply, webSite,
            reddit, twitter, coinName, coinMarketCap, priceInBtc, priceChangeInBtc,
            currentPrice, currentChange, currentPriceChange, oneDay, oneWeek, oneMonth, threeMonths,
            sixMonths, oneYear, allTime, currency;
    private CoinInfoApi coinGeckoApi;
    private Button buySell, addWatchingList;
    private ImageView backButton, coinIcon, expand, chartIcon;
    private CountryCodePicker countryCodePicker;
    private String[] currencySymbols;
    private ScrollView scrollView;
    private ProgressBar progressBar, chartProgressBar;
    private long from, to;
    private TextView active;
    private ArrayList<LineChartEntryModel> lineChartEntryModels;
    private ArrayList<CandleStickChartEntryModel> candleStickChartEntryModels;
    private ArrayList<String> timestamps;
    private LineChart lineChart;
    private CandleStickChart candleStickChart;
    private RelativeLayout twitterRedirect, webRedirect, redditRedirect;
    private int chartColor, days;
    private boolean firstFetch = true, inProgress = false;

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_currency_detail);

        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "usd");
        String watchingList = sharedPreferences.getString("watchingList", "");
        List<String> watchingCoins = new LinkedList<>(Arrays.asList(watchingList.split(",")));

        lineChartEntryModels = new ArrayList<>();
        candleStickChartEntryModels = new ArrayList<>();
        timestamps = new ArrayList<>();

        coinIcon = findViewById(R.id.coinIcon);
        coinName = findViewById(R.id.coinName);
        coinMarketCap = findViewById(R.id.coinMarketCap);
        backButton = findViewById(R.id.backButton);
        buySell = findViewById(R.id.addToProt);
        value = findViewById(R.id.value);
        oneHourChange = findViewById(R.id.oneHourChange);
        twentyFourHoursChange = findViewById(R.id.twentyFourHoursChange);
        sevenDaysChange = findViewById(R.id.sevenDaysChange);
        marketValue = findViewById(R.id.marketValue);
        twentyFourHoursMarketVolume = findViewById(R.id.twentyFoursHoursMarketVolume);
        circulatingSupply = findViewById(R.id.circulatingSupply);
        totalSupply = findViewById(R.id.totalSupply);
        webSite = findViewById(R.id.webSite);
        reddit = findViewById(R.id.reddit);
        twitter = findViewById(R.id.twitter);
        priceInBtc = findViewById(R.id.in_btc_value);
        priceChangeInBtc = findViewById(R.id.coin_price_in_btc_change);
        currentPrice = findViewById(R.id.currentPrice);
        currentChange = findViewById(R.id.change);
        currentPriceChange = findViewById(R.id.coin_price_change);
        scrollView = findViewById(R.id.crypto_currency_detail_scroll_view);
        progressBar = findViewById(R.id.crypto_currency_detail_progress_bar);
        oneDay = findViewById(R.id.cryto_currency_detail_24h);
        oneWeek = findViewById(R.id.cryto_currency_detail_1w);
        oneMonth = findViewById(R.id.cryto_currency_detail_1m);
        threeMonths = findViewById(R.id.cryto_currency_detail_3m);
        sixMonths = findViewById(R.id.cryto_currency_detail_6m);
        oneYear = findViewById(R.id.cryto_currency_detail_1y);
        allTime = findViewById(R.id.cryto_currency_detail_all);
        expand = findViewById(R.id.crypto_currency_detail_expand);
        chartIcon = findViewById(R.id.crypto_currency_detail_chart_icon);
        lineChart = findViewById(R.id.chart);
        candleStickChart = findViewById(R.id.candleStickChart);
        webRedirect = findViewById(R.id.crypto_currency_detail_web_redirect);
        redditRedirect = findViewById(R.id.crypto_currency_detail_reddit_redirect);
        twitterRedirect = findViewById(R.id.crypto_currency_detail_twitter_redirect);
        chartProgressBar = findViewById(R.id.crypto_currency_detail_chart_progress_bar);
        addWatchingList = findViewById(R.id.addWatchingList);
        currency = findViewById(R.id.crypto_currency_detail_currency);

        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));

        // Initial chart values
        chartType = "line";
        time = "oneDay";
        active = oneDay;
        oneDay.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
        oneDay.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
        // Current time in seconds
        to = System.currentTimeMillis() / 1000;
        // 24 hours ago in seconds
        from = to - (60 * 60 * 24);
        days = 1;

        makeProgressBarVisible();

        Intent intent = getIntent();
        coinModelId = intent.getStringExtra("id");
        coinGeckoApi = CoinInfoRetrofitClient.getInstance().getMyCoinGeckoApi();

        fetchData();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!inProgress) {
                    getCoinInfo();
                }
                handler.postDelayed(this, 10000);
            }
        };

        countryCodePicker = new CountryCodePicker();
        currencySymbols = countryCodePicker.getCountryCode(currencyText);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buySell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CryptoCurrencyDetailActivity.this,BuySellActivity.class);
                intent.putExtra("shortCut", coinShortCut);
                intent.putExtra("id", coinModelId);
                startActivity(intent);
            }
        });

        addWatchingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (watchingCoins.contains(coinModelId)) {
                    Toast.makeText(CryptoCurrencyDetailActivity.this, "Kripto para zaten izleme listenizde mevcut", Toast.LENGTH_LONG).show();
                } else {
                    // Since watchingList is a final variable,
                    // it is copied to another variable in order to make changes on it.
                    String copy = watchingList;
                    // Updating string to be written to Shared Preferences
                    copy += coinModelId + ",";
                    // If the add button is pressed without leaving the page,
                    // the coin id is added to the watchingCoins in order not to add the coin again.
                    watchingCoins.add(coinModelId);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("watchingList", copy);
                    editor.apply();

                    Toast.makeText(CryptoCurrencyDetailActivity.this, "Kripto para izleme listenize eklendi.", Toast.LENGTH_LONG).show();
                }
            }
        });

        oneDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneDay) {
                    oneDay.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
                    oneDay.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24);
                    days = 1;
                    active.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColor));
                    active = oneDay;
                    time = "oneDay";
                    setChartProgressBarVisible();
                    getMarketChart();
                    getOHLC();
                }
            }
        });

        oneWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneWeek) {
                    oneWeek.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
                    oneWeek.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 7);
                    days = 7;
                    active.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColor));
                    active = oneWeek;
                    time = "oneWeek";
                    setChartProgressBarVisible();
                    getMarketChart();
                    getOHLC();
                }
            }
        });

        oneMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneMonth) {
                    oneMonth.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
                    oneMonth.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 30);
                    days = 30;
                    active.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColor));
                    active = oneMonth;
                    time = "oneMonth";
                    setChartProgressBarVisible();
                    getMarketChart();
                    getOHLC();
                }
            }
        });

        threeMonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != threeMonths) {
                    threeMonths.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
                    threeMonths.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 30 * 3);
                    days = 90;
                    active.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColor));
                    active = threeMonths;
                    time = "threeMonths";
                    setChartProgressBarVisible();
                    getMarketChart();
                    getOHLC();
                }
            }
        });

        sixMonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != sixMonths) {
                    sixMonths.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
                    sixMonths.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 30 * 6);
                    days = 180;
                    active.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColor));
                    active = sixMonths;
                    time = "sixMonths";
                    setChartProgressBarVisible();
                    getMarketChart();
                    getOHLC();
                }
            }
        });

        oneYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneYear) {
                    oneYear.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
                    oneYear.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 365);
                    days = 365;
                    active.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColor));
                    active = oneYear;
                    time = "oneYear";
                    setChartProgressBarVisible();
                    getMarketChart();
                    getOHLC();
                }
            }
        });

        allTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != allTime) {
                    allTime.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
                    allTime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
                    from = 0;
                    to = System.currentTimeMillis() / 1000;
                    active.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bodyColor));
                    active.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColor));
                    active = allTime;
                    time = "allTime";
                    setChartProgressBarVisible();
                    getMarketChart();
                    getOHLC();
                }
            }
        });

        expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chartProgressBar.getVisibility() == View.GONE) {
                    Intent intent = new Intent(CryptoCurrencyDetailActivity.this, FullScreenChartActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("type", chartType);
                    intent.putExtra("color", chartColor);
                    intent.putParcelableArrayListExtra("lineChartModels", lineChartEntryModels);
                    intent.putParcelableArrayListExtra("candleStickChartModels", candleStickChartEntryModels);
                    intent.putStringArrayListExtra("timestamps", timestamps);
                    startActivity(intent);
                }
            }
        });

        chartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chartType.equals("line")) {
                    chartType = "candleStick";
                    lineChart.setVisibility(View.GONE);
                    candleStickChart.setVisibility(View.VISIBLE);
                    chartIcon.setImageResource(R.drawable.ic_linechart);
                } else {
                    chartType = "line";
                    candleStickChart.setVisibility(View.GONE);
                    lineChart.setVisibility(View.VISIBLE);
                    chartIcon.setImageResource(R.drawable.ic_candlestickchart);
                }
            }
        });

        webRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webLink));
                startActivity(intent);
            }
        });

        redditRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                try {
                    // Get the Reddit app if possible
                    getPackageManager().getPackageInfo("com.reddit.frontpage", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redditLink));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception ex) {
                    // No twitter app, revert to browser
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redditLink));
                }
                startActivity(intent);
            }
        });

        twitterRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                try {
                    // Get the Twitter app if possible
                    getPackageManager().getPackageInfo("com.twitter.android", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitterScreenName));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception ex) {
                    // No twitter app, revert to browser
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + twitterScreenName));
                }
                startActivity(intent);
            }
        });

        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CryptoCurrencyDetailActivity.this, CurrencyChooseActivity.class);
                intent.putExtra("converter", true);
                startActivityForResult(intent, CURRENCY_CHOOSE_CODE);
            }
        });
    }

    private void fetchData() {
        inProgress = true;
        // We use different threads to speed up data fetch.
        for (int i = 0; i < 4; i++) {
            Thread thread = new MyThread(i);
            thread.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstFetch)
            handler.postDelayed(runnable, 10000);
    }

    private void setChartProgressBarVisible() {
        if (chartType.equals("line")) lineChart.setVisibility(View.GONE);
        else candleStickChart.setVisibility(View.GONE);
        chartProgressBar.setVisibility(View.VISIBLE);
    }

    private void setChartProgressBarInvisible() {
        chartProgressBar.setVisibility(View.GONE);
        if (chartType.equals("line")) lineChart.setVisibility(View.VISIBLE);
        else candleStickChart.setVisibility(View.VISIBLE);
    }

    private void makeProgressBarVisible() {
        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void makeProgressBarInvisible() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    private void setCandleStickChart(List<CandleEntry> candleEntries) {

        int red = Color.parseColor("#f6465d");
        int green = Color.parseColor("#2ebd85");
        int defaultColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

        candleStickChart.setDragEnabled(true);
        candleStickChart.setScaleEnabled(true);
        candleStickChart.setDescription(null);
        candleStickChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                    }
                }

                return false;
            }
        });

        XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setTextColor(defaultColor);
        YAxis yAxisRight = candleStickChart.getAxisRight();
        candleStickChart.getAxisLeft().setTextColor(defaultColor);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String result = "";
                int v = (int) value;
                if (v < timestamps.size()) {
                    if (active == oneDay) {
                        result = getChartXAxisHourAndMinute(Float.parseFloat(timestamps.get(v)));
                    } else if (active == allTime) {
                        result = getChartXAxisYears(Float.parseFloat(timestamps.get(v)));
                    } else {
                        result = getChartXAxisDayAndMonth(Float.parseFloat(timestamps.get(v)));
                    }
                }
                return result;
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxisRight.setEnabled(false);
        candleStickChart.getLegend().setEnabled(false);

        CandleDataSet cds = new CandleDataSet(candleEntries, "Fiyatlar");
        cds.setAxisDependency(YAxis.AxisDependency.LEFT);
        cds.setShadowWidth(0.8f);
        cds.setBarSpace(0.15f);
        cds.setShadowColorSameAsCandle(true);
        cds.setDecreasingColor(red);
        cds.setDecreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        cds.setIncreasingColor(green);
        cds.setIncreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        cds.setNeutralColor(defaultColor);
        cds.setDrawValues(false);

        CandleData cd = new CandleData();
        if (!candleEntries.isEmpty()) {
            cd.addDataSet(cds);
        }

        candleStickChart.setData(cd);
        candleStickChart.notifyDataSetChanged();
        candleStickChart.invalidate();
    }

    private void lineChart(ArrayList<Entry> yValue) {

        int textColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

        // Customizing chart appearance
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(null);
        lineChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                    }
                }

                return false;
            }
        });

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
                } else if (active == allTime) {
                    result = getChartXAxisYears(value);
                } else {
                    result = getChartXAxisDayAndMonth(value);
                }
                return result;
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxisRight.setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        LineDataSet set = new LineDataSet(yValue, "Fiyatlar");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(0f);
        set.setLineWidth(2f);
        set.setFillAlpha(170);
        set.setDrawFilled(true);
        set.setColor(chartColor);
        set.setFillColor(chartColor);
        set.setDrawValues(false);

        LineData data = new LineData();
        if (!yValue.isEmpty()) {
            data.addDataSet(set);
        }

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

    private void getCoinInfo() {
        Call<JsonObject> call = coinGeckoApi.getCoinInfo(coinModelId,"false",false,true,false,false,false);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject coin = response.body();
                    String currencySymbol = currencySymbols[1];
                    NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));

                    JsonObject image = (JsonObject) coin.get("image");
                    JsonElement thumb = (JsonElement) image.get("large");

                    JsonElement name = (JsonElement) coin.get("name");
                    JsonElement shortCut = (JsonElement) coin.get("symbol");
                    coinShortCut = shortCut.isJsonNull() ? "" : shortCut.getAsString();
                    String nameText = name.getAsString() + " " + shortCut.getAsString().toUpperCase(Locale.ENGLISH);

                    JsonObject marketData = (JsonObject) coin.get("market_data");
                    JsonObject marketCap = (JsonObject) marketData.get("market_cap");
                    JsonElement marketCapInCurrency = marketCap.get(currencyText);
                    BigDecimal marketCapDouble = marketCapInCurrency.getAsBigDecimal();
                    String marketCapText = currencySymbol + " " + nf.format(marketCapDouble);

                    JsonObject price = (JsonObject) marketData.get("current_price");
                    JsonElement currentPriceVal = price.get(currencyText);
                    String currentPriceText = currencySymbol + " " + nf.format(currentPriceVal.getAsBigDecimal());

                    JsonElement currentBtcPrice = price.get("btc");
                    String currentBtcPriceText = currentBtcPrice.isJsonNull() ? "-" : "₿ " + nf.format(currentBtcPrice.getAsBigDecimal());

                    JsonObject changeOneObject = (JsonObject) marketData.get("price_change_percentage_1h_in_currency");
                    JsonElement oneHours = changeOneObject.isJsonNull() ? null : changeOneObject.get(currencyText);

                    JsonObject changeTwentyObject = (JsonObject) marketData.get("price_change_percentage_24h_in_currency");
                    JsonElement twentyFourHours = changeTwentyObject.isJsonNull() ? null : changeTwentyObject.get(currencyText);
                    JsonElement changePercentageInBtc = changeTwentyObject.isJsonNull() ? null : changeTwentyObject.get("btc");

                    JsonObject changeSevenDaysObject = (JsonObject) marketData.get("price_change_percentage_7d_in_currency");
                    JsonElement sevenDays = changeSevenDaysObject.isJsonNull() ? null : changeSevenDaysObject.get(currencyText);

                    JsonElement circulatingSupplyData = marketData.get("circulating_supply");
                    String circulatingSupplyText;

                    if (circulatingSupplyData.isJsonNull()) {
                        circulatingSupplyText = "-";
                    } else {
                        circulatingSupplyText = nf.format(circulatingSupplyData.getAsBigDecimal());
                    }

                    JsonElement totalSupplyData = marketData.get("total_supply");
                    String totalSupplyText;

                    if (totalSupplyData.isJsonNull()) {
                        totalSupplyText = "-";
                    } else {
                        totalSupplyText = nf.format(totalSupplyData.getAsBigDecimal());
                    }

                    JsonObject links = (JsonObject) coin.get("links");
                    JsonElement webSiteData = links.get("homepage");
                    JsonElement redditText = links.get("subreddit_url");
                    JsonElement twitterText = links.get("twitter_screen_name");
                    String twitterUri = twitterText.isJsonNull() ? "-" : "https://www.twitter.com/" + twitterText.getAsString();
                    JsonArray webSiteArray = webSiteData.isJsonNull() ? new JsonArray() : webSiteData.getAsJsonArray();
                    webLink =  webSiteArray.size() == 0 ? "" : webSiteArray.get(0).getAsString();
                    twitterScreenName = twitterText.isJsonNull() ? "" : twitterText.getAsString();
                    redditLink = redditText.isJsonNull() ? "" : redditText.getAsString();
                    if (webLink.isEmpty()) webRedirect.setVisibility(View.GONE);
                    if (twitterScreenName.isEmpty()) twitterRedirect.setVisibility(View.GONE);
                    if (redditLink.isEmpty()) redditRedirect.setVisibility(View.GONE);

                    Picasso.get().load(thumb.getAsString()).into(coinIcon);
                    coinName.setText(nameText);
                    coinMarketCap.setText(marketCapText);
                    marketValue.setText(marketCapText);
                    value.setText(currentPriceText);
                    currentPrice.setText(currentPriceText);
                    priceInBtc.setText(currentBtcPriceText);
                    oneHourChange.setText(oneHours == null || oneHours.isJsonNull()  ? "-" : "%" + oneHours.getAsDouble());
                    sevenDaysChange.setText(sevenDays == null || sevenDays.isJsonNull() ? "-" : "%" +sevenDays.getAsDouble());
                    twentyFourHoursChange.setText(twentyFourHours == null || twentyFourHours.isJsonNull() ? "-" : "%" + twentyFourHours.getAsDouble());
                    currentPriceChange.setText(twentyFourHours == null || twentyFourHours.isJsonNull() ? "-" : "%" + twentyFourHours.getAsDouble());
                    currentChange.setText(twentyFourHours == null || twentyFourHours.isJsonNull() ? "-" : "%" + twentyFourHours.getAsDouble());
                    priceChangeInBtc.setText(changePercentageInBtc == null || changePercentageInBtc.isJsonNull() ? "-" : "%" + changePercentageInBtc.getAsDouble());
                    circulatingSupply.setText(circulatingSupplyText);
                    totalSupply.setText(totalSupplyText);

                    int red = Color.parseColor("#f6465d");
                    int green = Color.parseColor("#2ebd85");
                    int defaultColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

                    if (oneHours != null && !oneHours.isJsonNull())
                        oneHourChange.setTextColor(oneHours.getAsDouble() > 0 ? green : oneHours.getAsDouble() < 0 ? red : defaultColor);
                    if (twentyFourHours != null && !twentyFourHours.isJsonNull()) {
                        twentyFourHoursChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? green : twentyFourHours.getAsDouble() < 0 ? red : defaultColor);
                        currentPriceChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? green : twentyFourHours.getAsDouble() < 0 ? red : defaultColor);
                        currentChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? green : twentyFourHours.getAsDouble() < 0 ? red : defaultColor);
                    }
                    if (changePercentageInBtc != null && !changePercentageInBtc.isJsonNull())
                        priceChangeInBtc.setTextColor(changePercentageInBtc.getAsDouble() > 0 ? green : changePercentageInBtc.getAsDouble() < 0 ? red : defaultColor);
                    if (sevenDays != null && !sevenDays.isJsonNull())
                        sevenDaysChange.setTextColor(sevenDays.getAsDouble() > 0 ? green : sevenDays.getAsDouble() < 0 ? red : defaultColor);

                    makeProgressBarInvisible();
                    if (firstFetch) {
                        handler.postDelayed(runnable, 10000);
                        firstFetch = false;
                    }
                    if (inProgress) inProgress = false;

                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                getCoinInfo();
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        getCoinInfo();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                getCoinInfo();
            }
        });
    }

    private void get24HVol() {
        Call<JsonObject> call = coinGeckoApi.getCoinSimple(coinModelId, currencyText, "true");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));

                    JsonObject coin = (JsonObject) response.body().get(coinModelId);
                    String currencySymbol = currencySymbols[1];
                    String element = currencyText + "_24h_vol";
                    JsonElement vol24H = coin.isJsonNull() ? null : coin.get(element);
                    String text = vol24H == null || vol24H.isJsonNull() ? "-" : currencySymbol + " " + nf.format(vol24H.getAsBigDecimal());

                    twentyFourHoursMarketVolume.setText(text);
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                get24HVol();
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        get24HVol();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                get24HVol();
            }
        });
    }

    private void getMarketChart() {
        lineChartEntryModels.clear();
        Call<JsonObject> call = coinGeckoApi.getMarketChart(coinModelId, currencyText, String.valueOf(from), String.valueOf(to));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject result = response.body();
                    if (result != null && !result.isJsonNull() && result.size() != 0) {
                        JsonArray prices = (JsonArray) result.get("prices");
                        ArrayList<Entry> yValue = new ArrayList<>();
                        float firstPrice = 0, lastPrice = 0;
                        if (prices != null && !prices.isJsonNull()) {
                            for (int i = 0; i < prices.size(); i++) {
                                if (i == 0) {
                                    JsonArray priceValues = (JsonArray) prices.get(i);
                                    if (priceValues != null && !priceValues.isJsonNull())
                                        firstPrice = priceValues.get(1).getAsFloat();
                                } else if (i == prices.size() - 1) {
                                    JsonArray priceValues = (JsonArray) prices.get(i);
                                    if (priceValues != null && !priceValues.isJsonNull())
                                        lastPrice = priceValues.get(1).getAsFloat();
                                }
                                if (i % 4 == 0) {
                                    JsonArray priceValues = (JsonArray) prices.get(i);
                                    if (priceValues != null && !priceValues.isJsonNull()) {
                                        float timestamp = priceValues.get(0).getAsFloat();
                                        float price = priceValues.get(1).getAsFloat();
                                        LineChartEntryModel model = new LineChartEntryModel(timestamp, price);
                                        lineChartEntryModels.add(model);
                                        yValue.add(new Entry(timestamp, price));
                                    }
                                }
                            }

                            int red = Color.parseColor("#f6465d");
                            int green = Color.parseColor("#2ebd85");
                            int defaultColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

                            chartColor = firstPrice < lastPrice ? green : firstPrice > lastPrice ? red : defaultColor;
                            lineChart(yValue);
                        }
                    } else {
                        getMarketChart();
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                getMarketChart();
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        getMarketChart();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                getMarketChart();
            }
        });
    }

    private void getOHLC() {
        candleStickChartEntryModels.clear();
        timestamps.clear();
        Call<JsonArray> call;
        if (active == allTime)
            call = coinGeckoApi.getOHLC(coinModelId, currencyText, "max");
        else
            call = coinGeckoApi.getOHLC(coinModelId, currencyText, days);

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    JsonArray body = response.body();
                    if (body != null) {
                        List<CandleEntry> candleEntries = new ArrayList<>();
                        for (int i = 0; i < body.size(); i++) {
                            JsonArray values = body.get(i).getAsJsonArray();
                            float timestamp = values.get(0).getAsFloat();
                            float open = values.get(1).getAsFloat();
                            float high = values.get(2).getAsFloat();
                            float low = values.get(3).getAsFloat();
                            float close = values.get(4).getAsFloat();

                            CandleStickChartEntryModel model = new CandleStickChartEntryModel(i, high, low, open, close);
                            candleStickChartEntryModels.add(model);
                            timestamps.add(String.valueOf(timestamp));
                            candleEntries.add(new CandleEntry(i, high, low, open, close));
                        }

                        setCandleStickChart(candleEntries);

                    } else {
                        getOHLC();
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                getOHLC();
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        getOHLC();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                getOHLC();
            }
        });

    }

    private class MyThread extends Thread {

        private int type;

        public MyThread(int type) {
            this.type = type;
        }

        @Override
        public void run() {
            super.run();
            switch (type) {
                case 0:
                    getCoinInfo();
                    break;
                case 1:
                    get24HVol();
                    break;
                case 2:
                    getMarketChart();
                    break;
                case 3:
                    getOHLC();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CURRENCY_CHOOSE_CODE && requestCode == CURRENCY_CHOOSE_CODE) {
            if (data != null) {
                currencyText = data.getStringExtra("currencyId");
                currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
                currencySymbols = countryCodePicker.getCountryCode(currencyText);
                makeProgressBarVisible();
                setChartProgressBarVisible();
                fetchData();
            }
        }
    }
}
