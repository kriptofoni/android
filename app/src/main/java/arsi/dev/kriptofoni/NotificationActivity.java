package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        textView = findViewById(R.id.notification_text_view);

        String message = getIntent().getStringExtra("message");
        textView.setText(message);
    }
}