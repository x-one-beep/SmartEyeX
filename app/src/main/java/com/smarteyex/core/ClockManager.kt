package com.smarteyex.core.clock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockManager {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TextView clockView;
    private final SharedPreferences prefs;

    private boolean useSystemTime = true;
    private long manualOffsetMillis = 0;

    public ClockManager(Context context, TextView clockView) {
        this.clockView = clockView;
        this.prefs = context.getSharedPreferences("clock_prefs", Context.MODE_PRIVATE);

        useSystemTime = prefs.getBoolean("use_system", true);
        manualOffsetMillis = prefs.getLong("manual_offset", 0);
    }

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (!useSystemTime) {
                now += manualOffsetMillis;
            }

            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(now));

            clockView.setText(time);
            handler.postDelayed(this, 1000);
        }
    };

    public void start() {
        handler.post(clockRunnable);
    }

    public void stop() {
        handler.removeCallbacks(clockRunnable);
    }

    public void setManualTime(long targetMillis) {
        manualOffsetMillis = targetMillis - System.currentTimeMillis();
        useSystemTime = false;
        save();
    }

    public void useSystemTime() {
        useSystemTime = true;
        manualOffsetMillis = 0;
        save();
    }

    private void save() {
        prefs.edit()
                .putBoolean("use_system", useSystemTime)
                .putLong("manual_offset", manualOffsetMillis)
                .apply();
    }
}
