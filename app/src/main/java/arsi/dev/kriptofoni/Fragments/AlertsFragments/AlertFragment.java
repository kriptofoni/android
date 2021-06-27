package arsi.dev.kriptofoni.Fragments.AlertsFragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Adapters.AlarmsRecyclerAdapter;
import arsi.dev.kriptofoni.AlarmActivity;
import arsi.dev.kriptofoni.CoinSelectActivity;
import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.HomeActivity;
import arsi.dev.kriptofoni.Models.AlarmModel;
import arsi.dev.kriptofoni.R;

public class AlertFragment extends Fragment {

    private final int COIN_SELECT_CODE = 1, CURRENCY_SELECT_CODE = 2;

    private RelativeLayout noAlarm, hasAlarm;
    private ImageView add;
    private Button noAlarmAdd;
    private TextView currency, noAlarmText;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private List<AlarmModel> alarmModels;
    private AlarmsRecyclerAdapter alarmsRecyclerAdapter;
    private SharedPreferences sharedPreferences;
    private String currencyText;

    private HomeActivity homeActivity;

    public AlertFragment() {}

    public AlertFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts_alert, container ,false);

        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "");

        alarmModels = new ArrayList<>();

        noAlarm = view.findViewById(R.id.alerts_alert_no_alarm);
        hasAlarm = view.findViewById(R.id.alerts_alert_has_alarm);
        add = view.findViewById(R.id.alerts_alert_add);
        noAlarmAdd = view.findViewById(R.id.alerts_alert_no_alarm_add);
        currency = view.findViewById(R.id.alerts_alert_currency);
        progressBar = view.findViewById(R.id.alerts_alert_progress_bar);
        noAlarmText = view.findViewById(R.id.no_alarm_text);

        recyclerView = view.findViewById(R.id.alerts_alert_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        alarmsRecyclerAdapter = new AlarmsRecyclerAdapter(alarmModels, this, currencyText);
        recyclerView.setAdapter(alarmsRecyclerAdapter);

        readFromMemory();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        noAlarmAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToCoinChoose();
            }
        });

        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));

        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CurrencyChooseActivity.class);
                intent.putExtra("converter", true);
                startActivityForResult(intent, CURRENCY_SELECT_CODE);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String tempCurrencyText = sharedPreferences.getString("currency", "usd");
        if (!tempCurrencyText.equals(currencyText)) {
            currencyText = tempCurrencyText;
            currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
        }
        readFromMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void intentToCoinChoose() {
        Intent intent = new Intent(getActivity(), CoinSelectActivity.class);
        startActivityForResult(intent, COIN_SELECT_CODE);
    }

    private void readFromMemory() {
        Gson gson = new Gson();
        String alarmModelsJson = sharedPreferences.getString("alarmModels", "");
        Type type = new TypeToken<List<AlarmModel>>() {}.getType();
        if (!alarmModelsJson.isEmpty()) {
            alarmModels = gson.fromJson(alarmModelsJson, type);
            List<AlarmModel> removeModels = new ArrayList<>();
            for (AlarmModel alarmModel : alarmModels) {
                if (!alarmModel.getCurrency().equals(currencyText))
                    removeModels.add(alarmModel);
            }
            alarmModels.removeAll(removeModels);
            if (!alarmModels.isEmpty())
                hasAlarm();
            else
                noAlarm();
        } else {
            alarmModels.clear();
            noAlarm();
        }

        alarmsRecyclerAdapter.setAlarmModels(alarmModels);
    }

    private void noAlarm() {
        progressBar.setVisibility(View.GONE);
        hasAlarm.setVisibility(View.GONE);
        noAlarm.setVisibility(View.VISIBLE);
        String text = currencyText.toUpperCase(Locale.ENGLISH) + " cinsinden bir alarmınız bulunmamakta.";
        noAlarmText.setText(text);
    }

    private void hasAlarm() {
        progressBar.setVisibility(View.GONE);
        noAlarm.setVisibility(View.GONE);
        add.setVisibility(View.VISIBLE);
        hasAlarm.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 102) {
            List<AlarmModel> removeModels = new ArrayList<>();
            removeModels.add(alarmModels.get(item.getGroupId()));
            alarmModels.removeAll(removeModels);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (!alarmModels.isEmpty()) {
                editor.putString("alarmModels", new Gson().toJson(alarmModels));
                hasAlarm();
            } else {
                editor.putString("alarmModels", "");
                noAlarm();
            }

            editor.apply();

            return true;
        } else
            return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COIN_SELECT_CODE && resultCode == COIN_SELECT_CODE) {
            if (data != null) {
                Intent intent = new Intent(getActivity(), AlarmActivity.class);
                intent.putExtra("id", data.getStringExtra("id"));
                intent.putExtra("shortCut", data.getStringExtra("shortCut"));
                intent.putExtra("name", data.getStringExtra("name"));
                intent.putExtra("image", data.getStringExtra("image"));
                startActivity(intent);
            }
        } else if (requestCode == CURRENCY_SELECT_CODE && resultCode == COIN_SELECT_CODE) {
            if (data != null) {
                homeActivity.resetMainFragments();
            }
        }
    }
}
