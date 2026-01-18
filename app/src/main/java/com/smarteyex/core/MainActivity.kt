<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="?attr/colorPrimary"
        android:title="SmartEyeX"
        android:titleTextColor="@android:color/white" />

    <!-- INI YANG DIPAKE CameraFragment -->
    <FrameLayout
        android:id="@+id/camera_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="?attr/actionBarSize">

        <!-- INI YANG DIPAKE HUD -->
        <FrameLayout
            android:id="@+id/floatingHudContainer"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"
            android:layout_gravity="top|end" />

        <LinearLayout
            android:id="@+id/kontrol_bawah"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="12dp"
            android:layout_gravity="bottom|center_horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="8dp">

            <Button
                android:id="@+id/btnToggleObserve"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/btn_observe" />

            <Button
                android:id="@+id/btnMemory"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="12dp"
                android:text="@string/btn_memory" />

        </LinearLayout>

    </FrameLayout>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
