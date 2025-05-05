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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public class PasswordModule {
    static final String TAG = "UPA KIOSK";
    final String RAW_DIR = "raw";
    final String DOWNLOADED_PARAM_FILENAME = "1-DLPARAM.TXT";
    final String KIOSK_PASSWORD_KEY = "KioskPassword";
    final String MERCHANT_NUMBER_KEY = "MerchantNumber";
    final String filePath = RAW_DIR + File.separator + DOWNLOADED_PARAM_FILENAME;
    public static SharedPreferences sharedPreferences;
    Context context;
    public PasswordModule(Context context) {
        this.context = context;
        parseFile(filePath);
    }

    public String getAdminPwd() {
        String adminPwd = getString(KIOSK_PASSWORD_KEY, "");

        if (adminPwd.isEmpty()) {
            adminPwd = generateInitialPassword();
        }
        return adminPwd;
    }

    private String generateInitialPassword() {
        String result = Integer.toString(getCurrentDayOfYear() * 2);
        while (result.length() < 3) {
            result = String.format("0%s", result);
        }
        return result;
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

    public String generateAdminPwd() {
        String adminPwd = getString(KIOSK_PASSWORD_KEY, "");

        if (adminPwd.isEmpty()) {
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
            adminPwd = String.valueOf(result % 1000000);
            adminPwd = formatToFixDigitNum(adminPwd, 6);
        }
        return adminPwd;
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
        String merchantId = getString(MERCHANT_NUMBER_KEY, null);
        if (isMultiMerchantSupported) {
            merchantId = merchantIdSplitVal.get(multiMerchantIndex);
        } else {
            if (merchantIdSplitVal != null && !merchantIdSplitVal.isEmpty()) {
                merchantId = merchantIdSplitVal.get(0);
            }
        }
        return merchantId;
    }

    public static String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void putString(String key, String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
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

            String tempProperty = properties.getProperty(MERCHANT_NUMBER_KEY);
            if (tempProperty != null) {
                String merchantNumber = properties.getProperty(MERCHANT_NUMBER_KEY).trim().replaceAll("^[\"]|[\"]$", "");
                if (!merchantNumber.isEmpty()) {
                    putString(MERCHANT_NUMBER_KEY, merchantNumber);
                }
            }

            tempProperty = properties.getProperty(KIOSK_PASSWORD_KEY);
            if (tempProperty != null) {
                String kioskPassword = properties.getProperty(KIOSK_PASSWORD_KEY, "").trim().replaceAll("^[\"]|[\"]$", "");
                if (!kioskPassword.isEmpty()) {
                    putString(KIOSK_PASSWORD_KEY, kioskPassword);
                }
            }

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

