package com.tuyafeng.orientationlock.service;

import android.annotation.TargetApi;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

import android.widget.Space;
import com.tuyafeng.orientationlock.BuildConfig;
import com.tuyafeng.orientationlock.MainActivity;
import com.tuyafeng.orientationlock.R;

import com.tuyafeng.orientationlock.utils.SimpleLog;

public class OrientationLockService extends Service {

    private static final String TAG = OrientationLockService.class.getSimpleName();

    private static final String NOTIFICATION_ID = BuildConfig.APPLICATION_ID + ".notification";

    public static final String ACTION_SET_ORIENTATION = BuildConfig.APPLICATION_ID + ".action.SET_ORIENTATION";
    public static final String KEY_ORIENTATION = "orientation";

    private Space holderView = null;
    private int currentOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_SCREEN_OFF.equals(action)){
                removeOrientationLayout();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                setSystemOrientation(currentOrientation);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= 26) {
            setForeground();
        }
        IntentFilter screenReceiverFilter = new IntentFilter();
        screenReceiverFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenReceiverFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenReceiver, screenReceiverFilter);
        SimpleLog.d(TAG, "register service receiver");
    }

    @Override
    public void onDestroy() {
        SimpleLog.d(TAG, "unregister service receiver");
        unregisterReceiver(screenReceiver);
        removeOrientationLayout();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_SET_ORIENTATION.equals(action)) {
            int orientation = intent.getIntExtra(KEY_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != currentOrientation) {
                setSystemOrientation(orientation);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(26)
    private void setForeground(){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID,
                getString(R.string.running_notification_channel_name),
                NotificationManager.IMPORTANCE_MIN);
        manager.createNotificationChannel(channel);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this, NOTIFICATION_ID)
                .setContentTitle(getString(R.string.running_notification_title))
                .setContentText(getString(R.string.running_notification_description))
                .setSmallIcon(R.drawable.ic_stat_orientation)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    private void setSystemOrientation(int screenOrientation) {
        SimpleLog.d(TAG, "set system orientation: " + screenOrientation);

        if (ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED == screenOrientation) {
            removeOrientationLayout();
        }

        currentOrientation = screenOrientation;

        WindowManager windowManager = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
        WindowManager.LayoutParams orientationLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);

        if (holderView == null) {
            holderView = new Space(this);
            holderView.setClickable(false);
            holderView.setFocusable(false);
            holderView.setFocusableInTouchMode(false);
            holderView.setLongClickable(false);

            windowManager.addView(holderView, orientationLayoutParams);
            holderView.setVisibility(View.GONE);
        }

        orientationLayoutParams.screenOrientation = screenOrientation;
        windowManager.updateViewLayout(holderView, orientationLayoutParams);
        holderView.setVisibility(View.VISIBLE);
    }

    private void removeOrientationLayout() {
        SimpleLog.d(TAG, "remove system orientation");
        if (holderView == null) return;
        WindowManager windowManager = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
        windowManager.removeViewImmediate(holderView);
        holderView = null;
    }
}
