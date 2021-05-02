package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AlarmActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "0";
    private EditText currencyAmount;
    private Button setAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        currencyAmount = findViewById(R.id.alarm_currency_amount);
        setAlarm = findViewById(R.id.alarm_add_alarm_button);

        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
        });

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