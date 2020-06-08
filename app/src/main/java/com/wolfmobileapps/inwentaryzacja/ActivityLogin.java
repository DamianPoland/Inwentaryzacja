package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.util.ArrayList;

import io.reactivex.Single;

public class ActivityLogin extends AppCompatActivity {

    // TAG
    private static final String TAG = "ActivityLogin";

    // views
    private LinearLayout linLayLoginNoInternetConnection;
    private Spinner spinnerUsers;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBarLoginWait;
    private LinearLayout linLayLoginConnectingSignalR;
    private ScrollView scroolViewLogin;


    // shar pref
    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    // lists
    private ArrayList<String> listOfUsers = new ArrayList<>();
    private ArrayAdapter<String> spinnerArrayAdapter;

    // for connectivityManager
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    // signalR
    private HubConnection hubConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //views
        linLayLoginNoInternetConnection = findViewById(R.id.linLayLoginNoInternetConnection);
        spinnerUsers = findViewById(R.id.spinnerUsers);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBarLoginWait = findViewById(R.id.progressBarLoginWait);
        linLayLoginConnectingSignalR = findViewById(R.id.linLayLoginConnectingSignalR);
        scroolViewLogin = findViewById(R.id.scroolViewLogin);

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF, MODE_PRIVATE);

        // open second activity if was looged before - Client don't want that
