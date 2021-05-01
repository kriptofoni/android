package arsi.dev.kriptofoni.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arsi.dev.kriptofoni.MoreActivities.AboutActivity;
import arsi.dev.kriptofoni.MoreActivities.ContractActivity;
import arsi.dev.kriptofoni.MoreActivities.CooperationActivity;
import arsi.dev.kriptofoni.MoreActivities.ToolsActivity;
import arsi.dev.kriptofoni.R;

public class MoreFragment extends Fragment {

    private TextView joinCommunityText, contractText, cooperationText, aboutText, helpText, toolsText;
    private ImageView joinCommunityImage, contractImage, cooperationImage, aboutImage, helpImage, toolsImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        joinCommunityText = view.findViewById(R.id.more_join_group);
        contractText = view.findViewById(R.id.more_contract);
        cooperationText = view.findViewById(R.id.more_cooperation);
        aboutText = view.findViewById(R.id.more_about);
        helpText = view.findViewById(R.id.more_help);
        toolsText = view.findViewById(R.id.more_tools);

        joinCommunityImage = view.findViewById(R.id.more_community_image);
        contractImage = view.findViewById(R.id.more_contract_image);
        cooperationImage = view.findViewById(R.id.more_cooperation_image);
        aboutImage = view.findViewById(R.id.more_about_image);
        helpImage = view.findViewById(R.id.more_help_image);
        toolsImage = view.findViewById(R.id.more_tools_image);

        joinCommunityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"));
                startActivity(intent);
            }
        });

        joinCommunityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"));
                startActivity(intent);
            }
        });

        contractText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ContractActivity.class);
                startActivity(intent);
            }
        });

        contractImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ContractActivity.class);
                startActivity(intent);
            }
        });

        cooperationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CooperationActivity.class);
                startActivity(intent);
            }
        });

        cooperationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CooperationActivity.class);
                startActivity(intent);
            }
        });

        aboutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AboutActivity.class);
                startActivity(intent);
            }
        });

        aboutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AboutActivity.class);
                startActivity(intent);
            }
        });

        helpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CooperationActivity.class);
                startActivity(intent);
            }
        });

        helpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CooperationActivity.class);
                startActivity(intent);
            }
        });

        toolsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ToolsActivity.class);
                startActivity(intent);
            }
        });

        toolsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ToolsActivity.class);
                startActivity(intent);
            }
        });



        return view;
    }
}
