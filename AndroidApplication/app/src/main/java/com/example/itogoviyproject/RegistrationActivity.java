package com.example.itogoviyproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.itogoviyproject.databinding.ActivityRegistrationBinding;
import com.example.itogoviyproject.server.ServerCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {
    private ActivityRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                binding.buttonRegistration.setEnabled(android.util.Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText()).matches() &&
                        checkPassword(binding.inputPassword.getText().toString()) != -1 &&
                        binding.inputUsername.getText().toString().length() > 4);
            }
        };

        binding.inputEmail.addTextChangedListener(userInputTextWatcher);
        binding.inputUsername.addTextChangedListener(userInputTextWatcher);
        binding.inputPassword.addTextChangedListener(userInputTextWatcher);

        binding.inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText()).matches())
                {
                    binding.textMessage.setText(R.string.message_error_register_email_is_now_valid);
                }else{
                    binding.textMessage.setText("");
                }
            }
        });
        binding.inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int checkResult = checkPassword(editable.toString());
                if(checkResult == -1)
                {
                    binding.textMessage.setText("");
                }else{
                    binding.textMessage.setText(checkResult);
                }
            }
        });

        binding.buttonRegistration.setOnClickListener(view -> {
            binding.buttonRegistration.setEnabled(false);
            binding.textMessage.setText("");
            ((Application) getApplication()).getServer().registration(binding.inputUsername.getText().toString(), binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString(),
                    new ServerCallback<Boolean, String, Object>() {
                        @Override
                        public void onDataReady(Boolean arg1, String arg2, Object arg3) {
                            if (arg1) {
                                ((Application) getApplication()).getLogger().logDebug("Registration", "Successfully Registration");
                            } else {
                                ((Application) getApplication()).getLogger().logDebug("Registration", "Cant register");
                                ((Application) getApplication()).getLogger().logDebug("Registration", arg2);
                                binding.textMessage.setText(R.string.message_error_user_name_or_emeail_is_not_free);
                            }
                        }
                    }, new ServerCallback<String, Integer, Object>() {
                        @Override
                        public void onDataReady(String arg1, Integer arg2, Object arg3) {
                            binding.textMessage.setText(R.string.message_error_cannt_send_request);
                            ((Application) getApplication()).getLogger().logError("Registration", "Cant Registration (" + arg1 + ")");
                        }
                    });
        });
        binding.buttonGoLogin.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
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
        String passwordPattern = "^.+\\W+.+$";
        pattern = Pattern.compile(passwordPattern);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            return R.string.message_error_register_password_need_spacial_symbols;
        }

        passwordPattern = "^.+\\d+.+$";
        pattern = Pattern.compile(passwordPattern);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            return R.string.message_error_register_password_need_number;
        }

        passwordPattern = "^.+[^\\W1234567890_]+.+$";
        pattern = Pattern.compile(passwordPattern, Pattern.UNICODE_CHARACTER_CLASS);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            return R.string.message_error_register_password_need_char;
        }

        return -1;
    }
}