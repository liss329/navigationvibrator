package com.example.terasaka.navi_vib;

/**
 * Created by Terasaka on 2016/09/27.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SubActivity extends Activity {

    static SQLiteDatabase mydb;
    private Button buttonSave, buttonRead;
    Handler handler = new Handler();
    final int INTERVAL_PERIOD = 1000;  //1秒間に1回実行
    Timer timer = new Timer();
    int i;
    TextView textview01;
    TextView textview02;
    int CountTimer = 1;

    @Override
    public void onCreate(Bundle savedInstanaeeteta) {
        super.onCreate(savedInstanaeeteta);
        setContentView(R.layout.list);

        textview01 = (TextView) findViewById(R.id.textview01);
        textview02 = (TextView) findViewById(R.id.textview02);

        WifiRecord();

        //DatabaseSave();
    }
/*
    WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
    WifiInfo info = manager.getConnectionInfo();
    manager.startScan();
*/
    /*public void onClick(View v){
        //テキストを取得

    }/*
*/

    public void WifiScan() {
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
        textview01.setText(getPackageName());


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, funresults);
        ListView list = (ListView) findViewById(R.id.listview);
        list.setAdapter(adapter);
    }

    public void WifiRecord() {
        timer.scheduleAtFixedRate(new TimerTask() {
            int m = 1;

            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out;
                        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
                        manager.startScan();
                        List<ScanResult> results = manager.getScanResults();
                        ArrayList<String> funresults = new ArrayList<String>();
                        int n = 0;
                        String s = "";
                        String[] ss = new String[100000];
                        String[] count_wifi = new String[10000];
                        String[] ssid = new String[results.size()];
                        String[] bssid = new String[results.size()];
                        int[] level = new int[results.size()];

                        for (int i = 0; i < results.size(); i++) {
                            //if (results.get(i).SSID.equals("free-wifi") == true || results.get(i).SSID.equals("fun-wifi") == true){
                            ssid[n] = results.get(i).SSID;
                            bssid[n] = results.get(i).BSSID;
                            level[n] = results.get(i).level;
                            String ver = "測定点2: ";
                            count_wifi[n] =  "\"" + ssid[n]  +"\""
                                    +  "," + "\"" + bssid[n] +"\""
                                    +  "," + "\"" + level[n] +"\"" + "\n";

                            //funresults.add(ssid[n]);
                            //funresults.add(bssid[n]);
                            //funresults.add(String.valueOf(level[n]))
                            s = s + count_wifi[n];
                            //Log.d("count_wifi",s);
                            textview01.setText(s);

                            n++;
                        }
                        try {
                            Log.v("debug", "書き込み");
                            // SDカードフォルダのパス
                            String sdPath = Environment.getExternalStorageDirectory().getPath();
                            // 作成するファイル名
                            String fileName = "/wifiscan" + "/wifiscan" + CountTimer + ".csv";
                            // 書き込み
                            BufferedWriter bw = null;
                            bw = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(sdPath + fileName), "utf-8"));
                            long currentTineMills = System.currentTimeMillis();

                            bw.write(s);
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        CountTimer += 1;
                    }
                });
            }
        }, 0, INTERVAL_PERIOD);
    }






    public void DatabaseSave(){
        DatabaseHelper SQLite = new DatabaseHelper(getApplicationContext());
        //読み書きできるように開く
        mydb = SQLite.getWritableDatabase();
        final Button btn =(Button)findViewById(R.id.DBbutton);
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                i = 0;
                Toast.makeText(getApplicationContext(), "計測中", Toast.LENGTH_LONG).show();

                timer.scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run() {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                WifiInsertDB();
                                i++;
                                System.out.println(i);

                                if (i == 1) {  //i==x回分データベースに記録したら終了
                                    timer.cancel();
                                    Intent intent = new Intent(SubActivity.this,MainActivity.class);
                                    startActivity(intent);
                                }

                            }
                        });

                    }
                }, 0, INTERVAL_PERIOD);

                for(int j=0; j<100; j++){
                    WifiInsertDB();
                    System.out.println(j);
                    //Sleep(1000);
                }
            }

        });

    }

    public void WifiInsertDB() {
        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        manager.startScan();
        GregorianCalendar cal1 = new GregorianCalendar();

        List<ScanResult> results = manager.getScanResults();

        int n = 0;
        String[] ssid = new String[results.size()];        //free-wifi
        String[] bssid = new String[results.size()];
        int[] level = new int[results.size()];


        //レコード追加
        ContentValues values = new ContentValues();



        for(int i=0;i<results.size();i++){
            //if(results.get(i).SSID.equals("free-wifi")==true){
            ssid[n]=results.get(i).SSID;
            bssid[n]=results.get(i).BSSID;
            level[n]=results.get(i).level;
            values.put("bssid", bssid[n]);
            values.put("level", level[n]);
            //			values.put("pointX", x);
            //			values.put("pointY", y);x
            //			values.put("time", cal1.getTime().toString());
            //mydb.insert("MyTable", null, values);
				/*テキストビューで表示
				TextView tv = new TextView(this);
				tv.setText(results.get(i));
				list.addView(tv);
				*/

            n++;

            //}
        }
        // Cursor cursor = mydb.query("mytable", new String[] {"_id", "bssid", "level"}, null, null, null, null, null);
        //cursor.moveToFirst();
//		System.out.println("cursor : " + cursor.getString(0));
    }
    //


}



