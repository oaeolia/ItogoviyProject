package com.nikol.sketchit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.nikol.sketchit.databinding.ActivityPrivateRoomMenuBinding;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;
import com.nikol.sketchit.server.ServerCallback;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PrivateRoomMenuActivity extends AppCompatActivity {
    private ActivityPrivateRoomMenuBinding binding;
    private Server server;
    private ILogger logger;
    private int roomId;
    private Timer updateTimer;
    private Thread waitingRoomThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivateRoomMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Application application = (Application) getApplication();
        server = application.getServer();
        logger = application.getLogger();

        binding.layoutSelectActionType.setVisibility(View.VISIBLE);

        binding.buttonJoinPrivateGame.setOnClickListener(view -> {
            binding.layoutSelectActionType.setVisibility(View.INVISIBLE);
            binding.layoutJoinPrivateGame.setVisibility(View.VISIBLE);
        });
        binding.buttonCreatePrivateGame.setOnClickListener(view -> startPrivateGame());

        binding.buttonJoin.setOnClickListener(view -> joinByToken(binding.textGameTokenInput.getText().toString().trim()));
        binding.buttonStartGame.setOnClickListener(view -> startGame(roomId));
        binding.buttonBack.setOnClickListener(view -> {
            Intent intent = new Intent(PrivateRoomMenuActivity.this, GameMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    private void startGame(int roomId) {
        server.startPrivateGame(roomId, new ServerCallback<String, Boolean, Object>() {
            @Override
            public void onDataReady(String message, Boolean status, Object arg) {
                if (!status) {
                    logger.logWarming("PrivateGameMenu", "Can`t start game: " + message);
                    startGame(roomId);
                }
            }
        }, new ServerCallback<String, Integer, Object>() {
            @Override
            public void onDataReady(String message, Integer errorCode, Object arg) {
                logger.logWarming("PrivateGameMenu", "Can`t start game: " + message);
                startGame(roomId);
            }
        });

        logger.logInfo("PrivateGameMenu", "Start game. Room id: " + roomId);
        startWaitingRoom(roomId);
    }


    private void startPrivateGame() {
        binding.layoutSelectActionType.setVisibility(View.INVISIBLE);
        binding.layoutLoad.setVisibility(View.VISIBLE);
        server.createPrivateGame(new ServerCallback<Integer, String, Boolean>() {
            @Override
            public void onDataReady(Integer roomId, String message, Boolean status) {
                if (!status) {
                    logger.logError("PrivateGameMenu", "Can`t create private game: " + message);
                    startPrivateGame();
                }

                logger.logInfo("PrivateGameMenu", "Create private game. Room id: " + roomId);

                binding.textGameToken.setText(message);
                binding.layoutLoad.setVisibility(View.INVISIBLE);
                binding.layoutPrivateGameManage.setVisibility(View.VISIBLE);
                PrivateRoomMenuActivity.this.roomId = roomId;
                startUpdates();
            }
        }, new ServerCallback<String, Integer, Object>() {
            @Override
            public void onDataReady(String message, Integer errorCode, Object arg) {
                logger.logError("PrivateGameMenu", "Can`t create private game: " + message);
                startPrivateGame();
            }
        });
    }

    private void startUpdates() {
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                server.getUsersPrivateGame(roomId, new ServerCallback<List<String>, String, Boolean>() {
                    @Override
                    public void onDataReady(List<String> users, String message, Boolean status) {
                        if (!status) {
                            logger.logWarming("PrivateGameMenu", "Can`t get users: " + message);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(PrivateRoomMenuActivity.this, android.R.layout.simple_list_item_1, users);
                        binding.listUsers.setAdapter(adapter);
                    }
                }, new ServerCallback<String, Integer, Object>() {
                    @Override
                    public void onDataReady(String message, Integer errorCode, Object arg) {
                        logger.logWarming("PrivateGameMenu", "Can`t get users: " + message);
                    }
                });
            }
        }, 0, 1000);
    }

    private void joinByToken(String token) {
        if (token.length() != Application.PRIVATE_GAME_TOKEN_LEN) {
            Toast.makeText(this, R.string.message_invalid_private_game_token, Toast.LENGTH_SHORT).show();
            return;
        }

        server.getPrivateRoom(token, new ServerCallback<Integer, String, Boolean>() {
            @Override
            public void onDataReady(Integer roomId, String message, Boolean status) {
                if (!status) {
                    logger.logError("PrivateGameMenu", "Can`t join private game: " + message);
                    Toast.makeText(PrivateRoomMenuActivity.this, R.string.message_invalid_private_game_token, Toast.LENGTH_SHORT).show();
                    return;
                }

                binding.layoutLoad.setVisibility(View.VISIBLE);
                binding.layoutJoinPrivateGame.setVisibility(View.INVISIBLE);
                logger.logInfo("PrivateGameMenu", "Join private game by token. Room id: " + roomId);
                startWaitingRoom(roomId);
            }
        }, new ServerCallback<String, Integer, Object>() {
            @Override
            public void onDataReady(String message, Integer errorCode, Object arg) {
                logger.logError("PrivateGameMenu", "Can`t join private game: " + message);
                joinByToken(token);
            }
        });
    }

    private void startWaitingRoom(int roomId) {
        binding.layoutLoad.setVisibility(View.VISIBLE);
        binding.layoutPrivateGameManage.setVisibility(View.INVISIBLE);
        binding.layoutJoinPrivateGame.setVisibility(View.INVISIBLE);
        binding.layoutSelectActionType.setVisibility(View.INVISIBLE);
        logger.logInfo("PrivateGameMenu", "Start waiting room");

        if (waitingRoomThread != null) {
            waitingRoomThread.interrupt();
        }
        waitingRoomThread = new Thread() {
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
                        public void onDataReady(String message, Boolean status, Object arg) {
                            logger.logInfo("PrivateGameMenu", "Game status: " + message);
                            if (message.equals("STARTED") && isWorking) {
                                interrupt();
                                logger.logInfo("PrivateGameMenu", "Game started");
                                Intent intent = new Intent(PrivateRoomMenuActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(MainActivity.ROOM_ID_INTENT_KEY, roomId);
                                isWorking = false;
                                startActivity(intent);
                                finish();
                                logger.logInfo("PrivateGameMenu", "Finished");
                                interrupt();
                            }
                        }
                    }, new ServerCallback<String, Integer, Object>() {
                        @Override
                        public void onDataReady(String message, Integer errorCode, Object arg) {
                            logger.logInfo("PrivateGameMenu", "Can`t check room: " + message);
                        }
                    });
                }
            }
        };
        waitingRoomThread.start();
    }
}