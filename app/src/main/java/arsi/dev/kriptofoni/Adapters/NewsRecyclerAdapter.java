package arsi.dev.kriptofoni.Adapters;

import android.text.Html;
import android.util.JsonToken;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import arsi.dev.kriptofoni.Fragments.NewsFragment;
import arsi.dev.kriptofoni.Models.NewsModel;
import arsi.dev.kriptofoni.R;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.Holder> {

    private final int BIG_CARD = 0, SMALL_CARD = 1;
    private List<NewsModel> models;
    private NewsFragment newsFragment;

    public NewsRecyclerAdapter(List<NewsModel> models, NewsFragment newsFragment) {
        this.models = models;
        this.newsFragment = newsFragment;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == BIG_CARD)
            view = layoutInflater.inflate(R.layout.news_card_big, null);
        else
            view = layoutInflater.inflate(R.layout.news_card_small, null);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NewsModel model = models.get(position);

        holder.title.setText(Html.fromHtml(model.getTitle(), Html.FROM_HTML_MODE_COMPACT));

        long newsTime = model.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long now = System.currentTimeMillis();

        String text = "";
        if (newsTime > now - (1000 * 60 * 60)) {
            int timeDiff = (int) (now - newsTime) / (1000 * 60);
            text = timeDiff + " dakika önce  " + model.getSource();
        } else if (newsTime > now - (1000 * 60 * 60 * 24)) {
            int timeDiff = (int) (now - newsTime) / (1000 * 60 * 60);
            text = timeDiff + " saat önce  " + model.getSource();
        } else {
            int timeDiff = (int) (now - newsTime) / (1000 * 60 * 60 * 24);
            text = timeDiff + " gün önce  " + model.getSource();
        }

        holder.dateAndSource.setText(text);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newsFragment.createWebView(model.getLink());
            }
        });

        if (!model.getThumbnail().isEmpty())
            Picasso.get().load(model.getThumbnail()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return BIG_CARD;
        else return SMALL_CARD;
    }

    public void setModels(List<NewsModel> filteredList) {
        models = filteredList;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        ImageView thumbnail;
        TextView title, dateAndSource;

        public Holder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.news_card);
            thumbnail = itemView.findViewById(R.id.news_card_thumbnail);
            title = itemView.findViewById(R.id.news_card_title);
            dateAndSource = itemView.findViewById(R.id.news_card_date_and_source);
        }
    }


}
