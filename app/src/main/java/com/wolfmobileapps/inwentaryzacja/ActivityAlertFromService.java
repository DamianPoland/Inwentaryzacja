package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class ActivityAlertFromService extends AppCompatActivity {

    private static final String TAG = "ActivityAlertFromServic";

    // views
    private TextView textViewMessage;
    private Button buttonMessage;
    private ProgressBar progressBarAlertWait;

    // shar pref
    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_from_service);

        // views
        textViewMessage = findViewById(R.id.textViewMessage);
        buttonMessage = findViewById(R.id.buttonMessage);
        progressBarAlertWait = findViewById(R.id.progressBarAlertWait);

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF,MODE_PRIVATE);

        // get intent extra
        Intent intentFromService = getIntent();
        if (intentFromService.hasExtra(C.INTENT_FROM_SERVICE)) {
            textViewMessage.setText(intentFromService.getStringExtra(C.INTENT_FROM_SERVICE));
        }

        // button
        buttonMessage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {

                // get user name from shar
                String userNameTakenFromShar  = shar.getString(C.NAME_OF_USER, "");

                // get dateTime - must be format: "YYYY-mm-dd hh:mm:ss"
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
//                sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
//                String dateTime = sdf.format(new Date(System.currentTimeMillis()));

                // show progressBarAlertWait and hide buttonMessage
                buttonMessage.setVisibility(View.GONE);
                progressBarAlertWait.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // build signalR request
                        try {
                            // 1. built connection
                            HubConnection hubConnection = HubConnectionBuilder.create(shar.getString(C.SIGNAL_R_URL_FOR_SHAR, C.SIGNAL_R_URL_STANDARD)).build();

                            // 2. start connection
                            hubConnection.start().blockingAwait(); // blockingAwait stop and wait to connection
                            Log.d(TAG, "startSignalR ConnectionState(): " + hubConnection.getConnectionState());

                            // 3. send request
                            hubConnection.invoke(Boolean.class, "HaveRead", userNameTakenFromShar); // example to true: codeFromScanner = "09140983601050918115", quantityInteger = "3925"

                            // 4 .stop connection
                            hubConnection.stop().blockingAwait(); //  wait for stop
                            Log.d(TAG, "stopSignalR ConnectionState(): " + hubConnection.getConnectionState());


                        } catch (Exception e) { // cath if hubConnection.start() is not possible
                            Log.d(TAG, "ActivityLogin, startSignalR: Exception: " + e);

                        }

                        // close Activity
                        finish();


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                }).start();
            }
        });
    }

    // NOT USED - show buttonMessage and hide progressBarAlertWait
    public void showButtonAndHideProgressBar() {
        buttonMessage.setVisibility(View.VISIBLE);
        progressBarAlertWait.setVisibility(View.GONE);
    }
}
