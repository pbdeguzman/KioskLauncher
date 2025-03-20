package com.priv.upakiosk;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class PasswordActivity extends AppCompatActivity {

    EditText etPassword;
    ImageButton btnClose;

    int timeout = 30000;
    Handler handler = new Handler();
    Runnable runnable;
    PasswordModule passwordManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        etPassword = findViewById(R.id.etPassword);
        btnClose = findViewById(R.id.btnClose);

        etPassword.setFocusable(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkPassword(etPassword.getText().toString());
                return true;
            }
            return false;
        });

        etPassword.addTextChangedListener(textWatcher);

        btnClose.setOnClickListener(v-> {
            setResult(RESULT_CANCELED);
            this.finish();
        });

        initPasswordManager();

        initScreenTimeOutTimer();
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            startScreenTimeOutTimer();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private void checkPassword(String password) {
        //String pass = "aaaa";
        String pass = passwordManager.getAdminPwd();
        if (password.equals(pass)) {
            setResult(RESULT_OK);
            this.finish();
        } else {
            stopScreenTimeOutTimer();
            showDialogBox("Invalid Password\nPlease Re-Enter");
        }
    }

    private void showDialogBox(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("Error");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.exclamation);

        builder.setPositiveButton("OK", (dialog, which) -> {
            startScreenTimeOutTimer();
            etPassword.setText("");
            etPassword.setFocusable(true);
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showTimeoutDialogBox(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("Error");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.exclamation);

        builder.setPositiveButton("OK", (dialog, which) -> {
            this.finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void initPasswordManager() {
        passwordManager = new PasswordModule(this);
        passwordManager.putString("MerchantNumber","777701451201");
    }

    private void initScreenTimeOutTimer() {
        runnable = () -> {
            showTimeoutDialogBox("Timeout");
            stopScreenTimeOutTimer();
        };
        startScreenTimeOutTimer();
    }

    private void startScreenTimeOutTimer() {
        if (handler != null && runnable != null) {
            Log.d("KIOSK", "Start timeout timer");
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, timeout);
        }
    }

    public void stopScreenTimeOutTimer() {
        if (handler != null && runnable != null) {
            Log.d("KIOSK", "Stop timeout timer");
            handler.removeCallbacks(runnable);
        }
    }

    /**
     * Handles the onBackPressed event
     */
    @Override
    public void onBackPressed() {
        //disable the application's onBackPress
    }

    private void exitPasswordScreen() {
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScreenTimeOutTimer();
    }
}
