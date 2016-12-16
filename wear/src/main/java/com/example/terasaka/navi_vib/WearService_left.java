package com.example.terasaka.navi_vib;

/**
 * Created by Terasaka on 2016/10/24.
 */
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearService_left extends WearableListenerService {
    private static final String TAG = "WearService_left";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.v(TAG, "onMessageReceived");

        if (messageEvent.getPath().equals("/left")) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on watch is: " + message);
            // Broadcast message to wearable activity for display

            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND);
            i.putExtra("message", message);

            int x = 2;
            i.putExtra("z",x);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }else{
            super.onMessageReceived(messageEvent);
        }

    }




}

