package androidsamples.java.tictactoe;

import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage message) {
//        super.onMessageReceived(message);
//        getFirebaseMessage(message.getNotification().getTitle(),message.getNotification().getBody());
//    }
//
//    public void getFirebaseMessage(String title,String msg){
//        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"My Notification").
//                setSmallIcon(R.drawable.ic_launcher_background).
//                setContentTitle("Game Notification").
//                setContentText("A new Two-Player").
//                setAutoCancel(true);
//        NotificationManagerCompat manager= NotificationManagerCompat.from(this);
//        manager.notify(101,builder.build());
//    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        getFirebaseMessage(message.getNotification().getTitle(),message.getNotification().getBody());
    }
    public void getFirebaseMessage(String title,String msg)
    {
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"My Notification").
                setContentTitle("Game Notification").
                setContentText("A new Two-Player Game has been started.").
                setAutoCancel(true);
        NotificationManagerCompat manager=NotificationManagerCompat.from(this);
        manager.notify(101,builder.build());
    }
}
