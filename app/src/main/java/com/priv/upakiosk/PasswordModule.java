package com.priv.upakiosk;

import static com.priv.upakiosk.MainActivity.KIOSK_PASSWORD_KEY;
import static com.priv.upakiosk.MainActivity.sharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class PasswordModule {
    static final String TAG = "UPA KIOSK";
    final String DEFAULT_PASSWORD_PREFIX = "UPA@Kiosk";
    final String MERCHANT_NUMBER_KEY = "MerchantNumber";

    Context context;
    public PasswordModule(Context context) {
        this.context = context;
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
        return DEFAULT_PASSWORD_PREFIX + result;
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



    public static Boolean getBoolean(String key, Boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
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

