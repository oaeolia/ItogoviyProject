package com.nikol.sketchit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nikol.sketchit.databinding.ActivityGameMenuBinding;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;
import com.nikol.sketchit.server.ServerCallback;

/**
 * Класс активности игрового меню. Отвечает за запуск поиска публичной игры или переход в меню приватной игры.
 */
public class GameMenuActivity extends AppCompatActivity {
    private ActivityGameMenuBinding binding;
    private Server server;
    private ILogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Application application = (Application) getApplication();
        server = application.getServer();
        logger = application.getLogger();

        super.onCreate(savedInstanceState);

        binding = ActivityGameMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonLogout.setOnClickListener(view -> {
            server.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.buttonPrivateRoom.setOnClickListener(this::openPrivateGame);

        binding.buttonRoomFindStart.setOnClickListener(view -> server.startFindRoom(new ServerCallback<Integer, String, Object>() {
            @Override
            public void onDataReady(Integer roomId, String message, Object arg) {
                if (roomId == -1) {
                    logger.logWarming("GameMenu", "Can`t start find room " + message);
                } else {
                    logger.logInfo("GameMenu", "Start waiting room: " + roomId);
                    binding.layoutMainScreen.setVisibility(View.INVISIBLE);
                    binding.layoutLoadScreen.setVisibility(View.VISIBLE);
                    startWaitingRoom(roomId);
                }
            }
        }, new ServerCallback<String, Integer, Object>() {
            @Override
            public void onDataReady(String message, Integer errorCode, Object arg) {
                logger.logError("GameMenu", "Can`t waiting room: " + message);
            }
        }));

        binding.buttonRules.setOnClickListener(this::showRules);
    }

    private void showRules(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_rules);
        builder.setMessage(R.string.message_rules);

        builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openPrivateGame(View view) {
        Intent intent = new Intent(this, PrivateRoomMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

                    server.checkRoom(roomId, new ServerCallback<String, Boolean, Object>() {
                        @Override
                        public void onDataReady(String message, Boolean status, Object arg3) {
                            logger.logInfo("GameMenu", "Game status: " + message);
                            if (message.equals("STARTED")) {
                                interrupt();
                                Intent intent = new Intent(GameMenuActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(MainActivity.ROOM_ID_INTENT_KEY, roomId);
                                startActivity(intent);
                                finish();
                                interrupt();
                                isWorking = false;
                            }
                        }
                    }, new ServerCallback<String, Integer, Object>() {
                        @Override
                        public void onDataReady(String message, Integer errorCode, Object arg) {
                            logger.logInfo("GameMenu", "Can`t check room: " + message);
                        }
                    });
                }
            }
        };
        thread.start();
    }
}