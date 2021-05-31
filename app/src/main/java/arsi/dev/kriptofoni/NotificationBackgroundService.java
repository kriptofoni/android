package arsi.dev.kriptofoni;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationBackgroundService extends Service {

    private Handler handler;
    private Runnable runnable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        boolean fromAlarm = intent.getBooleanExtra("fromAlarm", false);
        boolean fromBroadcast = intent.getBooleanExtra("fromBroadcast", false);
        if (fromAlarm) {
            doInForeground();
        } else if (fromBroadcast) {
            doWhenAppKilled();
        }
        return START_STICKY;
    }

    private void doWhenAppKilled() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "Alarms";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        Notification notification = builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.splash_screen)
                .setContentTitle("Title")
                .setContentText("Body")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();

        notificationManager.notify(1, notification);
        System.out.println("Background Service Called");
    }

    private void doInForeground() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Background Service Called");
                handler.postDelayed(this, 1000 * 60);
            }
        };
        handler.postDelayed(runnable, 1000 * 60);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Intent broadcastIntent = new Intent(getApplication(), NotificationBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);

        stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent(getApplication(), NotificationBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);

        stop();
    }

    private void stop() {
        if (handler != null) handler.removeCallbacks(runnable);
    }

}
