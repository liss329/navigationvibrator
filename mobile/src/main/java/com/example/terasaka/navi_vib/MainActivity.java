package com.example.terasaka.navi_vib;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private GoogleApiClient client;
    private static String TAG = "MainActivity";
    private int counter = 0;

    // メニューアイテム識別用のID
    private static final int MENU_ID_A = 0;
    private static final int MENU_ID_B = 1;
    private static final int MENU_ID_C = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_main);
        // ツールバーをアクションバーとしてセット
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Intent i = new Intent(this, Compass.class);
        final Intent i2 = new Intent(this, SubActivity.class);
        final Intent i3 = new Intent(this, SensorManaged.class);


      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        this.client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        this.client.connect();


        Button btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener(){
            public  void onClick(View v){
                //list画面を表示
                i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i2);
            }
        });

        Button vbtn = (Button)findViewById(R.id.vbtn);
        vbtn.setOnClickListener(new View.OnClickListener(){
            public  void onClick(View v){

                clickevent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }
        });

        Button run_btn = (Button)findViewById(R.id.run_btn);
        run_btn.setOnClickListener(new View.OnClickListener(){
            public  void onClick(View v){
                clickevent();
                i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i3);
//                i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i3);
              /*  String sendMessage = "Test Message Api: " + counter;
                counter++;

                // UI Thread がブロックする可能性があるので新しいThreadを使う
                new SendToDataLayerThread("/path", sendMessage).start();

                Log.d(TAG, "SendToDataLayerThread()");
*/
                //run();
            }
        });
        WifiInsertDB();
       // WifiScan_list();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult r = Wearable.MessageApi.sendMessage(
                                client,
                                node.getId(),
                                "/arrival",
                                "Hello world".getBytes()
                        ).await();
                    }
                }
            }).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void Wifiscan() {
        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        manager.startScan();
        //LinearLayout llayout = (LinearLayout) findViewById(R.id.llayout);

        List<ScanResult> results = manager.getScanResults();
        ArrayList<String> funresult = new ArrayList<String>();

        String apInfo[] = new String[4];

        //　SSIDを取得
        apInfo[0] = String.format("SSID : %s", info.getSSID());
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setText(apInfo[0]);

        // IPアドレスを取得
        int ipAdr = info.getIpAddress();
        apInfo[1] = String.format("IP Adrress : %02d.%02d.%02d.%02d",
                (ipAdr >> 0) & 0xff, (ipAdr >> 8) & 0xff, (ipAdr >> 16) & 0xff, (ipAdr >> 24) & 0xff);
        TextView textView2 = (TextView) findViewById(R.id.textView1);
        textView2.setText(apInfo[1]);

        // MACアドレスを取得
        apInfo[2] = String.format("MAC Address : %s", info.getMacAddress());
        TextView textView3 = (TextView) findViewById(R.id.textView1);
        textView3.setText(apInfo[2]);

        // 受信信号強度&信号レベルを取得
        int rssi = info.getRssi();
        int level = WifiManager.calculateSignalLevel(rssi, 5);
        apInfo[3] = String.format("現在接続中のAP RSSI : %d / Level : %d/4  / MAC Address : %s / SSID : %s / IP Adrress : %02d.%02d.%02d.%02d", rssi, level, info.getMacAddress(), info.getSSID(), (ipAdr >> 0) & 0xff, (ipAdr >> 8) & 0xff, (ipAdr >> 16) & 0xff, (ipAdr >> 24) & 0xff);
        TextView textView4 = (TextView) findViewById(R.id.textView1);
        textView4.setText(apInfo[3]);

        //DatabaseHelper dbHelper = new DatabaseHelper(this);
        //SQLiteDatabase db = dbHelper.getReadableDatabase();
        //db.close();

        DatabaseHelper helper = new DatabaseHelper(this);
        db = helper.getWritableDatabase();

        List <String> record = new ArrayList<String>();
        String[] ssid = new String[record.size()];
        String[] bssid = new String[record.size()];

        String[] level001 = new String[record.size()];


        ContentValues values = new ContentValues();
        for(int i = 0; i < record.size(); i++){
            int n = 0;
            //ssid[n] = record.get(i).SSID;

            values.put("SSID", info.getSSID());
            values.put("IP Adress", info.getIpAddress());
            values.put("MAC Adress", info.getMacAddress());
            values.put("RSSI", info.getRssi());
            values.put("level", level);
        }
/*
        Cursor cursor = db.query("personal_data", new String[] { "_id", "name",
                        "age" }, "delete_flg = ?", new String[] { "0" }, null, null,
                "age ASC");
        // カーソルから値を取り出す
        while (cursor.moveToNext()) {
            String str = cursor.getString(cursor.getColumnIndex("_id")) + "\t"
                    + cursor.getString(cursor.getColumnIndex("name")) + "\t"
                    + cursor.getString(cursor.getColumnIndex("age"));
            record.add(str);
        }
        */
        // テキストビューで表示
        for (int i = 0; i < record.size(); i++) {
            TextView tv = new TextView(this);
            tv.setText(record.get(i));
            //llayout.addView(tv);
        }
        // カーソルクローズ
        //cursor.close();
        // DBクローズ
        db.close();
        // MySQLiteOpenHelperクローズ
        helper.close();
    }
    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, apInfo);
    //setListAdapter(adapter);
    /*
        int n = 0;
        String[] ssid = new String[results.size()];
        String[] bssid = new String[results.size()];
        int[] level = new int[results.size()];

        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).SSID.equals("free-wifi") == true) {
                ssid[n] = results.get(i).SSID;
                bssid[n] = results.get(i).BSSID;
                level[n] = results.get(i).level;
                funresult.add(ssid[n]);
                funresult.add(bssid[n]);
                funresult.add(String.valueOf(level[n]));
                n++;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, funresult);
        ListView list = (ListView) findViewById(R.id.listView1);
    }
}
*/
    public void WifiInsertDB() {
        Wifiscan();
    }

    public void clickevent() {
        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        manager.startScan();
        List<ScanResult> results = manager.getScanResults();
        ArrayList<String> funresults = new ArrayList<String>();

        int n = 0;
        String[] ssid = new String[results.size()];
        String[] bssid = new String[results.size()];
        int[] level = new int[results.size()];

        for (int i = 0; i < results.size(); i++) {
            //if (results.get(i).SSID.equals("free-wifi") == true || results.get(i).SSID.equals("fun-wifi") == true){
            ssid[n] = results.get(i).SSID;
            bssid[n] = results.get(i).BSSID;
            level[n] = results.get(i).level;

            if (results.get(i).BSSID.equals("00:0c:e6:0a:c7:b7")) {
                if (level[n] > -60) {
                    Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG).show();
                }
                // アクティビティのActivity.getSystemService(String name)でバイブレートのサービスを受け取る
                // vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                // 単発40ミリ秒バイブする場合
                //vib.vibrate(40);
                // パターン40ミリ秒バイブ,40ミリ秒休みの2回バイブする場合(繰り返し無し-1)
                //vib.vibrate(new long[]{40, 40, 40, 40}, -1);
            }
            funresults.add(ssid[n]);
            funresults.add(bssid[n]);
            funresults.add(String.valueOf(level[n]));

            n++;
        }
    }

    public void WifiScan_list() {
        //FileOutputStream fileOutputstream  = openFileOutput("test.txt", Context.MODE_PRIVATE);

        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);

        manager.startScan();
        List<ScanResult> results = manager.getScanResults();
        ArrayList<String> funresults = new ArrayList<String>();

        int n = 0;
        String[] ssid = new String[results.size()];
        String[] bssid = new String[results.size()];
        int[] level = new int[results.size()];

        for (int i = 0; i < results.size(); i++) {
            //if (results.get(i).SSID.equals("free-wifi") == true || results.get(i).SSID.equals("fun-wifi") == true){
            ssid[n] = results.get(i).SSID;
            bssid[n] = results.get(i).BSSID;
            level[n] = results.get(i).level;

            funresults.add(ssid[n]);
            funresults.add(bssid[n]);
            funresults.add(String.valueOf(level[n]));

            n++;
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, funresults);
        ListView list = (ListView) findViewById(R.id.listview);
        list.setAdapter(adapter);
    }

}
