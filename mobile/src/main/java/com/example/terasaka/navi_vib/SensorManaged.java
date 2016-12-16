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
public class SensorManaged extends Activity {


    private SensorManager mSensorManager = null;
    private SensorEventListener mSensorEventListener = null;

    private float[] fAccell = null;
    private float[] fMagnetic = null;


    //地図表示
    int left = 1380;
    int top = 80;
    int right = 0;
    int bottom = 0;
    ImageView img;
    FrameLayout.MarginLayoutParams mlp;
    Bitmap bitmap2;

    int sty = 0;

    protected void onCreate(Bundle savedInstanceState) {
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
        img.setImageBitmap(bitmap2);


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

                    TextView t2 = (TextView) findViewById( R.id.textView2 );
                    t2.setText( "" + fAttitude[0] );
                    TextView t3 = (TextView) findViewById( R.id.textView3 );
                    t3.setText( "" + fAttitude[1]);
                    TextView t4 = (TextView) findViewById( R.id.textView4 );
                    t4.setText( "" + fAttitude[2]);
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