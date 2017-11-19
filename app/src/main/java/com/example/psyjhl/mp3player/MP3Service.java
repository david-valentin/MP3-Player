package com.example.psyjhl.mp3player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.os.RemoteCallbackList;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by psyjhl on 10/11/2016.
 */
public class MP3Service extends Service {
    MP3Player mp3Player = new MP3Player();
    NotificationCompat.Builder mp3PlayerNotification;
    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();
    ProgressCount counter;
    public class ProgressCount extends Thread implements Runnable
    {
        public int progress = 0;
        public boolean running = true;
        public boolean playingMusic = false;
        public int maxDuration;

        public ProgressCount()
        {
            this.start();
        }

        public void run()
        {
            while(this.running)
            {
                try {Thread.sleep(1000);} catch(Exception e) {return;}
                if(playingMusic) {
                    progress = mp3Player.getProgress();
                }
                doCallbacks(progress, maxDuration);
            }
        }
    }

    public void doCallbacks(int progress, int maxDuration)
    {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++)
        {
            remoteCallbackList.getBroadcastItem(i).callback.progressEvent(progress, maxDuration);
        }
        remoteCallbackList.finishBroadcast();
    }
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onCreate");
        super.onCreate();
        mp3PlayerNotification = new NotificationCompat.Builder(this);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),0);
        mp3PlayerNotification.setContentTitle("MP3Player");
        mp3PlayerNotification.setVisibility(1);
        mp3PlayerNotification.setSmallIcon(android.R.drawable.btn_star_big_on);
        mp3PlayerNotification.setContentIntent(pIntent);
        mp3PlayerNotification.build();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, mp3PlayerNotification.build());
        counter = new ProgressCount();
    }

    @Override
    public void onDestroy(){
        Log.d("g53mdp", "service onDestroy");
        counter.running = false;
        counter = null;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
        super.onDestroy();
    }
    public void play() {
        counter.playingMusic = true;
        mp3Player.play();
    }

    public void pause() {
        counter.playingMusic = false;
        mp3Player.pause();
    }

    public void stop() {
        counter.playingMusic = false;
        counter.progress = 0;
        setFileName("");
        mp3Player.stop();
    }

    public void load(String filePath){
        mp3Player.stop();
        counter.progress = 0;
        counter.playingMusic = true;
        mp3Player.load(filePath);
        counter.maxDuration = mp3Player.getDuration();
    }

    //Sets the notification text to be the ame of the song
    public void setFileName(String fileName){
        mp3PlayerNotification.setContentText(fileName);
        mp3PlayerNotification.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, mp3PlayerNotification.build());
    }
    //Checks if the mp3 player is playing
    public boolean isPlaying(){
        if(mp3Player.getState() == MP3Player.MP3PlayerState.STOPPED){
            return false;
        } else {
            return true;
        }
    }

    public String getFileName(){
        return (String) mp3PlayerNotification.mContentText;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        Log.d("g53mdp", "service onBind");
        return new MyBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("g53mdp", "service onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("g53mdp", "service onUnbind");
        return super.onUnbind(intent);
    }

    //Code Referenced from: http://stackoverflow.com/questions/19568315/how-to-handle-code-when-app-is-killed-by-swiping-in-android
    //This ensures service is destroyed when the task and process is destroyed even when music is playing
    @Override
    public void onTaskRemoved(Intent rootIntent){
        stopSelf();
    }

    public class MyBinder extends Binder implements IInterface {
        private CallBackInterface callback;
        public IBinder asBinder() {
            return this;
        }
        void play(){
            MP3Service.this.play();
        }
        void pause(){
            MP3Service.this.pause();
        }
        void stop(){
            MP3Service.this.stop();
        }
        void load(String filePath){
            MP3Service.this.load(filePath);
        }
        public void setFileName(String fileName){
            MP3Service.this.setFileName(fileName);
        }
        public String getFileName(){return MP3Service.this.getFileName();}
        public void registerCallback(CallBackInterface callback) {
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }
        public void unregisterCallback(CallBackInterface callback) {
            remoteCallbackList.unregister(MyBinder.this);
        }
        public boolean isPlaying(){
            return MP3Service.this.isPlaying();
        };
    }
}
