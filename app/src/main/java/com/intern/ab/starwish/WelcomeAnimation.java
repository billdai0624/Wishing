package com.intern.ab.starwish;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WelcomeAnimation extends AppCompatActivity {

    RelativeLayout layoutStar;
    ImageView iv, ivT, title;
    Handler handler;
    Runnable runnable;
    boolean resume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_animation);
        layoutStar = (RelativeLayout) findViewById(R.id.layoutStar);
        Animation translate = AnimationUtils.loadAnimation(this, R.anim.shootingstar_translate);
        layoutStar.startAnimation(translate);

        iv = (ImageView) findViewById(R.id.imageStar);
        Animation san = AnimationUtils.loadAnimation(this, R.anim.star_rotate);
        iv.startAnimation(san);

        ivT = (ImageView) findViewById(R.id.imageTail);
        Animation san2 = AnimationUtils.loadAnimation(this, R.anim.shootingstar_tail_scale);
        ivT.startAnimation(san2);
        title = (ImageView) findViewById(R.id.wishing_title);
        Animation titleAnim = AnimationUtils.loadAnimation(this, R.anim.title_scale);
        title.startAnimation(titleAnim);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplication(), MakeWish.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable, 2500);

        //overridePendingTransition(0, R.anim.zoom_exit);/
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
