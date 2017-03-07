package com.example.terasaka.navi_vib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Terasaka on 2016/09/27.
 */
public class Navigation extends Activity {
    private GoogleApiClient client;
    private Vibrator vib;
    private TextView mTextView;
    private static final String TAG = "Navigation";
    private long[] pattern = {100, 600, 200, 600};
    private String receivedMessage = null;
    private String message = "";
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circled_image_view2);



        Log.v(TAG, "onCreate()");
        this.vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(messageReceiver, messageFilter);

        //setScreen();
    }
/*
    private void setScreen(){
        setContentView(R.layout.activity_circled_image_view);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                textView = (TextView) stub.findViewById(R.id.text_view);
                textView.setText(message);
            }
        });
    }
*/
    public class MessageReceiver extends BroadcastReceiver {
        private static final String TAG = "MessageReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            receivedMessage = intent.getStringExtra("message");
            Log.d("MessageReceiver", "onReceive() receivedMessage = "+ receivedMessage);

            if (receivedMessage != null) {
                // 単発40ミリ秒バイブする場合
                vib.vibrate(pattern, -1);
                Log.d("MessageReceiver", "onReceive() receivedMessage = "+ 000 );

                int x = intent.getIntExtra("z",0);
                if(x == 1){
                    Log.d("MessageReceiver", "onReceive() receivedMessage = "+ 1 );
                    ((ImageView) findViewById(R.id.arrow)).setImageResource(R.drawable.arrival);

                }if(x == 2){
                    Log.d("MessageReceiver", "onReceive() receivedMessage = "+ 2 );
                    ((ImageView) findViewById(R.id.arrow)).setImageResource(R.drawable.right_arrow_gray002);
                }if(x == 3){
                    Log.d("MessageReceiver", "onReceive() receivedMessage = "+ 3 );
                    ((ImageView) findViewById(R.id.arrow)).setImageResource(R.drawable.right_arrow_gray);
                }

                if(receivedMessage == "Hello world") {
                    // Display message in UI
                    message = receivedMessage;
                    Log.d("MessageReceiver", "receivedMessage =" + "Hello");
                    ((ImageView) findViewById(R.id.arrow)).setImageResource(R.drawable.straight_arrow_gray);

                }else if(receivedMessage == "arrival"){
                    message = receivedMessage;
                    Log.d("MessageReceiver", "receivedMessage =" + "Arrival");
                }else if(receivedMessage == "left"){
                    message = receivedMessage;
                    Log.d("MessageReceiver", "receivedMessage =" + "Left");
                    ((ImageView) findViewById(R.id.arrow)).setImageResource(R.drawable.right_arrow_gray002);

                }else if(receivedMessage == "right"){
                    message = receivedMessage;
                    Log.d("MessageReceiver", "receivedMessage =" + "Right");
                    ((ImageView) findViewById(R.id.arrow)).setImageResource(R.drawable.right_arrow_gray);
                }
            } else {
                receivedMessage = "No Message";
                Log.d("MessageReceiver", "receivedMessage = No Message");
            }

            //setScreen();
        }
    }
}
/*
 <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/arrow"
        android:src="@drawable/right_arrow_gray"
        android:layout_gravity="center_horizontal" />

  <android.support.wearable.view.CircledImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:id="@+id/arrow"
        android:src="@drawable/right_arrow_gray"
        app:circle_border_color="#3d3d3d"
        app:circle_border_width="2dp"
        app:circle_color="#3d3d3d"
        app:circle_radius="40dp"
        app:circle_radius_pressed="60dp"/>
 */