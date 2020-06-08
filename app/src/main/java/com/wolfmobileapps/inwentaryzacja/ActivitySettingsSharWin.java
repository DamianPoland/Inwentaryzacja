package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ActivitySettingsSharWin extends AppCompatActivity {

    private static final String TAG = "ActivitySettingsSharWin";

    //vars
    private EditText editTextSharWinHostName;
    private EditText editTextSharWinDomainName;
    private EditText editTextSharWinUserName;
    private EditText editTextSharWinPassword;
    private EditText editTextSharWinShareName;
    private EditText editTextSharWinPath;
    private EditText editTextSharWinFileName;
    private Button buttonSaveSettingsSharWin;

    // shar pref
    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_shar_win);

        // vars
        editTextSharWinHostName = findViewById(R.id.editTextSharWinHostName);
        editTextSharWinDomainName = findViewById(R.id.editTextSharWinDomainName);
        editTextSharWinUserName = findViewById(R.id.editTextSharWinUserName);
        editTextSharWinPassword = findViewById(R.id.editTextSharWinPassword);
        editTextSharWinShareName = findViewById(R.id.editTextSharWinShareName);
        editTextSharWinPath = findViewById(R.id.editTextSharWinPath);
        editTextSharWinFileName = findViewById(R.id.editTextSharWinFileName);
        buttonSaveSettingsSharWin = findViewById(R.id.buttonSaveSettingsSharWin);

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF, MODE_PRIVATE);

        // titule action bar
        getSupportActionBar().setTitle("Ustawienia połączenia");

        // vars to SMB connection - set on edit texts
        String hostName = shar.getString(C.WINDOWS_SHARE_HOST_NAME, "192.168.1.106"); //  address - dla wifi 192.168.1.106 lub inny - sprawdzić w cmd ->IPcongig -> Połączenie lokalne/IPv4 Address
        editTextSharWinHostName.setText(hostName);
        String domainName = shar.getString(C.WINDOWS_SHARE_DOMAIN, "Domain"); // domain can be emmpty
        editTextSharWinDomainName.setText(domainName);
        String userName = shar.getString(C.WINDOWS_SHARE_USER_NAME, "TestUser"); // user name with access
        editTextSharWinUserName.setText(userName);
        String password = shar.getString(C.WINDOWS_SHARE_PASSWORD, "TestUser"); // password to access
        editTextSharWinPassword.setText(password);
        String shareName = shar.getString(C.WINDOWS_SHARE_SHARE_NAME, "SmbTest"); // shared folder
        editTextSharWinShareName.setText(shareName);
        String path = shar.getString(C.WINDOWS_SHARE_PATH, ""); // - folder in shared place - can be empty if all
        editTextSharWinPath.setText(path);
        String fileName = shar.getString(C.WINDOWS_SHARE_FILE_NAME, "FileExampleName.txt");
        editTextSharWinFileName.setText(fileName);

        // button to save all settings
        buttonSaveSettingsSharWin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get from edit texts
                String hostName = editTextSharWinHostName.getText().toString();
                String domainName = editTextSharWinDomainName.getText().toString();
                String userName = editTextSharWinUserName.getText().toString();
                String password = editTextSharWinPassword.getText().toString();
                String shareName = editTextSharWinShareName.getText().toString();
                String path = editTextSharWinPath.getText().toString();
                String fileName = editTextSharWinFileName.getText().toString();

                // save in shar
                editor = shar.edit();
                editor.putString(C.WINDOWS_SHARE_HOST_NAME, hostName);
                editor.putString(C.WINDOWS_SHARE_DOMAIN, domainName);
                editor.putString(C.WINDOWS_SHARE_USER_NAME, userName);
                editor.putString(C.WINDOWS_SHARE_PASSWORD, password);
                editor.putString(C.WINDOWS_SHARE_SHARE_NAME, shareName);
                editor.putString(C.WINDOWS_SHARE_PATH, path);
                editor.putString(C.WINDOWS_SHARE_FILE_NAME, fileName);
                editor.apply();

                Toast.makeText(ActivitySettingsSharWin.this, "Zapisano", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
