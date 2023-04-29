package com.example.itogoviyproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.itogoviyproject.databinding.ActivityLoginBinding;
import com.example.itogoviyproject.server.ServerCallback;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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
                binding.buttonLogin.setEnabled(binding.buttonLogin.getText().length() > 0 && binding.inputPassword.getText().length() > 0);
            }
        };

        binding.inputLogin.addTextChangedListener(userInputTextWatcher);
        binding.inputPassword.addTextChangedListener(userInputTextWatcher);

        binding.buttonLogin.setOnClickListener(view -> {
            binding.buttonLogin.setEnabled(false);
            binding.textErrorMessage.setText("");
            ((Application) getApplication()).getServer().login(binding.inputLogin.getText().toString(), binding.inputPassword.getText().toString(),
                    new ServerCallback<Boolean, String, Object>() {
                        @Override
                        public void onDataReady(Boolean arg1, String arg2, Object arg3) {
                            if (arg1) {
                                ((Application) getApplication()).getLogger().logDebug("Login", "Successfully logged in");
                            } else {
                                ((Application) getApplication()).getLogger().logDebug("Login", "Cant logged in");
                                ((Application) getApplication()).getLogger().logDebug("Login", arg2);
                                binding.textErrorMessage.setText(R.string.message_error_login_invalid_user_data);
                            }
                        }
                    }, new ServerCallback<String, Integer, Object>() {
                        @Override
                        public void onDataReady(String arg1, Integer arg2, Object arg3) {
                            binding.textErrorMessage.setText(R.string.message_error_cannt_send_request);
                            ((Application) getApplication()).getLogger().logError("Login", "Cant logged in (" + arg1 + ")");
                        }
                    });
        });
        binding.buttonGoRegistration.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}