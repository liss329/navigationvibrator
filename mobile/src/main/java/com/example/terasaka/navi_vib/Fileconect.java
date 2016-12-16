package com.example.terasaka.navi_vib;

/**
 * Created by Terasaka on 2016/11/02.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;




public class Fileconect extends Activity {

    static SQLiteDatabase mydb;
    private Button buttonSave, buttonRead;
    Handler handler = new Handler();
    final int INTERVAL_PERIOD = 1000;  //1秒間に1回実行
    Timer timer = new Timer();
    int i;
    TextView textview01;
    TextView textview02;


    @Override
    public void onCreate(Bundle savedInstanaeeteta) {
        super.onCreate(savedInstanaeeteta);
        setContentView(R.layout.list);

        textview01 = (TextView) findViewById(R.id.textview01);
        textview02 = (TextView) findViewById(R.id.textview02);

        FileCheck();

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

        android.net.wifi.WifiManager manager = (android.net.wifi.WifiManager) getSystemService(WIFI_SERVICE);

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

    public void FileCheck() {
        InputStream in;
        OutputStream out;
        String filename[] = new String[60];
        String b[] = new String[325557];

        String str;
        String write = "all007write.csv";

        //
        try {

            for (int i = 1; i <= 59; i++) {
                filename[i] = "007write" + i + ".txt";
                in = openFileInput(filename[i]);
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                out = openFileOutput(write, MODE_PRIVATE | MODE_APPEND);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));

                //一行ずつ読み込んで
                str = br.readLine();
                String line;

                while(str != null){
                    //strlist.add(str.split(","));
                    //一行の内容を','で分割してそれぞれを[count=ノード番号]の２次元目の配列の要素として格納
                    b[i] = b[i] + str + "\n";
                    //次の行を読み込み
                    str = br.readLine();
                }
                textview01.setText(b[i]);
                String FileReadCount = "FileRead" + i;
                Log.d(FileReadCount, b[i]);

                writer.print(b[i]);
                writer.close();
                br.close();
            }
        } catch (FileNotFoundException e) {
            Log.d("Error","FileNotFoundException");
        } catch (IOException e) {
            Log.d("Error","IOException");

        }

    }


    public void WifiInsertDB() {
        android.net.wifi.WifiManager manager = (android.net.wifi.WifiManager) getSystemService(WIFI_SERVICE);
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



