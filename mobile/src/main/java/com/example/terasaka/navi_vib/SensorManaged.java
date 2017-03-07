package com.example.terasaka.navi_vib;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.mikephil.charting.data.LineDataSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Terasaka on 2016/10/05.
 */
public class SensorManaged extends Activity{

    ArrayList<String>[][] point; //ここにデータを仮格納  [測定点数][0->ssid, 1->level];
    private SensorManager sensorManager = null;
    private SensorEventListener sensorEventListener = null;

    int rssiVal; // 測定点数
    Timer timer = new Timer();
    Handler handle = new Handler();
    int fpno = 0;
    final int INTERVAL_PERIOD = 1000;



    double vectorSize = 0;
    long changeTime = 0;
    double thresholdMin = 1;
    boolean vecx = true;
    boolean vecy = true;
    boolean vecz = true;
    double threshold = 15;
    long thresholdTime = 190;
    int vecchangecount = 0;

    boolean counted = false;
    long counter = -1;
    double oldVectorSize = 0;

    float step = (float) 0.0046 * 170; //ユーザの身長データを基に、歩幅決定 [歩幅(m)＝身長(m)*0.46];
    float drDistance = -step;

    boolean m_str = false;
    boolean m_back = false;
    boolean m_left = false;
    boolean m_right = false;

    //センサー描画
    private float[] fAccell = null;
    private float[] fMagnetic = null;


    private int count_timer_wknn = 1;



    final int INF = 100100100;    //INF 値(十分に大きな値)

    String array[][] = {};

    int p_count = 0;//デッドレコニングカウント


    double w_2[] = {0, 0, 0, 0, 0, 0};
    double w_3[] = {0, 0, 0, 0, 0, 0};
    double w_dis[] = {0, 0, 0, 0, 0, 0};
    double w_dis2[] = {0, 0, 0, 0, 0, 0};
    int k_0 = 1;
    int k_1, k_2, k_3, k_4, k_5 = 0;
    int r;


    //地図表示
    int left = 990;
    int top = 140;
    int right = 0;
    int bottom = 0;
    ImageView img;
    FrameLayout.MarginLayoutParams mlp;
    Bitmap bitmap2;

    int stx = 0;
    int sty = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        //地図表示初期化
        img = (ImageView) findViewById(R.id.googlemap2);

        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.googlemap2);
        // bitmapの画像を68×114で作成する
        bitmap2 = Bitmap.createScaledBitmap(bitmap, 66, 114, false);

        ViewGroup.LayoutParams lp = img.getLayoutParams();
        mlp = (ViewGroup.MarginLayoutParams) lp;

        mlp.setMargins(left, top, right, bottom);
        //マージンを設定
        img.setLayoutParams(mlp);
        img.setImageBitmap(bitmap2);

