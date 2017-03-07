package com.example.terasaka.navi_vib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

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
public class Compass extends Activity{
    private GoogleApiClient client;

    ArrayList<String>[][] point; //ここにデータを仮格納  [測定点数][0->ssid, 1->level];
    private SensorManager sensorManager = null;
    private SensorEventListener sensorEventListener = null;

    int rssiVal; // 測定点数
    Timer timer = new Timer();
    Handler handle = new Handler();
    int fpno = 0;
    final int INTERVAL_PERIOD = 1000;


    private float[] accelerometerValues = null; //
    private float[] magneticValues = null; //
    private float[] gyroscopeValues = null; //


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

    private Context context;


    //センサー描画
    private float[] fAccell = null;
    private float[] fMagnetic = null;
    private float[] fgyroscope = null;

    LineChart mChart;
    LineChart gyrChart;


    private int count_timer_wknn = 1;


    String[] names = new String[]{"x-value", "y-value", "z-value"};
    int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};

    final int INF = 100100100;    //INF 値(十分に大きな値)

    String array[][] = {};

    int p_count = 0;//デッドレコニングカウント

    int x_current_place = 0;
    int y_current_place = 0;

    private String receivedMessage = null;
    int info = 0;


    double w_2[] = {0, 0, 0, 0, 0, 0};
    double w_3[] = {0, 0, 0, 0, 0, 0};
    double w_dis[] = {0, 0, 0, 0, 0, 0};
    double w_dis2[] = {0, 0, 0, 0, 0, 0};
    int k_0 = 1;
    int k_1, k_2, k_3, k_4, k_5 = 0;
    static int r = 0;


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


    TextView t2;
    TextView t3;
    TextView t4;
    TextView t5;
    TextView t6;
    TextView t7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass_activity);

        /*
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
*/

        //リスナー
        this.client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        this.client.connect();

        t2 = (TextView) findViewById(R.id.textView2);
        t3 = (TextView) findViewById(R.id.textView3);
        t4 = (TextView) findViewById(R.id.textView4);
        t5 = (TextView) findViewById(R.id.textView5);
        t6 = (TextView) findViewById(R.id.textView6);
        t7 = (TextView) findViewById(R.id.textView7);


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

                // センサの取得値をそれぞれ保存しておく
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = event.values.clone();
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        fgyroscope = event.values.clone();
                        break;
                }


                // fAccell と fMagnetic から傾きと方位角を計算する
                if (fAccell != null && fMagnetic != null) {
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
                    TextView t1 = (TextView) findViewById(R.id.textView1);
                    t1.setText(buf);
                }

                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        //Log.d("mag", "Sucess");


                    // 増加量
                    dx = event.values[0] - oldx;
                    dy = event.values[1] - oldy;
                    dz = event.values[2] - oldz;
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
                        /*if(rad2deg(fAttitude[0]) > -180 && rad2deg(fAttitude[0])<= -135){
                            //right
                            stx = stx - 50;
                            img.setTranslationX(stx);
                        }else if(rad2deg(fAttitude[0]) > -90 && rad2deg(fAttitude[0])<= -20){
                            //back
                            sty = sty - 50;
                            img.setTranslationY(sty);
                        }else if(rad2deg(fAttitude[0]) > -20 && rad2deg(fAttitude[0])<= 0){
                            //left
                            stx = stx + 50;
                            img.setTranslationX(stx);
                        }else if(rad2deg(fAttitude[0]) > 0 && rad2deg(fAttitude[0])<= 45){
                            //left
                            stx = stx + 50;
                            img.setTranslationX(stx);
                        }else if(rad2deg(fAttitude[0]) > 45 && rad2deg(fAttitude[0])<= 135){
                            //straight
                            sty = sty + 50;
                            img.setTranslationY(sty);
                        }else if(rad2deg(fAttitude[0]) > 135 && rad2deg(fAttitude[0])<= 180){
                            //right
                            stx = stx - 50;
                            img.setTranslationX(stx);
                        }*/
                        counted = true;
                        vecchangecount = 0;
                        counter++;

                        sty = sty + 20;
                        //img.setTranslationY(sty);

                        float theta;
                        //drDistance = drDistance + step;

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
/*
            //DataMapインスタンス生成
            PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/datapath");
            DataMap dataMap = dataMapRequest.getDataMap();
            //データセット
            dataMap.putInt("key",0);
            //データ更新
            PutDataRequest request = dataMapRequest.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(client, request);
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    Log.d("TAG", "onResult:" + dataItemResult.getStatus());
                }
            });
*/
                    if (counter == 0 && p_count == 0) {
                        p_count++;
                        Vibration("/right", "right");
                        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(client);
                        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult result) {
                                String messagePayload = "Hello!";
                                for (Node node : result.getNodes()) {
                                    final byte[] bs = (messagePayload + " " + node.getId()).getBytes();
                                    PendingResult<MessageApi.SendMessageResult> messageResult =
                                            Wearable.MessageApi.sendMessage(client, node.getId(), "/path", bs);
                                    messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                        @Override
                                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                            Status status = sendMessageResult.getStatus();
                                            Log.d("TAG", "Status: " + status.toString());
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }

            public void onAccuracyChanged (Sensor sensor, int accuracy) {}
        };
        //センサー描画
        /*
        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setDescription(""); // 表のタイトルを空にする
        mChart.setData(new LineData()); // 空のLineData型インスタンスを追加

        gyrChart = (LineChart) findViewById(R.id.lineChart2);
        gyrChart.setDescription(""); // 表のタイトルを空にする
        gyrChart.setData(new LineData()); // 空のLineData型インスタンスを追加
        */

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        String data[][] = parse("Link_0123.csv", this);
        int adj[][] = new int[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                try {
                adj[i][j] = Integer.parseInt(data[i][j]);
                }catch (NullPointerException e){
                // textview08.setText("NullPointerException");
                }
            }
        }
        int n = 6;    //頂点数
        int s = 0;    //スタート頂点(頂点番号は -1 されている[元は 1])
        //int g = messageReceiver.onReceive(context,intent);
        int g = 5;    //ゴール頂点(頂点番号は -1 されている[元は 5])
        //static int r = 0;    //現在地ノード（始めはスタート頂点）
        /*int[][] adj = new int[][]{
                //接続元ノード, 接続先ノード, 重み
                {0, 1, 7},
                {0, 2, 9},
                {0, 5, 14},
                {1, 2, 10},
                {1, 3, 15},
                {2, 3, 11},
                {2, 5, 2},
                {3, 4, 6},
                {4, 5, 9}
        }; //s:0 → g:4 のとき、答えは 20*/
        /*int[][] adj = new int[][]{
                {0, 1, 155},
                {0, 4, 500},
                {1, 2, 120},
                {1, 5, 500},
                {2, 3, 395},
                {3, 6, 105},
                {4, 5, 155},
                {5, 6, 130},
                {0, 0, 0}
        };*/

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
        //textview04.setText(str_amc);

        //選択したリンク
        int len = paths.size();

        for (int i = 1; i < len; i++) {
            str_len = "" + paths.get(i);
            String cost_len = "" + paths.get(i).cost;
            if (i == 1) {
            } else if (i == 2) {
                //textview06.setText(str_len);
            } else if (i == 3) {
                //textview07.setText(str_len);
                //textview08.setText(cost_len);
            } else if (i == 4) {
                //textview08.setText(str_len);
            }
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

                        CSV("0303Node1.csv", 0);
                        CSV("0303Node2.csv", 1);
                        CSV("0303Node3.csv", 2);
                        CSV("0303Node4.csv", 3);
                        CSV("0303Node5.csv", 4);
                        CSV("0303Node6.csv", 5);

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
                            //Log.v("debug", "書き込み");
                            // SDカードフォルダのパス
                            String sdPath = Environment.getExternalStorageDirectory().getPath();
                            // 作成するファイル名
                            String fileName = "/wknn" + "/wknn" + count_timer_wknn + ".csv";
                            // 書き込み
                            BufferedWriter bw = null;

                            for (int j = 0; j < rssiVal; j++) { // 測定点番号ループ　(測定点1->測定点2->・・・)
                                for (int k = 0; k < point[j][0].size(); k++) { // 測定点データループ
                                    for (int i = 0; i < results.size(); i++) { // APスキャン結果ループ
                                        if (results.get(i).level > -2147483648) {   //明らかに微弱な電波は除外するためにRSSIの下限を定める
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
                            /*
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
                            */
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


//                            if (r == 0 || r == 2 || r == 3) {
                            if (r == 0) {
                                if (k_1 == 0 &&  w[1] > 0.3) {
                                    Vibration("/right", "right");
                                    k_1++;
                                    r = 1;
                                }
                            }
/*
                            if (r == 1 || r == 4) {
                                if (k_2 == 0 && w[2] > 0.45) {
                                    Vibration("/right", "right");
                                    k_2++;
                                    r = 2;
                                }
                            }
*/

//                            if ((r == 1 || r == 4) && k_3 == 0 && w_dis[3] >= 0 && w[3] > 0.38) {
                            if ((r == 1) && k_3 == 0 && w_dis[3] >= 0 && w[3] > 0.3) {
                                Vibration("/left", "left");
                                k_3++;
                                r = 3;
                            }

//                            if ((r == 2 || r == 3 || r == 5) && k_4 == 0 && w_dis[4] >= 0 && w[4] > 0.40) {
                            if ((r == 3) && k_4 == 0 && w_dis[4] >= 0 && w[4] > 0.3) {
                                Vibration("/right", "right");
                                k_4++;
                                r = 4;
                            }

                            if ((r == 4) && k_5 == 0 &&  w[4] > 0.3) {
                                Vibration("/arrival","arrival");
                                k_5++;
                                r = 5;
                            }


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

                            t2.setText("基準点1:" + String.valueOf(w[0]));
                            t3.setText("基準点2:" + w[1]);
                            t4.setText("基準点3:" + w[2]);
                            t5.setText("基準点4:" + w[3]);
                            t6.setText("基準点5:" + w[4]);
                            t7.setText("基準点6:" + w[5]);



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

    public class MessageReceiver extends BroadcastReceiver {
        private static final String TAG = "MessageReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            receivedMessage = intent.getStringExtra("message");
            info = intent.getIntExtra("z", 0);
            Log.d("MessageReceiver", "onReceive() receivedMessage = " + receivedMessage);

        }
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

    public void Vibration(final String s_arrival, final String ss_arrival){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult r = Wearable.MessageApi.sendMessage(
                            client,
                            node.getId(),
                            s_arrival,
                            ss_arrival.getBytes()
                    ).await();
                }
            }
        }).start();
    }

    /*----   コサイン類似度   ----*/
    /*
    public void fingerPrint_Cos_Similarity() {

        // タイマークラスによる自動実行
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handle.post(new Runnable() {
                    @Override
                    public void run() {

                        rssiVal = 7;
                        point = new ArrayList[rssiVal][3];
                        CSV("1104Node1.csv", 0);
                        CSV("1104Node2.csv", 1);
                        CSV("1104Node3.csv", 2);
                        CSV("1104Node4.csv", 3);
                        CSV("1104Node5.csv", 4);
                        CSV("1104Node6.csv", 5);
                        CSV("1104Node7.csv", 6);



                        double sum[] = new double[rssiVal];

                        double rssi_current = 0; // 現在位置で取得したRSSIを格納する
                        double rssi_database = 0; // データベースのrssiを格納する
                        double inner_product[] = new double[rssiVal];
                        double[] norm_value_current = new double[rssiVal];
                        double norm_current[] = new double[rssiVal];
                        double norm_value_database[] = new double[rssiVal];
                        double norm_database[] = new double[rssiVal];
                        double sim[] = new double[rssiVal];

                        int count[] = new int[rssiVal]; // 各測定点におけるDBとスキャン結果の一致APを数える
                        double max_sum = -10000;
                        int count_point = 0;


                        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
                        manager.startScan();
                        List<ScanResult> results = manager.getScanResults();//APのスキャン結果を取得
                        try {
                            Log.v("debug", "書き込み");
                            // SDカードフォルダのパス
                            String sdPath = Environment.getExternalStorageDirectory().getPath();
                            // 作成するファイル名
                            String fileName = "/cos_similarity" + "/fingerPrint_Cos_Similarity" + count_timer_cos_similarity + ".csv";
                            // 書き込み
                            BufferedWriter bw = null;

                            for (int i = 0; i < rssiVal; i++) { // 測定点番号ループ　(測定点1->測定点2->・・・)
                                for (int j = 0; j < point[i][0].size(); j++) { // 測定点データループ
                                    for (int k = 0; k < results.size(); k++) { // APスキャン結果ループ
                                        if (results.get(k).BSSID.equals(point[i][0].get(j))) { // APスキャン結果のBSSID＝測定点データのBSSIDならば　アルゴリズム計算
                                            rssi_current = results.get(k).level;
                                            rssi_database = Double.parseDouble(point[i][1].get(j));
                                            inner_product[i] = inner_product[i] + (rssi_current * rssi_database);

                                            norm_value_current[i] = norm_value_current[i] + (rssi_current * rssi_current);
                                            norm_current[i] = Math.sqrt(norm_value_current[i]);
                                            norm_value_database[i] = norm_value_database[i] + (rssi_database * rssi_database);
                                            norm_database[i] = Math.sqrt(norm_value_database[i]);

                                            sim[i] = inner_product[i] / (norm_current[i] * norm_database[i]);
                      }
                                    }
                                }
                                if(max_sum <  sim[i]) {
                                    max_sum = sim[i];
                                    count_point = i + 1;
                                    textview24.setText("(類似度)現在地は" + count_point);
                                }
                                textview17.setText("測定点1 類似度:" + sim[0] );
                                textview18.setText("測定点2 類似度:" + sim[1] );
                                textview19.setText("測定点3 類似度:" + sim[2] );
                                textview20.setText("測定点4 類似度:" + sim[3] );
                                textview21.setText("測定点5 類似度:" + sim[4] );
                                textview22.setText("測定点6 類似度:" + sim[5] );
                                textview23.setText("測定点7 類似度:" + sim[6] );

                                // Log.d("尤度: ", String.valueOf(sum[i]));
                            }
                            bw = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(sdPath + fileName), "utf-8"));
                            long currentTineMills = System.currentTimeMillis();

                            bw.write( "測定点1 類似度:" + "," + sim[0] + "\n"
                                    + "測定点2 類似度:" + "," + sim[1] + "\n"
                                    + "測定点3 類似度:" + "," + sim[2] + "\n"
                                    + "測定点4 類似度:" + "," + sim[3] + "\n"
                                    + "測定点5 類似度:" + "," + sim[4] + "\n"
                                    + "測定点6 類似度:" + "," + sim[5] + "\n"
                                    + "測定点7 類似度:" + "," + sim[6] + "\n"
                                    + "現在地は" + "," + count_point + "\n"
                                    + "time" + "," + currentTineMills + "\n");

                            //bw.write(sim[2] + "\n");
                            bw.close();

                        }catch (IndexOutOfBoundsException e){

                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        count_timer_cos_similarity += 1;
                    }
                });
            }

        }, 0, INTERVAL_PERIOD);
    }
    */
/*
    // Fingerprint手法の位置推定アルゴリズム
    public void fingerPrint_wKNN(){
        // タイマークラスによる自動実行
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handle.post(new Runnable() {
                    @Override
                    public void run() {

                        rssiVal = 5;
                        point = new ArrayList[rssiVal][3];
                        /*
                        CSV("1104Node1.csv", 0);
                        CSV("1104Node2.csv", 1);
                        CSV("1104Node3.csv", 2);
                        CSV("1104Node4.csv", 3);
                        CSV("1104Node5.csv", 4);
                        CSV("1104Node6.csv", 5);
                        CSV("1104Node7.csv", 6);
*/
/*
                        CSV("Node01_1201.csv", 0);
                        CSV("Node02_1201.csv", 1);
                        CSV("Node03_1201.csv", 2);
                        CSV("Node04_1201.csv", 3);
                        CSV("Node05_1201.csv", 4);

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
                            Log.v("debug", "書き込み");
                            // SDカードフォルダのパス
                            String sdPath = Environment.getExternalStorageDirectory().getPath();
                            // 作成するファイル名
                            String fileName = "/wknn" + "/wknn" + count_timer_wknn + ".csv";
                            // 書き込み
                            BufferedWriter bw = null;

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
                            //Log.d("weight" + t, String.valueOf(w[t]));
                            if (w[t] > max2) {
                                max2 = w[t];
                                fpno = t + 1;
                                textview16.setText("(類似度)現在地は" + fpno);
                            }

                            textview17.setText("測定点1 wk類似度:" + w[0] );
                            textview18.setText("測定点2 wk類似度:" + w[1] );
                            textview19.setText("測定点3 wk類似度:" + w[2] );
                            textview20.setText("測定点4 wk類似度:" + w[3] );
                            //textview21.setText("測定点5 wk類似度:" + w[4] );

                            //textview14.setText("測定点6 wk類似度:" + w[5] );
                            //textview15.setText("測定点7 wk類似度:" + w[6] );
                        }

                            for(int i = 0; i <= 4; i++) {
                                w_dis[i] = w[i] - w_2[i];
                            }

                            if(k_3 == 0 && w_dis[3] > 0 && w[3] > 0.55) {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                                        for (Node node : nodes.getNodes()) {
                                            MessageApi.SendMessageResult r = Wearable.MessageApi.sendMessage(
                                                    client,
                                                    node.getId(),
                                                    "/right",
                                                    "right".getBytes()
                                            ).await();
                                        }
                                    }
                                }).start();
                                k_3++;
                            }

                            bw = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(sdPath + fileName), "utf-8"));
                            long currentTineMills = System.currentTimeMillis();
                            bw.write( "測定点1 類似度:" + "," + w[0] + "\n"
                                    + "測定点2 類似度:" + "," + w[1] + "\n"
                                    + "測定点3 類似度:" + "," + w[2] + "\n"
                                    + "測定点4 類似度:" + "," + w[3] + "\n"
                                    + "測定点5 類似度:" + "," + w[4] + "\n"
                                    + "w_2[3]" + "," + w_2[3] + "\n"
                                    + "w_dis[3]" + "," + w_dis[3] + "\n"
                                    + "現在地は" + "," + fpno + "\n"
                                    + "time" + "," + currentTineMills + "\n");
                            //bw.write(w[2] + "\n");
                            bw.close();


                            for(int i = 0; i <= 4; i++) {
                                w_2[i] = w[i];
                            }
                            textview21.setText("w_2[3]" + w_2[3] );


                        }catch (IndexOutOfBoundsException ee){

                        }catch (Exception ee) {
                            ee.printStackTrace();
                        }
                        count_timer_wknn += 1;
                    }
                });
            }
        }, 0, INTERVAL_PERIOD);
    }
*/

/*----   尤度   ----*/
/*
    public void fingerPrint_Likelihood() {

        // タイマークラスによる自動実行
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handle.post(new Runnable() {
                    @Override
                    public void run() {

                        rssiVal = 7;
                        point = new ArrayList[rssiVal][3];
                        CSV("1104Node1.csv", 0);
                        CSV("1104Node2.csv", 1);
                        CSV("1104Node3.csv", 2);
                        CSV("1104Node4.csv", 3);
                        CSV("1104Node5.csv", 4);
                        CSV("1104Node6.csv", 5);
                        CSV("1104Node7.csv", 6);

                        double denominator[][] = new double[rssiVal][100];
                        double leftside[][] = new double[rssiVal][100];
                        double rightside[][] = new double[rssiVal][100];
                        double exValue1[][] = new double[rssiVal][100];
                        double infinity[][] = new double[rssiVal][100];
                        double value2[][] = new double[rssiVal][100];
                        double yi[][] = new double[rssiVal][100];
                        double value1[][] = new double[rssiVal][100];
                        double sum[] = new double[rssiVal];
                        double plausibility[][] = new double[rssiVal][100];

                        double rssi_current = 0; // 現在位置で取得したRSSIを格納する
                        double rssi_database = 0; // データベースのrssiを格納する

                        int count[] = new int[rssiVal]; // 各測定点におけるDBとスキャン結果の一致APを数える
                        double max_sum = -10000;
                        int count_point = 0;

                        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
                        manager.startScan();
                        List<ScanResult> results = manager.getScanResults();//APのスキャン結果を取得
                        try {
                            Log.v("debug", "書き込み");
                            // SDカードフォルダのパス
                            String sdPath = Environment.getExternalStorageDirectory().getPath();
                            // 作成するファイル名
                            String fileName = "/likelihood" + "/likelihood" + count_timer_likelihood + ".csv";
                            // 書き込み
                            BufferedWriter bw = null;

                            for (int i = 0; i < rssiVal; i++) { // 測定点番号ループ　(測定点1->測定点2->・・・)
                                for (int j = 0; j < point[i][0].size(); j++) { // 測定点データループ
                                    for (int k = 0; k < results.size(); k++) { // APスキャン結果ループ
                                        if (results.get(k).BSSID.equals(point[i][0].get(j))) { // APスキャン結果のBSSID＝測定点データのBSSIDならば　アルゴリズム計算
                                            rssi_current = results.get(k).level;
                                            rssi_database = Double.parseDouble(point[i][1].get(j));

                                            denominator[i][j] = 2 * Math.PI * Double.parseDouble(point[i][2].get(j)); // 2πσ^2
                                            if(denominator[i][j] <= 0.001){
                                                leftside[i][j] = 1 / Math.sqrt(0.001);
                                            }else {
                                                //Log.d("denominator",String.valueOf(Math.sqrt(denominator[2][j])));
                                                leftside[i][j] = 1 / Math.sqrt(denominator[i][j]);  // 1 / √2πσ^2
                                                //Log.d("left",String.valueOf(rightside[2][j]));
                                            }
                                            //Log.d("rssi_current",String.valueOf(rssi_current));
                                            //Log.d("rssi_database",String.valueOf(rssi_database));
                                            value1[i][j] = rssi_current - rssi_database;
                                            value2[i][j] = Math.pow(value1[i][j], 2);
                                            //Log.d("value1",String.valueOf(value1[2][j]));
                                            //Log.d("value2",String.valueOf(value2[2][j]));

                                            exValue1[i][j] = -1 * value2[i][j];
                                            //Log.d("exValue1",String.valueOf(exValue1[2][j]));
                                            infinity[i][j] = exValue1[i][j] / (2 * Double.parseDouble(point[i][2].get(j)));
                                            //Log.d("infinity",String.valueOf(infinity[2][j]));

                                            if(infinity[i][j] <  -700){
                                                rightside[i][j] = 0.00000000000001;
                                            }else{
                                                rightside[i][j] = Math.exp(infinity[i][j]);
                                            }
                                            //Log.d("rightside",String.valueOf(rightside[2][j]));

                                            plausibility[i][j] = leftside[i][j] * rightside[i][j];
                                            //Log.d("plausibility",String.valueOf(plausibility[2][j]));

                                            yi[i][j] = Math.log(plausibility[i][j]);
                                            //Log.d("yi",String.valueOf(yi[2][j]));

                                            sum[i] = sum[i] + yi[i][j];
                                            //Log.d("sum",String.valueOf(sum[2]));
                                        }
                                    }
                                }
                                if(sum[i] != 0 && max_sum <  sum[i]) {
                                    max_sum = sum[i];
                                    count_point = i + 1;
                                    textview08.setText("(尤度)現在地は" + count_point);
                                }
                                textview01.setText("測定点1 尤度:" + sum[0] );
                                textview02.setText("測定点2 尤度:" + sum[1] );
                                textview03.setText("測定点3 尤度:" + sum[2] );
                                textview04.setText("測定点4 尤度:" + sum[3] );
                                textview05.setText("測定点5 尤度:" + sum[4] );
                                textview06.setText("測定点6 尤度:" + sum[5] );
                                textview07.setText("測定点7 尤度:" + sum[6] );

                                // Log.d("尤度: ", String.valueOf(sum[i]));
                            }bw = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(sdPath + fileName), "utf-8"));
                            long currentTineMills = System.currentTimeMillis();


                            bw.write( "測定点1 類似度:" + "," + sum[0] + "\n"
                                    + "測定点2 類似度:" + "," + sum[1] + "\n"
                                    + "測定点3 類似度:" + "," + sum[2] + "\n"
                                    + "測定点4 類似度:" + "," + sum[3] + "\n"
                                    + "測定点5 類似度:" + "," + sum[4] + "\n"
                                    + "測定点6 類似度:" + "," + sum[5] + "\n"
                                    + "測定点7 類似度:" + "," + sum[6] + "\n"
                                    + "現在地は" + "," + count_point + "\n"
                                    + "time" + "," + currentTineMills + "\n");
                            //bw.write(sum[2] + "\n");
                            bw.close();

                        }catch (IndexOutOfBoundsException e){

                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        count_timer_likelihood += 1;
                    }
                });
            }

        }, 0, INTERVAL_PERIOD);
    }*/

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