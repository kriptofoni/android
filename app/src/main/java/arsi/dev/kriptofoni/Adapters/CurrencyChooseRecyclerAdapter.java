package arsi.dev.kriptofoni.Adapters;

import android.content.SharedPreferences;
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
import java.util.Locale;

import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.R;

public class CurrencyChooseRecyclerAdapter extends RecyclerView.Adapter<CurrencyChooseRecyclerAdapter.Holder> {

    private ArrayList<String> currencies;
    private SharedPreferences sharedPreferences;
    private String currency;
    private CountryCodePicker countryCodePicker;

    public CurrencyChooseRecyclerAdapter(ArrayList<String> currencies, SharedPreferences sharedPreferences) {
        this.currencies = currencies;
        this.sharedPreferences = sharedPreferences;
        this.currency = sharedPreferences.getString("currency", "usd");
        countryCodePicker = new CountryCodePicker();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.currency_choose_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String text = currencies.get(position);
        String[] arr = countryCodePicker.getCountryCode(text);
        String countryCode = arr[0];
        if (!arr[0].isEmpty()) {
            Picasso.get().load(String.format("https://www.countryflags.io/%s/flat/64.png", countryCode)).into(holder.icon);
        } else {
            Picasso.get().load(String.format("https://cryptoicons.org/api/icon/%s/200", text)).into(holder.icon);
        }
        String s = "";
        if (!arr[1].isEmpty()) {
            s = text.toUpperCase(Locale.ENGLISH) + "/" + arr[1];
        } else {
            s = text.toUpperCase(Locale.ENGLISH);
        }
        holder.text.setText(s);

        if (text.equals(currency)) {
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.check.setVisibility(View.INVISIBLE);
        }

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("currency", text);
                editor.apply();
                notifyDataSetChanged();
                setCurrency(text);
            }
        });
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    private void setCurrency(String currency) {
        this.currency = currency;
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView icon, check;
        TextView text;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.currency_choose_card_icon);
            text = itemView.findViewById(R.id.currency_choose_card_text);
            check = itemView.findViewById(R.id.currency_choose_card_check);
            card = itemView.findViewById(R.id.currency_choose_card);
        }
    }

    public void setCurrencies(ArrayList<String> currencies) {
        this.currencies = currencies;
        notifyDataSetChanged();
    }

    public void setCurrencies(ArrayList<String> currencies, boolean contains) {
        if (contains) {
            this.currencies = currencies;
        } else {
            this.currencies = new ArrayList<>();
        }
        notifyDataSetChanged();
    }
}
