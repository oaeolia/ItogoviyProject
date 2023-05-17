package com.nikol.sketchit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.nikol.sketchit.databinding.ActivityLoginBinding;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;
import com.nikol.sketchit.server.ServerCallback;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Server server;
    private ILogger logger;
    private boolean waitingResponse;

    private static final String APPLICATION_ID_PREFERENCE_NAME = "application_id";
    private static final String APPLICATION_TOKEN_PREFERENCE_NAME = "application_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Application application = (Application) getApplication();
        server = application.getServer();
        logger = application.getLogger();

        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSharedPreferences(Application.PREFERENCES_FILE_NAME, MODE_PRIVATE).contains(APPLICATION_ID_PREFERENCE_NAME)) {
            binding.progressBarLayout.setVisibility(View.VISIBLE);
            autoLogin();
            return;
        }

        initUserInput();
    }

    private void initUserInput() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);

        TextWatcher userInputTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.buttonLogin.setEnabled(binding.buttonLogin.getText().length() > 0 && binding.inputPassword.getText().length() > 0 && !waitingResponse);
            }
        };

        binding.inputLogin.addTextChangedListener(userInputTextWatcher);
        binding.inputPassword.addTextChangedListener(userInputTextWatcher);

        binding.buttonLogin.setOnClickListener(this::onClickLogin);

        binding.buttonGoRegistration.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });

//        TODO: Remove! (only for test)
        binding.buttonTestPaint.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void onClickLogin(View view) {
        waitingResponse = true;
        binding.inputLogin.setClickable(false);
        binding.inputPassword.setClickable(false);

        binding.buttonLogin.setEnabled(false);
        binding.textErrorMessage.setText("");
        server.login(
                binding.inputLogin.getText().toString(),
                binding.inputPassword.getText().toString(),
                new LoginServerCallback(),
                new LoginServerErrorCallback());
    }

    private void autoLogin() {
        logger.logDebug("Login", "Find application id");
        SharedPreferences preferences = getSharedPreferences(Application.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        server.loginByApplicationData(preferences.getString(APPLICATION_TOKEN_PREFERENCE_NAME, ""), preferences.getInt(APPLICATION_ID_PREFERENCE_NAME, 0), new ServerCallback<Boolean, String, Object>() {
            @Override
            public void onDataReady(Boolean isLogin, String message, Object arg) {
                if (isLogin) {
                    logger.logInfo("Login", "Successfully logged in");
                    moveToGameMenu();
                } else {
                    logger.logWarming("Login", "Cant logged in, error " + message);
                    initUserInput();
                }
            }
        }, new ServerCallback<String, Integer, Object>() {
            @Override
            public void onDataReady(String message, Integer errorCode, Object arg) {
                initUserInput();
                binding.textErrorMessage.setText(R.string.message_error_cant_auto_login);
                logger.logError("Login", "Cant logged in (" + message + ")");
            }
        });
    }

    private void moveToGameMenu() {
        Intent intent = new Intent(this, GameMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private class LoginServerCallback extends ServerCallback<Boolean, String, Object> {
        @Override
        public void onDataReady(Boolean isAuthed, String message, Object arg) {
            if (isAuthed) {
                logger.logInfo("Login", "Successfully logged in");
                moveToGameMenu();
            } else {
                logger.logWarming("Login", "Cant logged in, error " + message);
                binding.textErrorMessage.setText(R.string.message_error_login_invalid_user_data);
            }

            waitingResponse = false;
            binding.inputLogin.setClickable(true);
            binding.inputPassword.setClickable(true);
        }
    }

    private class LoginServerErrorCallback extends ServerCallback<String, Integer, Object> {
        @Override
        public void onDataReady(String message, Integer errorCode, Object arg) {
            binding.textErrorMessage.setText(R.string.message_error_cannt_send_request);
            logger.logError("Login", "Cant logged in (" + message + ")");

            waitingResponse = false;
            binding.inputLogin.setClickable(true);
            binding.inputPassword.setClickable(true);
        }
    }
}