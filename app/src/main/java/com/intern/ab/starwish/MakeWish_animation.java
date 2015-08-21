package com.intern.ab.starwish;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MakeWish_animation extends AppCompatActivity {
    ImageView risingStar;
    Handler handler;
    Runnable runnable;
    boolean resume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_wish_animation);

        risingStar = (ImageView) findViewById(R.id.risingStar);
        Animation rising = AnimationUtils.loadAnimation(this, R.anim.star_rising);
        risingStar.startAnimation(rising);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtras(getIntent().getExtras());
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                if (getIntent().getExtras().getBoolean("fromMain")) {
                    setResult(RESULT_FIRST_USER, intent);
                } else {
                    intent.setClass(getApplication(), MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        };
        handler.postDelayed(runnable, 3000);
        //overridePendingTransition(0, R.anim.zoom_exit);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resume = true;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resume) {
            handler.postDelayed(runnable, 1500);
        }
    }
}
