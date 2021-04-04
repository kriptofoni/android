package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinInfoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CryptoCurrencyDetailActivity extends AppCompatActivity{

    private String currencyText, coinModelId;
    private TextView value, oneHourChange, twentyFourHoursChange, sevenDaysChange,
            marketValue, twentyFourHoursMarketVolume, circulatingSupply, totalSupply, webSite,
            reddit, twitter, currency, coinName, coinMarketCap, priceInBtc, priceChangeInBtc,
            currentPrice, currentChange, currentPriceChange;
    private CoinInfoApi coinGeckoApi;
    private Button buySell;
    private ImageView backButton, coinIcon;
    private LineChart chart;
    private CandleStickChart candleStickChart;
    private CountryCodePicker countryCodePicker;
    private String[] currencySymbols;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_currency_detail);

        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "usd");

        coinIcon = findViewById(R.id.coinIcon);
        coinName = findViewById(R.id.coinName);
        coinMarketCap = findViewById(R.id.coinMarketCap);
        currency = findViewById(R.id.detail_currency);
        candleStickChart = findViewById(R.id.candleStickChart);
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
        chart = findViewById(R.id.chart);
        priceInBtc = findViewById(R.id.in_btc_value);
        priceChangeInBtc = findViewById(R.id.coin_price_in_btc_change);
        currentPrice = findViewById(R.id.currentPrice);
        currentChange = findViewById(R.id.change);
        currentPriceChange = findViewById(R.id.coin_price_change);
        scrollView = findViewById(R.id.crypto_currency_detail_scroll_view);
        progressBar = findViewById(R.id.crypto_currency_detail_progress_bar);

        makeProgressBarVisible();

        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));

        Intent intent = getIntent();

        coinModelId = intent.getStringExtra("id");
        coinGeckoApi = CoinInfoRetrofitClient.getInstance().getMyCoinGeckoApi();

        new GetCoinInfo().execute();
        new Get24HVol().execute();
        new GetMarketChart().execute();
        candleStickChart();

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
                startActivity(intent);
            }
        });
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

        candleStickChart.setDragEnabled(true);
        candleStickChart.setScaleEnabled(false);

        ArrayList<CandleEntry> ceList = new ArrayList<>();
        ceList.add(new CandleEntry(0, 4.62f, 2.02f, 2.70f, 4.13f));
        ceList.add(new CandleEntry(1, 5.50f, 2.70f, 3.35f, 4.96f));
        ceList.add(new CandleEntry(2, 5.25f, 3.02f, 3.50f, 4.50f));
        ceList.add(new CandleEntry(3, 6f,    3.25f, 4.40f, 5.0f));
        ceList.add(new CandleEntry(4, 5.57f, 2f,    2.80f, 4.5f));

        CandleDataSet cds = new CandleDataSet(ceList, "Entries");
        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setShadowWidth(0.7f);
        cds.setDecreasingColor(Color.RED);
        cds.setDecreasingPaintStyle(Style.FILL);
        cds.setIncreasingColor(Color.rgb(122, 242, 84));
        cds.setIncreasingPaintStyle(Style.STROKE);
        cds.setNeutralColor(Color.BLUE);
        cds.setValueTextColor(Color.RED);
        CandleData cd = new CandleData(cds);

        candleStickChart.setData(cd);
        candleStickChart.invalidate();
        candleStickChart.setVisibility(View.GONE);
    }

    private void lineChart(ArrayList<Entry> yValue){

        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);

        LineDataSet set1 = new LineDataSet(yValue,"Data Set 1");
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setValueTextSize(0f);
        set1.setLineWidth(2f);
        set1.setColor(Color.RED);

        set1.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

        chart.setData(data);
    }

    private class GetCoinInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Call<JsonObject> call = coinGeckoApi.getCoinInfo(coinModelId,"false",false,true,false,false,false);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject coin = response.body();
                        String currencySymbol = currencySymbols[1];
                        NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));

                        JsonObject image = (JsonObject) coin.get("image");
                        JsonElement thumb = (JsonElement) image.get("thumb");

                        JsonElement name = (JsonElement) coin.get("name");
                        JsonElement shortCut = (JsonElement) coin.get("symbol");
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
                        String a = webSiteArray.size() == 0 ? "-" : webSiteArray.get(0).getAsString();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                                webSite.setText(a);
                                reddit.setText(redditText.isJsonNull() ? "-" : redditText.getAsString());
                                twitter.setText(twitterUri);

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
                        });
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    doInBackground(voids);
                }
            });
            return null;
        }
    }

    private class Get24HVol extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                twentyFourHoursMarketVolume.setText(text);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    doInBackground(voids);
                }
            });
            return null;
        }
    }

    private class GetMarketChart extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Call<JsonObject> call = coinGeckoApi.getMarketChart(coinModelId, currencyText, "1617474683", "1617571894");
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject result = response.body();
                        JsonArray prices = (JsonArray) result.get("prices");
                        ArrayList<Entry> yValue = new ArrayList<>();
                        for (int i = 0; i < prices.size(); i++) {
                            if (i % 4 == 0) {
                                JsonArray priceValues = (JsonArray) prices.get(i);
                                float price = priceValues.get(1).getAsFloat();
                                yValue.add(new Entry(i, price));
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lineChart(yValue);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    doInBackground(voids);
                }
            });
            return null;
        }
    }
}