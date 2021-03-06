package com.m.sofiane.go4lunch.services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.m.sofiane.go4lunch.R;
import com.m.sofiane.go4lunch.activity.MainActivity;
import com.m.sofiane.go4lunch.models.NameOfResto;
import com.m.sofiane.go4lunch.utils.MyChoiceHelper;

import java.util.Objects;

/**
 * created by Sofiane M. 26/04/2020
 */
public class NotificationService extends BroadcastReceiver {

    Context mContext;
    String mNameForNotif;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        readDataFromFirebase(intent);
    }

    public void readDataFromFirebase(Intent intent) {
        MyChoiceHelper.readMyChoice().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (Objects.requireNonNull(document).exists()) {
                    NameOfResto l = document.toObject(NameOfResto.class);

                    if (Objects.requireNonNull(l).getId().equals("2")) {
                        mNameForNotif = mContext.getString(R.string.nochoicenotif);
                    } else {
                        mNameForNotif = mContext.getString(R.string.choicenotif) + l.getNameOfResto();
                    }

                    createNotification(intent, mNameForNotif);
                } else {
                    mNameForNotif = mContext.getString(R.string.nochoicenotif);
                }
            }
        });

    }

    private void createNotification(Intent intent, String mNameForNotif) {
        Intent intent1 = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent1, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Go 4 Lunch");
        inboxStyle.addLine(mNameForNotif);

        String channelId = "MyID";


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext, channelId)
                        .setSmallIcon(R.drawable.ic_local_dining_white_24dp)
                        .setContentTitle("Go 4 Lunch")
                        .setContentText("It's time to Lunch")
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Message";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);

        }
        Objects.requireNonNull(notificationManager).notify("TAG", 120, notificationBuilder.build());
        deleteFirebaseitem();
    }

    private void deleteFirebaseitem() {
        MyChoiceHelper.initMyChoice();

    }

}