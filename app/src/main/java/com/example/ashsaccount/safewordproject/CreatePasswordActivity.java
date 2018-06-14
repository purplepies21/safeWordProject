package com.example.ashsaccount.safewordproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class CreatePasswordActivity extends AppCompatActivity{

    PatternLockView patternLockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    setContentView(R.layout.acivity_create_password);
    patternLockView= (PatternLockView)findViewById(R.id.pattern_lock_view);
    patternLockView.addPatternLockListener(new PatternLockViewListener() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {

        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            SharedPreferences preferences= getSharedPreferences("PREFS", 0);
            SharedPreferences.Editor editor= preferences.edit();
            editor.putString("password", PatternLockUtils.patternToString(patternLockView, pattern));
            editor.apply();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            Log.v("potato", "intent started");
            finish();
        }

        @Override
        public void onCleared() {

        }
    });
    }
}
