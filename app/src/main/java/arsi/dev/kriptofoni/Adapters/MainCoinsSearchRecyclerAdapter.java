package arsi.dev.kriptofoni.Adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.CoinSelectActivity;
import arsi.dev.kriptofoni.CryptoCurrencyDetailActivity;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.R;

public class MainCoinsSearchRecyclerAdapter extends RecyclerView.Adapter<MainCoinsSearchRecyclerAdapter.Holder> {

    private List<CoinSearchModel> coins;
    private Fragment fragment;
    private CoinSelectActivity coinSelectActivity;

    public MainCoinsSearchRecyclerAdapter(List<CoinSearchModel> coins, Fragment fragment) {
        this.coins = coins;
        this.fragment = fragment;
    }

    public MainCoinsSearchRecyclerAdapter(List<CoinSearchModel> coins, CoinSelectActivity coinSelectActivity) {
        this.coins = coins;
        this.coinSelectActivity = coinSelectActivity;
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

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragment != null) {
                    System.out.println(coin.getId());
                    Intent intent = new Intent(fragment.getActivity(), CryptoCurrencyDetailActivity.class);
                    intent.putExtra("id", coin.getId());
                    fragment.startActivity(intent);
                } else if (coinSelectActivity != null) {
                    Intent intent = coinSelectActivity.getIntent();
                    intent.putExtra("shortCut", coin.getSymbol());
                    intent.putExtra("id", coin.getId());
                    coinSelectActivity.setResult(1, intent);
                    coinSelectActivity.finish();
                }
            }
        });
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
