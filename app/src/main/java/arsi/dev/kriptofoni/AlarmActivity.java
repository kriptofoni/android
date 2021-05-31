package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Models.AlarmModel;

public class AlarmActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "0";
    private List<AlarmModel> alarmModels;
    private EditText currencyAmount;
    private Button setAlarm;
    private TextView name, shortcut, currency, price;
    private ImageView image, back;
    private double coinPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        setupUI(findViewById(R.id.alarm_activity_constraint_layout));

        currencyAmount = findViewById(R.id.alarm_currency_amount);
        setAlarm = findViewById(R.id.alarm_add_alarm_button);
        name = findViewById(R.id.alarm_coin_name);
        shortcut = findViewById(R.id.alarm_coin_shortcut);
        image = findViewById(R.id.alarm_coin_image);
        currency = findViewById(R.id.alarm_currency);
        price = findViewById(R.id.alarm_coin_price);
        back = findViewById(R.id.alarm_back_button);

        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", 0);
        currency.setText(sharedPreferences.getString("currency", "usd").toUpperCase(Locale.ENGLISH));

        Gson gson = new Gson();
        String alarmModelsJson = sharedPreferences.getString("alarmModels", "");
        Type type = new TypeToken<List<AlarmModel>>() {}.getType();

        if (!alarmModelsJson.isEmpty()) alarmModels = gson.fromJson(alarmModelsJson, type);
        else alarmModels = new ArrayList<>();

        Intent intent = getIntent();

        String nameText = intent.getStringExtra("name");
        String shortCutText = intent.getStringExtra("shortCut");
        String imageText = intent.getStringExtra("image");
        String nameTextConcat = intent.getStringExtra("name") + " (" + intent.getStringExtra("shortCut").toUpperCase(Locale.ENGLISH) + ")";
        String shourtCutTextConcat = "GÜNCEL " + intent.getStringExtra("shortCut").toUpperCase(Locale.ENGLISH) + " FİYATI";
        name.setText(nameTextConcat);
        shortcut.setText(shourtCutTextConcat);
        price.setText(intent.getStringExtra("price"));
        Picasso.get().load(imageText).into(image);

        coinPrice = intent.getDoubleExtra("priceValue", 0);

        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = currencyAmount.getText().toString().trim();
                if (!text.isEmpty()) {
                    double price = Double.parseDouble(text);
                    Intent intent = new Intent(getApplication(), NotificationBackgroundService.class);
                    intent.putExtra("fromAlarm", true);
                    startService(intent);

                    AlarmModel model = new AlarmModel(nameText, shortCutText, imageText, price, price < coinPrice);
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

    private void func() {
        String message = "MESSAGE";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(AlarmActivity.this)
                .setSmallIcon(R.drawable.ic_market_icon__10)
                .setContentTitle("New Message")
                .setContentText(message)
                .setAutoCancel(true);

        Intent intent = new Intent(AlarmActivity.this,NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("message",message);

        PendingIntent pendingIntent = PendingIntent.getActivity(AlarmActivity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());

        createNotificationChannel();
    }

    private void createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notificationChannel);
            String description = getString(R.string.channelDescription);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

}