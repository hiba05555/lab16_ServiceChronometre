package com.example.servicechronometre;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HCChronometreService extends Service {

    private static final String HC_CHANNEL_ID = "hc_chrono_channel";
    private static final int HC_NOTIF_ID = 2001;
    private static final String HC_TAG = "HC_ChronoService";

    private final IBinder hcBinder = new HCLocalBinder();
    private int hcSecondes = 0;
    private boolean hcIsRunning = false;
    private ScheduledExecutorService hcExecutor;
    private NotificationManager hcNotifManager;

    public class HCLocalBinder extends Binder {
        public HCChronometreService getService() {
            return HCChronometreService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        hcNotifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        hcCreateChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent != null) ? intent.getAction() : null;

        if ("HC_STOP".equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!hcIsRunning) {
            hcIsRunning = true;
            hcSecondes = 0;
            startForeground(HC_NOTIF_ID, hcBuildNotification());
            hcStartTimer();
        }
        return START_STICKY;
    }

    private void hcStartTimer() {
        hcExecutor = Executors.newSingleThreadScheduledExecutor();
        hcExecutor.scheduleAtFixedRate(() -> {
            hcSecondes++;
            hcNotifManager.notify(HC_NOTIF_ID, hcBuildNotification());
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void hcCreateChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    HC_CHANNEL_ID,
                    "HC Chronomètre",
                    NotificationManager.IMPORTANCE_LOW
            );
            hcNotifManager.createNotificationChannel(channel);
        }
    }

    private Notification hcBuildNotification() {
        return new NotificationCompat.Builder(this, HC_CHANNEL_ID)
                .setContentTitle("⏱️ HC Chronomètre en cours")
                .setContentText("Temps écoulé : " + hcFormatTime(hcSecondes))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private String hcFormatTime(int sec) {
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    public int getHcSecondes() {
        return hcSecondes;
    }

    public String getHcFormattedTime() {
        return hcFormatTime(hcSecondes);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return hcBinder;
    }

    @Override
    public void onDestroy() {
        hcIsRunning = false;
        if (hcExecutor != null) hcExecutor.shutdown();
        stopForeground(true);
        super.onDestroy();
    }
}