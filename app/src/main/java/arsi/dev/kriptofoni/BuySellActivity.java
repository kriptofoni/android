package arsi.dev.kriptofoni;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arsi.dev.kriptofoni.Fragments.AlertsFragment;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.AlertFragment;
import arsi.dev.kriptofoni.Fragments.AlertsFragments.WatchingListFragment;
import arsi.dev.kriptofoni.Fragments.BuySellFragments.BuySellFragment;
import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.Models.PortfolioMemoryModel;

public class BuySellActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private final int BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT = 1;

    private BuySellFragment buyFragment, sellFragment;

    private ImageView exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);

        exit = findViewById(R.id.buy_sell_back_button);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        viewPager = findViewById(R.id.buy_sell_viewpager);
        setUpViewPager();

        tabLayout = findViewById(R.id.buy_sell_tabs);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setUpViewPager() {

        Adapter adapter = new Adapter(getSupportFragmentManager(), BEHAVIOUR_RESUME_ONLY_CURRENT_FRAGMENT);

        buyFragment = new BuySellFragment(this, true);
        sellFragment = new BuySellFragment(this,false);
        adapter.addFragment(buyFragment, "Satın Al");
        adapter.addFragment(sellFragment, "Sat");

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
            tabLayout.setTabTextColors(ContextCompat.getColor(getApplicationContext(), R.color.textColor), ContextCompat.getColor(getApplicationContext(), R.color.buttonColor));
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