package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import io.reactivex.Single;


public class ActivityScreans extends AppCompatActivity {

    private static final String TAG = "ActivityScreans";

    // views general
    private LinearLayout linearLayoutBottomButtons;
    private LinearLayout linLayScreansNoInternetConnection;
    private LinearLayout linLayScreansConnectingSignalR;
    private LinearLayout linLayScreansConnectingMSSQL;

    // views scanner
    private Button buttonBarScaner;
    private ScrollView scroolViewScanner;
    private EditText editTextScanCode;
    private TextView textViewTakenCode;
    private EditText editTextScanQuantity;
    private Button buttonScanSendDataToSignalR;
    private ProgressBar progressBarScanWait;

    // views defect
    private Button buttonBarDefect;
    private ScrollView scroolViewDefect;
    private Button buttonTakePicture;
    private ImageView imageViewOfPhotoFromCamera;
    private EditText editTextDescription;
    private Button buttonSendPhotoToMSSQL;
    private ProgressBar progressBarInDefectWait;
    private ImageView imagePhotoRotationRight;
    private ImageView imagePhotoRotationLeft;

    // views overView
    private Button buttonBarOverView;
    private LinearLayout linLayOverView;
    private ListView listViewOverView;
    private ArrayList<OverViewItem> listOverView;
    private OverWiewArrayAdapter adapterOverView;
    private ProgressBar progressBarInOverViewWait;

    // shar pref
    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    // vars
    public static final int PERMISSION_ALL = 1;
    static final int REQUEST_IMAGE_CAPTURE = 3; // to photo
    private String mCameraFileName = null;
    private Uri imageUri = null;
    private Bitmap imageBitmap = null;
    private int resultInt = 0; // to second thread response defect
    private String error = ""; // to second thread response defect
    private boolean isGetingDataFromMSSQL = false;
    private boolean isSignalRConnected = false; // if change for true than can show views (connectivityManager)
    private boolean isMSSQLConnected = false; // if change for true than can show views (connectivityManager)

    // for connectivityManager
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    // signalR
    private HubConnection hubConnection;

