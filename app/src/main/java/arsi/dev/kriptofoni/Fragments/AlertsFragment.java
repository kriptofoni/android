package arsi.dev.kriptofoni.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import arsi.dev.kriptofoni.Fragments.AlertsFragments.AlertFragment;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.WatchingListFragment;
import arsi.dev.kriptofoni.HomeActivity;
import arsi.dev.kriptofoni.Models.WatchingListModel;
import arsi.dev.kriptofoni.R;

public class AlertsFragment extends Fragment {

    private TextView text;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private final int BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT = 1;

    private AlertFragment alertFragment;
    private WatchingListFragment watchingListFragment;

    private HomeActivity homeActivity;

    public AlertsFragment() {}
    public AlertsFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        viewPager = view.findViewById(R.id.alerts_view_pager);
        setUpViewPager();

        tabLayout = view.findViewById(R.id.alerts_tab_layout);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#f2a900"));
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Fragment active;
        switch (viewPager.getCurrentItem()) {
            case 0:
                active = alertFragment;
                break;
            case 1:
                active = watchingListFragment;
                break;
            default:
                active = null;
        }

        if (hidden) {
            active.onPause();
        } else {
            active.onResume();
        }
    }

    private void setUpViewPager() {

        Adapter adapter = new Adapter(getChildFragmentManager(), BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT);

        alertFragment = new AlertFragment();
        watchingListFragment = new WatchingListFragment();
        adapter.addFragment(alertFragment, "Alerts");
        adapter.addFragment(watchingListFragment, "Watching List");

        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {

        private List<Fragment> alertFragmentsList = new ArrayList<>();
        private List<String> alertFragmentsTitlesList = new ArrayList<>();

        public Adapter(FragmentManager fragmentManager, int behaviour){
            super(fragmentManager, behaviour);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            tabLayout.setTabTextColors(Color.parseColor("#797676"), Color.parseColor("#f2a900"));
            return alertFragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return alertFragmentsList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return alertFragmentsTitlesList.get(position);
        }

        public void addFragment(Fragment fragment, String title){
            alertFragmentsList.add(fragment);
            alertFragmentsTitlesList.add(title);
        }
    }
}
