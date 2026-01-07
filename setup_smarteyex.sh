#!/data/data/com.termux/files/usr/bin/bash
# Script TOTAL LENGKAP untuk membuat proyek SmartEyeX dari awal di Termux
# Lanjutan dari kode sebelumnya, dengan penambahan: struktur package, integrasi API AI (Groq), error handling, dll.
# Jalankan: chmod +x total_smarteyex.sh && ./total_smarteyex.sh
# Setelah itu: cd SmartEyeX && ./gradlew assembleDebug

PROJECT_DIR="$HOME/SmartEyeX"
mkdir -p "$PROJECT_DIR"
cd "$PROJECT_DIR"

# build.gradle (project)
cat > build.gradle << 'EOF'
plugins {
    id 'com.android.application' version '8.1.4' apply false
}
EOF

# settings.gradle
cat > settings.gradle << 'EOF'
pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = 'SmartEyeX'
include ':app'
EOF

# gradle-wrapper.properties
mkdir -p gradle/wrapper
cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# app/build.gradle (dengan dependencies tambahan untuk AI API)
cat > app/build.gradle << 'EOF'
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.smarteyex'
    compileSdk 34

    defaultConfig {
        applicationId "com.smarteyex"
        minSdk 29
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig true
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.fragment:fragment:1.6.2'
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-service:2.7.0'
    implementation 'androidx.core:core:1.12.0'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'  // Untuk API AI
    implementation 'com.google.code.gson:gson:2.10.1'   // Untuk JSON parsing
}
EOF

# AndroidManifest.xml (lengkap dengan semua permissions & services)
cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.smarteyex">

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.SmartEyeXService"
            android:enabled="true"
            android:exported="false" />

        <service
            android:name=".service.NotificationListener"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>

        <receiver
            android:name=".service.BootReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

    </application>

</manifest>
EOF

# Struktur package: service, ai, ui
mkdir -p app/src/main/java/com/smarteyex/service
mkdir -p app/src/main/java/com/smarteyex/ai
mkdir -p app/src/main/java/com/smarteyex/ui

