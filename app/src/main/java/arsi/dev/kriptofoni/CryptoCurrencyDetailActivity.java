package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Coins.CoinData.Links;
import com.litesoftwares.coingecko.domain.Coins.CoinFullData;
import com.litesoftwares.coingecko.domain.Coins.MarketData;
import com.litesoftwares.coingecko.domain.Shared.Market;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import arsi.dev.kriptofoni.Models.LineChartEntryModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinInfoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CryptoCurrencyDetailActivity extends AppCompatActivity{

    private String currencyText, coinModelId, chartType, twitterScreenName, webLink, redditLink, coinShortCut, time;
    private TextView value, oneHourChange, twentyFourHoursChange, sevenDaysChange,
            marketValue, twentyFourHoursMarketVolume, circulatingSupply, totalSupply, webSite,
            reddit, twitter, coinName, coinMarketCap, priceInBtc, priceChangeInBtc,
            currentPrice, currentChange, currentPriceChange, oneDay, oneWeek, oneMonth, threeMonths,
            sixMonths, oneYear, allTime;
    private CoinInfoApi coinGeckoApi;
    private Button buySell;
    private ImageView backButton, coinIcon, expand, chartIcon;
    private CountryCodePicker countryCodePicker;
    private String[] currencySymbols;
    private ScrollView scrollView;
    private ProgressBar progressBar, chartProgressBar;
    private long from, to;
    private TextView active;
    private ArrayList<LineChartEntryModel> lineChartEntryModels;
    private LineChart lineChart;
    private CandleStickChart candleStickChart;
    private RelativeLayout twitterRedirect, webRedirect, redditRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_currency_detail);

        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "usd");

        lineChartEntryModels = new ArrayList<>();

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
        webRedirect = findViewById(R.id.crypto_currency_detail_web_redirect);
        redditRedirect = findViewById(R.id.crypto_currency_detail_reddit_redirect);
        twitterRedirect = findViewById(R.id.crypto_currency_detail_twitter_redirect);
        chartProgressBar = findViewById(R.id.crypto_currency_detail_chart_progress_bar);

        // Initial chart values
        chartType = "line";
        time = "oneDay";
        active = oneDay;
        oneDay.setTextColor(Color.parseColor("#000000"));
        // Current time in seconds
        to = System.currentTimeMillis() / 1000;
        // 24 hours ago in seconds
        from = System.currentTimeMillis() / 1000 - (60 * 60 * 24);

        makeProgressBarVisible();

        Intent intent = getIntent();
        coinModelId = intent.getStringExtra("id");
        coinGeckoApi = CoinInfoRetrofitClient.getInstance().getMyCoinGeckoApi();

        // We use different threads to speed up data fetch.
        for (int i = 0; i < 3; i++) {
            Thread thread = new MyThread(i);
            thread.start();
        }

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
                startActivity(intent);
            }
        });

        oneDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneDay) {
                    oneDay.setTextColor(Color.parseColor("#000000"));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24);
                    active.setTextColor(Color.parseColor("#797676"));
                    active = oneDay;
                    time = "oneDay";
                    setChartProgressBarVisible();
                    getMarketChart();
                }
            }
        });

        oneWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneWeek) {
                    oneWeek.setTextColor(Color.parseColor("#000000"));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 7);
                    active.setTextColor(Color.parseColor("#797676"));
                    active = oneWeek;
                    time = "oneWeek";
                    setChartProgressBarVisible();
                    getMarketChart();
                }
            }
        });

        oneMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneMonth) {
                    oneMonth.setTextColor(Color.parseColor("#000000"));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 30);
                    active.setTextColor(Color.parseColor("#797676"));
                    active = oneMonth;
                    time = "oneMonth";
                    setChartProgressBarVisible();
                    getMarketChart();
                }
            }
        });

        threeMonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != threeMonths) {
                    threeMonths.setTextColor(Color.parseColor("#000000"));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 30 * 3);
                    active.setTextColor(Color.parseColor("#797676"));
                    active = threeMonths;
                    time = "threeMonths";
                    setChartProgressBarVisible();
                    getMarketChart();
                }
            }
        });

        sixMonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != sixMonths) {
                    sixMonths.setTextColor(Color.parseColor("#000000"));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 30 * 6);
                    active.setTextColor(Color.parseColor("#797676"));
                    active = sixMonths;
                    time = "sixMonths";
                    setChartProgressBarVisible();
                    getMarketChart();
                }
            }
        });

        oneYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != oneYear) {
                    oneYear.setTextColor(Color.parseColor("#000000"));
                    to = System.currentTimeMillis() / 1000;
                    from = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 365);
                    active.setTextColor(Color.parseColor("#797676"));
                    active = oneYear;
                    time = "oneYear";
                    setChartProgressBarVisible();
                    getMarketChart();
                }
            }
        });

        allTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active != allTime) {
                    allTime.setTextColor(Color.parseColor("#000000"));
                    from = 0;
                    to = System.currentTimeMillis() / 1000;
                    active.setTextColor(Color.parseColor("#797676"));
                    active = allTime;
                    time = "allTime";
                    setChartProgressBarVisible();
                    getMarketChart();
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
                    intent.putParcelableArrayListExtra("lineChartModels", lineChartEntryModels);
                    startActivity(intent);
                }
            }
        });

        chartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    private void candleStickChart() {
//        chartType = "candlestick";
//
//        candleStickChart.setDragEnabled(true);
//        candleStickChart.setScaleEnabled(false);
//
//        ArrayList<CandleEntry> ceList = new ArrayList<>();
//        ceList.add(new CandleEntry(0, 4.62f, 2.02f, 2.70f, 4.13f));
//        ceList.add(new CandleEntry(1, 5.50f, 2.70f, 3.35f, 4.96f));
//        ceList.add(new CandleEntry(2, 5.25f, 3.02f, 3.50f, 4.50f));
//        ceList.add(new CandleEntry(3, 6f,    3.25f, 4.40f, 5.0f));
//        ceList.add(new CandleEntry(4, 5.57f, 2f,    2.80f, 4.5f));
//
//        CandleDataSet cds = new CandleDataSet(ceList, "Entries");
//        cds.setColor(Color.rgb(80, 80, 80));
//        cds.setShadowColor(Color.DKGRAY);
//        cds.setShadowWidth(0.7f);
//        cds.setDecreasingColor(Color.RED);
//        cds.setDecreasingPaintStyle(Style.FILL);
//        cds.setIncreasingColor(Color.rgb(122, 242, 84));
//        cds.setIncreasingPaintStyle(Style.STROKE);
//        cds.setNeutralColor(Color.BLUE);
//        cds.setValueTextColor(Color.RED);
//        CandleData cd = new CandleData(cds);
//
//        candleStickChart.setData(cd);
//        candleStickChart.invalidate();
//        candleStickChart.setVisibility(View.GONE);
    }

    private void lineChart(ArrayList<Entry> yValue) {

        chartType = "line";

        if (lineChart.getData() != null) {
            lineChart.clearValues();
            lineChart.notifyDataSetChanged();
        }

        // Customizing chart appearance
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(null);

        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxisRight = lineChart.getAxisRight();
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

        LineDataSet set = new LineDataSet(yValue, "Prices");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(0f);
        set.setLineWidth(2f);
        set.setColor(Color.BLACK);
        set.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        LineData data = new LineData(dataSets);

        lineChart.setData(data);
        lineChart.notifyDataSetChanged();
        setChartProgressBarInvisible();
        lineChart.invalidate();
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

                    if (oneHours != null && !oneHours.isJsonNull())
                        oneHourChange.setTextColor(oneHours.getAsDouble() > 0 ? Color.GREEN : oneHours.getAsDouble() < 0 ? Color.RED : Color.parseColor("#797676"));
                    if (twentyFourHours != null && !twentyFourHours.isJsonNull()) {
                        twentyFourHoursChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? Color.GREEN : twentyFourHours.getAsDouble() < 0 ? Color.RED : Color.parseColor("#797676"));
                        currentPriceChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? Color.GREEN : twentyFourHours.getAsDouble() < 0 ? Color.RED : Color.parseColor("#797676"));
                        currentChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? Color.GREEN : twentyFourHours.getAsDouble() < 0 ? Color.RED : Color.parseColor("#797676"));
                    }
                    if (changePercentageInBtc != null && !changePercentageInBtc.isJsonNull())
                        priceChangeInBtc.setTextColor(changePercentageInBtc.getAsDouble() > 0 ? Color.GREEN : changePercentageInBtc.getAsDouble() < 0 ? Color.RED : Color.parseColor("#797676"));
                    if (sevenDays != null && !sevenDays.isJsonNull())
                        sevenDaysChange.setTextColor(sevenDays.getAsDouble() > 0 ? Color.GREEN : sevenDays.getAsDouble() < 0 ? Color.RED : Color.parseColor("#797676"));

                    makeProgressBarInvisible();
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
                        for (int i = 0; i < prices.size(); i++) {
                            if (i % 4 == 0) {
                                JsonArray priceValues = (JsonArray) prices.get(i);
                                float timestamp = priceValues.get(0).getAsFloat();
                                float price = priceValues.get(1).getAsFloat();
                                LineChartEntryModel model = new LineChartEntryModel(timestamp, price);
                                lineChartEntryModels.add(model);
                                yValue.add(new Entry(timestamp, price));
                            }
                        }
                        lineChart(yValue);
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
            }
        }
    }
}
