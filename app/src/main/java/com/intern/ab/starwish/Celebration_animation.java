package com.intern.ab.starwish;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Celebration_animation extends AppCompatActivity {
    RelativeLayout allMaterialLayout;
    ImageView material1;
    ImageView material2;
    ImageView material3;
    ImageView material4;
    Handler handler;
    Runnable runnable;
    boolean resume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.celebration_animation);
        allMaterialLayout = (RelativeLayout) findViewById(R.id.all_celebration_material);
        Animation allMaterial = AnimationUtils.loadAnimation(this, R.anim.all_celebration_material);
        allMaterialLayout.startAnimation(allMaterial);

        material1 = (ImageView) findViewById(R.id.celebration_material1);
        Animation material1_anim = AnimationUtils.loadAnimation(this, R.anim.celebration_material1);
        material1.startAnimation(material1_anim);

        material2 = (ImageView) findViewById(R.id.celebration_material2);
        Animation material2_anim = AnimationUtils.loadAnimation(this, R.anim.celebration_material2);
        material2.startAnimation(material2_anim);

        material3 = (ImageView) findViewById(R.id.celebration_material3);
        Animation material3_anim = AnimationUtils.loadAnimation(this, R.anim.celebration_material3);
        material3.startAnimation(material3_anim);

        material4 = (ImageView) findViewById(R.id.celebration_material4);
        Animation material4_anim = AnimationUtils.loadAnimation(this, R.anim.celebration_material4);
        material4.startAnimation(material4_anim);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                /*Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                */
                finish();
            }
        };
        handler.postDelayed(runnable, 1800);
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
            handler.postDelayed(runnable, 800);
        }
    }
}