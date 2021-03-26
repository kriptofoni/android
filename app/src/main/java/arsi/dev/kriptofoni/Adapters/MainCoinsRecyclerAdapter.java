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
    private final int VIEW_TYPE_FOOTER = 0, VIEW_TYPE_CELL = 1;
    private CoinsFragment coinsFragment;

    public MainCoinsRecyclerAdapter(ArrayList<CoinModel> coins, CoinsFragment coinsFragment) {
        this.coins = coins;
        this.coinsFragment = coinsFragment;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == VIEW_TYPE_FOOTER) {
            view = layoutInflater.inflate(R.layout.page_changer, null);
        } else {
            view = layoutInflater.inflate(R.layout.main_coins_card, null);
        }
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position != coins.size()) {
            CoinModel coin = coins.get(position);
            holder.number.setText(String.valueOf(coin.getNumber()));
            Picasso.get().load(coin.getImageUri()).into(holder.icon);
            String name = coin.getName() + " (" + coin.getShortCut().toUpperCase(Locale.ENGLISH) + ")";
            holder.name.setText(name);
            holder.changeIn24Hours.setText(String.format("%%%.2f", coin.getChangeIn24Hours()));
            holder.changeIn24Hours.setTextColor(coin.getChangeIn24Hours() > 0 ? Color.GREEN : Color.RED);
            holder.priceChangeIn24Hours.setText(String.format("%.2f", coin.getPriceChangeIn24Hours()));
            holder.priceChangeIn24Hours.setTextColor(coin.getChangeIn24Hours() > 0 ? Color.GREEN : Color.RED);
            holder.currentPrice.setText(String.format("$%.2f", coin.getCurrentPrice()));

            // Testing buy button...
            Random random = new Random();
            if (random.nextBoolean()) holder.buy.setVisibility(View.VISIBLE);
            else holder.buy.setVisibility(View.INVISIBLE);

        } else {
            int currentPage = coinsFragment.getCurrentPage();
            int max = coinsFragment.getMax();
            String text = currentPage + " / " + max;
            holder.currentPage.setText(text);
            holder.previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentPage - 1 > 0) {
                        coinsFragment.previousPage();
                    }
                }
            });

            holder.next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentPage + 1 < max) {
                        coinsFragment.nextPage();
                    }
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        return (position == coins.size()) ? VIEW_TYPE_FOOTER : VIEW_TYPE_CELL;
    }

    @Override
    public int getItemCount() {
        return coins.size() + 1;
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView number, name, changeIn24Hours, priceChangeIn24Hours, currentPrice, currentPage;
        ImageView icon, previous, next;
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

            previous = itemView.findViewById(R.id.main_coins_previous_page);
            next = itemView.findViewById(R.id.main_coins_next_page);
            currentPage = itemView.findViewById(R.id.main_coins_page);
        }
    }

    public void setCoins(ArrayList<CoinModel> coins) {
        this.coins = coins;
        notifyDataSetChanged();
    }
}
