package com.rr.personal.data.receiver;

import android.content.Context;
import android.content.Intent;

import androidx.legacy.content.WakefulBroadcastReceiver;

import com.rr.personal.data.service.MyIntentService;

public class RealTimeReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyIntentService.enqueueWork(context, intent);
    }
}
