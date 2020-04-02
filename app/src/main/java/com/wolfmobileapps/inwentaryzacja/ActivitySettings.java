package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class ActivitySettings extends AppCompatActivity {

    private static final String TAG = "ActivitySettings";


    //views
    private Switch switchNotifications;

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

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF,MODE_PRIVATE);

        // turn on swith if was turned on before
        switchNotifications.setChecked(shar.getBoolean(C.SWITCH_NOTIFICATIONS_IS_ON, true));

        // turn ON or OF notifications
        switchNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchNotifications.isChecked()) {

                    //save in shar swith ON
                    editor = shar.edit();
                    editor.putBoolean(C.SWITCH_NOTIFICATIONS_IS_ON, true);
                    editor.apply();

                    Toast.makeText(ActivitySettings.this, "Powiadomienia włączone.", Toast.LENGTH_SHORT).show();

                } else {

                    //save in shar swith OFF
                    editor = shar.edit();
                    editor.putBoolean(C.SWITCH_NOTIFICATIONS_IS_ON, false);
                    editor.apply();

                    Toast.makeText(ActivitySettings.this, "Powiadomienia wyłączone.", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }
}