    // connection to MS SQL
    private ConnectionClassMSSQL connectionClassMSSQL; //Connection Class Variable
    private Connection connectionMSSQL;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screans);

        // views general
        linearLayoutBottomButtons = findViewById(R.id.linearLayoutBottomButtons);
        linLayScreansNoInternetConnection = findViewById(R.id.linLayScreansNoInternetConnection);
        linLayScreansConnectingSignalR = findViewById(R.id.linLayScreansConnectingSignalR);
        linLayScreansConnectingMSSQL = findViewById(R.id.linLayScreansConnectingMSSQL);

        // views scanner
        buttonBarScaner = findViewById(R.id.buttonBarScaner);
        scroolViewScanner = findViewById(R.id.scroolViewScanner);
        editTextScanCode = findViewById(R.id.editTextScanCode);
        textViewTakenCode = findViewById(R.id.textViewTakenCode);
        editTextScanQuantity = findViewById(R.id.editTextScanQuantity);
        buttonScanSendDataToSignalR = findViewById(R.id.buttonScanSendDataToSignalR);
        progressBarScanWait = findViewById(R.id.progressBarScanWait);

        // views defect
        buttonBarDefect = findViewById(R.id.buttonBarDefect);
        scroolViewDefect = findViewById(R.id.scroolViewDefect);
        buttonTakePicture = findViewById(R.id.buttonTakePicture);
        imageViewOfPhotoFromCamera = findViewById(R.id.imageViewOfPhotoFromCamera);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSendPhotoToMSSQL = findViewById(R.id.buttonSendPhotoToMSSQL);
        progressBarInDefectWait = findViewById(R.id.progressBarInDefectWait);
        imagePhotoRotationRight = findViewById(R.id.imagePhotoRotationRight);
        imagePhotoRotationLeft = findViewById(R.id.imagePhotoRotationLeft);

        // views overView
        buttonBarOverView = findViewById(R.id.buttonBarOverView);
        linLayOverView = findViewById(R.id.linLayOverView);
        listViewOverView = findViewById(R.id.listViewOverView);
        progressBarInOverViewWait = findViewById(R.id.progressBarInOverViewWait);

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF, MODE_PRIVATE);

        // titule action bar
        getSupportActionBar().setTitle(shar.getString(C.NAME_OF_USER, ""));

        // notification cannel
        createNotificationChannel();

        // ask for PERMISSIONS
        String[] permisionas = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasPermissions(this, permisionas)) {
            ActivityCompat.requestPermissions(this, permisionas, PERMISSION_ALL);
        }

        // start connectivity listener - start and stop EWERYTHING !
        startConnectivityManager();

        // SECTION: BUTTONS BAR TO NAVIGATE  _________________________________________________________________________________________
        buttonBarScaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setScannerSection();
            }
        });
        buttonBarDefect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeffectSection();
            }
        });
        buttonBarOverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOverViewSection();
            }
        });


        // SECTION: SCANNER ____________________________________________________________________________________________

        // when press actionSend (enter) than do this - scanner press enter itself ???
        editTextScanCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Log.d(TAG, "onEditorAction: editTextScanCode.setOnEditorActionListener:");

                    // get scanned code from editTextScanCode and put to textViewTakenCode
                    String codeFromEditText = editTextScanCode.getText().toString();
                    textViewTakenCode.setText(codeFromEditText);

                    handled = true;
                }
                return handled;
            }
        });

        // button send data
        buttonScanSendDataToSignalR.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {

                // get code
                String codeFromScanner = textViewTakenCode.getText().toString();
                if (codeFromScanner.equals("")) {
                    showAlertDialog("Zeskanuj produkt");
                    return;
                }

                // get quantity
                String quantity = editTextScanQuantity.getText().toString();
                if (quantity.equals("")) {
                    showAlertDialog("Podaj ilość.");
                    return;
                }
                if (quantity.length() > 4) {
                    showAlertDialog("Max ilośc to 9999.");
                    return;
                }
                int quantityInteger = Integer.parseInt(quantity);

                // show progressBarScanWait and hide buttonScanSendDataToSignalR
                progressBarScanWait.setVisibility(View.VISIBLE);
                buttonScanSendDataToSignalR.setVisibility(View.GONE);

                Log.d(TAG, "buttonScanSendDataToSignalR, onClick: codeFromScanner: " + codeFromScanner + ", quantityInteger: " + quantityInteger);

                // send data to signalR
                try {
                    Single<Boolean> exc = hubConnection.invoke(Boolean.class, "ScannedItem", codeFromScanner, quantityInteger); // true or false answer from signalR is random
                    exc.filter((Boolean x) -> Boolean.class.isInstance(x))
                            .cast(Boolean.class)
                            .subscribe((Boolean x) -> scannerDataResult(x)); // function to manage answer
                } catch (Exception e) {
                    Log.d(TAG, "buttonScanSendDataToSignalR, Exception: " + e);

                    // if catch exception than wait waitTime and try again connect to SignalR
                    startSignalRAgain();
                }
            }
        });

        // SECTION: DEFECT ______________________________________________________________________________________________
        // button take picture from camera
        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take picture from camera
                dispatchTakePictureIntent();
            }
        });

        imagePhotoRotationLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageBitmap != null) {
                    // rotation left 90 deg
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
                    imageBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                    //set bitmap in image view
                    imageViewOfPhotoFromCamera.setImageBitmap(imageBitmap);

                } else {
                    Toast.makeText(ActivityScreans.this, "Najpierw wybierz zdjęcie.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        imagePhotoRotationRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageBitmap != null) {
                    // rotation right 90 deg
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
                    imageBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                    //set bitmap in image view
                    imageViewOfPhotoFromCamera.setImageBitmap(imageBitmap);

                } else {
                    Toast.makeText(ActivityScreans.this, "Najpierw wybierz zdjęcie.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // button sent photo to MMSQL
        buttonSendPhotoToMSSQL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (connectionMSSQL == null) {  // if is NO connection
                    Log.d(TAG, "buttonSendPhotoToMSSQL: connection NOT CONNECTED");
                    Toast.makeText(ActivityScreans.this, "Brak połączenia z bazą danych", Toast.LENGTH_SHORT).show();
                } else { // if connection is CONNECTED
                    Log.d(TAG, "buttonSendPhotoToMSSQL: connection CONNECTED");

                    // check gotten data
                    if (imageBitmap == null) {
                        showAlertDialog("Dodaj zdjęcie");
                        return;
                    }
                    if (editTextDescription.getText().toString().equals("")) {
                        showAlertDialog("Dodaj opis");
                        return;
                    }
                    if (editTextDescription.getText().toString().length() > 99) {
                        showAlertDialog("Opis jest za długi (max 99 znaków)");
                        return;
                    }

                    // hide button Sent and show progress bar
                    buttonSendPhotoToMSSQL.setVisibility(View.GONE);
                    progressBarInDefectWait.setVisibility(View.VISIBLE);

                    // 1. dateTime - must be format: "YYYY-mm-dd hh:mm:ss"
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
                    String dateTime = sdf.format(new Date(System.currentTimeMillis()));

                    // 2. description
                    String description = editTextDescription.getText().toString();

                    // 3. image - normal size
                    Bitmap pictureFromView = imageBitmap; // bitmap
                    // bitmap into byte[]
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    pictureFromView.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] bArray = bos.toByteArray();
                    Log.d(TAG, "buttonSendPhotoToMSSQL, bArray.length: " + bArray.length);

                    // 4. user name from shar
                    String userName = shar.getString(C.NAME_OF_USER, "");

                    // 5. image - small size
                    // get size picture resized
                    int bitmapHeight = imageBitmap.getHeight();
                    int bitmapWidth = imageBitmap.getWidth();
                    int bitmapHeightResized = 100; // put Height of picture Resized - change only here
                    int bitmapWidthResized = (bitmapHeightResized * bitmapWidth) / bitmapHeight;
                    // resize bitmap to small picture
                    Bitmap pictureFromViewResized = Bitmap.createScaledBitmap(imageBitmap, bitmapWidthResized, bitmapHeightResized, true);
                    // bitmap into byte[]
                    ByteArrayOutputStream bosResized = new ByteArrayOutputStream();
                    pictureFromViewResized.compress(Bitmap.CompressFormat.JPEG, 100, bosResized);
                    byte[] bArrayResized = bosResized.toByteArray();
                    Log.d(TAG, "buttonSendPhotoToMSSQL, bArrayResized.length 2: " + bArrayResized.length);

                    new Thread(new Runnable() { // NOT in UI thread
                        @Override
                        public void run() {

                            // execute query and get result
                            try {
                                // create query
                                PreparedStatement preparedStatement = connectionMSSQL.prepareStatement("INSERT INTO dbo.androidTest(dateTime, description, picture, miniature, userName) VALUES (?, ?, ?, ?, ?)");
                                preparedStatement.setString(1, dateTime);
                                preparedStatement.setString(2, description);
                                preparedStatement.setBytes(3, bArray);
                                preparedStatement.setBytes(4, bArrayResized);
                                preparedStatement.setString(5, userName);

                                resultInt = preparedStatement.executeUpdate(); // result is OK if 1
                                //boolean resultInt = preparedStatement.execute(); // result is OK if true - don't work
                                preparedStatement.close(); // close
                            } catch (SQLException e) {
                                error = e.toString();
                                Log.d(TAG, "onCreate: SQLException: " + error);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() { //in UI thread

                                    if (resultInt == 0) { // resultInt = 0 - ERROR
                                        showAlertDialog("Nie wysłano wiadomości \n\nError: " + error);

                                        // change color of background for time
                                        changeBackgroudColorForTimeInSec(1);

                                    } else { // resultInt = 1 - SUCCES
                                        Toast.makeText(ActivityScreans.this, "Wiadomość wysłana.", Toast.LENGTH_SHORT).show();

                                        // clear views
                                        editTextDescription.setText("");
                                        imageViewOfPhotoFromCamera.setImageResource(R.drawable.question_mark);
                                        imageBitmap = null;
                                    }

                                    // hide progress bar and show button Sent
                                    buttonSendPhotoToMSSQL.setVisibility(View.VISIBLE);
                                    progressBarInDefectWait.setVisibility(View.GONE);

                                }
                            });
                        }
                    }).start();
                }
            }
        });

        // SECTION: OVERVIEW ____________________________________________________________________________________________

        // list and adapter for over View
        listOverView = new ArrayList<>();
        adapterOverView = new OverWiewArrayAdapter(this, 0, listOverView);
        listViewOverView.setAdapter(adapterOverView);
        listViewOverView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // start activity to show image in big screen
                String currentID = listOverView.get(position).getIdSQLData(); // put image to static variable
                Intent intentToShowBigPicture = new Intent(ActivityScreans.this, ActivityShowImage.class);
                intentToShowBigPicture.putExtra("intentToShowBigPicture", currentID);
                startActivity(intentToShowBigPicture);
            }
        });

    }
    // END onCreate_________________________________________________________________________________________________________________________________________________________________


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

                        //hide linLayScreansNoInternetConnection - hide inofrmation NO internet
                        linLayScreansNoInternetConnection.setVisibility(View.GONE);

                        // start signalR connection - hide linLayConnecting and show scroolViewLogin if signalR will connect, start service with notifications - start after SignalR connection available - if no than try again
                        startSignalR();

                        // start connection to MS SQL
                        startConnectionToMSSQL();

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
                        linLayScreansNoInternetConnection.setVisibility(View.VISIBLE); // show inofrmation NO internet
                        linLayScreansConnectingSignalR.setVisibility(View.VISIBLE); // show view SignalR connection ...
                        linLayScreansConnectingMSSQL.setVisibility(View.VISIBLE); // show view MS SQL connection ...
                        linearLayoutBottomButtons.setVisibility(View.GONE); // hide bottom buttons
                        scroolViewScanner.setVisibility(View.GONE); // hide view scanner
                        scroolViewDefect.setVisibility(View.GONE); // hide view deffect
                        linLayOverView.setVisibility(View.GONE); // hide view Over View

                        // stop signalR connection
                        if (hubConnection != null) {
                            hubConnection.stop();
                        }

                        // stop MS SQL connection - no need

                        // stop service
                        stopService(new Intent(ActivityScreans.this, ServiceNotifications.class));
                    }
                });
            }
        };
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    // start connection signalR
    public void startSignalR() {


        try {
            // 1. built connection
            hubConnection = HubConnectionBuilder.create(shar.getString(C.SIGNAL_R_URL_FOR_SHAR, C.SIGNAL_R_URL_STANDARD)).build();

            // 2. start connection
            hubConnection.start().blockingAwait(); // blockingAwait stop and wait to connection
            Log.d(TAG, "startSignalR ConnectionState(): " + hubConnection.getConnectionState());

            isSignalRConnected = true; // if change for true than can show views
            linLayScreansConnectingSignalR.setVisibility(View.GONE); // hide view SignalR connection ...
            showViewsIfAllConnectionsAreConnected(); // func to start main view

            //start service with notifications - start after SignalR connection available - if no than try again
            startService();


        } catch (Exception e) { // cath if hubConnection.start() is not possible
            Log.d(TAG, "ActivityScreans, startSignalR: Exception: " + e);
            Toast.makeText(this, "SignalR brak połączenia", Toast.LENGTH_SHORT).show();

            // if catch exception than wait waitTime and try again connect to SignalR
            startSignalRAgain();
        }
    }

    // if catch exception than wait waitTime and try again connect to SignalR
    public void startSignalRAgain() {

        // show buttonScanSendDataToSignalR and hide progressBarScanWait
        progressBarScanWait.setVisibility(View.GONE);
        buttonScanSendDataToSignalR.setVisibility(View.VISIBLE);

        // show and hide informations
        linLayScreansConnectingSignalR.setVisibility(View.VISIBLE); // show view SignalR connection ...
        linearLayoutBottomButtons.setVisibility(View.GONE); // hide bottom buttons
        scroolViewScanner.setVisibility(View.GONE); // hide view scanner
        scroolViewDefect.setVisibility(View.GONE); // hide view deffect
        linLayOverView.setVisibility(View.GONE); // hide view Over View

        Handler handler = new Handler();
        long waitTime = C.WAITING_TIME * 1000; // waitTime
        handler.postDelayed(new Runnable() {
            public void run() {

                // start signalR after waitTime
                startSignalR();
            }
        }, waitTime); // delay
    }


    // start connection to MS SQL
    public void startConnectionToMSSQL() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                connectionClassMSSQL = new ConnectionClassMSSQL(); // Connection Class MS SQL Initialization
                connectionMSSQL = connectionClassMSSQL.CONN(ActivityScreans.this); //Connection Object, if connection is not conected than return NULL

                Log.d(TAG, "startConnectionToMSSQL, connectionMSSQL: " + connectionMSSQL);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (connectionMSSQL == null) { // if is NO connection
                            Log.d(TAG, "startConnectionToMSSQL: connection NOT CONNECTED");
                            Toast.makeText(ActivityScreans.this, "MS SQL brak połączenia", Toast.LENGTH_LONG).show();

                            // if is no connection than try again connect after C.WAITING_TIME
                            Handler handler = new Handler();
                            long waitTime = C.WAITING_TIME * 1000; // waitTime
                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    // start Connection To MSSQL after waitTime
                                    startConnectionToMSSQL();
                                }
                            }, waitTime); // delay

                        } else { // if connection is CONNECTED
                            Log.d(TAG, "startConnectionToMSSQL: connection CONNECTED");
                            isMSSQLConnected = true; // if change for true than can show views
                            linLayScreansConnectingMSSQL.setVisibility(View.GONE); // hide view MS SQL connection ...
                            showViewsIfAllConnectionsAreConnected(); // func to start main view
                        }
                    }
                });
            }
        }).start();
    }

    // start service
    public void startService() {
        if (shar.getBoolean(C.SWITCH_NOTIFICATIONS_IS_ON, true)) {

            // stop service
            stopService(new Intent(ActivityScreans.this, ServiceNotifications.class));

            // start service
            startService(new Intent(ActivityScreans.this, ServiceNotifications.class));
        }
    }

    // func to start main view
    public void showViewsIfAllConnectionsAreConnected() {

        // check if SignalR and MS SQL is connected
        if (!isSignalRConnected) {
            return;
        }
        if (!isMSSQLConnected) {
            return;
        }

        //show wiews - setScannerSection or setDeffectSection or setOverViewSection
        setScannerSection();

    }

    // SECTION: SCANNER  _____________________________________________________________________________________________

    public void scannerDataResult(boolean result) {
        Log.d(TAG, "scannerDataResult: " + result);

        // must be on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // show buttonScanSendDataToSignalR and hide progressBarScanWait
                progressBarScanWait.setVisibility(View.GONE);
                buttonScanSendDataToSignalR.setVisibility(View.VISIBLE);

                // menage result
                if (result) {
                    Toast.makeText(ActivityScreans.this, "Wysłano.", Toast.LENGTH_SHORT).show();

                    // clear view
                    editTextScanCode.setText(""); // clear edit text
                    textViewTakenCode.setText(""); // clear text View
                    editTextScanQuantity.setText(""); // clear edit text

                } else {
                    showAlertDialog("Błąd! \nNie dostarczono.");

                    // change color of background for time
                    changeBackgroudColorForTimeInSec(1);
                }
            }
        });
    }

    // SECTION: DEFECT  _____________________________________________________________________________________________
    // take picture from camera
    private void dispatchTakePictureIntent() {  // to camera

        // must be
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //stworzenie nazwy pliku dla nowego zdjęcia, bedzie nadpisywana za każdym razem
        File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "example.jpg");

        //zapisanie ścieżki do nowego zdjęcia z aparatu
        mCameraFileName = outFile.toString();
        Uri outUri = Uri.fromFile(outFile);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "External storage not available", Toast.LENGTH_SHORT).show();
        }
    }

    // result of taken picture from camera and save picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            // camera photo
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                imageUri = Uri.fromFile(new File(mCameraFileName)); //zwraca zdjęcie z aparatu jako ścieszkę uri
                Log.d(TAG, "onActivityResult: imageUri z aparatu: " + imageUri);
            }

            // save picture to imageBitmap from taken mageUri from camera
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageViewOfPhotoFromCamera.setImageBitmap(imageBitmap); // set imageBitmap in image View
            Log.d(TAG, "onActivityResult, imageBitmap SAVED and set in imageView");

        } catch (Exception e) {
            Log.d(TAG, "onActivityResult, ERROr, imageBitmap NOT saved because e: " + e);
        }
    }


    // SECTION: OVERVIEW _________________________________________________________________________________________________________________
    // get data from serwer MS SQL
    public void getDataFromServer() {

        //can't start new request if old one is not finish
        if (isGetingDataFromMSSQL) {
            return;
        }

        // clear list before load elements
        listOverView.clear();

        // change var for true if start getting data - can't start new request if old one is not finish
        isGetingDataFromMSSQL = true;

        // show progress bar
        progressBarInOverViewWait.setVisibility(View.VISIBLE);

        if (connectionMSSQL == null) { // if is NO connection
            Log.d(TAG, "getDataFromServer: connection NOT CONNECTED");
            Toast.makeText(this, "Brak połączenia z bazą danych", Toast.LENGTH_SHORT).show();
        } else { // if connection is CONNECTED
            Log.d(TAG, "getDataFromServer: connection CONNECTED");

            new Thread(new Runnable() { // NOT in UI thread
                @Override
                public void run() {

                    // execute query and get result
                    try {

                        // set number of rows taken from MSSQL and show in list view
                        int numberOFRowsToShow = 20;

                        Statement statement = connectionMSSQL.createStatement();
                        //ResultSet result = statement.executeQuery("SELECT indx WHERE userName='Jan.kowalski'"); // query for indx,
                        //ResultSet result = statement.executeQuery("SELECT indx,dateTime,description,picture FROM dbo.androidTest"); // query to MS SQL for all rows
                        ResultSet result = statement.executeQuery("SELECT TOP " + numberOFRowsToShow + " indx,dateTime,description,miniature FROM dbo.androidTest ORDER BY indx DESC"); // query to MS SQL for last numberOFRowsToShow rows

                        // put data to array
                        for (int i = 0; i < numberOFRowsToShow; i++) { // number of rows to show - can be more than is in DB - won't be crash - olny thow exception
                            result.next(); // start from next line - must be because first line has no data
                            String indxFromResult = "" + result.getInt("indx"); // take indx from MS SQL
                            String dateTimeFromResult = result.getString("dateTime"); // take dateTime from MS SQL
                            String descriptionFromResult = result.getString("description"); // take description from MS SQL

                            // get picture miniature
                            byte[] bArrayMiniatureTakenFromMSSQL = result.getBytes("miniature"); // take picture miniature from MS SQL
                            // set picture miniature
                            Bitmap bitmapFromResultMiniature = null;
                            if (bArrayMiniatureTakenFromMSSQL != null) {
                                bitmapFromResultMiniature = BitmapFactory.decodeByteArray(bArrayMiniatureTakenFromMSSQL, 0, bArrayMiniatureTakenFromMSSQL.length); // byte[] into bitmap (miniature)
                            }

                            // add element to list view
                            listOverView.add(new OverViewItem(bitmapFromResultMiniature, indxFromResult, dateTimeFromResult, descriptionFromResult));

                            //result
                            Log.d(TAG, "getDataFromServer: RESULT: " + "indxFromResult: " + indxFromResult + ", dateTimeFromResult: " + dateTimeFromResult + ", descriptionFromResult: " + descriptionFromResult + ", number of row: " + i);

                            Log.d(TAG, "getDataFromServer: listOverView.size(): " + listOverView.size());
                        }

                    } catch (SQLException e) { // cath when is set numberOFRowsToShow more than exist - can be - no crash
                        Log.d(TAG, "onCreate: SQLException: " + e);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { //in UI thread

                            // reverse list if need
                            //Collections.reverse(listOverView);

                            // notyfi adapter
                            adapterOverView.notifyDataSetChanged();

                            // hide progress bar
                            progressBarInOverViewWait.setVisibility(View.GONE);

                            // change var for false if end getting data - can't start new request if old one is not finish
                            isGetingDataFromMSSQL = false;
                        }
                    });
                }
            }).start();
        }
    }

    // SECTION: REST _____________________________________________ __________________________________________________________

    // change color of background for time
    public void changeBackgroudColorForTimeInSec(double time) {
        long timeLong = (long) (time * 1000);

        // make vibration
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }

        //play sound
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.moonless);
        mediaPlayer.start();

        //change color
        scroolViewScanner.setBackgroundColor(Color.RED);
        scroolViewDefect.setBackgroundColor(Color.RED);
        linLayOverView.setBackgroundColor(Color.RED);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after
                scroolViewScanner.setBackgroundColor(Color.TRANSPARENT);
                scroolViewDefect.setBackgroundColor(Color.TRANSPARENT);
                linLayOverView.setBackgroundColor(Color.TRANSPARENT);
            }
        }, timeLong); // delay
    }


    // ask for permissions
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // show alert dialog
    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityScreans.this);
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

    // SECTION: BUTTONS BAR TO NAVIGATE  _______________________________________________________________________________________________________
    public void setScannerSection() {
        // set liner layouts visibility
        linearLayoutBottomButtons.setVisibility(View.VISIBLE);
        scroolViewScanner.setVisibility(View.VISIBLE);
        scroolViewDefect.setVisibility(View.GONE);
        linLayOverView.setVisibility(View.GONE);

        // set buttons background
        buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorPrimaryDarkBlue900));
        buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));
        buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBackgroundGray200));
        buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBackgroundGray200));
        buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
    }

    public void setDeffectSection() {
        // set liner layouts visibility
        linearLayoutBottomButtons.setVisibility(View.VISIBLE);
        scroolViewScanner.setVisibility(View.GONE);
        scroolViewDefect.setVisibility(View.VISIBLE);
        linLayOverView.setVisibility(View.GONE);

        // set buttons background
        buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBackgroundGray200));
        buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorPrimaryDarkBlue900));
        buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));
        buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBackgroundGray200));
        buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
    }

    public void setOverViewSection() {

        // set liner layouts visibility
        linearLayoutBottomButtons.setVisibility(View.VISIBLE);
        scroolViewScanner.setVisibility(View.GONE);
        scroolViewDefect.setVisibility(View.GONE);
        linLayOverView.setVisibility(View.VISIBLE);

        // set buttons background
        buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBackgroundGray200));
        buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBackgroundGray200));
        buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorPrimaryDarkBlue900));
        buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));

        // get data from server
        getDataFromServer();
    }

    // SECTION: MENU  _______________________________________________________________________________________________________

    // create notification channel
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Inwentaryzacja";
            String description = "Powiadomienia";
            int importance = NotificationManager.IMPORTANCE_LOW; // low importance - no sound
            NotificationChannel channel = new NotificationChannel(C.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_screans, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_info:
                startActivity(new Intent(ActivityScreans.this, ActivityInfo.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(ActivityScreans.this, ActivitySettings.class));
                break;
            case R.id.menu_log_out:
                // save user logged OUT in shar pref
                editor = shar.edit();
                editor.putBoolean(C.USER_IS_LOGGED, false);
                editor.apply();

                // stop signalR connection
                if (hubConnection != null) {
                    hubConnection.stop().blockingAwait(); //  wait for stop
                }

                // stop connectivity listener
                connectivityManager.unregisterNetworkCallback(networkCallback);

                // stop service
                Intent intent = new Intent(ActivityScreans.this, ServiceNotifications.class);
                stopService(intent);

                // finish current activity
                finish();

                // start login activity
                startActivity(new Intent(ActivityScreans.this, ActivityLogin.class));

                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
