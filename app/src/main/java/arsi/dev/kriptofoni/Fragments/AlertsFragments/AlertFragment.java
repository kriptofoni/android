package arsi.dev.kriptofoni.Fragments.AlertsFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import arsi.dev.kriptofoni.Adapters.AlarmsRecyclerAdapter;
import arsi.dev.kriptofoni.Models.AlarmModel;
import arsi.dev.kriptofoni.R;

public class AlertFragment extends Fragment {

    private RelativeLayout noAlarm, hasAlarm;
    private ImageView add;
    private Button noAlarmAdd;
    private TextView currency;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private List<AlarmModel> alarmModels;
    private AlarmsRecyclerAdapter alarmsRecyclerAdapter;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts_alert, container ,false);

        sharedPreferences = getActivity().getSharedPreferences("Preferences", 0);
        String currencyText = sharedPreferences.getString("currency", "");

        alarmModels = new ArrayList<>();

        noAlarm = view.findViewById(R.id.alerts_alert_no_alarm);
        hasAlarm = view.findViewById(R.id.alerts_alert_has_alarm);
        add = view.findViewById(R.id.alerts_alert_add);
        noAlarmAdd = view.findViewById(R.id.alerts_alert_no_alarm_add);
        currency = view.findViewById(R.id.alerts_alert_currency);
        progressBar = view.findViewById(R.id.alerts_alert_progress_bar);

        recyclerView = view.findViewById(R.id.alerts_alert_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        alarmsRecyclerAdapter = new AlarmsRecyclerAdapter(alarmModels, this, currencyText);
        recyclerView.setAdapter(alarmsRecyclerAdapter);

        readFromMemory();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        readFromMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void readFromMemory() {
        Gson gson = new Gson();
        String alarmModelsJson = sharedPreferences.getString("alarmModels", "");
        Type type = new TypeToken<List<AlarmModel>>() {}.getType();
        if (!alarmModelsJson.isEmpty()) {
            alarmModels = gson.fromJson(alarmModelsJson, type);
            hasAlarm();
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
    }

    private void hasAlarm() {
        progressBar.setVisibility(View.GONE);
        noAlarm.setVisibility(View.GONE);
        add.setVisibility(View.VISIBLE);
        hasAlarm.setVisibility(View.VISIBLE);
    }
}
