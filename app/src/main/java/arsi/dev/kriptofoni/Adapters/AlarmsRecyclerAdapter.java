package arsi.dev.kriptofoni.Adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Fragments.AlertsFragments.AlertFragment;
import arsi.dev.kriptofoni.Models.AlarmModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.R;

public class AlarmsRecyclerAdapter extends RecyclerView.Adapter<AlarmsRecyclerAdapter.Holder> {

    private List<AlarmModel> alarmModels;
    private AlertFragment alertFragment;
    private String currency;
    private CountryCodePicker countryCodePicker;
    private NumberFormat nf;

    public AlarmsRecyclerAdapter(List<AlarmModel> alarmModels, AlertFragment alertFragment, String currency) {
        this.alarmModels = alarmModels;
        this.alertFragment = alertFragment;
        this.currency = currency;
        countryCodePicker = new CountryCodePicker();
        nf = NumberFormat.getInstance(new Locale("tr", "TR"));
        nf.setMaximumFractionDigits(2);
    }

    @NonNull
    @Override
    public AlarmsRecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.alarm_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmsRecyclerAdapter.Holder holder, int position) {
        AlarmModel model = alarmModels.get(position);

        String nameText = model.getName() + " (" + model.getShortCut().toUpperCase(Locale.ENGLISH) + ")";
        holder.name.setText(nameText);
        holder.smallerOrBigger.setText(model.isSmaller() ? "Şunun altında: " : "Şunun üzerinde: ");
        String priceText = nf.format(model.getPrice()) + " " + currency.toUpperCase(Locale.ENGLISH);
        holder.price.setText(priceText);
        Picasso.get().load(model.getImage()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return alarmModels.size();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        RelativeLayout card;
        ImageView image;
        TextView name, smallerOrBigger, price;

        public Holder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.alarm_card);
            image = itemView.findViewById(R.id.alarm_card_icon);
            name = itemView.findViewById(R.id.alarm_card_name);
            smallerOrBigger = itemView.findViewById(R.id.alarm_card_bigger_or_smaller);
            price = itemView.findViewById(R.id.alarm_card_price);
            card.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(this.getAdapterPosition(), 102, 0, "Sil");
        }
    }

    public void setAlarmModels(List<AlarmModel> alarmModels) {
        this.alarmModels = alarmModels;
        notifyDataSetChanged();
    }
}
