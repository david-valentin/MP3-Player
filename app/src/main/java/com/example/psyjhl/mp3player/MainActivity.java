package com.example.psyjhl.mp3player;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.os.RemoteCallbackList;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    File selectedFromList;
    private MP3Service.MyBinder myService = null;
    String currentSongString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("g53mdp", "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView lv = (ListView) findViewById(R.id.songList);
        File musicDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/");
        File list[] = musicDir.listFiles();
        lv.setAdapter(new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1, list));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter,
                                    View myView,
                                    int myItemInt,
                                    long mylng) {
                selectedFromList = (File) (lv.getItemAtPosition(myItemInt));
                String fileName = selectedFromList.getName();
                myService.load(selectedFromList.getAbsolutePath());
                myService.setFileName(fileName);
                TextView currentSong = (TextView) findViewById(R.id.currentSong);
                currentSong.setText(fileName);
                currentSongString = fileName;
            }
        });

        this.startService(new Intent(this, MP3Service.class));
        this.bindService(new Intent(this, MP3Service.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy(){
        if(serviceConnection!=null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
        //if the mp3player is in stop state then you stop the service
        if(!myService.isPlaying()){
            this.stopService(new Intent(this, MP3Service.class));
        }

        Log.d("g53mdp", "MainActivity onDestroy");
        super.onDestroy();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceConnected");
            myService = (MP3Service.MyBinder) service;
            myService.registerCallback(callback);
            TextView currentSong = (TextView) findViewById(R.id.currentSong);
            currentSong.setText(myService.getFileName());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            myService.unregisterCallback(callback);
            myService = null;
        }
    };

    public void playMusic(View V) {
        myService.play();
    }

    public void pauseMusic(View V) {
        myService.pause();
    }

    public void stopMusic(View V) {
        myService.stop();
        TextView currentSong = (TextView) findViewById(R.id.currentSong);
        currentSong.setText("Select A Song");

    }

    CallBackInterface callback = new CallBackInterface() {
        @Override
        public void progressEvent(final int progress, final int maxDuration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressBar musicProgress = (ProgressBar) findViewById(R.id.musicProgress);
                    musicProgress.setMax(maxDuration-1);
                    musicProgress.setProgress(progress);

                    TextView timeStamp = (TextView) findViewById(R.id.CurrentProgressText);
                    timeStamp.setText(timeFormat(progress/1000));

                    TextView maxTime = (TextView) findViewById(R.id.MaxDurationText);
                    maxTime.setText(timeFormat(maxDuration/1000 - 1));
                }
            });
        }
    };

    //Function used to format the progress into minutes and seconds for UI
    private String timeFormat(int seconds) {
        if (seconds == -1){
            return"0:00";
        }
        int minute = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (remainingSeconds < 10) {
            return String.valueOf(minute) + ":0" + String.valueOf(remainingSeconds);
        } else {
            return String.valueOf(minute) + ":" + String.valueOf(remainingSeconds);
        }
    }


}

