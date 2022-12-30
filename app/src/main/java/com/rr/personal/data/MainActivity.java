package com.rr.personal.data;

import android.app.Activity;
import android.os.Bundle;
import android.os.FileObserver;

public class MainActivity extends Activity {

    private FileObserver mPhotoObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        boolean enabled = isAccessibilityServiceEnabled(getApplicationContext(), RealTimeService.class);
//        if(!enabled) {
//            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
//            startActivityForResult(intent, 7878);
//        }
    }
}