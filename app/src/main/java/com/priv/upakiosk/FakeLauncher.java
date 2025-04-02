package com.priv.upakiosk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class FakeLauncher extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_launcher);
        Log.d("FakeLauncher", "FakeLauncher - onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("FakeLauncher", "FakeLauncher - onResume");
    }
}