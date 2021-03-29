package arsi.dev.kriptofoni;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.litesoftwares.coingecko.CoinGeckoApiClient;
import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Fragments.MainFragment;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.Retrofit.Coin;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoApi;
import arsi.dev.kriptofoni.Retrofit.CoinGeckoRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAllCoinss extends AsyncTaskLoader<List<String>> {

    private String currency;
    private CoinGeckoApi myCoinGeckoApi;
    private ArrayList<String> names;
    private CoinGeckoApiClient client;
    private MainFragment mainFragment;
    private ArrayList<CoinSearchModel> coinSearchModels, mostInc24Hours;

    public GetAllCoinss(@NonNull Context context, String currency, CoinGeckoApi myCoinGeckoApi, MainFragment mainFragment) {
        super(context);
        this.currency = "usd";
        this.myCoinGeckoApi = CoinGeckoRetrofitClient.getInstance().getMyCoinGeckoApi();
        this.names = new ArrayList<>();
        this.client = new CoinGeckoApiClientImpl();
        this.mainFragment = mainFragment;
        this.coinSearchModels = new ArrayList<>();
        this.mostInc24Hours = new ArrayList<>();
    }

    @Nullable
    @Override
    public List<String> loadInBackground() {
        for (int i = 1; i <= 27; i++) {
            final int index = i;
            Call<List<CoinMarket>> call = myCoinGeckoApi.getCoinMarkets(currency, null,"id_asc", 250, i, false, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    if (response.isSuccessful()) {
                        List<CoinMarket> coins = response.body();
                        for (int j = 0; j < coins.size(); j++) {
                            CoinMarket coin = coins.get(j);
                            String name = coin.getName();
                            String id = coin.getId();
                            String symbol = coin.getSymbol();
                            String image = coin.getImage();
                            double priceChangeIn24h = coin.getPrice_change_percentage_24h_in_currency();
                            double priceChangeIn7d = coin.getPrice_change_percentage_7d_in_currency();
                            double marketCap = coin.getMarket_cap();
                            double marketCapRank = coin.getMarket_cap_rank();
                            CoinSearchModel model = new CoinSearchModel(marketCapRank, id, name, symbol, marketCap, image);
                            CoinSearchModel model24H = new CoinSearchModel((index - 1) * 250 + j + 1, id, name, symbol, image, priceChangeIn24h);
                            coinSearchModels.add(model);
                            mostInc24Hours.add(model24H);
                        }
                        mainFragment.setCoinModelsForSearch(coinSearchModels);
                        mainFragment.setMostIncIn24List(mostInc24Hours);
                    } else {
                        System.out.println(response.code());
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {

                }
            });
        }


//        Call<List<Coin>> coins = myCoinGeckoApi.getCoins(false);
//        coins.enqueue(new Callback<List<Coin>>() {
//            @Override
//            public void onResponse(Call<List<Coin>> call, Response<List<Coin>> response) {
//                if (response.isSuccessful()) {
//                    List<Coin> coinList = response.body();
//                    for (int i = 0; i < coinList.size(); i++) {
//                        if (!names.contains(coinList.get(i).getName())) names.add(coinList.get(i).getName());
//                    }
//                    System.out.println("83 " + names.size());
//                } else {
//                    System.out.println(response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Coin>> call, Throwable t) {
//
//            }
//        });

        return names;
    }
}
