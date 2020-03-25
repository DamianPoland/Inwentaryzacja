package com.wolfmobileapps.inwentaryzacja;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClassMSSQL {
    @SuppressLint("NewApi")
    public static Connection CONN() {

        String _user = "androidAccess"; // user naem
        String _pass = "Wiosna2020123..."; // user password
        String _DB = "Inwentaryzacja"; // DB name
        String _server = "lachmana.dyndns.org:1433"; // serwer url: "lachmana.dyndns.org:1433"


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + _server + ";"
                    + "databaseName=" + _DB + ";user=" + _user + ";password="
                    + _pass + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException se) {
            Log.e("ERRO - ConnectionClassMSSQL: ", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO - ConnectionClassMSSQL: ", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO - ConnectionClassMSSQL: ", e.getMessage());
        }
        return conn;
    }
}
