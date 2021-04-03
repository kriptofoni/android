package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BuySellActivity extends AppCompatActivity {

    private Button buy, sell, addOperation;
    private TextView datePickerClick, datePickerText, timePickerText;
    private EditText notesInput, currenciesInputText, costInput, priceInput;
    private boolean buttonType = true;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);
        timePickerText = findViewById(R.id.operationTime);
        priceInput = findViewById(R.id.priceInputText);
        costInput = findViewById(R.id.editCostInputText);
        datePickerClick = findViewById(R.id.operationDateClick);
        datePickerText = findViewById(R.id.operationDate);
        buy = findViewById(R.id.buyButton);
        sell = findViewById(R.id.sellButton);
        notesInput = findViewById(R.id.notesEditText);
        addOperation = findViewById(R.id.addOperation);
        currenciesInputText = findViewById(R.id.currenciesInputText);
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonType = false;
            }
        });

        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonType = true;
            }
        });



        addOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String notes = notesInput.getText().toString();
                String currencyAmountText = currenciesInputText.getText().toString();
                String costInputText = costInput.getText().toString();
                double currencyAmount = Double.parseDouble(currencyAmountText);
                double costInput = Double.parseDouble(costInputText);
            }
        });

        datePick();

    }


    private void datePick(){



        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                GregorianCalendar selectedDate = new GregorianCalendar(i,i1,i2);
                int month = i1+1;
                datePickerText.setText(i2 + "/" + month+"/" + i);
            }

        };

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                String h,m;
                if (i < 10) {
                    if (i1 < 10) {
                        h = "0"+i;
                        m = "0"+i1;
                    } else {
                        h = "0"+i;
                        m = String.valueOf(i1);
                    }
                } else {
                    if (i1 < 10) {
                        h = String.valueOf(i);
                        m = "0"+i1;
                    } else {
                        h = String.valueOf(i);
                        m = String.valueOf(i1);
                    }
                }
                String time =" " + h + "." + m;
                timePickerText.setText(time);
            }

        };

        datePickerClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                Calendar cal1 = Calendar.getInstance();
                int hour = cal1.get(Calendar.HOUR_OF_DAY);
                int minute = cal1.get(Calendar.MINUTE);


                TimePickerDialog dialog1 = new TimePickerDialog(BuySellActivity.this, AlertDialog.THEME_HOLO_DARK,timeSetListener, hour,minute,true);
                dialog1.show();

                DatePickerDialog dialog = new DatePickerDialog(BuySellActivity.this, AlertDialog.THEME_HOLO_LIGHT,dateSetListener,year,month,day);
                dialog.getDatePicker().setMinDate(new Date().getTime()-60000);
                dialog.show();

            }
        });

    }

}