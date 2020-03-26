package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;





public class ActivityScreans extends AppCompatActivity {

    private static final String TAG = "ActivityScreans";

    // views scanner
    private Button buttonBarScaner;
    private LinearLayout linLayScanner;

    // views defect
    private Button buttonBarDefect;
    private LinearLayout linLayDefect;
    private Button buttonTakePicture;
    private ImageView imageViewOfPhotoFromCamera;
    private EditText editTextDescription;
    private Button buttonSendPhotoToMSSQL;

    // views overView
    private Button buttonBarOverView;
    private LinearLayout linLayOverView;
    private Button buttonGetDataFromMSSQL;
    private ImageView imageViewHelper;

    // shar pref
    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    // vars
    public static final int PERMISSION_ALL = 1;
    static final int REQUEST_IMAGE_CAPTURE = 3; // to photo
    private String mCameraFileName = null;
    private Uri imageUri = null;
    private Bitmap imageBitmap = null;

    // connection to MS SQL
    private ConnectionClassMSSQL connectionClassMSSQL; //Connection Class Variable
    private Connection connectionMSSQL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screans);

        // views scanner
        buttonBarScaner = findViewById(R.id.buttonBarScaner);
        linLayScanner = findViewById(R.id.linLayScanner);

        // views defect
        buttonBarDefect = findViewById(R.id.buttonBarDefect);
        linLayDefect = findViewById(R.id.linLayDefect);
        buttonTakePicture = findViewById(R.id.buttonTakePicture);
        imageViewOfPhotoFromCamera = findViewById(R.id.imageViewOfPhotoFromCamera);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSendPhotoToMSSQL = findViewById(R.id.buttonSendPhotoToMSSQL);

        // views overView
        buttonBarOverView = findViewById(R.id.buttonBarOverView);
        linLayOverView = findViewById(R.id.linLayOverView);
        buttonGetDataFromMSSQL = findViewById(R.id.buttonGetDataFromMSSQL);
        imageViewHelper = findViewById(R.id.imageViewHelper);



        // shar pref
        shar = getSharedPreferences(C.NAME_OF_SHAR_PREF,MODE_PRIVATE);

        // ask for PERMISSIONS
        String[] permisionas = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasPermissions(this, permisionas)) {
            ActivityCompat.requestPermissions(this, permisionas, PERMISSION_ALL);
        }

        // start connection to MS SQL
        startConnectionToMSSQL();

        // SECTION: BUTTONS BAR TO NAVIGATE  _____________________________________________________________
        buttonBarScaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // set liner layouts visibility
                linLayScanner.setVisibility(View.VISIBLE);
                linLayDefect.setVisibility(View.GONE);
                linLayOverView.setVisibility(View.GONE);

                // set buttons background
                buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorPrimaryBlue600));
                buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));
                buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
                buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
                buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
                buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
            }
        });
        buttonBarDefect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // set liner layouts visibility
                linLayScanner.setVisibility(View.GONE);
                linLayDefect.setVisibility(View.VISIBLE);
                linLayOverView.setVisibility(View.GONE);

                // set buttons background
                buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
                buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
                buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorPrimaryBlue600));
                buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));
                buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
                buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
            }
        });
        buttonBarOverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // set liner layouts visibility
                linLayScanner.setVisibility(View.GONE);
                linLayDefect.setVisibility(View.GONE);
                linLayOverView.setVisibility(View.VISIBLE);

                // set buttons background
                buttonBarScaner.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
                buttonBarScaner.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
                buttonBarDefect.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorBackgroundGray200));
                buttonBarDefect.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorBlack));
                buttonBarOverView.setBackgroundColor(ContextCompat.getColor(ActivityScreans.this,R.color.colorPrimaryBlue600));
                buttonBarOverView.setTextColor(ContextCompat.getColor(ActivityScreans.this, R.color.colorWhite));
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
                } else { // if connection is CONNECTED
                    Log.d(TAG, "buttonSendPhotoToMSSQL: connection CONNECTED");

                    try {

                        // 1. dateTime - must be format: "YYYY-mm-dd hh:mm:ss"
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                        String dateTime = sdf.format(new Date(System.currentTimeMillis()));

                        // 2. description
                        String description = editTextDescription.getText().toString();
                        editTextDescription.setText(""); // clear view

                        // 3. image - Implicit conversion from data type varchar to varbinary(max) is not allowed. Use the CONVERT function to run this query.
                        // bitmap into byte[]
                        Bitmap pictureFromView = imageBitmap; // bitmap
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        pictureFromView.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        byte[] bArray = bos.toByteArray();

                        InputStream inputStream = new ByteArrayInputStream(bArray);

                        //Blob blob = new javax.sql.rowset.serial.oracle.sql.BLOB(pdfBytes);

                        //Blob blob1 = new SerialBlob
//                        Blob blob = new javax.sql.rowset.serial.SerialBlob(bArray);

                        //String encodedImage = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);

                        // byte[] into bitmap
                        Bitmap bm = BitmapFactory.decodeByteArray(bArray, 0 ,bArray.length);
                        //Bitmap  bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length); // from blob

                        // set image second
                        ImageView imageViewOfPhotoFromCameraSecond = findViewById(R.id.imageViewOfPhotoFromCameraSecond);
                        imageViewOfPhotoFromCameraSecond.setImageBitmap(bm);

                        Log.d(TAG, "onClick: bArray: " + bArray );

                        // 4. user name from shar
                        String userName = shar.getString(C.NAME_OF_USER, "");

                        // create query
                        String queryStatement = "INSERT INTO dbo.androidTest(dateTime, description, userName) VALUES (?, ?, ?)";

                        PreparedStatement preparedStatement = connectionMSSQL.prepareStatement("INSERT INTO dbo.androidTest(dateTime, description, userName, picture) VALUES (?, ?, ?, ?)");
                        //preparedStatement.setBytes(1, bArray); // parameterIndex+1 - first ? in scope, must be "?" instead bArray
                        //preparedStatement.setBlob(1, inputStream, bArray.length);
                        //preparedStatement.setBytes(1, bArray);

                        preparedStatement.setString(1, dateTime);
                        preparedStatement.setString(2, description);
                        preparedStatement.setString(3, userName);
                        preparedStatement.setBytes(4, bArray);



                        int resultInt = preparedStatement.executeUpdate(); // result is OK if 1
                        //boolean resultInt = preparedStatement.execute(); // result is OK if true
                        preparedStatement.close();
                        Log.d(TAG, "buttonSendPhotoToMSSQL: EndOf TRY - Seccess ? + resultInt: " + resultInt);






//                        // create query
//                        String queryStatement = "Insert into dbo.androidTest " + // MS SQL DB name is  "dbo.androidTest "
//                                " (dateTime, description, picture, userName) values " // names of vars (columns) send to MS SQL DB - dateTime(NOT null, date time format), description(can be null, String max 100 chars), picture(can be null, varbinary format), userName(NOT null String max 50 chars),
//                                + "('"
//                                + dateTime
//                                + "','"
//                                + description
//                                + "','"
//                                + "?"
//                                + "','"
//                                + userName
//                                + "')";
//                        PreparedStatement preparedStatement = connectionMSSQL.prepareStatement(queryStatement);
//                        //preparedStatement.setBytes(1, bArray); // parameterIndex+1 - first ? in scope, must be "?" instead bArray
//                        //preparedStatement.setBlob(1, inputStream, bArray.length);
//                        preparedStatement.setBytes(1, bArray);
//                        int resultInt = preparedStatement.executeUpdate(); // result is OK if 1
//                        //boolean resultInt = preparedStatement.execute(); // result is OK if true
//                        preparedStatement.close();
//                        Log.d(TAG, "buttonSendPhotoToMSSQL: EndOf TRY - Seccess ? + resultInt: " + resultInt);

                    } catch (SQLException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onCreate: SQLException: " + e);
                    }
                }
            }
        });

        // SECTION: OVERVIEW ____________________________________________________________________________________________


        buttonGetDataFromMSSQL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFromServer();
            }
        });

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

        // zdjęcie z aparatu
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = Uri.fromFile(new File(mCameraFileName)); //zwraca zdjęcie z aparatu jako ścieszkę uri
            Log.d(TAG, "onActivityResult: imageUri z aparatu: " + imageUri);
        }

        // zapisuje zdjęcie do imageBitmap na podstawie pobranego imageUri z kamery lub z dysku
        try {
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Log.d(TAG, "onActivityResult: blok try do zapisania imageBitmap");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //pokazanie zapisanego zdjęcia w imageView
        imageViewOfPhotoFromCamera.setImageBitmap(imageBitmap);
    }



    // SECTION: OVERVIEW _________________________________________________________________________________________________________________
    // get data from serwer MS SQL
    public void getDataFromServer () {

        if (connectionMSSQL == null) { // if is NO connection
            Log.d(TAG, "getDataFromServer: connection NOT CONNECTED");
        } else { // if connection is CONNECTED
            Log.d(TAG, "getDataFromServer: connection CONNECTED");

            try {
                Statement statement = connectionMSSQL.createStatement();
                //ResultSet result = statement.executeQuery("SELECT userName,description FROM dbo.androidTest");
                // ResultSet result = statement.executeQuery("SELECT userName WHERE userName='Jan.kowalski'");
                ResultSet result = statement.executeQuery("SELECT dateTime,description,userName,picture FROM dbo.androidTest"); // query to data
                Log.d(TAG, "getDataFromServer: result: " + result);

                for (int i = 0; i < 20; i++) { // number of rows to show - can be more than is in DB - won't be crash - olny thow exception
                    result.next(); // star from next line - must be because first line has no data
                    String dateTimeFromResult = result.getString("dateTime");
                    String descriptionFromResult = result.getString("description");
                    String userNameFromResult = result.getString("userName");


                    // take image from MS SQL
                    byte[] bArrayTakenFromMSSQL = result.getBytes("picture");

                    if (bArrayTakenFromMSSQL != null){
                        // byte[] into bitmap
                        Bitmap bmTakenFromMSSQL = BitmapFactory.decodeByteArray(bArrayTakenFromMSSQL, 0 ,bArrayTakenFromMSSQL.length);
                        // set image in image view
                        imageViewHelper.setImageBitmap(bmTakenFromMSSQL);
                    }




                    //result
                    Log.d(TAG, "getDataFromServer: RESULT: dateTimeFromResult: " + dateTimeFromResult + ", descriptionFromResult: " + descriptionFromResult  + ", userNameFromResult: " + userNameFromResult +  ", number of row: " + i);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // SECTION: PERMISSIONS_____________________________________________ __________________________________________________________
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

}
