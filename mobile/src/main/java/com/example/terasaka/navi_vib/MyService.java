package com.example.terasaka.navi_vib;

/**
 * Created by Terasaka on 2016/09/27.
 */

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class MyService extends WearableListenerService {
    private static final String TAG = "MyService";
    private int i = 0;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.v(TAG, "onMessageReceived");

        Log.d("MyService", "onMessageReceived");
        Log.d("MyService", messageEvent.getPath());
        Log.d("MyService", new String(messageEvent.getData()));
        Log.d("","");
            //Toast.makeText(getApplicationContext(), "データ受け取り完了", Toast.LENGTH_LONG).show();
        //if (messageEvent.getPath().equals("/arrival")) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on watch is: " + message);
            // Broadcast message to wearable activity for display


            Intent i2 = new Intent(this, Compass.class);
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND);
            i.putExtra("message", message);
            i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i2);

            int x = 5;
            i.putExtra("z",x);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        //}else{
           // super.onMessageReceived(messageEvent);
        //}




    }
}
/*


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.v(TAG, "onMessageReceived");

        if (messageEvent.getPath().equals("/arrival")) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on watch is: " + message);
            // Broadcast message to wearable activity for display


            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND);
            i.putExtra("message", message);

            int x = 1;
            i.putExtra("z",x);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }else{
            super.onMessageReceived(messageEvent);
        }

    }
}*/