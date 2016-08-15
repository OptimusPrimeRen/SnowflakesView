package com.opr.snowflakesview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private SnowflakesView mSnowflakesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSnowflakesView = (SnowflakesView) findViewById(R.id.snowflakes);

        mSnowflakesView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSnowflakesView.startSnow();
            }
        }, 1000);

        mSnowflakesView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSnowflakesView.pauseSnow();
            }
        }, 10000);

        mSnowflakesView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSnowflakesView.resumeSnow();
            }
        }, 15000);

        mSnowflakesView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSnowflakesView.stopSnow();
            }
        }, 20000);

    }
}
