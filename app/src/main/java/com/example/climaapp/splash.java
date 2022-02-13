package com.example.climaapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class splash extends AppCompatActivity {
    private VideoView vV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        vV= findViewById(R.id.videoView2);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.splash);
        vV.setVideoURI(video);
        vV.start();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                 Intent intent = new Intent(splash.this, MainActivity.class);
                 startActivity(intent);
                 finish();
            }
        }, 5000);

    }
}