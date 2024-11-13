package com.priv.upakiosk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "[NAAT-1294]";
    private static final int MAX_CHAR_LIMIT = 100;
    final String BAT_FILE = "permission.bat";
    final String RAW_DIR = "raw";
    final String SUPPORTED_APPS = "supported_apps.json";
    final String filePath = RAW_DIR + File.separator + SUPPORTED_APPS;

    boolean permissionGranted = false;
    final int READ_EXTERNAL_STORAGE_CODE = 1000;
    final int WRITE_EXTERNAL_STORAGE_CODE = 1001;
    final int WRITE_SECURE_SETTINGS_CODE = 1002;

    private int REQUESTED_MODE = 1;
    final int MODE_STANDARD = 1;
    final int MODE_MERCHANT = 2;
    final int MODE_KIOSK    = 3;

    final String UPA_APP = "UPA";
    final String UPA_PACKAGE = "com.global.integrated";
    final String UDS_APP = "UDS Downloader";
    final String UDS_PACKAGE = "com.global.tms";

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private RecyclerView rvAppIcons;
    private View mainLayout;
    private SwipeRefreshLayout refreshLayout;

    boolean tapUp = false, tapDown = false, tapLeft = false, tapRight = false;

    private String touchSequence = "";
    Context context;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        mainLayout = findViewById(R.id.mainLayout);
        rvAppIcons = findViewById(R.id.rvAppIcons);
        refreshLayout = findViewById(R.id.refreshLayout);

        //pull to refresh to reload the supported apps
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            loadSupportedAppIcons();
        });

        // Check if the application is already the default launcher
        if (!isMyLauncherDefault()) {
            // Create an intent with the CLEAR_TOP flag
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Start the intent
            startActivity(intent);
            finish();
        }

        //Set Device Operating Mode to KIOSK MODE
        try {
            if (getDeviceOperatingMode() != MODE_KIOSK) {
                REQUESTED_MODE = MODE_KIOSK;
                checkPermission(REQUESTED_MODE);
            } else {
                Log.d(TAG, "Device Operating Mode is already on KIOSK MODE");
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

        loadSupportedAppIcons();
        mainLayout.setOnTouchListener(tapHandler);
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener tapHandler = (view, motionEvent) -> {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();

        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            checkGesture(x, y);
        }
        return true;
    };

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                Toast.makeText(context, "eyyy", Toast.LENGTH_SHORT).show();
                break;
        }

        return false;
    }

    /**
     * Handles the onBackPressed event
     */
    @Override
    public void onBackPressed() {
        //disable the application's onBackPress
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(MainActivity.this, "Write Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Write Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == WRITE_SECURE_SETTINGS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Write Secure Settings Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Write Secure Settings Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Changing Device Operating Mode
     * @param mode
     * 1 - Android Standard Mode
     * 2 - POS Mode
     * 3 - Kiosk Mode
     */
    private void setDeviceOperatingMode(int mode) {
            Settings.Global.putInt(getContentResolver(), "device_operating_mode", mode);
            Intent intent = new Intent("com.verifone.DEVICE_OPERATING_MODE");
            sendBroadcast(intent);
            showDialogBox("Operating Mode has been changed \n\nPlease reboot your device.");
    }

    private int getDeviceOperatingMode() throws Settings.SettingNotFoundException {
        return Settings.Global.getInt(getContentResolver(), "device_operating_mode");
    }

    /**
     * a Request Permission
     */
    private void checkPermission(int mode) {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.MANAGE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_STORAGE_CODE);
//            Log.d(TAG, "MANAGE_EXTERNAL_STORAGE permission is not yet granted");
//        } else {
//            Log.d(TAG, "MANAGE_EXTERNAL_STORAGE permission is already granted");
//        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "WRITE_SECURE_SETTINGS permission is not yet granted");

            //Permission Request
//            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_SECURE_SETTINGS }, WRITE_SECURE_SETTINGS_CODE);

            //Special Permission Request
//            checkSpecialPermission();

            //Request Via ADB/CMD
//            try {
//                checkPermissionViaAdb();
//            } catch (IOException e) {
//                Log.d(TAG, "Error: " + e.getMessage());
//            }
        } else {
            Log.d(TAG, "WRITE_SECURE_SETTINGS permission is already granted");
            setDeviceOperatingMode(mode);
        }
    }

    /**
     * Grant Permission via ADB command
     */
    private void checkPermissionViaAdb() throws IOException {
        Process process = null;

        //Using Process
        //Process process = Runtime.getRuntime().exec("adb shell pm grant com.priv.upakiosk android.permission.WRITE_SECURE_SETTINGS");

        //Using ProcessBuilder
        //ProcessBuilder processBuilder = new ProcessBuilder("pm grant com.priv.upakiosk android.permission.WRITE_SECURE_SETTINGS");

        //Running ADB via CMD
        File fileDir = new File(context.getFilesDir()  + File.separator + RAW_DIR);
        final String filePath = fileDir + File.separator + BAT_FILE;
        final String CMD = "cmd";
        final String C = "/c";
        final String START = "start";

        final File fileCheck = new File(filePath);

        //ProcessBuilder processBuilder = new ProcessBuilder(CMD + " " + C + " " +  START + " " + filePath);
        ProcessBuilder processBuilder = new ProcessBuilder("cmd /c start " + filePath);
        //ProcessBuilder processBuilder = new ProcessBuilder(CMD, C, START, filePath);
        if (fileCheck.exists()) {
            process = processBuilder.start();
        }

        if (process != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Log.d(TAG, bufferedReader.toString());
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void processAssetFiles() throws IOException {
        final String RAW_DIR = "raw";
        final String CONFIG_DIR = "config";
        AssetManager assetManager = context.getAssets();
        String[] files = assetManager.list(RAW_DIR);
        if (files != null) {
            for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(RAW_DIR + File.separator + filename);

                    File outFile = new File(CONFIG_DIR, filename);
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch (IOException e) {
                    Log.e(getClass().getName(), "Failed to copy asset file: " + e.getMessage());
                } finally {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) in.close();
                }
            }
        }
    }

    /**
     * POC for Special Permission
     **/
    private void checkSpecialPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.permission);
        builder.setMessage(R.string.write_secure_settings);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            //Using intent: Settings.ACTION_SECURITY_SETTINGS
            //Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS, Uri.parse("package:" + getPackageName()));
            //requestSpecialPermissionIntent.launch(intent);

            //Using string: Manifest.permission.WRITE_SECURE_SETTINGS
            requestSpecialPermissionString.launch(Manifest.permission.WRITE_SECURE_SETTINGS);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Special Request Permission using Intent
     */
    private final ActivityResultLauncher<Intent> requestSpecialPermissionIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                checkPermission(REQUESTED_MODE);
            }
    );

    /**
     * Special Request Permission using String (e.i. Manifest.permission.WRITE_SECURE_SETTINGS)
     */
    private final ActivityResultLauncher<String> requestSpecialPermissionString = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        result -> {
            if (result) {
                checkPermission(REQUESTED_MODE);
            }
        });

    /**
     * Display Alert Dialog Box
     * @param message
     */
    private void showDialogBox(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message);
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.exclamation);

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void closeDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to close the UPA Kiosk Launcher?");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.exclamation);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            finish();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Call Device Settings
     * @param context
     */
    private void showSystemSettings(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Reboot Device
     * Note: The application must be signed
     */
    private void rebootDevice() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        pm.reboot(null);
    }

    /**
     * Check if the application is the Default Home Launcher
     * @return
     */
    private boolean isMyLauncherDefault() {
        String packageName = getPackageName();
        String defaultLauncher = null;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            defaultLauncher = resolveInfo.activityInfo.packageName;
        }

        return defaultLauncher != null && defaultLauncher.equals(packageName);
    }

    /**
     * Get the Package Info of the installed applications
     * @param getSysPackages
     * @return ArrayList of the installed apps
     */
    @SuppressLint("QueryPermissionsNeeded")
    private ArrayList<PackageInfo> getInstalledApps(boolean getSysPackages) {
        ArrayList<PackageInfo> res = new ArrayList<>();
        List<android.content.pm.PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for(int i = 0; i < packages.size() ;i++) {
            android.content.pm.PackageInfo p = packages.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue ;
            }
            PackageInfo newInfo = new PackageInfo();
            newInfo.setName(p.applicationInfo.loadLabel(getPackageManager()).toString());
            newInfo.setPackageName(p.packageName);
            newInfo.setVersionName(p.versionName);
            newInfo.setVersionCode(p.versionCode);
            newInfo.setIcon(p.applicationInfo.loadIcon(getPackageManager()));
            res.add(newInfo);
        }
        return res;
    }

    /**
     * Get the supported apps by opening the JSON file from the assets folder
     * @return ArrayList of the supported apps
     */
    private ArrayList<PackageInfo> getSupportedApps() {
        ArrayList<PackageInfo> supportedApps = new ArrayList<>();
        final String jsonString = readJsonFile(MainActivity.this, filePath);

        try {
            JSONArray packageList = new JSONArray(jsonString);
            for (int i = 0; i < packageList.length(); i++) {
                JSONObject item = packageList.getJSONObject(i);
                PackageInfo supportedApp = new PackageInfo();
                supportedApp.setPackageName(item.getString("Package"));
                supportedApp.setName(item.getString("Name"));
                supportedApps.add(supportedApp);
            }
        } catch (JSONException err) {
            Log.e(getClass().getName(), err.getMessage());
        }

        return supportedApps;
    }

    /**
     * Display the supported apps to the screen using RecyclerView
     */
    private void loadSupportedAppIcons() {
        ArrayList<PackageInfo> supportedApps = getSupportedApps();
        ArrayList<PackageInfo> installedApps = getInstalledApps(true);
        ArrayList<PackageInfo> appIcons = new ArrayList<>();

        //get the supported package info on the installed apps list
        for (PackageInfo installed : installedApps) {
            for (PackageInfo supported: supportedApps) {
                if (installed.getPackageName().equalsIgnoreCase(supported.getPackageName())) {
                    appIcons.add(installed);
                    break;
                }
            }
        }

        //callback
        AppIconAdapter.Callback callback = item -> {
            if (item != null) {
                launchApp(item);
            }
        };

        //set data on recycler adapter
        AppIconAdapter adapter = new AppIconAdapter(appIcons);
        adapter.setCallback(callback);
        rvAppIcons.setAdapter(adapter);

        //display the icons as tile view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        rvAppIcons.setLayoutManager(gridLayoutManager);
        rvAppIcons.setHasFixedSize(true);
    }

    /**
     * Launches the supported apps from the Recycler View
     * @param item
     */
    private void launchApp(PackageInfo item) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(item.getPackageName());
            startActivity(intent);
        } catch (Exception e) {
            showDialogBox(item.getName() + " application is not installed.");
        }
    }

    /**
     * Set the UPA Kiosk Launcher to fullscreen
     */
    public void fullscreen() {
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullscreen();
    }

    /**
     * Read JSON file that consists of supported apps
     * @param context
     * @param filePath
     * @return
     */
    public static String readJsonFile(Context context, String filePath) {
        Log.d(TAG, "start readJsonFile()");
        String jsonData = null;
        int len;
        byte[] buffer;
        InputStream stream = null;

        try {
            // Open json file
            if (new File(filePath).exists()) {
                stream = new FileInputStream(new File(filePath));
            } else {
                stream = context.getAssets().open(filePath);
            }
            len = stream.available();
            buffer = new byte[len];
            // Read the json file and set it to buffer
            stream.read(buffer);

            // set the buffer to jsonData string
            jsonData = new String(buffer);

        } catch (IOException err) {
            err.printStackTrace();
        } finally {
            // Close the file
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.d(TAG,"error: "+e.getMessage());
                }
            }
        }
        return jsonData;
    }

    /**
     * Turn the Device Operating Mode to Android Standard Mode
     * @return
     * Gesture: RIGHT LEFT RIGHT LEFT DOWN DOWN UP UP
     */
    private boolean standardAndroidModeSequence() {
        REQUESTED_MODE = MODE_STANDARD;
        checkPermission(REQUESTED_MODE);
        return false;
    }

    /**
     * Open the Device Settings
     * @return
     * Gesture: UP UP DOWN DOWN LEFT RIGHT LEFT RIGHT
     */
    private boolean openSettingsSequence() {
        showSystemSettings(MainActivity.this);
        return false;
    }

    /**
     * Check the touch sequence
     * @param x
     * @param y
     */
    private void checkGesture(int x, int y) {
        int padding = 60;
        int centerX = mainLayout.getWidth() / 2;
        int centerY = mainLayout.getHeight() / 2;
        int top = mainLayout.getTop();
        int bottom = mainLayout.getBottom();
        int left = mainLayout.getLeft();
        int right = mainLayout.getRight();

        //reset the characterSequence when reached the MAX_CHAR_LIMIT
        if (touchSequence.length() > MAX_CHAR_LIMIT) touchSequence = "";

        try {
            if (x > (centerX - padding) && x <= (centerX + padding) & y > top && y <= (top + padding)) { //top
                Log.d(TAG, "TAP TOP");
                touchSequence = touchSequence + "U";
            } else if (x > (centerX - padding) && x <= (centerX + padding) & y > (bottom - padding) && y <= bottom) { //bot
                Log.d(TAG, "TAP BOTTOM");
                touchSequence = touchSequence + "D";
            } else if (x > left && x <= padding && y > (centerY - padding) && y <= (centerY + padding)) { //left
                Log.d(TAG, "TAP LEFT");
                touchSequence = touchSequence + "L";
            } else if (x > (right - padding) && x <= right && y > (centerY - padding) && y <= (centerY + padding)) { //right
                Log.d(TAG, "TAP RIGHT");
                touchSequence = touchSequence + "R";
            }
        } catch (Exception e) {
            touchSequence = "";
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            Log.d(TAG, "touchSequence: " + touchSequence);
        }

        if (touchSequence.contains(getString(R.string.OPEN_SETTING_SEQUENCE))) {
            touchSequence = "";
            openSettingsSequence();
        } else if (touchSequence.contains(getString(R.string.STANDARD_MODE_SEQUENCE))) {
            touchSequence = "";
            standardAndroidModeSequence();
        }
    }
}