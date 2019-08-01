package com.hitqz.robot.hkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    HCSdk hcSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.sv_normal_camera);
        HCSdkManager.getInstance().initAndLogin(this);
        hcSdk = HCSdkManager.getNormalHCSdk(this);
        hcSdk.setSurfaceView(surfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hcSdk != null) {
            hcSdk.startSinglePreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hcSdk != null) {
            hcSdk.stopSinglePreview();
        }
    }
}
