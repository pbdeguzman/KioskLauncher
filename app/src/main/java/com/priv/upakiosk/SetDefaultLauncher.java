package com.priv.upakiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.TtsSpan;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class SetDefaultLauncher {

    public static final String LAUNCHER_CLASS = "com.android.launcher.launcher3.Launcher";
    public static final String LAUNCHER_PACKAGE = "com.android.launcher";

    Activity activity;
    SetDefaultLauncher(Activity activity){
        this.activity=activity;
    }
    enum HomeState {
        GEL_IS_DEFAULT, OTHER_LAUNCHER_IS_DEFAULT, NO_DEFAULT
    }
    public boolean launchHomeOrClearDefaultsDialog() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveActivity = activity.getPackageManager().resolveActivity(
                intent, 0);
        @SuppressLint("QueryPermissionsNeeded")
        HomeState homeState = LAUNCHER_PACKAGE
                .equals(resolveActivity.activityInfo.applicationInfo.packageName) && LAUNCHER_CLASS
                .equals(resolveActivity.activityInfo.name) ? HomeState.GEL_IS_DEFAULT
                : !inResolveInfoList(
                resolveActivity, activity.getPackageManager()
                        .queryIntentActivities(intent, 0)) ? HomeState.NO_DEFAULT
                : HomeState.OTHER_LAUNCHER_IS_DEFAULT;
        switch (homeState) {
            case GEL_IS_DEFAULT:
            case NO_DEFAULT:
                intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return true;
            default:
                showClearDefaultsDialog(resolveActivity);
                return false;
        }
    }
    @SuppressLint({"NewApi", "StringFormatInvalid"})
    private void showClearDefaultsDialog(ResolveInfo resolveInfo) {
        CharSequence string;
        final Intent intent;
        CharSequence loadLabel = resolveInfo.loadLabel(activity.getPackageManager());
        if (Build.VERSION.SDK_INT < 21
                || activity.getPackageManager().resolveActivity(
                new Intent("android.settings.HOME_SETTINGS"), 0) == null) {
            string = activity.getString(R.string.change_default_home_dialog_body,
                    loadLabel);
            intent = new Intent(
                    "android.settings.APPLICATION_DETAILS_SETTINGS",
                    Uri.fromParts("package",
                            resolveInfo.activityInfo.packageName, null));
        } else {
            intent = new Intent("android.settings.HOME_SETTINGS");
            string = new SpannableString(activity.getString(
                    R.string.change_default_home_dialog_body_settings,
                    loadLabel));
            ((SpannableString) string)
                    .setSpan(
                            new TtsSpan.TextBuilder(
                                    activity.getString(
                                            R.string.change_default_home_dialog_body_settings_tts,
                                            loadLabel)).build(), 0, string
                                    .length(), 18);
        }

        new AlertDialog.Builder(activity)
                .setMessage(string)
                .setNegativeButton(
                        activity.getString(R.string.change_default_home_dialog_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                activity.finish();
                            }
                        })
                .setOnCancelListener(new DialogInterface. OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        activity.finish();
                    }
                })
                .setPositiveButton(
                        activity.getString(R.string.change_default_home_dialog_proceed),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                try {
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                } catch (Exception e) {
                                    setDefLauncher(activity);
                                }
                            }
                        }).create().show();
    }

    private boolean inResolveInfoList(ResolveInfo resolveInfo,
                                      List<ResolveInfo> list) {
        for (ResolveInfo resolveInfo2 : list) {
            if (resolveInfo2.activityInfo.name
                    .equals(resolveInfo.activityInfo.name)
                    && resolveInfo2.activityInfo.packageName
                    .equals(resolveInfo.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setDefLauncher(Context c) {
        PackageManager p = c.getPackageManager();
        ComponentName cN = new ComponentName(c, FakeLauncher.class);
        p.setComponentEnabledSetting(cN,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        c.startActivity(selector);
        p.setComponentEnabledSetting(cN,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
