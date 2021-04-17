package arsi.dev.kriptofoni.Adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import arsi.dev.kriptofoni.CryptoCurrencyDetailActivity;
import arsi.dev.kriptofoni.Fragments.MainFragments.CoinsFragment;
import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.R;

public class MainCoinsRecyclerAdapter extends RecyclerView.Adapter<MainCoinsRecyclerAdapter.Holder> {

    private List<CoinModel> coins;
    private ViewGroup parent;
    private String currencySymbol = "";
    private String type;
    private Fragment fragment;

    public MainCoinsRecyclerAdapter(List<CoinModel> coins, Fragment fragment, String type) {
        this.coins = coins;
        this.fragment = fragment;
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
        Picasso.get().load(coin.getImageUri()).into(holder.icon);
        String name = coin.getName() + "\n#" + coin.getNumber() + " - " + coin.getShortCut().toUpperCase(Locale.ENGLISH);
        holder.name.setText(name);
        if (type.equals("24")) {
            holder.changeIn24Hours.setText(String.format("%%%.2f", coin.getChangeIn24Hours()));
            holder.changeIn24Hours.setTextColor(coin.getChangeIn24Hours() > 0 ? Color.GREEN : Color.RED);
            holder.priceChangeIn24Hours.setText(String.format("%s%.2f", currencySymbol, coin.getPriceChangeIn24Hours()));
            holder.priceChangeIn24Hours.setTextColor(coin.getChangeIn24Hours() > 0 ? Color.GREEN : Color.RED);
        } else {
            holder.changeIn24Hours.setText(String.format("%%%.2f", coin.getChangeIn7Days()));
            holder.changeIn24Hours.setTextColor(coin.getChangeIn7Days() > 0 ? Color.GREEN : Color.RED);
            holder.priceChangeIn24Hours.setText(String.format("%s%.2f", currencySymbol, coin.getPriceChangeIn7Days()));
            holder.priceChangeIn24Hours.setTextColor(coin.getChangeIn7Days() > 0 ? Color.GREEN : Color.RED);
        }
        holder.currentPrice.setText(String.format("%s%.2f", currencySymbol, coin.getCurrentPrice()));
        if (position % 2 == 1) holder.card.setBackgroundColor(Color.parseColor("#ededed"));
        else holder.card.setBackgroundColor(Color.parseColor("#ffffff"));
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(fragment.getActivity(), CryptoCurrencyDetailActivity.class);
                intent.putExtra("id", coin.getId());
                fragment.startActivity(intent);
            }
        });

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

        TextView name, changeIn24Hours, priceChangeIn24Hours, currentPrice;
        ImageView icon;
        Button buy;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.main_coins_card_coin_name);
            changeIn24Hours = itemView.findViewById(R.id.main_coins_card_change_percent_in_24_hours);
            priceChangeIn24Hours = itemView.findViewById(R.id.main_coins_card_change_in_24_hours);
            currentPrice = itemView.findViewById(R.id.main_coins_card_current_price);
            icon = itemView.findViewById(R.id.main_coins_card_icon);
            buy = itemView.findViewById(R.id.main_coins_card_buy);
            card = itemView.findViewById(R.id.main_coins_card);
        }
    }

    public void setCoins(List<CoinModel> coins) {
        this.coins = coins;
        notifyDataSetChanged();
    }

    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
        notifyDataSetChanged();
    }
}