// (1)各種センサーの用意
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorEventListener = new SensorEventListener(){

            public void onSensorChanged (SensorEvent event) {
                //センサーの値が変化すると呼ばれる
                float dx;
                float dy;
                float dz;
                float oldx = 0f;
                float oldy = 0f;
                float oldz = 0f;
                int x = 0;


                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = event.values.clone();
                        break;
                }

                if (fMagnetic != null && fAccell != null) {
                    float[] R = new float[16];
                    float[] I = new float[16];

                    SensorManager.getRotationMatrix(R, I, fAccell,
                            fMagnetic);

                    float[] actual_orientation = new float[3];

                    SensorManager.getOrientation(R, actual_orientation);
                    // 求まった方位角をラジアンから度に変換する
                    float direction = (float) Math.toDegrees(actual_orientation[0]);

                    if (rad2deg(direction) > -180 && rad2deg(direction) <= -135) {
                        //right
                        m_right = true;
                    } else if (rad2deg(direction) > -90 && rad2deg(direction) <= -20) {
                        //back
                        m_back = true;
                    } else if (rad2deg(direction) > -20 && rad2deg(direction) <= 0) {
                        //left
                        m_left = true;
                    } else if (rad2deg(direction) > 0 && rad2deg(direction) <= 45) {
                        //left
                        m_left = true;
                    } else if (rad2deg(direction) > 45 && rad2deg(direction) <= 135) {
                        //straight
                        m_str = true;
                    } else if (rad2deg(direction) > 135 && rad2deg(direction) <= 180) {
                        //right
                        m_right = true;
                    }
                }

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // 増加量
                    dx = event.values[0] - oldx;
                    dy = event.values[1] - oldy;
                    dz = event.values[2] - oldz;
                    // ベクトル量をピタゴラスの定義から求める。
                    // が正確な値は必要でなく、消費電力から平方根まで求める必要はない
                    // vectorSize = Math.sqrt((double)(dx*dx+dy*dy+dz*dz));
                    vectorSize = dx * dx + dy * dy + dz * dz;
                    // ベクトル計算を厳密に行うと計算量が上がるため、簡易的な方向を求める。
                    // 一定量のベクトル量があり向きの反転があった場合（多分走った場合）
                    // vecchangecountはSENSOR_DELAY_NORMALの場合、200ms精度より
                    // 加速度変化が検出できないための専用処理。精度を上げると不要
                    // さらに精度がわるいことから、連続のベクトル変化は検知しない。
                    long dt = new Date().getTime() - changeTime;
                    boolean dxx = Math.abs(dx) > thresholdMin && vecx != (dx >= 0);
                    boolean dxy = Math.abs(dy) > thresholdMin && vecy != (dy >= 0);
                    boolean dxz = Math.abs(dz) > thresholdMin && vecz != (dz >= 0);
                    if (vectorSize > threshold && dt > thresholdTime
                            && (dxx || dxy || dxz)) {
                        vecchangecount++;
                        changeTime = new Date().getTime();

                    }
                    // ベクトル量がある状態で向きが２回（上下運動とみなす）変わった場合
                    // または、ベクトル量が一定値を下回った（静止とみなす）場合、カウント許可
                    if (vecchangecount > 1 || vectorSize < 1) {
                        counted = false;
                        vecchangecount = 0;
                    }
                    // カウント許可で、閾値を超えるベクトル量がある場合、カウント
                    if (!counted && vectorSize > threshold) {

                        counted = true;
                        vecchangecount = 0;
                        counter++;
                        if(m_str == true){
                            sty = sty + 20;
                            img.setTranslationX(stx);
                            img.setTranslationY(sty);
                        }if(m_back == true){
                            sty = sty - 20;
                            img.setTranslationX(stx);
                            img.setTranslationY(sty);
                        }if(m_right == true){
                            stx = stx - 20;
                            img.setTranslationX(stx);
                            img.setTranslationY(sty);
                        }if(m_left == true){
                            stx = stx + 20;
                            img.setTranslationX(stx);
                            img.setTranslationY(sty);
                        }

                        float theta;
                        drDistance = drDistance + step;

				/*
				 * if(pretheta >= 0 && pretheta < 90){ theta = pretheta;
				 * drDistance = (float)(drDistance + (step * Math.cos(theta)));
				 * } if(pretheta >= 90 && pretheta < 180){ theta = 180 -
				 * pretheta; drDistance = (float)(drDistance - (step *
				 * Math.cos(theta))); } if(pretheta >= 180 && pretheta < 270){
				 * theta = pretheta - 180; drDistance = (float)(drDistance -
				 * (step * Math.cos(theta))); } if(pretheta >= 270 && pretheta
				 * <= 360){ theta = 360 - pretheta; drDistance =
				 * (float)(drDistance + (step * Math.cos(theta))); }
				 */

                    }
                    // カウント自の加速度の向きを保存
                    vecx = dx >= 0;
                    vecy = dy >= 0;
                    vecz = dz >= 0;
                    // 状態更新
                    oldVectorSize = vectorSize;
                    // 加速度の保存
                    oldx = event.values[0];
                    oldy = event.values[1];
                    oldz = event.values[2];
                }

            }/*
                // センサの取得値をそれぞれ保存しておく
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = event.values.clone();
                        break;
                }


                // fAccell と fMagnetic から傾きと方位角を計算する
                if (fAccell != null) {
                    // 増加量
                    dx = event.values[0] - oldx;
                    dy = event.values[1] - oldy;
                    dz = event.values[2] - oldz;
                    if (fMagnetic != null) {

                        // 回転行列を得る
                        float[] inR = new float[9];
                        SensorManager.getRotationMatrix(
                                inR,
                                null,
                                fAccell,
                                fMagnetic);
                        // ワールド座標とデバイス座標のマッピングを変換する
                        float[] outR = new float[9];
                        SensorManager.remapCoordinateSystem(
                                inR,
                                SensorManager.AXIS_X, SensorManager.AXIS_Y,
                                outR);
                        // 姿勢を得る
                        float[] fAttitude = new float[3];
                        SensorManager.getOrientation(
                                outR,
                                fAttitude);

                        String buf =
                                "---------- Orientation --------\n" +
                                        String.format("方位角\n\t%f\n", rad2deg(fAttitude[0])) +
                                        String.format("前後の傾斜\n\t%f\n", rad2deg(fAttitude[1])) +
                                        String.format("左右の傾斜\n\t%f\n", rad2deg(fAttitude[2]));
                        TextView t = (TextView) findViewById(R.id.textView1);
                        t.setText(buf);
                /*

                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    //Log.d("mag", "Sucess");
*/

