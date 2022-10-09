package com.example.currencyconverter;

import static com.loopj.android.http.AsyncHttpClient.log;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
//    https://free.currconv.com/api/v7/convert?q=USD_INR&compact=ultra&apiKey=38b9f7e70c1b1eb6089e


    private final String URL = "https://free.currconv.com/api/v7/convert?q=";
    private final String URL_BALANCE = "&compact=ultra&apiKey=81f74e22136a5f3fd783";
    String[] currency1;
    String[] currency2;

    TextView priceView;
    Float convertedPrice;
    String userTypedPrice;

    boolean connected;

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Spinner spinner1 = findViewById(R.id.spinner1);
        Spinner spinner2 = findViewById(R.id.spinner2);
        Button myButton = findViewById(R.id.button);
        priceView = findViewById(R.id.priceView);
        EditText textField = findViewById(R.id.editTextNumber);
//        ImageButton myImageButton = findViewById(R.id.imageButton);

        currency1 = new String[1];
        currency2 = new String[1];

        final int[] pos1 = new int[1];
        final int[] pos2 = new int[1];

        myButton.setEnabled(false);


        connected = false;
        checkConnection();

        if (!connected){
            Toast.makeText(getApplicationContext(), "Check Your Connection!", Toast.LENGTH_LONG).show();

            priceView.setText("Error!");


        }







        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency_array, R.layout.custom_spinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency1[0] = parent.getItemAtPosition(position).toString();
                Log.d("myApp", "currency:" + currency1[0]);


                //button behavior

                pos1[0] = position;

                if (pos1[0] != 0 && pos2[0] != 0) {
                    myButton.setEnabled(true);
                }else myButton.setEnabled(pos1[0] != 0 && pos2[0] != 0);








            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency2[0] = parent.getItemAtPosition(position).toString();
                Log.d("myApp", "currency:" + currency2[0]);

                pos2[0] = position;

                if (pos1[0] != 0 && pos2[0] != 0) {
                    myButton.setEnabled(true);
                }else if (pos1[0] == 0 || pos2[0] == 0){
                    myButton.setEnabled(false);

                }else{
                    myButton.setEnabled(true);
                }






            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        myImageButton.setOnClickListener(v -> {
//            Intent myIntent = new Intent(MainActivity.this, CountryInfo.class);
//            startActivity(myIntent);
//
//        });




        textField.setOnEditorActionListener((v, actionId, event) -> {
            userTypedPrice = textField.getText().toString();
            return false;

        });



        myButton.setOnClickListener(v -> {
            Log.d("myApp", "user convertedPrice:" + userTypedPrice);
            String finalURL = URL + currency1[0] + "_" + currency2[0] + URL_BALANCE;
            log.d("myApp", "url:" + finalURL);

            checkConnection();

            if(!connected){
                Toast.makeText(getApplicationContext(), "Check your connection and try again!", Toast.LENGTH_SHORT).show();

            }else {


                networking(finalURL);
                textField.getText().clear();
            }











        });


    }

    private void checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){

            Log.d("myApp", "Network Available");

            connected = true;

        }else{
            Log.d("myApp", "Network Not Available");
            connected = false;
        }
    }


    private void networking(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    Log.d("myApp", "JSON" + response.toString());

                    Iterator<String> keys = response.keys();
                    while (keys.hasNext()) {
                        String keyValue = keys.next();
                        String valueString = response.getString(keyValue);
                        Log.d("myApp", "final:" + valueString);
                        convertedPrice = Float.parseFloat(valueString);
                        Log.d("myApp", "Final Price:" + String.format("% .3f", convertedPrice));



                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }


                    if (userTypedPrice == null || userTypedPrice == "") {
                        Toast.makeText(getApplicationContext(), "Converted to 1 " + currency2[0], Toast.LENGTH_SHORT).show();
                        priceView.setText(String.format("% .3f", convertedPrice));

                    } else {
                        Float userPrice = Float.parseFloat(userTypedPrice);
                        convertedPrice = userPrice * convertedPrice;
                        priceView.setText(String.format("% .3f", convertedPrice));
                        Toast.makeText(getApplicationContext(), "Converted to " + userPrice + " " + currency2[0], Toast.LENGTH_SHORT).show();
                        userTypedPrice = "";
                    }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("myApp", "Request Failed:" + statusCode);
                Log.d("myApp", "Response Fail:" + errorResponse);
                Log.d("myApp", "Error:" + throwable.toString());

            }
        });
    }


}