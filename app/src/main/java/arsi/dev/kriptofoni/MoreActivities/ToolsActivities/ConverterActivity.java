package arsi.dev.kriptofoni.MoreActivities.ToolsActivities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import arsi.dev.kriptofoni.CoinSelectActivity;
import arsi.dev.kriptofoni.CurrencyChooseActivity;
import arsi.dev.kriptofoni.R;
import arsi.dev.kriptofoni.Retrofit.CoinInfoApi;
import arsi.dev.kriptofoni.Retrofit.CoinInfoRetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.math.BigDecimal.valueOf;
import static java.math.MathContext.DECIMAL64;

public class ConverterActivity extends AppCompatActivity {

    private final int CURRENCY_SELECT_CODE = 1, COIN_SELECT_CODE = 2;

    private CoinInfoApi coinInfoApi;

    private EditText cryptoEditText, currencyEditText;
    private Button convertButton;
    private RelativeLayout cryptoSelect, currencySelect;
    private TextView cryptoText, currencyText;
    private ImageView back, reload;
    private ProgressBar progressBar;

    private boolean isCryptoChancing, isCurrencyChancing;
    private String coinId, currencyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        coinInfoApi = CoinInfoRetrofitClient.getInstance().getMyCoinGeckoApi();

        cryptoEditText = findViewById(R.id.converter_crypto_edit_text);
        currencyEditText = findViewById(R.id.converter_currency_edit_text);
        convertButton = findViewById(R.id.converter_convert_button);
        cryptoSelect = findViewById(R.id.converter_crypto_select);
        currencySelect = findViewById(R.id.converter_currency_select);
        cryptoText = findViewById(R.id.converter_crypto_text);
        currencyText = findViewById(R.id.converter_currency_text);
        back = findViewById(R.id.converter_back_button);
        progressBar = findViewById(R.id.converter_progress_bar);
        reload = findViewById(R.id.converter_reload);

        cryptoSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConverterActivity.this, CoinSelectActivity.class);
                startActivityForResult(intent, COIN_SELECT_CODE);
            }
        });

        currencySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConverterActivity.this, CurrencyChooseActivity.class);
                intent.putExtra("converter", true);
                startActivityForResult(intent, CURRENCY_SELECT_CODE);
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cryptoEditText.getText().toString().isEmpty() && currencyEditText.getText().toString().isEmpty()) {
                    Toast.makeText(ConverterActivity.this, "Please enter the price that you want to convert", Toast.LENGTH_SHORT).show();
                } else {
                    convertButton.setClickable(false);
                    reload.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    convert();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setInitialValues();
        textController();
    }

    private void convert() {
        Call<JsonObject> call = coinInfoApi.getCoinSimple(coinId, currencyId, "false");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject body = response.body();
                    if (body != null) {
                        JsonObject coin = (JsonObject) body.get(coinId);
                        if (coin != null) {
                            double price = coin.get(currencyId).getAsDouble();

                            if (isCryptoChancing) {
                                double converted = Double.parseDouble(cryptoEditText.getText().toString()) * price;
                                currencyEditText.setText(String.valueOf(converted));
                            } else {
                                double converted = Double.parseDouble(currencyEditText.getText().toString()) / price;
                                cryptoEditText.setText(String.format(Locale.ENGLISH,"%.7f", converted));
                            }

                            reset();

                            convertButton.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                            reload.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    convert();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                convert();
            }
        });
    }

    private void reset() {
        isCryptoChancing = false;
        isCurrencyChancing = false;

        cryptoEditText.setEnabled(true);
        currencyEditText.setEnabled(true);
    }

    private void setInitialValues() {
        coinId = "bitcoin";
        currencyId = "usd";

        cryptoText.setText("BTC");
        currencyText.setText("USD");
    }

    private void textController() {

        cryptoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isCryptoChancing = true;
                currencyEditText.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (cryptoEditText.getText().toString().isEmpty()) {
                    isCryptoChancing = false;
                    currencyEditText.setEnabled(true);
                }
            }
        });

        currencyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isCurrencyChancing = true;
                cryptoEditText.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (currencyEditText.getText().toString().isEmpty()) {
                    isCurrencyChancing = false;
                    cryptoEditText.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CURRENCY_SELECT_CODE && resultCode == CURRENCY_SELECT_CODE) {
            if (data != null) {
                currencyId = data.getStringExtra("currencyId");
                currencyText.setText(currencyId.toUpperCase(Locale.ENGLISH));

                reset();

                cryptoEditText.setText("");
                currencyEditText.setText("");
            }
        } else if (requestCode == COIN_SELECT_CODE && resultCode == CURRENCY_SELECT_CODE) {
            if (data != null) {
                coinId = data.getStringExtra("id");
                cryptoText.setText(data.getStringExtra("shortCut").toUpperCase(Locale.ENGLISH));

                reset();

                cryptoEditText.setText("");
                currencyEditText.setText("");
            }
        }
    }
}