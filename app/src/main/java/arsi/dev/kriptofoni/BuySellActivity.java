package arsi.dev.kriptofoni;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import arsi.dev.kriptofoni.Models.CoinSearchModel;
import arsi.dev.kriptofoni.Models.PortfolioMemoryModel;

public class BuySellActivity extends AppCompatActivity {

    private final int COIN_SELECT_REQUEST_CODE = 1;
    private Button buy, sell, addOperation;
    private TextView datePickerClick, datePickerText, timePickerText, currencies;
    private EditText notesInput, currenciesInputText, costInput, priceInput;
    private boolean buttonType = false;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private ImageView backButton;
    private List<PortfolioMemoryModel> models;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private String shortCut = "";
    private double timestamp;

    private int year = 0, month = 0, day = 0, hour = 0, minute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);

        models = new ArrayList<>();

        sharedPreferences = getSharedPreferences("Preferences", 0);
        String portfolioJson = sharedPreferences.getString("portfolio", "");
        gson = new Gson();
        Type type = new TypeToken<List<PortfolioMemoryModel>>() {}.getType();
        if (!portfolioJson.isEmpty()) models = gson.fromJson(portfolioJson, type);

        timePickerText = findViewById(R.id.operationTime);
        priceInput = findViewById(R.id.priceInputText);
        costInput = findViewById(R.id.editCostInputText);
        datePickerClick = findViewById(R.id.operationDateClick);
        datePickerText = findViewById(R.id.operationDate);
        backButton = findViewById(R.id.buy_sell_back_button);
        buy = findViewById(R.id.buyButton);
        sell = findViewById(R.id.sellButton);
        notesInput = findViewById(R.id.notesEditText);
        addOperation = findViewById(R.id.addOperation);
        currenciesInputText = findViewById(R.id.currenciesInputText);
        currencies = findViewById(R.id.currencies);

        Intent intent = getIntent();
        String coinShortCut = intent.getStringExtra("shortCut");
        if (coinShortCut != null) {
            shortCut = coinShortCut;
            String text = "Total " + coinShortCut.toUpperCase(Locale.ENGLISH);
            currencies.setText(text);
        }

        buy.setTextColor(Color.parseColor("#5b8d65"));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonType) {
                    buy.setTextColor(Color.parseColor("#5b8d65"));
                    sell.setTextColor(Color.parseColor("#797676"));
                    buttonType = false;
                }
            }
        });

        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!buttonType) {
                    sell.setTextColor(Color.parseColor("#ea768c"));
                    buy.setTextColor(Color.parseColor("#797676"));
                    buttonType = true;
                }
            }
        });

        currencies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuySellActivity.this, CoinSelectActivity.class);
                startActivityForResult(intent, COIN_SELECT_REQUEST_CODE);
            }
        });

        addOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currencyAmountText = currenciesInputText.getText().toString();
                String costInputText = costInput.getText().toString();
                String priceInputText = priceInput.getText().toString();

                if (!shortCut.isEmpty() && !currencyAmountText.isEmpty() && !priceInputText.isEmpty()) {
                    double price = Double.parseDouble(priceInputText);
                    double currencyAmount = Double.parseDouble(currencyAmountText);
                    double costInput = !costInputText.isEmpty() ? Double.parseDouble(costInputText) : 0;
                    String notes = notesInput.getText().toString().trim();

                    boolean contains = false;

                    for (PortfolioMemoryModel model : models) {
                        if (model.getShortCut().equals(shortCut)) {
                            contains = true;
                            break;
                        }
                    }

                    if (contains) {
                        
                    } else {
                        PortfolioMemoryModel model = new PortfolioMemoryModel(currencyAmount, timestamp, price, costInput, notes, shortCut);
                    }


                } else {
                    Toast.makeText(BuySellActivity.this, "Please fill necessary fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        datePick();
    }

    private void datePick() {

        dateSetListener = (datePicker, i, i1, i2) -> {
            GregorianCalendar selectedDate = new GregorianCalendar(i, i1, i2);
            year = i;
            int month = i1 + 1;
            BuySellActivity.this.month = month;
            day = i2;
            datePickerText.setText(i2 + "/" + month + "/" + i);
        };

        timeSetListener = (timePicker, i, i1) -> {
            String h, m;
            if (i < 10) {
                if (i1 < 10) {
                    h = "0" + i;
                    m = "0" + i1;
                } else {
                    h = "0" + i;
                    m = String.valueOf(i1);
                }
            } else {
                if (i1 < 10) {
                    h = String.valueOf(i);
                    m = "0" + i1;
                } else {
                    h = String.valueOf(i);
                    m = String.valueOf(i1);
                }
            }
            String time = " " + h + "." + m;
            hour = Integer.parseInt(h);
            minute = Integer.parseInt(m);
            GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, day, hour, minute);
            timestamp = (double) gregorianCalendar.getTimeInMillis();
            timePickerText.setText(time);
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

                TimePickerDialog dialog1 = new TimePickerDialog(BuySellActivity.this, AlertDialog.THEME_HOLO_DARK, timeSetListener, hour, minute, true);
                dialog1.show();
                dialog1.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (BuySellActivity.this.year != 0) {
                            GregorianCalendar gregorianCalendar = new GregorianCalendar(BuySellActivity.this.year, BuySellActivity.this.month, BuySellActivity.this.day);
                            timestamp = (double) gregorianCalendar.getTimeInMillis();
                        }
                    }
                });

                DatePickerDialog dialog = new DatePickerDialog(BuySellActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateSetListener, year, month, day);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        timestamp = 0;
                        dialog1.cancel();
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COIN_SELECT_REQUEST_CODE && resultCode == COIN_SELECT_REQUEST_CODE) {
            String text = "Total " + data.getStringExtra("shortCut").toUpperCase(Locale.ENGLISH);
            shortCut = data.getStringExtra("shortCut");
            currencies.setText(text);
        }
    }
}