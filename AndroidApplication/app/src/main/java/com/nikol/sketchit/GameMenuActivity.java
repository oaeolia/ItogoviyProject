package com.nikol.sketchit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.nikol.sketchit.databinding.ActivityGameMenuBinding;
import com.nikol.sketchit.server.ServerCallback;

public class GameMenuActivity extends AppCompatActivity {
    private ActivityGameMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonLogout.setOnClickListener(view -> {
            ((Application) getApplication()).getServer().logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.buttonRoomFindStart.setOnClickListener(view -> {
            Application application = (Application) getApplication();
            application.getServer().startFindRoom(new ServerCallback<Integer, String, Object>() {
                @Override
                public void onDataReady(Integer arg1, String arg2, Object arg3) {
                    if (arg1 == -1) {
                        application.getLogger().logDebug("Test", arg2);
                    } else {
                        application.getLogger().logDebug("Test", String.valueOf(arg1));
                        binding.layoutMainScreen.setVisibility(View.INVISIBLE);
                        binding.layoutLoadScreen.setVisibility(View.VISIBLE);
                        startWaitingRoom(arg1);
                    }
                }
            }, new ServerCallback<String, Integer, Object>() {
                @Override
                public void onDataReady(String arg1, Integer arg2, Object arg3) {
                    application.getLogger().logDebug("Test", arg1);
                }
            });
        });
    }


    private void startWaitingRoom(int roomId) {
        Thread thread = new Thread() {
            private boolean isWorking = true;

            @Override
            public void run() {
                while (isWorking) {
                    try {
                        //noinspection BusyWait
                        sleep(1000);
                    } catch (InterruptedException e) {
                        continue;
                    }

                    Application application = (Application) getApplication();
                    application.getServer().checkRoom(roomId, new ServerCallback<String, Boolean, Object>() {
                        @Override
                        public void onDataReady(String message, Boolean status, Object arg3) {
                            application.getLogger().logDebug("Test", message);
                            if (message.equals("STARTED")) {
                                application.getLogger().logDebug("Test", "STARTED");
                                interrupt();
                                Intent intent = new Intent(GameMenuActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("roomId", roomId);
                                startActivity(intent);
                                finish();
                                interrupt();
                                isWorking = false;
                            }
                        }
                    }, new ServerCallback<String, Integer, Object>() {
                        @Override
                        public void onDataReady(String arg1, Integer arg2, Object arg3) {
                            application.getLogger().logDebug("Test", arg1);
                        }
                    });
                }
            }
        };
        thread.start();
    }
}