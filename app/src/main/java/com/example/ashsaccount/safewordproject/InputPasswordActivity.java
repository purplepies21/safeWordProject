package com.example.ashsaccount.safewordproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class InputPasswordActivity extends AppCompatActivity{

    PatternLockView mPatternLockView;
    String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password);
        final SharedPreferences preferences= getSharedPreferences("PREFS",0);
        password=preferences.getString("password","0");


        mPatternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if(password.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {


                    Intent intent = new Intent(getApplicationContext(), FileActivity.class);
                    startActivity(intent);
                    finish();
                }else{

                    Toast.makeText(InputPasswordActivity.this, "Wrong Pattern!", Toast.LENGTH_SHORT).show();
                    mPatternLockView.clearPattern();
                }

            }

            @Override
            public void onCleared() {

            }
        });
    }
}
