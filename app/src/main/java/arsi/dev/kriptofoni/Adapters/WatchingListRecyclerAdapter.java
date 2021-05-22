package arsi.dev.kriptofoni.Adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.CryptoCurrencyDetailActivity;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.WatchingListFragment;
import arsi.dev.kriptofoni.Models.WatchingListModel;
import arsi.dev.kriptofoni.R;

public class WatchingListRecyclerAdapter extends RecyclerView.Adapter<WatchingListRecyclerAdapter.Holder> {

    private List<WatchingListModel> coins;
    private WatchingListFragment watchingListFragment;
    private String currencySymbol = "";
    private boolean selectingMode = false;
    private ViewGroup parent;

    public WatchingListRecyclerAdapter(List<WatchingListModel> coins, WatchingListFragment watchingListFragment) {
        this.coins = coins;
        this.watchingListFragment = watchingListFragment;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.watching_list_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        WatchingListModel coin = coins.get(position);

        int red = Color.parseColor("#f6465d");
        int green = Color.parseColor("#2ebd85");
        int defaultColor = ContextCompat.getColor(parent.getContext(), R.color.textColor);

        holder.name.setText(coin.getName());
        holder.price.setText(String.format(Locale.ENGLISH, "%s%.2f", currencySymbol, coin.getPrice()));
        holder.priceChangePercent.setText(String.format(Locale.ENGLISH, "%%%.2f", coin.getPriceChangeIn24Hours()));

        holder.price.setTextColor(coin.getPriceChangeIn24Hours() > 0 ? green : coin.getPriceChangeIn24Hours() < 0 ? red : defaultColor);
        holder.priceChangePercent.setTextColor(coin.getPriceChangeIn24Hours() > 0 ? green : coin.getPriceChangeIn24Hours() < 0 ? red : defaultColor);

        holder.number.setText(String.valueOf(position + 1));
        Picasso.get().load(coin.getIcon()).into(holder.icon);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectingMode) {
                    coin.setSelected(!coin.isSelected());
                    holder.checkBox.setChecked(coin.isSelected());
                    if (coin.isSelected()) watchingListFragment.addId(coin.getId());
                    else watchingListFragment.removeId(coin.getId());
                } else {
                    Intent intent = new Intent(watchingListFragment.getActivity(), CryptoCurrencyDetailActivity.class);
                    intent.putExtra("id", coin.getId());
                    watchingListFragment.startActivity(intent);
                }
            }
        });

        if (selectingMode) {
            holder.number.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setChecked(false);
            holder.checkBox.setVisibility(View.GONE);
            holder.number.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView number, name, priceChangePercent, price;
        ImageView icon;
        CheckBox checkBox;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.watching_list_card_coin_name);
            priceChangePercent = itemView.findViewById(R.id.watching_list_card_change_percent_in_24_hours);
            price = itemView.findViewById(R.id.watching_list_card_current_price);
            icon = itemView.findViewById(R.id.watching_list_card_icon);
            card = itemView.findViewById(R.id.watching_list_card);
            number = itemView.findViewById(R.id.watching_list_card_number);
            checkBox = itemView.findViewById(R.id.watching_list_card_check_box);
        }
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public void setSelectingMode(boolean selectingMode) {
        this.selectingMode = selectingMode;
        notifyDataSetChanged();
    }
}
