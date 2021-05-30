package arsi.dev.kriptofoni.Fragments.BuySellFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arsi.dev.kriptofoni.BuySellActivity;
import arsi.dev.kriptofoni.CoinSelectActivity;
import arsi.dev.kriptofoni.Models.PortfolioMemoryModel;
import arsi.dev.kriptofoni.R;

public class BuySellFragment extends Fragment {

    private final int COIN_SELECT_REQUEST_CODE = 1;
    private Button addOperation;
    private TextView datePickerClick, currencies;
    private EditText notesInput, currenciesInputText, costInput, priceInput, datePickerText;
    private RelativeLayout coinSelect;
    private boolean operationType = false, fromPortfolio = false;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private List<PortfolioMemoryModel> models;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private String shortCut = "", currency, id;
    private double timestamp = 0;

    private Map<String, Double> quantities;

    private BuySellActivity buySellActivity;

    public BuySellFragment() {}

    public BuySellFragment(BuySellActivity buySellActivity, Boolean operationType) {
        this.buySellActivity = buySellActivity;
        this.operationType = operationType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy_sell, container, false);

        setupUI(view.findViewById(R.id.buy_sell_fragment_constraint_layout));

        priceInput = view.findViewById(R.id.priceInputText);
        costInput = view.findViewById(R.id.editCostInputText);
        datePickerClick = view.findViewById(R.id.operationDateClick);
        datePickerText = view.findViewById(R.id.operationDate);
        notesInput = view.findViewById(R.id.notesEditText);
        addOperation = view.findViewById(R.id.addOperation);
        currenciesInputText = view.findViewById(R.id.currenciesInputText);
        currencies = view.findViewById(R.id.currencies);
        coinSelect = view.findViewById(R.id.buy_sell_crypto_select);

        Intent intent = buySellActivity.getIntent();
        String coinShortCut = intent.getStringExtra("shortCut");
        fromPortfolio = intent.getBooleanExtra("fromPortfolio", false);
        if (coinShortCut != null) {
            shortCut = coinShortCut;
            id = intent.getStringExtra("id");
            currencies.setText(coinShortCut.toUpperCase(Locale.ENGLISH));
        } else {
            currencies.setText("BTC");
            shortCut = "BTC";
            id = "bitcoin";
        }

        coinSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(buySellActivity, CoinSelectActivity.class);
                startActivityForResult(intent, COIN_SELECT_REQUEST_CODE);
            }
        });

        addOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currencyAmountText = currenciesInputText.getText().toString();
                String costInputText = costInput.getText().toString();
                String priceInputText = priceInput.getText().toString();

                if (!shortCut.isEmpty() && !currencyAmountText.isEmpty() && !priceInputText.isEmpty() && timestamp != 0) {

                    double price = Double.parseDouble(priceInputText);
                    double currencyAmount = Double.parseDouble(currencyAmountText);
                    double costInput = !costInputText.isEmpty() ? Double.parseDouble(costInputText) : 0;
                    String notes = notesInput.getText().toString().trim();

                    if (!operationType) {
                        if (quantities.get(shortCut) == null) {
                            new AlertDialog.Builder(buySellActivity)
                                    .setTitle("Invalid Operation")
                                    .setMessage(String.format("Portföyünüzde %s bulunmadığı için satış işlemi gerçekleştiremezsiniz.", shortCut.toUpperCase(Locale.ENGLISH)))
                                    .setNegativeButton("OK", null)
                                    .show();
                        } else if (quantities.get(shortCut) < currencyAmount) {
                            new AlertDialog.Builder(buySellActivity)
                                    .setTitle("Invalid Operation")
                                    .setMessage(String.format("Sahip olduğunuzdan fazla %s satış işlemi gerçekleştiremezsiniz.", shortCut.toUpperCase(Locale.ENGLISH)))
                                    .setNegativeButton("OK", null)
                                    .show();
                        } else {
                            createModel(currencyAmount, price, costInput, notes, "sell");
                        }
                    } else {
                        createModel(currencyAmount, price, costInput, notes, "buy");
                    }
                } else {
                    Toast.makeText(buySellActivity, "Lütfen gerekli alanları doldurunuz.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        datePick();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPreferences = buySellActivity.getSharedPreferences("Preferences", 0);
        String portfolioJson = sharedPreferences.getString("portfolio", "");
        currency = sharedPreferences.getString("currency", "");
        gson = new Gson();
        Type type = new TypeToken<List<PortfolioMemoryModel>>() {}.getType();
        if (!portfolioJson.isEmpty()) models = gson.fromJson(portfolioJson, type);
        else models = new ArrayList<>();

        quantities = new HashMap<>();

        if (!models.isEmpty()) {
            for (PortfolioMemoryModel model : models) {
                double quantity = model.getType().equals("buy") ? model.getQuantity() : -1 * model.getQuantity();
                if (quantities.get(model.getShortCut()) != null) {
                    quantities.put(model.getShortCut(), quantities.get(model.getShortCut()) + quantity);
                } else {
                    quantities.put(model.getShortCut(), quantity);
                }
            }
        }
    }

    private void createModel(double currencyAmount, double price, double costInput, String notes, String type) {
        PortfolioMemoryModel model = new PortfolioMemoryModel(currencyAmount, timestamp, price, costInput, notes, shortCut, type, currency, id);
        models.add(model);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(models);
        editor.putString("portfolio", json);
        editor.apply();

        if (fromPortfolio) {
            buySellActivity.setResult(2);
            buySellActivity.finish();
        } else {
            Toast.makeText(buySellActivity, "İşleminiz portföyünüze eklenmiştir.", Toast.LENGTH_SHORT).show();
        }
    }

    private void datePick() {

        dateSetListener = (datePicker, i, i1, i2) -> {
            int month = i1 + 1;
            datePickerText.setText(i2 + "/" + month + "/" + i);
            Date date = new Date(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            GregorianCalendar selectedDate = new GregorianCalendar(i, i1, i2, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            timestamp = (double) selectedDate.getTimeInMillis();
        };

        datePickerClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerClick();
            }
        });

        datePickerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerClick();
            }
        });
    }

    private void datePickerClick() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(buySellActivity, dateSetListener, year, month, day);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                timestamp = 0;
            }
        });
        dialog.show();
    }

    private void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(buySellActivity);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COIN_SELECT_REQUEST_CODE && resultCode == COIN_SELECT_REQUEST_CODE) {
            shortCut = data.getStringExtra("shortCut");
            id = data.getStringExtra("id");
            currencies.setText(data.getStringExtra("shortCut").toUpperCase(Locale.ENGLISH));
        }
    }
}
