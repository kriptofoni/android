package arsi.dev.kriptofoni.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.R;

public class MainCoinsSearchRecyclerAdapter extends RecyclerView.Adapter<MainCoinsSearchRecyclerAdapter.Holder> {

    private List<CoinSearchModel> coins;

    public MainCoinsSearchRecyclerAdapter(List<CoinSearchModel> coins) {
        this.coins = coins;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.main_coins_search_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CoinSearchModel coin = coins.get(position);
        if ((int) coin.getMarketCapRank() != 10000) {
            holder.number.setText(String.valueOf((int) coin.getMarketCapRank()));
        } else {
            holder.number.setText("");
        }

        holder.name.setText(coin.getName());
        Picasso.get().load(coin.getImage()).into(holder.icon);
        if (position % 2 == 1) holder.card.setBackgroundColor(Color.parseColor("#ededed"));
        else holder.card.setBackgroundColor(Color.parseColor("#ffffff"));
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    public void setCoins(ArrayList<CoinSearchModel> coins) {
        this.coins = coins;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView number, name;
        ImageView icon;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            number = itemView.findViewById(R.id.main_coins_search_number);
            name = itemView.findViewById(R.id.main_coins_search_coin_name);
            icon = itemView.findViewById(R.id.main_coins_search_icon);
            card = itemView.findViewById(R.id.main_coins_search_card);

        }
    }
}
