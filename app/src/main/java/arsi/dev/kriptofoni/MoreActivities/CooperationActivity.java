package arsi.dev.kriptofoni.MoreActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import arsi.dev.kriptofoni.R;

public class CooperationActivity extends AppCompatActivity {

    private EditText subjectText, mainText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooperation);

        subjectText = findViewById(R.id.coop_subject_input);
        mainText = findViewById(R.id.coop_main_input);
        sendButton = findViewById(R.id.coop_send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String recipient = "denizegencay@gmail.com";
                String subject = subjectText.getText().toString();
                String main = mainText.getText().toString();
                String[] recipients = recipient.split(",");

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL,recipients);
                intent.putExtra(Intent.EXTRA_SUBJECT,subject);
                intent.putExtra(Intent.EXTRA_TEXT,main);

                intent.setType("message/rfc822");
                startActivity(intent);
            }
        });


    }



}