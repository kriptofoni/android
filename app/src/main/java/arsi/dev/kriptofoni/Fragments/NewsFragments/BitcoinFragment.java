package arsi.dev.kriptofoni.Fragments.NewsFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Adapters.NewsRecyclerAdapter;
import arsi.dev.kriptofoni.Fragments.NewsFragment;
import arsi.dev.kriptofoni.Models.NewsModel;
import arsi.dev.kriptofoni.R;

public class BitcoinFragment extends Fragment {

    private NewsFragment newsFragment;
    private List<NewsModel> models;
    private RecyclerView recyclerView;
    private NewsRecyclerAdapter newsRecyclerAdapter;

    public BitcoinFragment() {}

    public BitcoinFragment(NewsFragment newsFragment) {
        this.newsFragment = newsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_bitcoin, container, false);

        models = new ArrayList<>();

        recyclerView = view.findViewById(R.id.news_bitcoin_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        newsRecyclerAdapter = new NewsRecyclerAdapter(models, newsFragment);
        recyclerView.setAdapter(newsRecyclerAdapter);

        return view;
    }

    public void setModels(List<NewsModel> models) {
        this.models.clear();
        this.models.addAll(models);
        newsRecyclerAdapter.notifyDataSetChanged();
    }
}
