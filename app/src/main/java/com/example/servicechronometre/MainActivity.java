package com.example.servicechronometre;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private TextView tvTemps, tvStatus;
    private MaterialButton btnStart, btnStop;

    private HCChronometreService hcService;
    private boolean hcIsBound = false;

    private final Handler hcHandler = new Handler(Looper.getMainLooper());
    private final Runnable hcUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (hcIsBound && hcService != null) {
                tvTemps.setText(hcService.getHcFormattedTime());
                hcHandler.postDelayed(this, 1000);
            }
        }
    };

    private final ServiceConnection hcConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HCChronometreService.HCLocalBinder binder =
                    (HCChronometreService.HCLocalBinder) service;
            hcService = binder.getService();
            hcIsBound = true;
            tvStatus.setText("✅ Service en cours...");
            hcHandler.post(hcUpdateRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            hcIsBound = false;
            tvStatus.setText("❌ Service déconnecté");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTemps = findViewById(R.id.tvTemps);
        tvStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> hcStartService());
        btnStop.setOnClickListener(v -> hcStopService());
    }

    private void hcStartService() {
        Intent intent = new Intent(this, HCChronometreService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, hcConnection, Context.BIND_AUTO_CREATE);
        tvStatus.setText("🔄 Démarrage du service...");
    }

    private void hcStopService() {
        Intent intent = new Intent(this, HCChronometreService.class);
        intent.setAction("HC_STOP");
        stopService(intent);

        if (hcIsBound) {
            unbindService(hcConnection);
            hcIsBound = false;
        }

        hcHandler.removeCallbacks(hcUpdateRunnable);
        tvTemps.setText("00:00");
        tvStatus.setText("⏹️ Service arrêté");
    }

    @Override
    protected void onDestroy() {
        if (hcIsBound) {
            unbindService(hcConnection);
            hcHandler.removeCallbacks(hcUpdateRunnable);
        }
        super.onDestroy();
    }
}