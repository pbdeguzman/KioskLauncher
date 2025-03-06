package com.priv.upakiosk;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "KIOSKBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (!isDefaultLauncher(context)) {
                Intent activityIntent = new Intent(context, MainActivity.class);
                context.startActivity(activityIntent);
            } else {
                Log.d(TAG, "UPA Kiosk is the default launcher");
            }
        }
    }

    public static boolean isDefaultLauncher(Context context) {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String myPackageName = context.getPackageName();
        List<ComponentName> activities = new ArrayList<>();
        final PackageManager packageManager = context.getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPackageExisting(Context context, String targetPackage){
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

}
