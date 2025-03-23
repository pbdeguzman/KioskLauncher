package com.priv.upakiosk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.global.fb.settings.SettingsModule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public class PasswordModule {
    static final String TAG = "UPA KIOSK";
    final String RAW_DIR = "raw";
    final String DOWNLOADED_PARAM_FILENAME = "DLPARAM.TXT";
    final String filePath = RAW_DIR + File.separator + DOWNLOADED_PARAM_FILENAME;
    private final SharedPreferences sharedPreferences;
    Context context;
    public PasswordModule(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(this.context.getPackageName(), Activity.MODE_PRIVATE);
        parseFile(filePath);
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
        String merchantId = getString("MerchantNumber", null);
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

    private static final int READ_SUCCESS = 99; // internal status if ParseFile() succeeds
    private static final int STATUS_INVALID_UPDATE_FILE = 7;
    private int parseFile(String fileName) {
        String dataFilePath = context.getFilesDir() + File.separator;
        String filePath = dataFilePath + fileName;
        Properties properties = new Properties();

        BufferedReader reader = null;
        try {
            if (new File(filePath).exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName), StandardCharsets.UTF_8));
            }
            properties = loadPropertiesFile(reader);
            String temp = properties.getProperty("MerchantNumber");
            String merchantNumber = temp .trim().replaceAll("^[\"]|[\"]$", "");
            putString("MerchantNumber", merchantNumber);
        } catch (IOException err) {
            Log.d(TAG, "Error: " + err.getMessage());
            return STATUS_INVALID_UPDATE_FILE;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        }

        return READ_SUCCESS;
    }

    private Properties loadPropertiesFile(BufferedReader reader){
        Properties properties = new Properties();
        String str;
        try {
            while ((str = reader.readLine()) != null) {
                String key = getAsciiString(str.split("=", 2)[0]);
                String value = getAsciiString(str.split("=", 2)[1]);

                properties.setProperty(key, value);
            }
        } catch (IOException e) {
            Log.d(TAG, "error: " + e.getMessage() );
        }
        return properties;
    }

    private String getAsciiString(String str) {
        //Only allow valid ascii strings
        return str.replaceAll( "\\P{ASCII}", "");
    }
}
