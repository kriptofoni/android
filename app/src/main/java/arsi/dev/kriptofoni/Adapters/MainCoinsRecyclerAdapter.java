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

import com.litesoftwares.coingecko.domain.Coins.CoinMarkets;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import arsi.dev.kriptofoni.Models.CoinModel;
import arsi.dev.kriptofoni.R;

public class MainCoinsRecyclerAdapter extends RecyclerView.Adapter<MainCoinsRecyclerAdapter.Holder> {

    private ArrayList<CoinModel> coins;
    private ViewGroup parent;

    public MainCoinsRecyclerAdapter(ArrayList<CoinModel> coins) {
        this.coins = coins;
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
        holder.name.setText(coin.getName());
        holder.shortCut.setText(coin.getShortCut().toUpperCase(new Locale("en","US")));
        holder.changeIn24Hours.setText(String.valueOf(coin.getChangeIn24Hours()));
        holder.changeIn24Hours.setTextColor(coin.getChangeIn24Hours() > 0 ? Color.GREEN : Color.RED);
        holder.changeIn7Days.setText(String.valueOf(coin.getChangeIn7Days()));
        holder.changeIn24Hours.setTextColor(coin.getChangeIn7Days() > 0 ? Color.GREEN : Color.RED);
        holder.currentPrice.setText(String.valueOf(coin.getCurrentPrice()));
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView number, name, shortCut, changeIn24Hours, changeIn7Days, currentPrice;
        ImageView icon;
        Button buy;

        public Holder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.main_coins_number);
            name = itemView.findViewById(R.id.main_coins_coin_name);
            shortCut = itemView.findViewById(R.id.main_coins_coin_shortcut);
            changeIn24Hours = itemView.findViewById(R.id.main_coins_change_in_24_hours);
            changeIn7Days = itemView.findViewById(R.id.main_coins_change_in_7_days);
            currentPrice = itemView.findViewById(R.id.main_coins_current_price);
            icon = itemView.findViewById(R.id.main_coins_icon);
            buy = itemView.findViewById(R.id.main_coins_buy);
        }
    }
}
