package com.tuyafeng.orientationlock;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.tuyafeng.orientationlock.preference.PreferenceManager;
import com.tuyafeng.orientationlock.service.OrientationLockService;
import com.tuyafeng.orientationlock.utils.*;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private PreferenceManager preferenceManager;
    private int currentOrientation;
    private final SparseIntArray orientationMap = new SparseIntArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();

        preferenceManager = PreferenceManager.getInstance(this);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, R.id.tv_orientation_default);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, R.id.tv_orientation_full_sensor);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, R.id.tv_orientation_landscape);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, R.id.tv_orientation_reverse_landscape);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, R.id.tv_orientation_sensor_landscape);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, R.id.tv_orientation_portrait);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, R.id.tv_orientation_reverse_portrait);
        orientationMap.put(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, R.id.tv_orientation_sensor_portrait);

        int orientation = PermissionUtils.isDrawOverlaysPermissionGranted(this)
                ? preferenceManager.getOrientation() : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        setOrientation(orientation);
    }

    private void setOrientation(int orientation) {
        SimpleLog.d(TAG, "select orientation: " + orientation);
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            if (!PermissionUtils.isDrawOverlaysPermissionGranted(this)) {
                PermissionUtils.requestDrawOverlaysPermission(this);
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        preferenceManager.setOrientation(orientation);
        int lastViewId = orientationMap.get(currentOrientation, View.NO_ID);
        if (lastViewId != View.NO_ID) {
            findViewById(lastViewId).setBackgroundResource(R.drawable.bg_button);
        }
        int viewId = orientationMap.get(orientation, View.NO_ID);
        if (viewId != View.NO_ID) {
            findViewById(viewId).setBackgroundResource(R.drawable.bg_selected);
        }
        Intent intent = new Intent(this, OrientationLockService.class);
        intent.setAction(OrientationLockService.ACTION_SET_ORIENTATION);
        intent.putExtra(OrientationLockService.KEY_ORIENTATION, orientation);
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            stopService(intent);
        } else {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        currentOrientation = orientation;
    }

    private void initView() {
        LinearLayout rootView = findViewById(R.id.ll_root);
        List<View> childViews = ViewUtils.getAllChildViews(rootView);
        Drawable icon;
        int iconSize = ViewUtils.dp(this, 32);
        int iconColor = Build.VERSION.SDK_INT >= 21
                ? ContextUtils.getColorFromAttr(this, android.R.attr.colorAccent)
                : ContextUtils.getColor(this, R.color.icon_tint);
        for (View view : childViews) {
            if (view instanceof TextView) {
                Drawable[] drawables = ((TextView) view).getCompoundDrawables();
                icon = drawables[1];
                if (icon == null) {
                    continue;
                }
                icon.setBounds(0, 0, iconSize, iconSize);
                ViewUtils.setDrawableColorFilter(icon, iconColor);
                ((TextView) view).setCompoundDrawables(drawables[0], icon, drawables[2], drawables[3]);
                if (view.getId() != View.NO_ID) {
                    view.setOnClickListener(this);
                }
            }
        }
        findViewById(R.id.iv_about).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://github.com/tuyafeng/OrientationLock");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ignore) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int index = orientationMap.indexOfValue(id);
        if (index < 0) {
            return;
        }
        int orientation = orientationMap.keyAt(index);
        setOrientation(orientation);
    }
}