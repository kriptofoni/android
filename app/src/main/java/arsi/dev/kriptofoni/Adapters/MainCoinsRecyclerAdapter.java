package arsi.dev.kriptofoni.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import arsi.dev.kriptofoni.Fragments.MainFragments.CoinsFragment;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.R;

public class MainCoinsRecyclerAdapter extends RecyclerView.Adapter<MainCoinsRecyclerAdapter.Holder> {

    private ArrayList<CoinModel> coins;
    private ViewGroup parent;
    private CoinsFragment coinsFragment;
    private String currencySymbol = "";
    private String type;

    public MainCoinsRecyclerAdapter(ArrayList<CoinModel> coins, CoinsFragment coinsFragment, String type) {
        this.coins = coins;
        this.coinsFragment = coinsFragment;
        this.type = type;
    }

    public MainCoinsRecyclerAdapter(ArrayList<CoinModel> coins, String type) {
        this.coins = coins;
        this.type = type;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.main_coins_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CoinModel coin = coins.get(position);
        holder.number.setText(String.valueOf(coin.getNumber()));
        Picasso.get().load(coin.getImageUri()).into(holder.icon);
        String name = coin.getName() + " (" + coin.getShortCut().toUpperCase(Locale.ENGLISH) + ")";
        holder.name.setText(name);
        if (type.equals("24")) {
            holder.changeIn24Hours.setText(String.format("%%%.2f", coin.getChangeIn24Hours()));
            holder.changeIn24Hours.setTextColor(coin.getChangeIn24Hours() > 0 ? Color.GREEN : Color.RED);
            holder.priceChangeIn24Hours.setText(String.format("%s%.2f", currencySymbol, coin.getPriceChangeIn24Hours()));
            holder.priceChangeIn24Hours.setTextColor(coin.getChangeIn24Hours() > 0 ? Color.GREEN : Color.RED);
        } else {
            holder.changeIn24Hours.setText(String.format("%%%.2f", coin.getChangeIn7Days()));
            holder.changeIn24Hours.setTextColor(coin.getChangeIn7Days() > 0 ? Color.GREEN : Color.RED);
            holder.priceChangeIn24Hours.setText(String.format("%s%.2f", currencySymbol, coin.getChangeIn7Days()));
            holder.priceChangeIn24Hours.setTextColor(coin.getChangeIn7Days() > 0 ? Color.GREEN : Color.RED);
        }
        holder.currentPrice.setText(String.format("%s%.2f", currencySymbol, coin.getCurrentPrice()));


        // Testing buy button...
        Random random = new Random();
        if (random.nextBoolean()) holder.buy.setVisibility(View.VISIBLE);
        else holder.buy.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView number, name, changeIn24Hours, priceChangeIn24Hours, currentPrice;
        ImageView icon;
        Button buy;

        public Holder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.main_coins_number);
            name = itemView.findViewById(R.id.main_coins_coin_name);
            changeIn24Hours = itemView.findViewById(R.id.main_coins_change_percent_in_24_hours);
            priceChangeIn24Hours = itemView.findViewById(R.id.main_coins_change_in_24_hours);
            currentPrice = itemView.findViewById(R.id.main_coins_current_price);
            icon = itemView.findViewById(R.id.main_coins_icon);
            buy = itemView.findViewById(R.id.main_coins_buy);
        }
    }

    public void setCoins(ArrayList<CoinModel> coins) {
        this.coins = coins;
        notifyDataSetChanged();
    }

    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
        notifyDataSetChanged();
    }
}
