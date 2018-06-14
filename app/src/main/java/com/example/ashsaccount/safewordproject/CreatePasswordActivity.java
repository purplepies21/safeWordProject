package com.example.ashsaccount.safewordproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.andrognito.patternlockview.PatternLockView;

public class CreatePasswordActivity extends AppCompatActivity{

    PatternLockView patternLockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    setContentView(R.layout.acivity_create_password);
    patternLockView= (PatternLockView)findViewById(R.id.pattern_lock_view);

    }
}
