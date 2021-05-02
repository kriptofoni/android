package arsi.dev.kriptofoni.MoreActivities.ToolsActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import arsi.dev.kriptofoni.R;

public class ConverterActivity extends AppCompatActivity {

    private EditText cryptoEditText, currencyEditText;
    private Button convertButton;
    private Spinner cryptoSpinner, currencySpinner;
    private boolean isCryptoChancing, isCurrencyChancing;

    String[] crypto = {"aaa","bbb","ccc"};
    String[] currency = {"asdf","bbsdfb","casdcc"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        cryptoEditText = findViewById(R.id.converter_crypto_edit_text);
        currencyEditText = findViewById(R.id.converter_currency_edit_text);
        convertButton = findViewById(R.id.converter_convert_button);
        cryptoSpinner = findViewById(R.id._converter_crypto_spinner);
        currencySpinner = findViewById(R.id.converter_currency_spinner);

        spinnerController();
        textController();

    }

    private void spinnerController() {

        ArrayAdapter<String> cryptoAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, crypto);
        cryptoSpinner.setAdapter(cryptoAdapter);
        cryptoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {  //crypto spinner clicked situation
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //default should be bitcoin
                System.out.println("CryptoSelected");

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                System.out.println("NothingSelected");

            }
        });


        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, currency);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {  //currency spinner clicked situation
                //default should be USD
                System.out.println("CurrencySelected");

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                System.out.println("NothingSelected");

            }
        });


    }

    private void textController() {

        cryptoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                isCryptoChancing = true;
                currencyEditText.setEnabled(false);

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(cryptoEditText.getText().toString().isEmpty()){

                    isCryptoChancing = false;
                    currencyEditText.setEnabled(true);

                }
            }
        });

        currencyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                isCurrencyChancing = true;
                cryptoEditText.setEnabled(false);


            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (currencyEditText.getText().toString().isEmpty()){

                    isCurrencyChancing = false;
                    cryptoEditText.setEnabled(true);

                }

            }
        });


    }


}