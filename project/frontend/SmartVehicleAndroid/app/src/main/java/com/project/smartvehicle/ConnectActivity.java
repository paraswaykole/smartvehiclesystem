package com.project.smartvehicle;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroViewPager;
import com.project.smartvehicle.onboardingviews.Slide1;

import java.util.Timer;
import java.util.TimerTask;


public class ConnectActivity extends AppIntro {

    public static AppIntroViewPager PAGER;
    private boolean isCheckingConnection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(Slide1.newInstance(R.layout.view_slide1));
        addSlide(Slide1.newInstance(R.layout.view_slide2));
        addSlide(Slide1.newInstance(R.layout.view_slide3));

        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));
        showSkipButton(false);
        setProgressButtonEnabled(false);
        PAGER = pager;
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    public void startCheckingConnection(){
        if(isCheckingConnection)
            return;

        isCheckingConnection = true;
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(APIManager.getInstance().check_connection_to_device(ConnectActivity.this)) {
                    ConnectActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timer.cancel();
                            Intent i = new Intent(ConnectActivity.this,StartDrivingActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                }
            }
        }, 3000,5000);
    }

}
