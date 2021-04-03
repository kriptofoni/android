package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CryptoCurrencyDetailActivity extends AppCompatActivity{

    private String currencyText, coinModelId;
    private TextView value, oneHourChange, twentyFourHoursChange, sevenDaysChange,
            marketValue, twentyFourHoursMarketVolume, circulatingSupply, totalSupply, webSite,
            reddit, twitter, currency, coinName, coinMarketCap, priceInBtc, priceChangeInBtc,
            currentPrice, currentChange, currentPriceChange, currentChangeInBtc;
    private CoinGeckoApi coinGeckoApi;
    private Button buySell;
    private ImageView backButton, coinIcon;
    private LineChart chart;
    private CandleStickChart candleStickChart;
    private CountryCodePicker countryCodePicker;
    private String[] currencySymbols;

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
        currentChangeInBtc = findViewById(R.id.coin_price_in_btc_change);

        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));

        Intent intent = getIntent();

        coinModelId = intent.getStringExtra("id");
        coinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();

        getDataForModel();
        lineChart();
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
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.rgb(122, 242, 84));
        cds.setIncreasingPaintStyle(Paint.Style.STROKE);
        cds.setNeutralColor(Color.BLUE);
        cds.setValueTextColor(Color.RED);
        CandleData cd = new CandleData(cds);

        candleStickChart.setData(cd);
        candleStickChart.invalidate();
        candleStickChart.setVisibility(View.GONE);
    }

    private void lineChart(){

        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);

        ArrayList<Entry> yValue = new ArrayList<>();

        yValue.add(new Entry(0,60f));
        yValue.add(new Entry(2,50f));
        yValue.add(new Entry(3,40f));
        yValue.add(new Entry(4,30f));
        yValue.add(new Entry(5,20f));
        yValue.add(new Entry(6,10f));
        yValue.add(new Entry(7,80f));
        yValue.add(new Entry(8,90f));
        yValue.add(new Entry(9,100f));
        yValue.add(new Entry(10,110f));
        yValue.add(new Entry(11,120f));
        yValue.add(new Entry(12,130f));
        yValue.add(new Entry(13,140f));
        yValue.add(new Entry(14,150f));
        yValue.add(new Entry(15,160f));
        yValue.add(new Entry(16,170f));
        yValue.add(new Entry(17,180f));

        LineDataSet set1 = new LineDataSet(yValue,"Data Set 1");

        set1.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

        chart.setData(data);
        chart.setVisibility(View.GONE);
    }

    private void getDataForModel(){
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
                    System.out.println(image + " , " + thumb);
                    Picasso.get().load(thumb.getAsString()).into(coinIcon);

                    JsonElement name = (JsonElement) coin.get("name");
                    JsonElement shortCut = (JsonElement) coin.get("symbol");
                    String nameText = name.getAsString() + " " + shortCut.getAsString().toUpperCase(Locale.ENGLISH);
                    coinName.setText(nameText);

                    JsonObject marketData = (JsonObject) coin.get("market_data");
                    JsonObject marketCap = (JsonObject) marketData.get("market_cap");
                    JsonElement marketCapInCurrency = marketCap.get(currencyText);
                    BigDecimal marketCapDouble = marketCapInCurrency.getAsBigDecimal();
                    String marketCapText = currencySymbol + " " + nf.format(marketCapDouble);
                    coinMarketCap.setText(marketCapText);
                    marketValue.setText(marketCapText);

                    JsonObject price = (JsonObject) marketData.get("current_price");
                    JsonElement currentPriceVal = price.get(currencyText);
                    String currentPriceText = currencySymbol + " " + nf.format(currentPriceVal.getAsBigDecimal());
                    value.setText(currentPriceText);
                    currentPrice.setText(currentPriceText);

                    JsonElement currentBtcPrice = price.get("btc");
                    priceInBtc.setText(String.valueOf(currentBtcPrice.getAsDouble()));

                    JsonObject changeOneObject = (JsonObject) marketData.get("price_change_percentage_1h_in_currency");
                    JsonElement oneHours = changeOneObject.get(currencyText);
                    oneHourChange.setText(String.valueOf("%" +oneHours.getAsDouble()));
                    oneHourChange.setTextColor(oneHours.getAsDouble() > 0 ? Color.GREEN : Color.RED);

                    JsonObject changeTwentyObject = (JsonObject) marketData.get("price_change_percentage_24h_in_currency");
                    JsonElement twentyFourHours = changeTwentyObject.get(currencyText);
                    JsonElement changePercentageInBtc = changeTwentyObject.get("btc");
                    twentyFourHoursChange.setText(String.valueOf("%" + twentyFourHours.getAsDouble()));
                    currentPriceChange.setText(String.valueOf("%" + twentyFourHours.getAsDouble()));
                    currentChange.setText(String.valueOf("%" + twentyFourHours.getAsDouble()));
                    currentChangeInBtc.setText(String.valueOf("%" + changePercentageInBtc.getAsDouble()));
                    twentyFourHoursChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? Color.GREEN : Color.RED);
                    currentPriceChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? Color.GREEN : Color.RED);
                    currentChange.setTextColor(twentyFourHours.getAsDouble() > 0 ? Color.GREEN : Color.RED);
                    currentChangeInBtc.setTextColor(twentyFourHours.getAsDouble() > 0 ? Color.GREEN : Color.RED);

                    JsonObject changeSevenDaysObject = (JsonObject) marketData.get("price_change_percentage_7d_in_currency");
                    JsonElement sevenDays = changeSevenDaysObject.get(currencyText);
                    sevenDaysChange.setText(String.valueOf("%" +sevenDays.getAsDouble()));
                    sevenDaysChange.setTextColor(sevenDays.getAsDouble() > 0 ? Color.GREEN : Color.RED);

                    JsonElement twentyFourHoursMarketCapData = marketData.get("market_cap_change_percentage_24h");
                    twentyFourHoursMarketVolume.setText(String.valueOf(twentyFourHoursMarketCapData.getAsDouble()));

                    JsonElement circulatingSupplyData = marketData.get("circulating_supply");
                    String circulatingSupplyText = nf.format(circulatingSupplyData.getAsBigDecimal());
                    circulatingSupply.setText(circulatingSupplyText);

                    JsonElement totalSupplyData = marketData.get("total_supply");
                    String totalSupplyText = nf.format(totalSupplyData.getAsBigDecimal());
                    totalSupply.setText(totalSupplyText);

                    JsonObject links = (JsonObject) coin.get("links");
                    JsonElement webSiteData = links.get("homepage");
                    JsonElement redditText = links.get("subreddit_url");
                    JsonElement twitterText = links.get("twitter_screen_name");
                    String twitterUri = "https://www.twitter.com/" + twitterText.getAsString();
                    JsonArray webSiteArray = webSiteData.getAsJsonArray();
                    String a = webSiteArray.get(0).getAsString();
                    webSite.setText(a);
                    reddit.setText(redditText.getAsString());
                    twitter.setText(twitterUri);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                getDataForModel();
            }
        });
    }
}