package com.priv.upakiosk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.global.fb.settings.SettingsModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public class PasswordModule {
    static final String TAG = "UPA KIOSK";
    final String RAW_DIR = "raw";
    final String DOWNLOADED_PARAM_FILENAME = "DLPARAM.txt";
    final String filePath = RAW_DIR + File.separator + DOWNLOADED_PARAM_FILENAME;
    private final SharedPreferences sharedPreferences;

    public PasswordModule(Context context) {
        String file = scanFile(context, filePath);
        Properties properties = new Properties();
        try {

            properties.load(new StringReader(file));
        } catch (Exception exception) {

        }
        //readFile(context, filePath);
        this.sharedPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
    }
    public String getAdminPwd() {
        String merchantId = getMerchantNumber();

        final int MNUMLENGTH = 6;
        String xxx = Integer.toString(getCurrentDayOfYear() * 2);
        final String defaultMNUM = "123456789012";
        Calendar calendar = Calendar.getInstance();
        int y = calendar.get(Calendar.YEAR);
        String yy = Integer.toString(y);
        int m = calendar.get(Calendar.MONTH) + 1;
        String mm = Integer.toString(m);
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        String dd = Integer.toString(d);
        y = Integer.parseInt(yy.substring(yy.length() - 1));
        m = Integer.parseInt(mm.substring(mm.length() - 1));
        d = Integer.parseInt(dd.substring(dd.length() - 1));
        String dym = Integer.toString(d) + Integer.toString(y) + Integer.toString(m);
        String xxxdym = xxx + dym;
        String mnum = (merchantId != null && merchantId.length() < MNUMLENGTH) ? defaultMNUM : merchantId;
        String zzzzzz = mnum.substring(mnum.length() - 6);
        int result = Integer.parseInt(xxxdym) + Integer.parseInt(zzzzzz);
        String adminPwd = String.valueOf(result % 1000000);
        adminPwd = formatToFixDigitNum(adminPwd, 6);
        return adminPwd;
    }

    public int getCurrentDayOfYear() {
        Calendar d = Calendar.getInstance();
        int month = d.get(Calendar.MONTH);
        int year = d.get(Calendar.YEAR);
        int days = d.get(Calendar.DAY_OF_MONTH);

        for (int ii = 0; ii < month; ii++) {
            d.set(year, ii + 1, 0);
            days += d.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        return days;
    }

    public String formatToFixDigitNum(String num, int fixedLen) {
        String finalNumber = "";
        String pad = "0".repeat(fixedLen);

        if (!num.isEmpty()) {
            finalNumber = pad.substring(0, pad.length() - num.length()) + num;
        }
        return finalNumber;
    }

    private static boolean isMultiMerchantSupported;
    private static List<String> merchantIdSplitVal;
    private static int multiMerchantIndex;

    public String getMerchantNumber() {
        String merchantId = getString("MerchantNumber", "1234");
        if (isMultiMerchantSupported) {
            merchantId = merchantIdSplitVal.get(multiMerchantIndex);
        } else {
            if (merchantIdSplitVal != null && !merchantIdSplitVal.isEmpty()) {
                merchantId = merchantIdSplitVal.get(0);
            }
        }
        return merchantId;
    }

    private String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    /**
     * Read file that consists of data
     * @param context
     * @param filePath
     * @return
     */
    public static String readFile(Context context, String filePath) {
        Log.d(TAG, "start readTextFile()");
        String fileData = null;
        int len;
        byte[] buffer;
        InputStream stream = null;
        String dataFilePath = context.getFilesDir() + File.separator;
        String supportedFile = dataFilePath + filePath;

        try {
            // Open file
            if (new File(supportedFile).exists()) {
                stream = new FileInputStream(new File(supportedFile));
            } else {
                stream = context.getAssets().open(filePath);
            }
            len = stream.available();
            buffer = new byte[len];
            // Read the file and set it to buffer
            stream.read(buffer);

            // set the buffer to fileData string
            fileData = new String(buffer);

        } catch (IOException err) {
            Log.d(TAG,"error: " + err.getMessage());
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
        return fileData;
    }

    private String scanFile(Context context, String fileName) {
        String dataFilePath = context.getFilesDir() + File.separator;
        String filePath = dataFilePath + fileName;

        File file = new File(filePath);
        String line = "";
        Scanner scanner;
        try {

            if (file.exists()) {
                scanner = new Scanner(file);
            } else {
                scanner = new Scanner(context.getAssets().open(fileName));
            }

            int lineNum = 0;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                lineNum++;
                if(line.contains("MerchantNumber")) {
                    System.out.println("ho hum, i found it on line " + lineNum);
                    break;
                }
            }
        } catch(IOException e) {
            Log.d(TAG,"error: " + e.getMessage());
        }

        return line;
    }
}