/*
                        // ベクトル量をピタゴラスの定義から求める。
                        // が正確な値は必要でなく、消費電力       から平方根まで求める必要はない
                        // vectorSize = Math.sqrt((double)(dx*dx+dy*dy+dz*dz));
                        vectorSize = dx * dx + dy * dy + dz * dz;
                        // ベクトル計算を厳密に行うと計算量が上がるため、簡易的な方向を求める。
                        // 一定量のベクトル量があり向きの反転があった場合（多分走った場合）
                        // vecchangecountはSENSOR_DELAY_NORMALの場合、200ms精度より
                        // 加速度変化が検出できないための専用処理。精度を上げると不要
                        // さらに精度がわるいことから、連続のベクトル変化は検知しない。
                        long dt = new Date().getTime() - changeTime;
                        boolean dxx = Math.abs(dx) > thresholdMin && vecx != (dx >= 0);
                        boolean dxy = Math.abs(dy) > thresholdMin && vecy != (dy >= 0);
                        boolean dxz = Math.abs(dz) > thresholdMin && vecz != (dz >= 0);
                        if (vectorSize > threshold && dt > thresholdTime
                                && (dxx || dxy || dxz)) {
                            vecchangecount++;
                            changeTime = new Date().getTime();

                        }
                        // ベクトル量がある状態で向きが２回（上下運動とみなす）変わった場合
                        // または、ベクトル量が一定値を下回った（静止とみなす）場合、カウント許可
                        if (vecchangecount > 1 || vectorSize < 1) {
                            counted = false;
                            vecchangecount = 0;
                        }
                        // カウント許可で、閾値を超えるベクトル量がある場合、カウント
                        if (!counted && vectorSize > threshold) {
                            counted = true;
                            vecchangecount = 0;
                            counter++;

                            if (rad2deg(fAttitude[0]) > -180 && rad2deg(fAttitude[0]) <= -135) {
                                //right
                                stx = stx - 20;
                                img.setTranslationX(stx);
                                img.setTranslationY(sty);
                            } else if (rad2deg(fAttitude[0]) > -90 && rad2deg(fAttitude[0]) <= -20) {
                                //back
                                sty = sty - 20;
                                img.setTranslationX(stx);
                                img.setTranslationY(sty);
                            } else if (rad2deg(fAttitude[0]) > -20 && rad2deg(fAttitude[0]) <= 0) {
                                //left
                                stx = stx + 20;
                                img.setTranslationX(stx);
                                img.setTranslationY(sty);
                            } else if (rad2deg(fAttitude[0]) > 0 && rad2deg(fAttitude[0]) <= 45) {
                                //left
                                stx = stx + 20;
                                img.setTranslationX(stx);
                                img.setTranslationX(stx);
                            } else if (rad2deg(fAttitude[0]) > 45 && rad2deg(fAttitude[0]) <= 135) {
                                //straight
                                sty = sty + 20;
                                img.setTranslationX(stx);
                                img.setTranslationY(sty);
                            } else if (rad2deg(fAttitude[0]) > 135 && rad2deg(fAttitude[0]) <= 180) {
                                //right
                                stx = stx - 20;
                                img.setTranslationX(stx);
                                img.setTranslationY(sty);
                            }
                        /*
                        sty = sty + 20;
                        img.setTranslationY(sty);
*/
             /*

                            float theta;
                            drDistance = drDistance + step;

                        }
                    }
                        // カウント自の加速度の向きを保存
                        vecx = dx >= 0;
                        vecy = dy >= 0;
                        vecz = dz >= 0;
                        // 状態更新
                        oldVectorSize = vectorSize;
                        // 加速度の保存
                        oldx = event.values[0];
                        oldy = event.values[1];
                        oldz = event.values[2];

                        String str_acc = "加速度センサー値:"
                                + "\nX軸:" + event.values[SensorManager.DATA_X]
                                + "\nY軸:" + event.values[SensorManager.DATA_Y]
                                + "\nZ軸:" + event.values[SensorManager.DATA_Z];

                        String str_count = "歩数" + counter;
                        if (counter == 0 && p_count == 0) {
                            p_count++;

                        } else if (counter == 16 && p_count == 1) {
                            p_count++;

                        } else if (counter == 24 && p_count == 1) {
                            p_count++;
                            counter = 0;
                        }
                    }

            }*/
            public void onAccuracyChanged (Sensor sensor, int accuracy) {}
        };

        String data[][] = parse("Link_0123.csv", this);
        int adj[][] = new int[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                adj[i][j] = Integer.parseInt(data[i][j]);
            }
        }
        int n = 6;    //頂点数
        int s = 0;    //スタート頂点(頂点番号は -1 されている[元は 1])
        //int g = messageReceiver.onReceive(context,intent);
        int g = 5;    //ゴール頂点(頂点番号は -1 されている[元は 5])
        r = 0;    //現在地ノード（始めはスタート頂点）


        List<Edge>[] edges = new List[n];
        List<Edge> paths = new ArrayList<Edge>();    //選択した辺(戻値用)

        for (int i = 0; i < n; i++) {
            edges[i] = new ArrayList<Edge>();
        }

        /*ノード間隣接関係*/
        for (int i = 0; i < adj.length; i++) {
            edges[adj[i][0]].add(new Edge(adj[i][0], adj[i][1], adj[i][2]));
            edges[adj[i][1]].add(new Edge(adj[i][1], adj[i][0], adj[i][2])); //無向グラフなので、逆方向も接続する
        }

        int ans = dijkstra(n, edges, s, g, paths);//s → g の最短距離
        String str_amc = "" + ans;
        String str_len = "";

        //選択したリンク
        int len = paths.size();
        for (int i = 1; i < len; i++) {

            str_len = "" + paths.get(i);
            String cost_len = "" + paths.get(i).cost;
        }

        // Fingerprint手法の位置推定アルゴリズム
        // タイマークラスによる自動実行
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handle.post(new Runnable() {
                    @Override
                    public void run() {

                        rssiVal = 6;
                        point = new ArrayList[rssiVal][3];

                        CSV("Node01_0123.csv", 0);
                        CSV("Node02_0123.csv", 1);
                        CSV("Node03_0123.csv", 2);
                        CSV("Node04_0123.csv", 3);
                        CSV("Node05_0123.csv", 4);
                        CSV("Node06_0123.csv", 5);

                        double max1 = 0;
                        double max2 = 0;
                        double sigma1[] = new double[rssiVal];

                        double sigma2 = 0;
                        double e[] = new double[rssiVal];
                        double w[] = new double[rssiVal];
                        double rssi_current = 0; // 現在位置で取得したRSSIを格納する
                        double rssi_database = 0; // データベースのrssiを格納する
                        int countscsn[] = new int[rssiVal]; // 各測定点におけるDBとスキャン結果の一致APを数える


                        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
                        manager.startScan();
                        List<ScanResult> results = manager.getScanResults();

                        try {
                            for (int j = 0; j < rssiVal; j++) { // 測定点番号ループ　(測定点1->測定点2->・・・)
                                for (int k = 0; k < point[j][0].size(); k++) { // 測定点データループ
                                    for (int i = 0; i < results.size(); i++) { // APスキャン結果ループ
                                        if (results.get(i).level > -70) {   //明らかに微弱な電波は除外するためにRSSIの下限を定める
                                            if (results.get(i).BSSID.equals(point[j][0].get(k))) { // APスキャン結果のBSSID＝測定点データのBSSIDならば　アルゴリズム計算
                                                countscsn[j]++;
                                                rssi_current = results.get(i).level;
                                                rssi_database = Double.parseDouble(point[j][1].get(k));
                                                sigma1[j] = sigma1[j] + Math.pow(rssi_current - rssi_database, 2);
                                                break;
                                            }
                                            //}
                                        }
                                    }
                                    if (countscsn[j] > max1) {
                                        max1 = countscsn[j];
                                    }
                                    //Log.d("count", String.valueOf(count[j]));
                                    e[j] = Math.sqrt(sigma1[j]);
                                }

                            }

                            // ---------------------------------------
                            // 取得RSSIとDB内RSSIの一致AP数の違いに対応
                            // 一致AP数が一番多い測定点にAP数を合わせるために
                            // 足りないAP数のぶんだけ
                            // 各測定点における取得RSSIとDB内RSSIの誤差の平均を加算していく
                            for (int o = 0; o < rssiVal; o++) {
                                double difVal = max1 - countscsn[o];
                                if (difVal != 0 && countscsn[o] > 5) { // 一致AP数が明らかに少ない測定点を省くためにcount[o] > ○　を入れているが不要？　else if以降も同様
                                    sigma1[o] = sigma1[o] + ((difVal * sigma1[o]) / countscsn[o]);
                                } else if (difVal != 0 && countscsn[o] <= 5) {
                                    sigma1[o] = 0;
                                }
                                e[o] = Math.sqrt(sigma1[o]);
                                //Log.d("ruijido"+ o+1, String.valueOf(e[o]));

                            }
                            // ---------------------------------------


                            // 2本目の式（重み付け）
                            for (int l = 0; l < rssiVal; l++) { // 類似度ループ
                                if (e[l] != 0)
                                    sigma2 = sigma2 + (1 / (e[l] * e[l])); // 　0で除算しないためのif
                            }

                            for (int t = 0; t < rssiVal; t++) { // 類似度ループ
                                if (e[t] == 0) { // 0で除算しないためのif
                                    w[t] = 0;
                                } else if (e[t] != 0) {
                                    w[t] = (1 / (e[t] * e[t])) / sigma2;
                                }
                                if (w[t] > max2) {
                                    max2 = w[t];
                                    fpno = t + 1;
                                }

                            }


                            for (int i = 0; i <= 5; i++) {
                                w_dis[i] = w[i] - w_2[i];
                            }
                            for (int i = 0; i <= 5; i++) {
                                w_dis2[i] = w[i] - w_3[i];
                            }


                            if (r == 0 || r == 2 || r == 3) {
                                if (k_1 == 0 && w_dis[1] >= 0 && w[1] > 0.34) {
                                    k_1++;
                                    r = 1;
                                    top = 100;
                                }
                            }

                            if (r == 1 || r == 4) {
                                if (k_2 == 0 && w_dis[2] >= 0 && w[2] > 0.48) {
                                    k_2++;
                                    r = 2;
                                    top = 200;
                                }
                            }


                            if ((r == 1 || r == 4) && k_3 == 0 && w_dis[3] >= 0 && w[3] > 0.4) {
                                k_3++;
                                r = 3;
                                top = 250;
                            }

                            if ((r == 2 || r == 3 || r == 5) && k_4 == 0 && w_dis[4] >= 0 && w[4] > 0.5) {
                                k_4++;
                                r = 4;
                            }

                            if ((r == 4) && k_5 == 0 && w_dis[5] <= 0.1 && w_dis[5] >= -0.1 && w[4] > 0.45) {
                                k_5++;
                                r = 5;
                            }


/*
                            bw = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(sdPath + fileName), "utf-8"));
                            long currentTineMills = System.currentTimeMillis();
                            bw.write("測定点1 類似度:" + "," + w[0] + "\n"
                                    + "測定点2 類似度:" + "," + w[1] + "\n"
                                    + "測定点3 類似度:" + "," + w[2] + "\n"
                                    + "測定点4 類似度:" + "," + w[3] + "\n"
                                    + "測定点5 類似度:" + "," + w[4] + "\n"
                                    + "測定点6 類似度:" + "," + w[5] + "\n"
                                    + "w_2[0]" + "," + w_2[0] + "\n"
                                    + "w_2[1]" + "," + w_2[1] + "\n"
                                    + "w_2[2]" + "," + w_2[2] + "\n"
                                    + "w_2[3]" + "," + w_2[3] + "\n"
                                    + "w_2[4]" + "," + w_2[4] + "\n"
                                    + "w_2[5]" + "," + w_2[5] + "\n"
                                    + "w_dis[3]" + "," + w_dis[3] + "\n"
                                    + "w_dis2[3]" + "," + w_dis2[3] + "\n"
                                    + "最大類似度地点" + "," + fpno + "\n"
                                    + "Current_point" + "," + r + "\n"
                                    + "time" + "," + currentTineMills + "\n");
                            //bw.write(w[2] + "\n");
                            bw.close();
*/

                            for (int i = 0; i <= 4; i++) {
                                w_2[i] = w[i];
                            }
                            for (int i = 0; i <= 4; i++) {
                                w_3[i] = w_2[i];
                            }

                        } catch (IndexOutOfBoundsException ee) {

                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                        count_timer_wknn += 1;
                    }
                });

            }
        }, 0, INTERVAL_PERIOD);
    }


    protected void onStart() { // ⇔ onStop
        Log.d("onStart", "Sucess" );
        super.onStart();
    }



    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Log.d("onResume", "Sucess" );
        super.onResume();
        // Listenerの登録
        List<Sensor> sensors_ACC = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> sensors_GYR = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        List<Sensor> sensors_MAG = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

        if (sensors_ACC.size() > 0) {
            Sensor s_acc = sensors_ACC.get(0);
            sensorManager.registerListener(sensorEventListener, s_acc, SensorManager.SENSOR_DELAY_UI);
            Log.d("TYPE_ACCELEROMETER", "Sucess" );

        }
        if (sensors_GYR.size() > 0) {
            Sensor s_gyr = sensors_GYR.get(0);
            sensorManager.registerListener(sensorEventListener, s_gyr, SensorManager.SENSOR_DELAY_UI);
            Log.d("TYPE_GYROSCOPE", "Sucess" );

        }
        if (sensors_MAG.size() > 0) {
            Sensor s_mag = sensors_MAG.get(0);
            sensorManager.registerListener(sensorEventListener, s_mag, SensorManager.SENSOR_DELAY_UI);
            Log.d("TYPE_MAGNETIC_FIELD", "Sucess" + sensors_MAG );

        }
    }

    public void onPause(){
        super.onPause();
        if(sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        Log.d("onStop", "Sucess" );
        super.onStop();
        // Listenerの登録解除
        sensorManager.unregisterListener(sensorEventListener);
    }

/*
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        //センサーの精度が変更されると呼ばれる
        Log.d("onAccuracyChanged", "Sucess" );
    }
*/

    private float rad2deg( float rad ) {
        return rad * (float) 180.0 / (float) Math.PI;
        //return rad *  (float) Math.toDegrees(rad);

    }

    private LineDataSet createSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setLineWidth(2.5f); // 線の幅を指定
        set.setColor(color); // 線の色を指定
        set.setDrawCircles(false); // ポイントごとの円を表示しない
        set.setDrawValues(false); // 値を表示しない

        return set;
    }

    public String[][] parse(String file, Context context) {
        // AssetManagerの呼び出し

        AssetManager assetManager = context.getResources().getAssets();
        try {
            // CSVファイルの読み込み
            InputStream is = assetManager.open(file);
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);

            ArrayList<String[]> csvData = new ArrayList<String[]>();
            String line = "";

            //ファイルを行単位で読む
            while ((line = bufferReader.readLine()) != null) {
                csvData.add(line.split(",", 0)); // ArrayListへ読み込
            }
            array = new String[csvData.size()][];
            for (int i = 0; i < csvData.size(); i++) {
                array[i] = csvData.get(i); // ArrayListから配列へ
            }

            bufferReader.close();
            return array;
        } catch (IOException e) {
            //e.printStackTrace();
            //textview08.setText("読み込みに失敗しました");
        } catch (NoSuchElementException e) {
            //textview08.setText("NoSuchElementException");
        } catch (NullPointerException e) {
            //textview08.setText("NullPointerException");
        }
        return array;
    }

    // CSVファイルを読み込み、ArrayListに格納する（ 引数はファイル名, 測定点番号）
    public void CSV(String pass, int no) {
        // AssetManagerの呼び出し
        AssetManager assetManager = getResources().getAssets();
        try {
            // 初期化
            point[no][0] = new ArrayList<String>();
            point[no][1] = new ArrayList<String>();
            point[no][2] = new ArrayList<String>();

            InputStream fin = null;
            fin = assetManager.open(pass);
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));

            // 最終行まで読み込む
            String line = "";

            while ((line = br.readLine()) != null) {

                // 1行をデータの要素に分割
                StringTokenizer st = new StringTokenizer(line, ",");
                int i = 0;
                while (st.hasMoreTokens()) {
                    // 1行の各要素をタブ区切りで表示
                    String tmp = st.nextToken();
                    tmp = tmp.replaceAll("\"", "");
                    point[no][i].add(tmp);
                    i++;
                }
            }
            br.close();

        } catch (FileNotFoundException e) {
            // Fileオブジェクト生成時の例外捕捉
            e.printStackTrace();
        } catch (IOException e) {
            // BufferedReaderオブジェクトのクローズ時の例外捕捉
            e.printStackTrace();
        }
    }

    //ダイクストラ法[単一始点最短経路(Single Source Shortest Path)]
    //選択した通路を返す

    int dijkstra(int n, List<Edge>[] edges, int s, int g, List<Edge> paths) {

        int[] distance = new int[n];        //始点からの最短距離
        int[] parent = new int[n];

        Arrays.fill(distance, INF);    //各頂点までの距離を初期化(INF 値)
        distance[s] = 0;    //始点の距離は０
        Arrays.fill(parent, -1);    //-1は始点または未到達


        Queue<Edge> q = new PriorityQueue<Edge>();
        q.add(new Edge(s, s, 0));     //始点を入れる

        while (!q.isEmpty()) {
            Edge e = q.poll();        //最小距離(cost)の頂点を取り出す
            if (distance[e.target] < e.cost) {
                continue;
            }

            //隣接している頂点の最短距離を更新する
            for (Edge v : edges[e.target]) {
                if (distance[v.target] > distance[e.target] + v.cost) {  //(始点～)接続元＋接続先までの距離
                    distance[v.target] = distance[e.target] + v.cost;    //現在記録されている距離より小さければ更新
                    q.add(new Edge(e.target, v.target, distance[v.target]));  //始点～接続先までの距離
                    parent[v.target] = e.target; //接続元を記録


                }
            }

        }
        //選択した辺(逆から辿る[到達点→始点])
        paths.clear();
        if (parent[g] > -1) {
            int p = g;
            while (p > -1) {
                paths.add(new Edge(parent[p], p, distance[p]));
                p = parent[p];
            }
            Collections.reverse(paths);    //反転する(始点→到達点)
        }

        return distance[g];    //到達できなかったときは、INF となる
    }

    //辺情報の構造体
    class Edge implements Comparable<Edge> {
        public int source = 0;    //接続元ノード
        public int target = 0;    //接続先ノード
        public int cost = 0;      //重み

        public Edge(int source, int target, int cost) {
            this.source = source;
            this.target = target;
            this.cost = cost;
        }

        @Override
        public int compareTo(Edge o) {
            return this.cost - o.cost;    //重みの小さい順
        }

        @Override
        public String toString() {    //デバッグ用
            return "source = " + source + ", target = " + target + ", cost = " + cost;
        }
    }


}