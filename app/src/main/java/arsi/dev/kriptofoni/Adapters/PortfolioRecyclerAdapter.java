package arsi.dev.kriptofoni.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arsi.dev.kriptofoni.Fragments.PortfolioFragment;
import arsi.dev.kriptofoni.Models.PortfolioModel;
import arsi.dev.kriptofoni.R;

public class PortfolioRecyclerAdapter extends RecyclerView.Adapter<PortfolioRecyclerAdapter.Holder> {

    private List<PortfolioModel> coins;
    private PortfolioFragment portfolioFragment;
    private String currencySymbol;
    private boolean selectingMode = false;
    private Map<String, Double> timestamps;
    private ViewGroup parent;

    public PortfolioRecyclerAdapter(List<PortfolioModel> coins, PortfolioFragment portfolioFragment) {
        this.coins = coins;
        this.portfolioFragment = portfolioFragment;
        timestamps = new HashMap<>();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.portfolio_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
        PortfolioModel coin = coins.get(position);

        int red = Color.parseColor("#f6465d");
        int green = Color.parseColor("#2ebd85");
        int defaultColor = ContextCompat.getColor(parent.getContext(), R.color.textColor);

        Picasso.get().load(coin.getImage()).into(holder.icon);
        holder.name.setText(coin.getShortCut().toUpperCase(Locale.ENGLISH));
        holder.quantity.setText(String.format(Locale.ENGLISH,"%.2f", coin.getQuantity()));
        holder.totalPrice.setText(String.format(Locale.ENGLISH,"%s%s", currencySymbol, nf.format(coin.getTotalPrice())));

        holder.change24H.setText(String.format(Locale.ENGLISH,"%s%s", currencySymbol, nf.format(coin.getPriceChange24Hours())));
        holder.changePercentage24H.setText(String.format(Locale.ENGLISH,"%%%s", nf.format(coin.getPriceChangePercentage24Hours())));
        holder.price.setText(String.format(Locale.ENGLISH,"%s%s", currencySymbol, nf.format(coin.getCurrentPrice())));

        holder.change24H.setTextColor(coin.getPriceChange24Hours() < 0 ? red : coin.getPriceChange24Hours() > 0 ? green : defaultColor);
        holder.changePercentage24H.setTextColor(coin.getPriceChangePercentage24Hours() < 0 ? red : coin.getPriceChangePercentage24Hours() > 0 ? green : defaultColor);
        holder.price.setTextColor(coin.getPriceChangePercentage24Hours() < 0 ? red : coin.getPriceChangePercentage24Hours() > 0 ? green : defaultColor);

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coin.setChecked(!coin.isChecked());
                holder.checkBox.setChecked(coin.isChecked());
                if (coin.isChecked()) portfolioFragment.addId(coin.getId());
                else portfolioFragment.removeId(coin.getId());
            }
        });

        if (selectingMode) {
            holder.icon.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.icon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        ImageView icon;
        TextView name, quantity, totalPrice, change24H, changePercentage24H, price;
        CheckBox checkBox;

        public Holder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.portfolio_card);
            icon = itemView.findViewById(R.id.portfolio_card_icon);
            name = itemView.findViewById(R.id.portfolio_card_coin_name);
            quantity = itemView.findViewById(R.id.portfolio_card_coin_quantity);
            totalPrice = itemView.findViewById(R.id.portfolio_card_coin_total_price);
            change24H = itemView.findViewById(R.id.portfolio_card_change_in_24_hours);
            changePercentage24H = itemView.findViewById(R.id.portfolio_card_change_percent_in_24_hours);
            price = itemView.findViewById(R.id.portfolio_card_current_price);
            checkBox = itemView.findViewById(R.id.portfolio_card_check_box);
        }
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public void setSelectingMode(boolean selectingMode) {
        this.selectingMode = selectingMode;
        notifyDataSetChanged();
    }

    public void setTimestamps(Map<String, Double> timestamps) {
        this.timestamps = timestamps;
    }
}
