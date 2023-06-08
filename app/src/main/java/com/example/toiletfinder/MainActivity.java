package com.example.toiletfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView motherImageView = findViewById(R.id.motherImageView);


        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_mother_daughter);
        motherImageView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended, open new activity
                Intent intent = new Intent(MainActivity.this, Choice.class);
                startActivity(intent);

                // Finish current activity
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
            }
        });

        motherImageView.startAnimation(animation);

    }
}
