package arsi.dev.kriptofoni.MoreActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arsi.dev.kriptofoni.MoreActivities.ToolsActivities.ConverterActivity;
import arsi.dev.kriptofoni.R;

public class ToolsActivity extends AppCompatActivity {

    private TextView converterTextView;
    private ImageView converterImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        converterTextView = findViewById(R.id.tools_converter_text);
        converterImageView = findViewById(R.id.tools_converter_image);

        converterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ToolsActivity.this, ConverterActivity.class);
                startActivity(intent);

            }
        });

        converterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent(ToolsActivity.this, ConverterActivity.class);
                startActivity(intent1);

            }
        });
    }
}