package com.wolfmobileapps.inwentaryzacja;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.security.PrivateKey;

public class ServiceNotifications extends Service {

    private static final String TAG = "ServiceNotifications";

    private Notification notification;

    private HubConnection hubConnection = null;

    public ServiceNotifications() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // właczenie servisu
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // create notification
        setTheNotificationWithTapAction();
        // wystartowanie service
        startForeground(101, notification); //nadać unikalne Id, notification z j.w.

        startSignalR(); // start signalR

        return START_STICKY; // what to do when memmory lack
    }

    // start signalR connection
    public void startSignalR() {

        // if service is running than not start new one
        if (hubConnection != null) {
            return;
        }

        // start service
        try {
            // 1. built connection
            hubConnection = HubConnectionBuilder.create(C.SERWER_URL).build();

            // 2 built method to show alert sended by signalR - must be build after build connection and before start connection
            hubConnection.on("Alert", (alert) -> {
                Log.d(TAG, "New Alert from signalR: " + alert);
                Intent intentFromService = new Intent(ServiceNotifications.this, ActivityAlertFromService.class);
                intentFromService.putExtra(C.INTENT_FROM_SERVICE , alert);
                startActivity(intentFromService);
            }, String.class);

            // 3. start connection
            hubConnection.start().blockingAwait(); // blockingAwait stop and wait to connection
            Log.d(TAG, "startSignalR ConnectionState(): " + hubConnection.getConnectionState());

        } catch (Exception e) { // cath if hubConnection.start() is not possible
            Log.d(TAG, "ServiceNotifications, startSignalR: Exception: " + e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // stop signalR connection
        if (hubConnection != null) {
            hubConnection.stop(); //  wait for stop -can't be .blockingAwait() because of runtime Exception
        }
    }

    // create notification
    public void setTheNotificationWithTapAction() {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, ActivityScreans.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, C.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_info_white_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        notification = builder.build();
    }


}
