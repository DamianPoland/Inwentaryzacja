package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;


public class ActivityScreans extends AppCompatActivity {

    private static final String TAG = "ActivityScreans";

    // views scanner
    private Button buttonBarScaner;
    private ScrollView scroolViewScanner;

    // views defect
    private Button buttonBarDefect;
    private ScrollView scroolViewDefect;
    private Button buttonTakePicture;
    private ImageView imageViewOfPhotoFromCamera;
    private EditText editTextDescription;
    private Button buttonSendPhotoToMSSQL;
    private ProgressBar progressBarInDefectWait;

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

    // connection to MS SQL
    private ConnectionClassMSSQL connectionClassMSSQL; //Connection Class Variable
    private Connection connectionMSSQL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screans);

        // views scanner
        buttonBarScaner = findViewById(R.id.buttonBarScaner);
        scroolViewScanner = findViewById(R.id.scroolViewScanner);

        // views defect
        buttonBarDefect = findViewById(R.id.buttonBarDefect);
        scroolViewDefect = findViewById(R.id.scroolViewDefect);
        buttonTakePicture = findViewById(R.id.buttonTakePicture);
        imageViewOfPhotoFromCamera = findViewById(R.id.imageViewOfPhotoFromCamera);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSendPhotoToMSSQL = findViewById(R.id.buttonSendPhotoToMSSQL);
        progressBarInDefectWait = findViewById(R.id.progressBarInDefectWait);

        // views overView
        buttonBarOverView = findViewById(R.id.buttonBarOverView);
        linLayOverView = findViewById(R.id.linLayOverView);
        listViewOverView = findViewById(R.id.listViewOverView);
        progressBarInOverViewWait = findViewById(R.id.progressBarInOverViewWait);

        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF,MODE_PRIVATE);

        // ask for PERMISSIONS
        String[] permisionas = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasPermissions(this, permisionas)) {
            ActivityCompat.requestPermissions(this, permisionas, PERMISSION_ALL);
        }

        // when app starts open Deffect section
        setScannerSection();

        // start connection to MS SQL
        startConnectionToMSSQL();

        // list and adapter for over View
        listOverView = new ArrayList<>();
        adapterOverView = new OverWiewArrayAdapter(this, 0 , listOverView);
        listViewOverView.setAdapter(adapterOverView);

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





        // SECTION: DEFECT ______________________________________________________________________________________________
        // button take picture from camera
        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take picture from camera
                dispatchTakePictureIntent();
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
                    if (editTextDescription.getText().toString().equals("")){
                        showAlertDialog("Dodaj opis");
                        return;
                    }
                    if (editTextDescription.getText().toString().length() > 99){
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

                        // 3. image
                        Bitmap pictureFromView = imageBitmap; // bitmap
                        // bitmap into byte[]
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        pictureFromView.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        byte[] bArray = bos.toByteArray();

                        // 4. user name from shar
                        String userName = shar.getString(C.NAME_OF_USER, "");

                        new Thread(new Runnable() { // NOT in UI thread
                            @Override
                            public void run() {

                                // execute query and get result
                                try {
                                    // create query
                                    PreparedStatement preparedStatement = connectionMSSQL.prepareStatement("INSERT INTO dbo.androidTest(dateTime, description, picture, userName) VALUES (?, ?, ?, ?)");
                                    preparedStatement.setString(1, dateTime);
                                    preparedStatement.setString(2, description);
                                    preparedStatement.setBytes(3, bArray);
                                    preparedStatement.setString(4, userName);

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
                                        } else { // resultInt = 1 - SUCCES
                                            Toast.makeText(ActivityScreans.this, "Wiadomość wysłana.", Toast.LENGTH_SHORT).show();
                                        }

                                        // hide progress bar and show button Sent
                                        buttonSendPhotoToMSSQL.setVisibility(View.VISIBLE);
                                        progressBarInDefectWait.setVisibility(View.GONE);

                                        // clear views
                                        editTextDescription.setText("");
                                        imageViewOfPhotoFromCamera.setImageResource(R.drawable.question_mark);
                                    }
                                });
                            }
                        }).start();
                }
            }
        });

        // SECTION: OVERVIEW ____________________________________________________________________________________________




    }
    // END onCreate_________________________________________________________________________________________________________________________________________________________________


    // start connection to MS SQL
    public void startConnectionToMSSQL () {

        connectionClassMSSQL = new ConnectionClassMSSQL(); // Connection Class MS SQL Initialization
        connectionMSSQL = connectionClassMSSQL.CONN(); //Connection Object

        if (connectionMSSQL == null) {  // if is NO connection
            Log.d(TAG, "startConnectionToMSSQL: connection NOT CONNECTED");
        } else { // if connection is CONNECTED
            Log.d(TAG, "startConnectionToMSSQL: connection CONNECTED");
        }
    }

    // SECTION: SCANNER  _____________________________________________________________________________________________





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

        // camera photo
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = Uri.fromFile(new File(mCameraFileName)); //zwraca zdjęcie z aparatu jako ścieszkę uri
            Log.d(TAG, "onActivityResult: imageUri z aparatu: " + imageUri);
        }

        // save picture to imageBitmap from taken mageUri from camera
        try {
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageViewOfPhotoFromCamera.setImageBitmap(imageBitmap); // set imageBitmap in image View
            Log.d(TAG, "onActivityResult, imageBitmap SAVED and set in imageView");
        } catch (IOException e) {
            Log.d(TAG, "onActivityResult, ERROr, imageBitmap NOT saved because e: " + e);
        }
    }



    // SECTION: OVERVIEW _________________________________________________________________________________________________________________
    // get data from serwer MS SQL
    public void getDataFromServer () {

        // clear list before load elements
        listOverView.clear();

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
                        int numberOFRowsToShow = 5;

                        Statement statement = connectionMSSQL.createStatement();
                        //ResultSet result = statement.executeQuery("SELECT indx WHERE userName='Jan.kowalski'"); // query for indx,
                        //ResultSet result = statement.executeQuery("SELECT indx,dateTime,description,picture FROM dbo.androidTest"); // query to MS SQL for all rows
                        ResultSet result = statement.executeQuery("SELECT TOP " + numberOFRowsToShow + " indx,dateTime,description,picture FROM dbo.androidTest ORDER BY indx DESC"); // query to MS SQL for last numberOFRowsToShow rows

                        // put data to array
                        for (int i = 0; i < numberOFRowsToShow; i++) { // number of rows to show - can be more than is in DB - won't be crash - olny thow exception
                            result.next(); // start from next line - must be because first line has no data
                            String indxFromResult = "" + result.getInt("indx"); // take indx from MS SQL
                            String dateTimeFromResult = result.getString("dateTime"); // take dateTime from MS SQL
                            String descriptionFromResult = result.getString("description"); // take description from MS SQL

                            // get picture
                            byte[] bArrayTakenFromMSSQL = result.getBytes("picture"); // take picture from MS SQL
                            // set picture
                            Bitmap bitmapFromResult = null;
                            if (bArrayTakenFromMSSQL != null){
                                bitmapFromResult = BitmapFactory.decodeByteArray(bArrayTakenFromMSSQL, 0 ,bArrayTakenFromMSSQL.length); // byte[] into bitmap
                            }

                            // add element to list view
                            listOverView.add(new OverViewItem(bitmapFromResult,indxFromResult,dateTimeFromResult, descriptionFromResult));

                            //result
                            Log.d(TAG, "getDataFromServer: RESULT: " + "indxFromResult: " + indxFromResult + ", dateTimeFromResult: " + dateTimeFromResult + ", descriptionFromResult: " + descriptionFromResult  +  ", number of row: " + i);
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
                        }
                    });
                }
            }).start();
        }
    }

    // SECTION: REST _____________________________________________ __________________________________________________________
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
    public void setScannerSection () {
        // set liner layouts visibility
        scroolViewScanner.setVisibility(View.VISIBLE);
        scroolViewDefect.setVisibility(View.GONE);
        linLayOverView.setVisibility(View.GONE);

        // set buttons background
        buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorPrimaryBlue600));
        buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));
        buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
        buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
        buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
    }
    public void setDeffectSection () {
        // set liner layouts visibility
        scroolViewScanner.setVisibility(View.GONE);
        scroolViewDefect.setVisibility(View.VISIBLE);
        linLayOverView.setVisibility(View.GONE);

        // set buttons background
        buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
        buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorPrimaryBlue600));
        buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));
        buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
        buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
    }
    public void setOverViewSection () {

        // set liner layouts visibility
        scroolViewScanner.setVisibility(View.GONE);
        scroolViewDefect.setVisibility(View.GONE);
        linLayOverView.setVisibility(View.VISIBLE);

        // set buttons background
        buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
        buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
        buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
        buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorPrimaryBlue600));
        buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));

        // get data from server
        getDataFromServer();
    }
}
