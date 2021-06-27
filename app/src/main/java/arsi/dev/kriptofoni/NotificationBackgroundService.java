package arsi.dev.kriptofoni;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import arsi.dev.kriptofoni.Models.AlarmModel;
import arsi.dev.kriptofoni.Retrofit.AlarmsApi;
import arsi.dev.kriptofoni.Retrofit.AlarmsRetrofitClient;
import arsi.dev.kriptofoni.Retrofit.CoinMarket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationBackgroundService extends Service {

    private Handler handler;
    private Runnable runnable;
    private AlarmsApi alarmsApi;
    private String currency;
    private SharedPreferences sharedPreferences;
    private Boolean killService = false;
    private List<AlarmModel> alarmModels;
    private List<String> currencies;
    private Gson gson;
    private boolean fromAlarm, fromBroadcast;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        alarmsApi = AlarmsRetrofitClient.getInstance().getMyCoinGeckoApi();

        currencies = new ArrayList<>();
        System.out.println("onstartcommand");

        sharedPreferences = getSharedPreferences("Preferences", 0);
        currency = sharedPreferences.getString("currency", "usd");

        fromAlarm = intent.getBooleanExtra("fromAlarm", false);
        fromBroadcast = intent.getBooleanExtra("fromBroadcast", false);
        if (fromAlarm) {
            doInForeground();
        } else if (fromBroadcast) {
            doWhenAppKilled();
        }

        return START_REDELIVER_INTENT;
    }

    private void doInForeground() {
        startHandler();
    }

    private void doWhenAppKilled() {
        Notification notification = new NotificationCompat.Builder(this, "Background Service")
                .setContentTitle("Arka Plan Etkinliği")
                .setContentText("Kriptofoni size bildirim gönderebilmek için arka planda çalışmaya devam ediyor.")
                .setSmallIcon(R.drawable.splash_screen)
                .build();
        startForeground(2001, notification);
        startHandler();
    }

    private void startHandler() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                readMemory();
                handler.postDelayed(this, 1000 * 30);
            }
        };
        handler.postDelayed(runnable, 1000 * 30);
    }

    private void readMemory() {
        gson = new Gson();
        String alarmModelsJson = sharedPreferences.getString("alarmModels", "");
        Type type = new TypeToken<List<AlarmModel>>() {}.getType();

        if (!alarmModelsJson.isEmpty()) {
            alarmModels = gson.fromJson(alarmModelsJson, type);

            StringBuilder stringBuilder = new StringBuilder();

            for (AlarmModel model : alarmModels) {
                if (stringBuilder.indexOf(model.getId()) != -1) {
                    stringBuilder.append(model.getId()).append(",");
                }
                if (!currencies.contains(model.getCurrency())) currencies.add(model.getCurrency());
            }

            makeApiCall(stringBuilder.toString());
        } else {
            alarmModels = new ArrayList<>();
            killService = true;
            stopSelf();
            stop();
        }
    }

    private void makeApiCall(String ids) {
        System.out.println("api call made");
        NumberFormat nf = NumberFormat.getInstance(new Locale("tr", "TR"));
        nf.setMaximumFractionDigits(2);
        List<AlarmModel> modelsToRemove = new ArrayList<>();
        for (int i = 0; i < currencies.size(); i++) {
            String currency = currencies.get(i);
            final int finalI = i;
            Call<List<CoinMarket>> call = alarmsApi.getCoinMarkets(currency, ids, "market_cap_desc", 50, 1, false, "24h,7d");
            call.enqueue(new Callback<List<CoinMarket>>() {
                @Override
                public void onResponse(Call<List<CoinMarket>> call, Response<List<CoinMarket>> response) {
                    if (response.isSuccessful()) {
                        List<CoinMarket> body = response.body();
                        if (body != null) {
                            for (CoinMarket coinMarket : body) {
                                for (AlarmModel model : alarmModels) {
                                    if (model.getId().equals(coinMarket.getId()) && model.getCurrency().equals(currency)) {
                                        if (model.isSmaller() && coinMarket.getCurrent_price() <= model.getPrice()) {
                                            System.out.println("smaller " + model.getPrice() + " " + coinMarket.getCurrent_price());
                                            modelsToRemove.add(model);
                                            String bodyText = model.getName() + " " + nf.format(model.getPrice()) + " " + currency.toUpperCase(Locale.ENGLISH) + "'nin altına düştü";
                                            sendNotification(bodyText, model.getId());
                                        } else if (!model.isSmaller() &&  coinMarket.getCurrent_price() >= model.getPrice()) {
                                            System.out.println("bigger " + model.getPrice() + " " + coinMarket.getCurrent_price());
                                            modelsToRemove.add(model);
                                            String bodyText = model.getName() + " " + nf.format(model.getPrice()) + " " + currency.toUpperCase(Locale.ENGLISH) + "'nin üzerine çıktı";
                                            sendNotification(bodyText, model.getId());
                                        }
                                    }
                                }
                            }

                            if (finalI == currencies.size() - 1) {
                                alarmModels.removeAll(modelsToRemove);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                if (!alarmModels.isEmpty()) {
                                    String alarmModelsJson = gson.toJson(alarmModels);
                                    editor.putString("alarmModels", alarmModelsJson);
                                } else {
                                    killService = true;
                                    stopSelf();
                                    editor.putString("alarmModels", "");
                                }

                                editor.apply();
                            }
                        } else {
                            makeApiCall(ids);
                        }
                    } else {
                        if (response.code() == 429) {
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    makeApiCall(ids);
                                    handler.postDelayed(this, 5000);
                                }
                            };
                            handler.postDelayed(runnable, 5000);
                        } else {
                            makeApiCall(ids);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<CoinMarket>> call, Throwable t) {
                    makeApiCall(ids);
                }
            });
        }
    }

    private void sendNotification(String bodyText, String coinId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "Alarms";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent notificationIntent;

        if (App.isRunning) {
            notificationIntent = new Intent(this, CryptoCurrencyDetailActivity.class);
            notificationIntent.putExtra("id", coinId);
        } else {
            notificationIntent = new Intent(this, HomeActivity.class);
            notificationIntent.putExtra("fromNotification", true);
            notificationIntent.putExtra("coinId", coinId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.splash_screen)
                .setContentTitle("Fiyat Alarmı")
                .setContentText(bodyText)
                .setSound(alarmSound)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(1, notification);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        stopSelf();
        stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!killService) {
            Intent broadcastIntent = new Intent(getApplication(), NotificationBroadcastReceiver.class);
            sendBroadcast(broadcastIntent);
        } else {
            stopForeground(true);
        }

        stop();
    }

    private void stop() {
        if (handler != null) handler.removeCallbacks(runnable);
    }

}
