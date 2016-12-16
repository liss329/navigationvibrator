package com.example.terasaka.navi_vib;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;




public class MainActivity extends Activity {
    private GoogleApiClient client;
    private int count = 0;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // 初期化
            this.client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d("MyFragment", "onConnected");
                        }
                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d("MyFragment", "onConnectionSuspended");
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.d("MyFragment", "onConnectionFailed");
                        }
                    })
                    .addApi(Wearable.API)
                    .build();
            this.client.connect();

            this.client = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
            this.client.connect();



            final Intent i = new Intent(this, Navigation.class);
            final Intent j = new Intent(this, MainActivity.class);
            final Button button = (Button) findViewById(R.id.button);

            ListView listView = (ListView)findViewById(R.id.listview);

            // アダプタの作成
            listView.setAdapter(new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_single_choice,
                    SIZES)
            );
            // 選択の方式の設定
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            // 指定したアイテムがチェックされているかを設定
            //listView.setItemChecked(0, true);

            //選択アイテムを取得
            final SparseBooleanArray checked = listView.getCheckedItemPositions();

            // アイテムがクリックされた時に呼び出されるコールバックを登録
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent,
                                        View view, int position, long id) {
                    // クリックされた時の処理
                    count = position + 1;
                }

            });

            // 現在チェックされているアイテムの position を取得
            //int s = listView.getCheckedItemPosition();

            // ボタンクリックイベント
            Button btn = (Button)findViewById(R.id.button);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(count == 1){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Log.d("MyFragment", "onClick");
                                final String message = "1";
                                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                                for (Node node : nodes.getNodes()) {
                                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                            client,
                                            node.getId(),
                                            "/1",
                                            message.getBytes())
                                            .await();
                                    if (result.getStatus().isSuccess()) {
                                        Log.d("onClick", "isSuccess is true1");
                                    } else {
                                        Log.d("onClick", "isSuccess is false");
                                    }
                                }
                            }
                        }).start();
                    }else if(count == 2){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Log.d("MyFragment", "onClick");
                                final String message = "2";
                                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                                for (Node node : nodes.getNodes()) {
                                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                            client,
                                            node.getId(),
                                            "/2",
                                            message.getBytes())
                                            .await();
                                    if (result.getStatus().isSuccess()) {
                                        Log.d("onClick", "isSuccess is true");
                                    } else {
                                        Log.d("onClick", "isSuccess is false");
                                    }
                                }
                            }
                        }).start();
                    }else if(count == 4){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Log.d("MyFragment", "onClick");
                                final String message = "328";
                                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                                for (Node node : nodes.getNodes()) {
                                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                            client,
                                            node.getId(),
                                            "/328教員室前",
                                            message.getBytes())
                                            .await();
                                    if (result.getStatus().isSuccess()) {
                                        Log.d("onClick", "isSuccess is true");
                                    } else {
                                        Log.d("onClick", "isSuccess is false");
                                    }
                                }
                            }
                        }).start();
                    }else{
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Log.d("MyFragment", "onClick");
                                final String message = "else";
                                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                                for (Node node : nodes.getNodes()) {
                                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                            client,
                                            node.getId(),
                                            "/hello",
                                            message.getBytes())
                                            .await();
                                    if (result.getStatus().isSuccess()) {
                                        Log.d("onClick", "isSuccess is true");
                                    } else {
                                        Log.d("onClick", "isSuccess is false");
                                    }
                                }
                            }
                        }).start();
                    }
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            });
        }
    // ListView に表示させる文字列
    private static final String[] SIZES = new String[] {
            "測定点1", "測定点2", "測定点3","328教員室前","測定点5"
    };
}

/*
            RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton radiobutton2 = (RadioButton) findViewById(R.id.radiobutton_2);
                    RadioButton radiobutton3 = (RadioButton) findViewById(R.id.radiobutton_3);
                    RadioButton radiobutton4 = (RadioButton) findViewById(R.id.radiobutton_3);
                    RadioButton radiobutton5 = (RadioButton) findViewById(R.id.radiobutton_3);
                    RadioButton radiobutton6 = (RadioButton) findViewById(R.id.radiobutton_3);
                    RadioButton radiobutton7 = (RadioButton) findViewById(R.id.radiobutton_3);



                    if (radiobutton2.isChecked() == true) {
                        // チェックされた状態の時の処理を記述
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Log.d("MyFragment", "onClick");
                                        final String message = "Hello world";
                                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                                        for (Node node : nodes.getNodes()) {
                                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                                    client,
                                                    node.getId(),
                                                    "/hello",
                                                    message.getBytes())
                                                    .await();
                                            if (result.getStatus().isSuccess()) {
                                                Log.d("onClick", "isSuccess is true");
                                            } else {
                                                Log.d("onClick", "isSuccess is false");
                                            }
                                        }
                                    }
                                }).start();


                            }

                        });
                    }else if(radiobutton3.isChecked() == true){
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(j);
                            }
                        });

                    }else{
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(j);
                            }
                        });
                    }
                }
            });*/

/*

    static class GridViewPagerAdapter extends FragmentGridPagerAdapter {

        public GridViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getFragment(int row, int col) {

            return MyFragment.newInstance();
        }

        @Override
        public int getRowCount() {
            return 2;
        }

        @Override
        public int getColumnCount(int i) {
            return 2;
        }


    }


    static class MyFragment extends CardFragment {
        private GoogleApiClient client;



        @Override
        public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            this.client = new GoogleApiClient.Builder(this.getActivity())
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                           // Log.d("MyFragment", "onConnected");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            //Log.d("MyFragment", "onConnectionSuspended");
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            //Log.d("MyFragment", "onConnectionFailed");
                        }
                    })
                    .addApi(Wearable.API)
                    .build();
            this.client.connect();


            Button button = new Button(this.getActivity());
            button.setText("329教員室前");


            button.setOnClickListener(new View.OnClickListener() {



                @Override
                public void onClick(View v) {



                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Log.d("MyFragment", "onClick");
                            final String message = "Hello world";
                            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                            for (Node node : nodes.getNodes()) {
                                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                        client,
                                        node.getId(),
                                        "/hello",
                                        message.getBytes())
                                        .await();
                                if (result.getStatus().isSuccess()) {
                                    //Log.d("onClick", "isSuccess is true");
                                } else {
                                    //Log.d("onClick", "isSuccess is false");
                                }
                            }
                        }
                    }).start();
                }
            });
            return button;
        }

        public static MyFragment newInstance() {
            return new MyFragment();
        }
    }

    public void clickbtn() {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Log.d("MyFragment", "onClick");
                    final String message = "Hello world";
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                client,
                                node.getId(),
                                "/hello",
                                message.getBytes())
                                .await();
                        if (result.getStatus().isSuccess()) {
                            //Log.d("onClick", "isSuccess is true");
                        } else {
                            //Log.d("onClick", "isSuccess is false");
                        }
                    }
                }
            }).start();
        }


}
*/