# MainActivity.java (di ui package, dengan error handling)
cat > app/src/main/java/com/smarteyex/ui/MainActivity.java << 'EOF'
package com.smarteyex.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MenuItem;
import com.smarteyex.service.SmartEyeXService;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            requestPermissions();
            startService(new Intent(this, SmartEyeXService.class));
            setupNavigation();
            loadFragment(new HomeFragment());
        } catch (Exception e) {
            e.printStackTrace();  // Error handling
        }
    }

    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS
        };
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    private void setupNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        try {
            if (item.getItemId() == R.id.nav_home) fragment = new HomeFragment();
            else if (item.getItemId() == R.id.nav_status) fragment = new StatusFragment();
            else if (item.getItemId() == R.id.nav_log) fragment = new LogFragment();
            else if (item.getItemId() == R.id.nav_audio) fragment = new AudioFragment();
            else if (item.getItemId() == R.id.nav_camera) fragment = new CameraFragment();
            else if (item.getItemId() == R.id.nav_memory) fragment = new MemoryFragment();
            else if (item.getItemId() == R.id.nav_permissions) fragment = new PermissionsFragment();
            else if (item.getItemId() == R.id.nav_hardware) fragment = new HardwareFragment();
            else if (item.getItemId() == R.id.nav_settings) fragment = new SettingsFragment();
            if (fragment != null) loadFragment(fragment);
            drawerLayout.closeDrawers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

# SmartEyeXService.java (di service package, dengan error handling)
cat > app/src/main/java/com/smarteyex/service/SmartEyeXService.java << 'EOF'
package com.smarteyex.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import com.smarteyex.ai.AudioEngine;
import com.smarteyex.ai.CameraVisionEngine;
import com.smarteyex.ai.MemoryEngine;

public class SmartEyeXService extends Service {
    private PowerManager.WakeLock wakeLock;
    private AudioEngine audioEngine;
    private CameraVisionEngine cameraEngine;
    private MemoryEngine memoryEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createNotificationChannel();
            startForeground(1, buildNotification());
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartEyeX:WakeLock");
            wakeLock.acquire();
            audioEngine = new AudioEngine(this);
            cameraEngine = new CameraVisionEngine(this);
            memoryEngine = new MemoryEngine(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (wakeLock != null) wakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        try {
            NotificationChannel channel = new NotificationChannel("smarteyex", "SmartEyeX Service", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, "smarteyex")
                .setContentTitle("SmartEyeX")
                .setContentText("AI Assistant Running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
    }
}
EOF

# NotificationListener.java (di service package, dengan error handling)
cat > app/src/main/java/com/smarteyex/service/NotificationListener.java << 'EOF'
package com.smarteyex.service;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.smarteyex.ai.AudioEngine;
import com.smarteyex.ai.MemoryEngine;

public class NotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String text = sbn.getNotification().extras.getString("android.text");
            if (text != null) {
                MemoryEngine memory = new MemoryEngine(this);
                memory.saveLog("Notification: " + text);
                AudioEngine audio = new AudioEngine(this);
                audio.speak(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

# BootReceiver.java (di service package, dengan error handling)
cat > app/src/main/java/com/smarteyex/service/BootReceiver.java << 'EOF'
package com.smarteyex.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                context.startService(new Intent(context, SmartEyeXService.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

# AudioEngine.java (di ai package, dengan integrasi API AI Groq dan error handling)
cat > app/src/main/java/com/smarteyex/ai/AudioEngine.java << 'EOF'
package com.smarteyex.ai;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AudioEngine {
    private TextToSpeech tts;
    private OkHttpClient client = new OkHttpClient();
    public AudioEngine(Context context) {
        try {
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.getDefault());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void speak(String text) {
        try {
            String aiResponse = processWithAI(text);  // Integrasi AI
            tts.speak(aiResponse != null ? aiResponse : text, TextToSpeech.QUEUE_FLUSH, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String processWithAI(String input) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("model", "llama3-8b-8192");
            json.addProperty("messages", "[{\"role\": \"user\", \"content\": \"" + input + "\"}]");
            Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .post(okhttp3.RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json")))
                .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JsonObject responseJson = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // Fallback
    }
}
EOF

# CameraVisionEngine.java (di ai package, dengan error handling)
cat > app/src/main/java/com/smarteyex/ai/CameraVisionEngine.java << 'EOF'
package com.smarteyex.ai;

import android.content.Context;
import android.hardware.Camera;

public class CameraVisionEngine {
    private Camera camera;

    public CameraVisionEngine(Context context) {
        try {
            // Standby mode, ready for future vision AI
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startStandby() {
        try {
            // Placeholder for low-power standby
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

# MemoryEngine.java (di ai package, dengan error handling)
cat > app/src/main/java/com/smarteyex/ai/MemoryEngine.java << 'EOF'
package com.smarteyex.ai;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Entity
public class LogEntry {
    @PrimaryKey(autoGenerate = true) public int id;
    public String content;
}

@Dao
public interface LogDao {
    @Insert void insert(LogEntry log);
    @Query("SELECT * FROM LogEntry") List<LogEntry> getAll();
}

@androidx.room.Database(entities = {LogEntry.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LogDao logDao();
}

public class MemoryEngine {
    private AppDatabase db;

    public MemoryEngine(Context context) {
        try {
            db = Room.databaseBuilder(context, AppDatabase.class, "smarteyex.db").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLog(String content) {
        try {
            new Thread(() -> {
                try {
                    LogEntry log = new LogEntry();
                    log.content = content;
                    db.logDao().insert(log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

# HardwareBridge.java (di ai package, dengan error handling)
cat > app/src/main/java/com/smarteyex/ai/HardwareBridge.java << 'EOF'
package com.smarteyex.ai;

import android.content.Context;

public class HardwareBridge {
    public HardwareBridge(Context context) {
        try {
            // Placeholder for future hardware modes (Bluetooth, USB, etc.)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToHardwareMode() {
        try {
            // Logic placeholder
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

# Fragment classes (di ui package, dengan error handling)
cat > app/src/main/java/com/smarteyex/ui/HomeFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_home, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/StatusFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StatusFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_status, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/LogFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LogFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_log, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/AudioFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AudioFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_audio, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/CameraFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CameraFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_camera, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/MemoryFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MemoryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_memory, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/PermissionsFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PermissionsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_permissions, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/HardwareFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HardwareFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_hardware, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

cat > app/src/main/java/com/smarteyex/ui/SettingsFragment.java << 'EOF'
package com.smarteyex.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_settings, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
EOF

# XML Layouts
mkdir -p app/src/main/res/layout
mkdir -p app/src/main/res/menu
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi

# activity_main.xml
cat > app/src/main/res/layout/activity_main.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <FrameLayout
        android:id="@+id/fragment_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/nav_menu" />

</androidx.drawerlayout.widget.DrawerLayout>
EOF

# fragment_home.xml
cat > app/src/main/res/layout/fragment_home.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="AI Dashboard" />
</LinearLayout>
EOF

# fragment_status.xml
cat > app/src/main/res/layout/fragment_status.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Status Sistem" />
</LinearLayout>
EOF

# fragment_log.xml
cat > app/src/main/res/layout/fragment_log.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Log Notifikasi" />
</LinearLayout>
EOF

# fragment_audio.xml
cat > app/src/main/res/layout/fragment_audio.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Audio Control" />
</LinearLayout>
EOF

# fragment_camera.xml
cat > app/src/main/res/layout/fragment_camera.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Camera Control" />
</LinearLayout>
EOF

# fragment_memory.xml
cat > app/src/main/res/layout/fragment_memory.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Memory Center" />
</LinearLayout>
EOF

# fragment_permissions.xml
cat > app/src/main/res/layout/fragment_permissions.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Permissions Manager" />
</LinearLayout>
EOF

# fragment_hardware.xml
cat > app/src/main/res/layout/fragment_hardware.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Hardware Mode" />
</LinearLayout>
EOF

# fragment_settings.xml
cat > app/src/main/res/layout/fragment_settings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Settings" />
</LinearLayout>
EOF

# nav_menu.xml
cat > app/src/main/res/menu/nav_menu.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_home" android:title="Home" />
    <item android:id="@+id/nav_status" android:title="Status Sistem" />
    <item android:id="@+id/nav_log" android:title="Log Notifikasi" />
    <item android:id="@+id/nav_audio" android:title="Audio Control" />
    <item android:id="@+id/nav_camera" android:title="Camera Control" />
    <item android:id="@+id/nav_memory" android:title="Memory Center" />
    <item android:id="@+id/nav_permissions" android:title="Permissions Manager" />
    <item android:id="@+id/nav_hardware" android:title="Hardware Mode" />
    <item android:id="@+id/nav_settings" android:title="Settings" />
</menu>
EOF

# strings.xml
cat > app/src/main/res/values/strings.xml << 'EOF'
<resources>
    <string name="app_name">SmartEyeX</string>
</resources>
EOF

# colors.xml
cat > app/src/main/res/values/colors.xml << 'EOF'
<resources>
    <color name="colorPrimary">#6200EE</color>
    <color name="colorPrimaryDark">#3700B3</color>
    <color name="colorAccent">#03DAC5</color>
</resources>
EOF

# styles.xml
cat > app/src/main/res/values/styles.xml << 'EOF'
<resources>
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
</resources>
EOF

# ic_launcher placeholders
touch app/src/main/res/mipmap-mdpi/ic_launcher.png
touch app/src/main/res/mipmap-hdpi/ic_launcher.png
touch app/src/main/res/mipmap-xhdpi/ic_launcher.png
touch app/src/main/res/mipmap-xxhdpi/ic_launcher.png
touch app/src/main/res/mipmap-xxxhdpi/ic_launcher.png

# gradlew script
cat > gradlew << 'EOF'
#!/bin/bash

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \$.*\$$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`" >&-
APP_HOME="`pwd -P`"
cd "$SAVED" >&-

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors.
if [ "$cygwin" = "false" -a "$darwin" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=Gradle\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=`find -L / -maxdepth 1 -mindepth 1 -type d 2>/dev/null`
    SEP=""
    for dir in $ROOTDIRSRAW ; do
        ROOTDIRS="$ROOTDIRS$SEP$dir"
        SEP="|"
    done
    OURCYGPATTERN="(^($ROOTDIRS))"
    # Add a user-defined pattern to the cygpath arguments
    if [ "$GRADLE_CYGPATTERN" != "" ] ; then
        OURCYGPATTERN="$OURCYGPATTERN|($GRADLE_CYGPATTERN)"
    fi
    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    i=0
    for arg in "$@" ; do
        CHECK=`echo "$arg"|egrep -c "$OURCYGPATTERN" -`
        CHECK2=`echo "$arg"|egrep -c "^-"`                                 ### Determine if an option

        if [ $CHECK -ne 0 ] && [ $CHECK2 -eq 0 ] ; then                    ### Added a condition
            eval `echo args$i`=`cygpath --path --ignore --mixed "$arg"`
        else
            eval `echo args$i`="\"$arg\""
        fi
        i=$((i+1))
    done
    case $i in
        (0) set -- ;;
        (1) set -- "$args0" ;;
        (2) set -- "$args0" "$args1" ;;
        (3) set -- "$args0" "$args1" "$args2" ;;
        (4) set -- "$args0" "$args1" "$args2" "$args3" ;;
        (5) set -- "$args0" "$args1" "$args2" "$args3" "$args4" ;;
        (6) set -- "$args0" "$args1" "$args2" "$args3" "$args4" "$args5" ;;
        (7) set -- "$args0" "$args1" "$args2" "$args3" "$args4" "$args5" "$args6" ;;
        (8) set -- "$args0" "$args1" "$args2" "$args3" "$args4
EOF
