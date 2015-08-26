package com.intern.ab.starwish;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

public class WelcomeAnimation extends AppCompatActivity {

    RelativeLayout layoutStar;
    ImageView iv, ivT, title;
    Handler handler;
    Runnable runnable;
    boolean resume;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_animation);
        mediaPlayer = new MediaPlayer();
        String fileName = "android.resource://" + getPackageName() + "/" + R.raw.bling;
        try {
            mediaPlayer.setDataSource(this, Uri.parse(fileName));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("Fail to play sound", e.toString());
        }
        layoutStar = (RelativeLayout) findViewById(R.id.layoutStar);
        final Animation translate = AnimationUtils.loadAnimation(this, R.anim.shootingstar_translate);
        layoutStar.startAnimation(translate);
        translate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                try {
                    Thread.sleep(300);
                    mediaPlayer.start();

                } catch (Exception e) {
                    Log.e("Fail to play sound", e.toString());
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        iv = (ImageView) findViewById(R.id.imageStar);
        Animation san = AnimationUtils.loadAnimation(this, R.anim.star_rotate);
        iv.startAnimation(san);

        ivT = (ImageView) findViewById(R.id.imageTail);
        Animation san2 = AnimationUtils.loadAnimation(this, R.anim.shootingstar_tail_scale);
        ivT.startAnimation(san2);
        title = (ImageView) findViewById(R.id.wishing_title);
        Animation titleAnim = AnimationUtils.loadAnimation(this, R.anim.title_scale);
        title.startAnimation(titleAnim);
        titleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplication(), MakeWish.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                } catch (Exception e) {
                    Log.e("Fail to play sound", e.toString());
                }
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
