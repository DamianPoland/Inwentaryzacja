package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.util.ArrayList;

import io.reactivex.Single;

public class ActivityLogin extends AppCompatActivity {

    // TAG
    private static final String TAG = "ActivityLogin";

    // views
    private Spinner spinnerUsers;
    private EditText editTextPassword;
    private Button buttonLogin;

    // shar pref
    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    // lists
    private ArrayList<String> listOfUsers = new ArrayList<>();
    private ArrayAdapter<String> spinnerArrayAdapter;

    // signalR
    private HubConnection hubConnection;
    private String serverUrl = "http://lachmana.dyndns.org:15000/hub"; // url: "http://lachmana.dyndns.org:15000/hub"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //views
        spinnerUsers = findViewById(R.id.spinnerUsers);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF,MODE_PRIVATE);






        // TODO: comment that after - only for testing because log out not exist - rest adjusted
        // open second activity if was looged before
        boolean userIsLogged = shar.getBoolean(C.USER_IS_LOGGED, false);
        if (userIsLogged) {
            startActivity(new Intent(ActivityLogin.this, ActivityScreans.class));
            return;
        }







        // button login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "startSignalR: hubConnection.getConnectionState(): " + hubConnection.getConnectionState());

                // get user name from shar
                String userNameTakenFromList  = shar.getString(C.NAME_OF_USER, "");

                if (userNameTakenFromList.equals("")) {
                    showAlertDialog("Wybierz nazwę użytkownika");
                    return;
                }

                if (editTextPassword.getText().toString().equals("")) {
                    showAlertDialog("Wpisz hasło");
                    return;
                }

                // login users in signalR
                String userName = userNameTakenFromList;
                String password = editTextPassword.getText().toString();

                Single<Boolean> exc = hubConnection.invoke(Boolean.class, "Login", userName, password);
                exc.filter((Boolean x) -> Boolean.class.isInstance(x))
                        .cast(Boolean.class)
                        .subscribe((Boolean x) -> loginResult(x));
            }
        });

        // start signalR connection
        startSignalR();
    }

    // END onCreate____________________________________________________________________________________________________



    // start signalR connection
    public void startSignalR() {

        // 1. built connection
        hubConnection = HubConnectionBuilder.create(serverUrl).build();

        // 2 built method to show alert sended by signalR - must be build after build connection and before start connection
        hubConnection.on("Alert", (alert) -> {
            Log.d(TAG, "New Alert from signalR: " + alert);
        }, String.class);

        // 3. start connection
        hubConnection.start().blockingAwait(); // blockingAwait stop and wait to connection
        Log.d(TAG, "startSignalR ConnectionState(): " + hubConnection.getConnectionState());

        // get users from signalR
        getUsersFromSignalR();
    }


    // get users from signalR
    public void getUsersFromSignalR() {

        Single<String[]> ex = hubConnection.invoke(String[].class, "Users");
        ex.filter((String[] x) -> String[].class.isInstance(x))
                .cast(String[].class)
                .subscribe((String[] x) -> unpackData(x)); // start function to unpack list of users
    }

    // unspack list of users users
    public void unpackData(String[] data) {

        // clear list of users
        listOfUsers.clear();

        // add users names to list
        for (int i = 0; i < data.length; i++) {
            listOfUsers.add(data[i]);
            Log.d(TAG, "unpackData: " + data[i]);
        }

        //set Spinner with users
        spinnerArrayAdapter = new ArrayAdapter<>(ActivityLogin.this, android.R.layout.simple_list_item_1, listOfUsers);
        spinnerUsers.setAdapter(spinnerArrayAdapter);
        spinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // get user name from spinner
                String userNameTakenFromList = parent.getItemAtPosition(position).toString();

                // save userName in shar pref
                editor = shar.edit();
                editor.putString(C.NAME_OF_USER, userNameTakenFromList);
                editor.apply();

                Log.d(TAG, "onItemSelected: " + userNameTakenFromList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no code
            }
        });
    }

    public void loginResult(boolean result) {

        // must be on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // clear password view
                editTextPassword.setText("");

                // if result true than open activity, if false than show alert
                if (result) {

                    // save user IS logged in shar pref
                    editor = shar.edit();
                    editor.putBoolean(C.USER_IS_LOGGED, result);
                    editor.apply();

                    // standard password is 1234
                    Log.d(TAG, "loginResult: " + result);
                    startActivity(new Intent(ActivityLogin.this, ActivityScreans.class));

                } else {

                    // save user IS NOT logged in shar pref
                    editor = shar.edit();
                    editor.putBoolean(C.USER_IS_LOGGED, result);
                    editor.apply();

                    showAlertDialog("Niepoprawne hasło");
                    Log.d(TAG, "loginResult: " + result);
                }
            }
        });
    }

    // show alert dialog
    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
        builder.setTitle("Info");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        }).create();
        builder.show();
    }
}
