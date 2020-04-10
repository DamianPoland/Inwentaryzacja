package com.wolfmobileapps.inwentaryzacja;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ActivityShowImage extends AppCompatActivity {

    // views
    ProgressBar progressBarWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        //views
        progressBarWaiting = findViewById(R.id.progressBarWaiting);

        // get currentID from intent
        Intent intent = getIntent();
        String currentID = intent.getStringExtra("intentToShowBigPicture");

        // progress bar waiting VISIBLE
        progressBarWaiting.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // execute query and get result
                    ConnectionClassMSSQL connectionClassMSSQL = new ConnectionClassMSSQL(); // Connection Class MS SQL Initialization
                    Connection connectionMSSQL = connectionClassMSSQL.CONN(ActivityShowImage.this); //Connection Object
                    Statement statement = connectionMSSQL.createStatement();
                    ResultSet result = statement.executeQuery("SELECT picture FROM dbo.androidTest WHERE indx IN (" + currentID + ")"); // query to MS SQL for last numberOFRowsToShow rows

                    // get picture BIG
                    result.next();
                    byte[] bArrayTakenFromMSSQL = result.getBytes("picture"); // take picture from MS SQL
                    // set picture BIG in bitmap
                    Bitmap bitmapFromResult = null;
                    if (bArrayTakenFromMSSQL != null) {
                        bitmapFromResult = BitmapFactory.decodeByteArray(bArrayTakenFromMSSQL, 0, bArrayTakenFromMSSQL.length); // byte[] into bitmap
                    }

                    Bitmap finalBitmapFromResult = bitmapFromResult;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // progress bar waiting GONE
                            progressBarWaiting.setVisibility(View.GONE);

                            // show image in big screen
                            TouchImageView img = new TouchImageView(ActivityShowImage.this);
                            img.setImageBitmap(finalBitmapFromResult); // get image from static variable from Activity Screans
                            img.setMaxZoom(4f);
                            setContentView(img);


                        }
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
