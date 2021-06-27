package arsi.dev.kriptofoni;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Models.AlarmModel;
import arsi.dev.kriptofoni.Pickers.CountryCodePicker;
import arsi.dev.kriptofoni.Retrofit.CoinInfoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmActivity extends AppCompatActivity {

    private final int CURRENCY_CHOOSE_CODE = 1;
    private List<AlarmModel> alarmModels;
    private EditText currencyAmount;
    private Button setAlarm;
    private TextView name, shortcut, currency, price;
    private ImageView image, back;
    private double coinPrice;
    private CoinInfoApi coinInfoApi;
    private String currencyText, coinId;

    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        setupUI(findViewById(R.id.alarm_activity_constraint_layout));
        coinInfoApi = CoinInfoRetrofitClient.getInstance().getMyCoinGeckoApi();

        currencyAmount = findViewById(R.id.alarm_currency_amount);
        setAlarm = findViewById(R.id.alarm_add_alarm_button);
        name = findViewById(R.id.alarm_coin_name);
        shortcut = findViewById(R.id.alarm_coin_shortcut);
        image = findViewById(R.id.alarm_coin_image);
        currency = findViewById(R.id.alarm_currency);
        price = findViewById(R.id.alarm_coin_price);
        back = findViewById(R.id.alarm_back_button);

        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", 0);
        currencyText = sharedPreferences.getString("currency", "usd");
        currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlarmActivity.this, CurrencyChooseActivity.class);
                intent.putExtra("converter", true);
                intent.putExtra("changeAppCurrency", false);
                startActivityForResult(intent, CURRENCY_CHOOSE_CODE);
            }
        });

        Gson gson = new Gson();
        String alarmModelsJson = sharedPreferences.getString("alarmModels", "");
        Type type = new TypeToken<List<AlarmModel>>() {}.getType();

        if (!alarmModelsJson.isEmpty()) alarmModels = gson.fromJson(alarmModelsJson, type);
        else alarmModels = new ArrayList<>();

        Intent intent = getIntent();

        coinId = intent.getStringExtra("id");
        String nameText = intent.getStringExtra("name");
        String shortCutText = intent.getStringExtra("shortCut");
        String imageText = intent.getStringExtra("image");
        String priceText = intent.getStringExtra("price");

        String nameTextConcat = intent.getStringExtra("name") + " (" + intent.getStringExtra("shortCut").toUpperCase(Locale.ENGLISH) + ")";
        String shortCutTextConcat = "GÜNCEL " + intent.getStringExtra("shortCut").toUpperCase(Locale.ENGLISH) + " FİYATI";
        name.setText(nameTextConcat);
        shortcut.setText(shortCutTextConcat);
        Picasso.get().load(imageText).into(image);
        if (priceText != null) {
            price.setText(intent.getStringExtra("price"));
            coinPrice = intent.getDoubleExtra("priceValue", 0);
        } else {
            fetchCoinPrice();
        }

        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = currencyAmount.getText().toString().trim();
                if (!text.isEmpty()) {
                    double price = Double.parseDouble(text);
                    if (!isMyServiceRunning(NotificationBackgroundService.class)) {
                        Intent intent = new Intent(getApplication(), NotificationBackgroundService.class);
                        intent.putExtra("fromAlarm", true);
                        startService(intent);
                    } else {
                        System.out.println("service running");
                    }

                    AlarmModel model = new AlarmModel(coinId, nameText, shortCutText, imageText, price, price < coinPrice, currencyText);
                    alarmModels.add(model);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String json = new Gson().toJson(alarmModels);
                    editor.putString("alarmModels", json);
                    editor.apply();

                    Toast.makeText(AlarmActivity.this, "Alarm başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AlarmActivity.this, "Lütfen fiyat bilgisi giriniz", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        boolean firstOpen = sharedPreferences.getBoolean("firstOpen", true);
        if (firstOpen) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstOpen", false);
            editor.apply();

            for (Intent permissionIntent : POWERMANAGER_INTENTS)
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    // show dialog to ask user action
                    new AlertDialog.Builder(this)
                            .setTitle("Bildirim İzni")
                            .setMessage("Uygulama kapalıyken bildirim alabilmek için lütfen açılan pencerede Kriptofoni'yi aktif hale getirin.")
                            .setPositiveButton("İzin ver", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivityForResult(permissionIntent, 1234);
                                }
                            })
                            .setNegativeButton("Reddet", null)
                            .show();
                    break;
                }
        }
    }

    private void fetchCoinPrice() {
        Call<JsonObject> call = coinInfoApi.getCoinSimple(coinId, currencyText, "false");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject body = response.body();
                    if (body != null) {
                        JsonObject coin = (JsonObject) body.get(coinId);
                        if (!coin.isJsonNull()) {
                            coinPrice = coin.get(currencyText).getAsDouble();
                            NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
                            nf.setMaximumFractionDigits(2);
                            CountryCodePicker countryCodePicker = new CountryCodePicker();
                            String priceText = countryCodePicker.getCountryCode(currencyText)[1] + " " + nf.format(coinPrice);
                            price.setText(priceText);
                        }
                    } else {
                        fetchCoinPrice();
                    }
                } else {
                    if (response.code() == 429) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                fetchCoinPrice();
                                handler.postDelayed(this, 5000);
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    } else {
                        fetchCoinPrice();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                fetchCoinPrice();
            }
        });
    }

    private void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(AlarmActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CURRENCY_CHOOSE_CODE && resultCode == CURRENCY_CHOOSE_CODE) {
            if (data != null) {
                currencyText = data.getStringExtra("currencyId");
                currency.setText(currencyText.toUpperCase(Locale.ENGLISH));
                fetchCoinPrice();
            }
        }
    }
}