package com.rr.personal.data.activity;

import static com.rr.personal.data.App.hideApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class BackgroundActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "Open Activity", Toast.LENGTH_SHORT).show();

        hideApp(this);
    }
}
