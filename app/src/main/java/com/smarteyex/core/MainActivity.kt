package com.yourpackage.smarteyex;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Containers
    private FrameLayout splashContainer;
    private FrameLayout startContainer;
    private FrameLayout dashboardContainer;

    // UI Elements
    private Button btnStart;
    private Button btnObserve;
    private Button btnMemory;
    private TextView tvClock;
    private TextView tvAiResponse;

    // Handler
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        startSplash();
        startClock();
        setupActions();
    }

    // =========================
    // INIT UI
    // =========================
    private void bindViews() {
        splashContainer = findViewById(R.id.splash_container);
        startContainer = findViewById(R.id.start_container);
        dashboardContainer = findViewById(R.id.dashboard_container);

        btnStart = findViewById(R.id.btn_start);
        btnObserve = findViewById(R.id.btnToggleObserve);
        btnMemory = findViewById(R.id.btnMemory);

        tvClock = findViewById(R.id.tv_clock);
        tvAiResponse = findViewById(R.id.tv_ai_response);
    }

    // =========================
    // SPLASH LOGIC
    // =========================
    private void startSplash() {
        uiHandler.postDelayed(() -> {
            splashContainer.setVisibility(View.GONE);
            startContainer.setVisibility(View.VISIBLE);
        }, 2000); // 2 detik splash
    }

    // =========================
    // CLOCK REALTIME
    // =========================
    private void startClock() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(new Date());
                tvClock.setText(time);
                uiHandler.postDelayed(this, 1000);
            }
        });
    }

    // =========================
    // BUTTON ACTIONS
    // =========================
    private void setupActions() {

        btnStart.setOnClickListener(v -> {
            startContainer.setVisibility(View.GONE);
            dashboardContainer.setVisibility(View.VISIBLE);
        });

        btnObserve.setOnClickListener(v -> {
            tvAiResponse.setText("👁️ Observe mode aktif");
        });

        btnMemory.setOnClickListener(v -> {
            tvAiResponse.setText("🧠 Memory dibuka (SmartEyeX 130809)");
        });
    }
}
