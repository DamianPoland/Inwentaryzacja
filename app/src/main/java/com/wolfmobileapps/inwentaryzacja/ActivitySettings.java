package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class ActivitySettings extends AppCompatActivity {

    private static final String TAG = "ActivitySettings";


    //views
    private Switch switchNotifications;
    private EditText editTextSignalRURL;
    private EditText editTextMSSQL_URL;
    private Button buttonSignalRURL;
    private Button buttonMSSQL_URL;

    // shar pref
    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ustawienia");

        //views
        switchNotifications = findViewById(R.id.switchNotifications);
        editTextSignalRURL = findViewById(R.id.editTextSignalRURL);
        editTextMSSQL_URL = findViewById(R.id.editTextMSSQL_URL);
        buttonSignalRURL = findViewById(R.id.buttonSignalRURL);
        buttonMSSQL_URL = findViewById(R.id.buttonMSSQL_URL);

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF,MODE_PRIVATE);

        // turn on swith if was turned on before
        switchNotifications.setChecked(shar.getBoolean(C.SWITCH_NOTIFICATIONS_IS_ON, true));

        // show current SignalR URL
        String currentSignal_R_URL = shar.getString(C.SIGNAL_R_URL_FOR_SHAR, C.SIGNAL_R_URL_STANDARD);
        editTextSignalRURL.setText(currentSignal_R_URL);

        // show current MS SQL
        String currentMS_SQL_URL = shar.getString(C.MS_SQL_URL_FOR_SHAR, C.MS_SQL_URL_STANDARD);
        editTextMSSQL_URL.setText(currentMS_SQL_URL);

        // turn ON or OF notifications
        switchNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchNotifications.isChecked()) {

                    //save in shar swith ON
                    editor = shar.edit();
                    editor.putBoolean(C.SWITCH_NOTIFICATIONS_IS_ON, true);
                    editor.apply();

                    // start service
                    startService(new Intent(ActivitySettings.this, ServiceNotifications.class));

                    Toast.makeText(ActivitySettings.this, "Powiadomienia włączone.", Toast.LENGTH_SHORT).show();

                } else {

                    //save in shar swith OFF
                    editor = shar.edit();
                    editor.putBoolean(C.SWITCH_NOTIFICATIONS_IS_ON, false);
                    editor.apply();

                    // stop service
                    stopService(new Intent(ActivitySettings.this, ServiceNotifications.class));

                    Toast.makeText(ActivitySettings.this, "Powiadomienia wyłączone.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSignalRURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // save new SignalR URL
                String signalR_URL = editTextSignalRURL.getText().toString();
                editor = shar.edit();
                editor.putString(C.SIGNAL_R_URL_FOR_SHAR, signalR_URL);
                editor.apply();

                Toast.makeText(ActivitySettings.this, "SignalR URL zmieniony.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonMSSQL_URL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //save new MSSQL URL
                String msSQL_URL = editTextMSSQL_URL.getText().toString();
                editor = shar.edit();
                editor.putString(C.MS_SQL_URL_FOR_SHAR, msSQL_URL);
                editor.apply();

                Toast.makeText(ActivitySettings.this, "MS SQL URL zmieniony.", Toast.LENGTH_SHORT).show();

            }
        });
    }
}

