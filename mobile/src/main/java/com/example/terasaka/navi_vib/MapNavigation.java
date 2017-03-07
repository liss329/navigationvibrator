package com.example.terasaka.navi_vib;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by Terasaka on 2016/12/08.
 */
public class MapNavigation extends Activity {


    /*
        private TextView mStepCounterText;
        private SensorManager mSensorManager;
        private Sensor mStepCounterSensor;
        private float steps;
        private TextView nameLists;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.sensor);

            mStepCounterText = (TextView) findViewById(R.id.pedometer);
            steps = 0;

            nameLists = (TextView)findViewById(R.id.namelist_id);
            mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        }

        @Override
        protected void onResume() {
            super.onResume();

            //KITKAT以上かつTYPE_STEP_COUNTERが有効ならtrue
            boolean isTarget = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    && getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);

            if (isTarget) {
                //TYPE_STEP_COUNTERが有効な場合の処理
                Log.d("hasStepCounter", "STEP-COUNTER is available!!!");
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

                setStepCounterListener();
            } else {
                //TYPE_STEP_COUNTERが無効な場合の処理
                Log.d("hasStepCounter", "STEP-COUNTER is NOT available.");
                mStepCounterText.setText("STEP-COUNTER is NOT available.");
            }

            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            String str = "実装されているセンサー一覧:\n";
            for(Sensor s : sensors) {
                str += s.getName() + "\n";
                }
            nameLists.setText(str);
            }



        private void setStepCounterListener() {
            if (mStepCounterSensor != null) {
                //ここでセンサーリスナーを登録する
                mSensorManager.registerListener(mStepCountListener, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }

        private final SensorEventListener mStepCountListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                //センサーから取得した値をテキストビューに表示する
                steps = sensorEvent.values[0];
                mStepCounterText.setText(String.format(Locale.US, "%f", steps));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }*/
    private SensorManager mSensorManager = null;
    private SensorEventListener mSensorEventListener = null;

    private float[] fAccell = null;
    private float[] fMagnetic = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);



        mSensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE );

        mSensorEventListener = new SensorEventListener()
        {
            public void onSensorChanged (SensorEvent event) {
                // センサの取得値をそれぞれ保存しておく
                switch( event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = event.values.clone();
                        break;
                }

                // fAccell と fMagnetic から傾きと方位角を計算する
                if( fAccell != null && fMagnetic != null ) {
                    // 回転行列を得る
                    float[] inR = new float[9];
                    SensorManager.getRotationMatrix(
                            inR,
                            null,
                            fAccell,
                            fMagnetic );
                    // ワールド座標とデバイス座標のマッピングを変換する
                    float[] outR = new float[9];
                    SensorManager.remapCoordinateSystem(
                            inR,
                            SensorManager.AXIS_X, SensorManager.AXIS_Y,
                            outR );
                    // 姿勢を得る
                    float[] fAttitude = new float[3];
                    SensorManager.getOrientation(
                            outR,
                            fAttitude );

                    String buf =
                            "---------- Orientation --------\n" +
                                    String.format( "方位角\n\t%f\n", rad2deg( fAttitude[0] )) +
                                    String.format( "前後の傾斜\n\t%f\n", rad2deg( fAttitude[1] )) +
                                    String.format( "左右の傾斜\n\t%f\n", rad2deg( fAttitude[2] ));
                    TextView t = (TextView) findViewById( R.id.textView1 );
                    t.setText( buf );
/*
                    TextView t2 = (TextView) findViewById( R.id.textView2 );
                    t2.setText( "" + fAttitude[0] );
                    TextView t3 = (TextView) findViewById( R.id.textView3 );
                    t3.setText( "" + fAttitude[1]);
                    TextView t4 = (TextView) findViewById( R.id.textView4 );
                    t4.setText( "" + fAttitude[2]);
                    */
                }
            }
            public void onAccuracyChanged (Sensor sensor, int accuracy) {}
        };
    }

    private float rad2deg( float rad ) {
        return rad * (float) 180.0 / (float) Math.PI;
        //return rad *  (float) Math.toDegrees(rad);

    }

    protected void onStart() { // ⇔ onStop
        super.onStart();

        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER ),
                SensorManager.SENSOR_DELAY_UI );
        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD ),
                SensorManager.SENSOR_DELAY_UI );
    }

    protected void onStop() { // ⇔ onStart
        super.onStop();

        mSensorManager.unregisterListener( mSensorEventListener );
    }
}