package arsi.dev.kriptofoni.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arsi.dev.kriptofoni.R;

public class MoreFragment extends Fragment {

    private TextView joinCommunity, contract, cooperation, about, help;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        joinCommunity = view.findViewById(R.id.more_join_group);
        contract = view.findViewById(R.id.more_contract);
        cooperation = view.findViewById(R.id.more_cooperation);
        about = view.findViewById(R.id.more_about);
        help = view.findViewById(R.id.more_help);
        return view;
    }
}
