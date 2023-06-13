package com.nikol.sketchit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.nikol.sketchit.databinding.ActivityRegistrationBinding;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;
import com.nikol.sketchit.server.ServerCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс активности регистрации. Отвечает за процесс регистрации.
 */
public class RegistrationActivity extends AppCompatActivity {
    private ActivityRegistrationBinding binding;
    private ILogger logger;
    private Server server;
    private boolean waitingResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Application application = (Application) getApplication();
        logger = application.getLogger();
        server = application.getServer();

        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextWatcher userInputTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int buffer = checkUserInput();
                binding.buttonRegistration.setEnabled(buffer == -1 && !waitingResponse);
                if (buffer != -1) {
                    binding.textMessage.setText(buffer);
                } else {
                    binding.textMessage.setText("");
                }
            }
        };

        binding.inputUsername.addTextChangedListener(userInputTextWatcher);
        binding.inputEmail.addTextChangedListener(userInputTextWatcher);
        binding.inputPassword.addTextChangedListener(userInputTextWatcher);

        binding.buttonRegistration.setOnClickListener(this::onClickRegister);
        binding.buttonGoLogin.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    public void onClickRegister(View view) {
        binding.buttonRegistration.setEnabled(false);
        binding.textMessage.setText("");

        binding.inputUsername.setClickable(false);
        binding.inputEmail.setClickable(false);
        binding.inputPassword.setClickable(false);

        waitingResponse = true;
        server.registration(binding.inputUsername.getText().toString(), binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString(),
                new ServerCallback<Boolean, String, Object>() {
                    @Override
                    public void onDataReady(Boolean successfullyRegistered, String message, Object arg) {
                        if (successfullyRegistered) {
                            logger.logInfo("Registration", "Successfully registered");
                            server.login(binding.inputUsername.getText().toString(), binding.inputPassword.getText().toString(), new ServerCallback<Boolean, String, Object>() {
                                @Override
                                public void onDataReady(Boolean isAuthed, String message, Object arg) {
                                    if (isAuthed) {
                                        logger.logInfo("Login", "Successfully logged in");
                                        Intent intent = new Intent(RegistrationActivity.this, GameMenuActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        logger.logWarming("Login", "Cant logged in, error " + message);

                                        waitingResponse = false;
                                        binding.inputUsername.setClickable(true);
                                        binding.inputEmail.setClickable(true);
                                        binding.inputPassword.setClickable(true);

                                        binding.textMessage.setText(R.string.message_error_cant_login);
                                    }
                                }
                            }, new ServerCallback<String, Integer, Object>() {
                                @Override
                                public void onDataReady(String message, Integer errorCode, Object arg) {
                                    logger.logError("Registration", "Can`t login, server error: " + message);

                                    binding.textMessage.setText(R.string.message_error_cant_login);

                                    waitingResponse = false;
                                    binding.inputUsername.setClickable(true);
                                    binding.inputEmail.setClickable(true);
                                    binding.inputPassword.setClickable(true);
                                }
                            });
                        } else {
                            logger.logError("Registration", "Can`t register, server error: " + message);
                            binding.textMessage.setText(R.string.message_error_user_name_or_emeail_is_not_free);
                        }
                        waitingResponse = false;

                        binding.inputUsername.setClickable(true);
                        binding.inputEmail.setClickable(true);
                        binding.inputPassword.setClickable(true);
                    }
                }, new ServerCallback<String, Integer, Object>() {
                    @Override
                    public void onDataReady(String message, Integer errorCode, Object arg3) {
                        binding.textMessage.setText(R.string.message_error_cannt_send_request);
                        logger.logError("Registration", "Cant register (" + message + ")");
                        waitingResponse = false;

                        binding.inputUsername.setClickable(true);
                        binding.inputEmail.setClickable(true);
                        binding.inputPassword.setClickable(true);
                    }
                });
    }

    private int checkUserInput() {
        int buffer = checkUsername(binding.inputUsername.getText().toString());
        if (buffer != -1) {
            return buffer;
        }

        buffer = checkPassword(binding.inputPassword.getText().toString());
        if (buffer != -1) {
            return buffer;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText()).matches()) {
            return R.string.message_error_register_email_is_now_valid;
        }

        return -1;
    }

    private int checkUsername(String username) {
        if (username.length() < 4 || username.length() > 20) {
            return R.string.message_error_register_username_not_valid;
        }

        Pattern pattern = Pattern.compile("^\\w+$");
        return pattern.matcher(username).matches() ? -1 : R.string.message_error_register_username_not_valid;
    }

    private int checkPassword(String password) {
        if (password.length() < 7) {
            return R.string.message_error_register_password_is_short;
        }
        if (password.length() > 20) {
            return R.string.message_error_register_password_is_long;
        }

        if (password.contains(" ")) {
            return R.string.message_error_register_password_have_spaces;
        }

        Pattern pattern;
        Matcher matcher;
        String passwordPattern = "^.*\\W+.*$";
        pattern = Pattern.compile(passwordPattern);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            return R.string.message_error_register_password_need_spacial_symbols;
        }

        passwordPattern = "^.*\\d+.*$";
        pattern = Pattern.compile(passwordPattern);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            return R.string.message_error_register_password_need_number;
        }

        passwordPattern = "^.*[^\\W1234567890_]+.*$";
        pattern = Pattern.compile(passwordPattern);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            return R.string.message_error_register_password_need_char;
        }

        return -1;
    }
}