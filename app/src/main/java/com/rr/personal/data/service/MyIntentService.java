package com.rr.personal.data.service;

import static com.rr.personal.data.App.EXTERNAL_STORAGE;
import static com.rr.personal.data.App.isServiceRunning;
import static com.rr.personal.data.App.mData;
import static com.rr.personal.data.App.mDeviceName;
import static com.rr.personal.data.App.mPhotoStorage;
import static com.rr.personal.data.App.mServeice;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Pattern;

public class MyIntentService extends JobIntentService {

    public static final int JOB_ID = 1;
    private Handler handler;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MyIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final Bundle extras = intent.getExtras();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                final StorageReference reference = mPhotoStorage.child("name");
                byte[] bytes = "Raiyan".getBytes();

                reference.putBytes(bytes).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                        Log.d("Upload Progress", progress+" "+snapshot.getTotalByteCount());
                    }
                }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        Log.d("UploadUpload", task.isSuccessful()+"");
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Log.d("CompleteComplete", task.isSuccessful()+"");
                        if (task.isSuccessful()) {
                            mData.child("online").setValue(System.currentTimeMillis()+"");
                        }
                    }
                });
                if (extras == null || extras.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "RECEIVED : null", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "RECEIVED: "+extras.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