//        boolean userIsLogged = shar.getBoolean(C.USER_IS_LOGGED, false);
//        if (userIsLogged) {
//
//            // start next activity and close this
//            startNextActivity();
//        }


        // TODO: 1/3 (ominięcie połaczeń signal R i MSSQL) odkomentować to co niżej
        startActivity(new Intent(ActivityLogin.this, ActivityScreans.class));
        if (1==1){
            return;
        }



        // start connectivity listener
        startConnectivityManager();

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

        // button login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {
                Log.d(TAG, "startSignalR: hubConnection.getConnectionState(): " + hubConnection.getConnectionState());

                // get user name from shar
                String userNameTakenFromList = shar.getString(C.NAME_OF_USER, "");

                if (userNameTakenFromList.equals("")) {
                    showAlertDialog("Wybierz nazwę użytkownika");
                    return;
                }

                if (editTextPassword.getText().toString().equals("")) {
                    showAlertDialog("Wpisz hasło");
                    return;
                }

                // show progressBarLoginWait and hide buttonLogin
                progressBarLoginWait.setVisibility(View.VISIBLE);
                buttonLogin.setVisibility(View.GONE);

                // login users in signalR
                String userName = userNameTakenFromList;
                String password = editTextPassword.getText().toString();

                // request to signalR
                try {
                    Single<Boolean> exc = hubConnection.invoke(Boolean.class, "Login", userName, password);
                    exc.filter((Boolean x) -> Boolean.class.isInstance(x))
                            .cast(Boolean.class)
                            .subscribe((Boolean x) -> loginResult(x));
                } catch (Exception e) {
                    Log.d(TAG, "Exception, onClick: " + e);
                    Toast.makeText(ActivityLogin.this, "Exception: " + e, Toast.LENGTH_LONG).show();

                    // show buttonLogin and hide progressBarLoginWait
                    showButtonAndHideProgress();
                }
            }
        });
    }

    // END onCreate____________________________________________________________________________________________________


    // start signalR connection
    public void startSignalR() {

        try {
            // 1. built connection
            hubConnection = HubConnectionBuilder.create(shar.getString(C.SIGNAL_R_URL_FOR_SHAR, C.SIGNAL_R_URL_STANDARD)).build();

            // 2. start connection
            hubConnection.start().blockingAwait(); // blockingAwait stop and wait to connection
            Log.d(TAG, "startSignalR ConnectionState(): " + hubConnection.getConnectionState());

            // 3. get users from signalR
            getUsersFromSignalR();

        } catch (Exception e) { // cath if hubConnection.start() is not possible
            Log.d(TAG, "ActivityLogin, startSignalR: Exception: " + e);
            Toast.makeText(this, "SignalR brak połączenia", Toast.LENGTH_SHORT).show();

            // if catch exception than wait waitTime and try again connect to SignalR
            startSignalRAgain();
        }
    }

    // if catch exception than wait waitTime and try again connect to SignalR
    public void startSignalRAgain () {

        Handler handler = new Handler();
        long waitTime = C.WAITING_TIME*1000; // waitTime
        handler.postDelayed(new Runnable() {
            public void run() {

                // start signalR after waitTime
                startSignalR();
            }
        }, waitTime); // delay
    }

    // get users from signalR
    @SuppressLint("CheckResult")
    public void getUsersFromSignalR() {

        Single<String[]> ex = hubConnection.invoke(String[].class, "Users");
        ex.filter((String[] x) -> String[].class.isInstance(x))
                .cast(String[].class)
                .subscribe((String[] x) -> unpackData(x)); // start function to unpack list of users
    }

    // unspack list of users users
    public void unpackData(String[] data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // clear list of users
                listOfUsers.clear();

                // add users names to list
                for (int i = 0; i < data.length; i++) {
                    listOfUsers.add(data[i]);
                    Log.d(TAG, "unpackData: " + data[i]);
                }
                spinnerArrayAdapter.notifyDataSetChanged();

                // show views after connect Succes
                linLayLoginConnectingSignalR.setVisibility(View.GONE);
                scroolViewLogin.setVisibility(View.VISIBLE);
            }
        });
    }

    public void loginResult(boolean result) {

        // must be on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // show buttonLogin and hide progressBarLoginWait
                showButtonAndHideProgress();

                // clear password view
                editTextPassword.setText("");

                // if result true than open activity, if false than show alert
                if (result) {

                    // save user IS logged in shar pref
                    editor = shar.edit();
                    editor.putBoolean(C.USER_IS_LOGGED, result);
                    editor.apply();

                    // stop signalR connection
                    if (hubConnection != null) {
                        hubConnection.stop().blockingAwait(); //  wait for stop
                    }

                    // stop connectivity listener
                    connectivityManager.unregisterNetworkCallback(networkCallback);

                    // start next activity and close this
                    startNextActivity();

                } else {

                    // save user IS NOT logged in shar pref
                    editor = shar.edit();
                    editor.putBoolean(C.USER_IS_LOGGED, result);
                    editor.apply();

                    showAlertDialog("Niepoprawne hasło");
                }
            }
        });
    }

    // show buttonLogin and hide progressBarLoginWait
    public void showButtonAndHideProgress () {
        progressBarLoginWait.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.VISIBLE);
    }

    // start next activity and close this
    public void startNextActivity() {

        // close current activity - on back pressed in next activity don's work
        finish();

        // standard ActivityScreans (standard password is 1234)
        startActivity(new Intent(ActivityLogin.this, ActivityScreans.class));
    }

    // start connectivity listener
    public void startConnectivityManager() {

        // start connectivity listener
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // network available
                Log.d(TAG, "ConnectivityManager, conection YES: ");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //hide linLayLoginNoInternetConnection - hide inofrmation NO internet
                        linLayLoginNoInternetConnection.setVisibility(View.GONE);

                        // start signalR connection - hide linLayConnecting and show scroolViewLogin if signalR will connect
                        startSignalR();
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                // network unavailable
                Log.d(TAG, "ConnectivityManager, conection NO: ");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // show and hide informations
                        linLayLoginNoInternetConnection.setVisibility(View.VISIBLE); // show inofrmation NO internet
                        linLayLoginConnectingSignalR.setVisibility(View.VISIBLE); // show view connection
                        scroolViewLogin.setVisibility(View.GONE); // hide view to login
                    }
                });
            }
        };
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
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

    // menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_info_login:
                startActivity(new Intent(ActivityLogin.this, ActivityInfo.class));
                break;
            case R.id.menu_settings_login:
                startActivity(new Intent(ActivityLogin.this, ActivitySettings.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
