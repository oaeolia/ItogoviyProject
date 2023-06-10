package com.nikol.sketchit.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.nikol.sketchit.Application;
import com.nikol.sketchit.ChatLayoutAdapter;
import com.nikol.sketchit.GameMenuActivity;
import com.nikol.sketchit.GameRoundEndMessageFragment;
import com.nikol.sketchit.MainActivity;
import com.nikol.sketchit.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Locale;

public class GameLayoutBridge {
    private final ActivityMainBinding binding;
    private final MainActivity mainActivity;
    private final ChatLayoutAdapter chatLayoutAdapter;
    private final ChatLayoutManager chatLayoutManager;

    private boolean isWaitingState;
    private long roundTime = -1;
    private GameRoundEndMessageFragment nowMessageFragment = null;


    public GameLayoutBridge(ActivityMainBinding binding, MainActivity mainActivity) {
        this.binding = binding;
        this.mainActivity = mainActivity;

        chatLayoutAdapter = new ChatLayoutAdapter();
        chatLayoutManager = new ChatLayoutManager(mainActivity);
        binding.layoutChat.setLayoutManager(chatLayoutManager);
        binding.layoutChat.setAdapter(chatLayoutAdapter);
        binding.layoutChat.setPaintView(binding.paintView);

        chatLayoutManager.setScrollEnabled(true);
        chatLayoutManager.scrollToPosition(chatLayoutAdapter.getItemCount() - 1);
        chatLayoutManager.setScrollEnabled(false);
    }

    public void setLoadState() {
        binding.progressBarLayout.setVisibility(View.VISIBLE);
        binding.buttonDraw.setVisibility(View.INVISIBLE);
        binding.buttonErase.setVisibility(View.INVISIBLE);
        binding.layoutPaintColors.setVisibility(View.INVISIBLE);
        binding.buttonEnterVariant.setVisibility(View.INVISIBLE);
        binding.imageCanvas.setVisibility(View.INVISIBLE);
        binding.textTime.setVisibility(View.INVISIBLE);
        binding.paintView.setVisibility(View.VISIBLE);
        binding.textWord.setText("");
        binding.paintView.clear();
        mainActivity.setEnableDraw(false);
        if (nowMessageFragment != null) {
            nowMessageFragment.dismiss();
        }
    }

    public void setPaintState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.buttonDraw.setVisibility(View.VISIBLE);
        binding.buttonErase.setVisibility(View.VISIBLE);
        binding.layoutPaintColors.setVisibility(View.VISIBLE);
        binding.buttonEnterVariant.setVisibility(View.INVISIBLE);
        binding.inputVariant.setVisibility(View.INVISIBLE);
        binding.imageCanvas.setVisibility(View.INVISIBLE);
        binding.textTime.setVisibility(View.VISIBLE);
        binding.paintView.setVisibility(View.VISIBLE);
        binding.textWord.setText("");
        binding.paintView.clear();
        mainActivity.setEnableDraw(true);
        isWaitingState = false;
        startRoundTimer();
        if (nowMessageFragment != null) {
            nowMessageFragment.dismiss();
        }
    }

    public void setWatchState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.buttonDraw.setVisibility(View.INVISIBLE);
        binding.buttonErase.setVisibility(View.INVISIBLE);
        binding.buttonEnterVariant.setVisibility(View.VISIBLE);
        binding.inputVariant.setVisibility(View.VISIBLE);
        binding.imageCanvas.setVisibility(View.VISIBLE);
        binding.textTime.setVisibility(View.VISIBLE);
        binding.paintView.setVisibility(View.INVISIBLE);
        binding.textWord.setText("");
        binding.paintView.clear();
        mainActivity.setEnableDraw(false);
        isWaitingState = false;
        startRoundTimer();
        if (nowMessageFragment != null) {
            nowMessageFragment.dismiss();
        }
    }


    public void setMessageState(String message, String rightAnswer, boolean isVariantTrue, int remainingTime) {
        ((Application) mainActivity.getApplication()).getLogger().logDebug("TEST", "setMessageState: " + message + " " + remainingTime + " " + rightAnswer);
        // TODO: Add message text
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.buttonDraw.setVisibility(View.INVISIBLE);
        binding.buttonErase.setVisibility(View.INVISIBLE);
        binding.layoutPaintColors.setVisibility(View.INVISIBLE);
        binding.buttonEnterVariant.setVisibility(View.INVISIBLE);
        binding.inputVariant.setVisibility(View.INVISIBLE);
        binding.layoutChat.setVisibility(View.VISIBLE);
        mainActivity.setEnableDraw(false);
        isWaitingState = true;
        startRoundTimer();

        if (nowMessageFragment != null) {
            nowMessageFragment.dismiss();
        }

        nowMessageFragment = new GameRoundEndMessageFragment();

        Bundle args = new Bundle();
        args.putString(GameRoundEndMessageFragment.ARG_KEY_MESSAGE, message);
        args.putString(GameRoundEndMessageFragment.ARG_KEY_RIGHT_ANSWER, rightAnswer);
        args.putBoolean(GameRoundEndMessageFragment.ARG_KEY_IS_ANSWER_RIGHT, isVariantTrue);
        args.putInt(GameRoundEndMessageFragment.ARG_KEY_REMAINING_TIME, remainingTime);
        nowMessageFragment.setArguments(args);

        nowMessageFragment.setCancelable(false);
        nowMessageFragment.show(mainActivity.getSupportFragmentManager(), "Test");
    }

    public void updateChat(List<String> message) {
        chatLayoutManager.setScrollEnabled(true);
        chatLayoutAdapter.getUpdates(message);
        chatLayoutManager.scrollToPosition(chatLayoutAdapter.getItemCount() - 1);
        chatLayoutManager.setScrollEnabled(false);
    }

    public void setEnterButtonOnClickListener(View.OnClickListener listener) {
        binding.buttonEnterVariant.setOnClickListener(listener);
    }

    public void clearVariant() {
        binding.inputVariant.setText("");
    }

    public boolean isVariantEmpty() {
        return binding.inputVariant.getText().toString().isEmpty();
    }

    public String getVariant() {
        return binding.inputVariant.getText().toString();
    }

    public Bitmap getCanvasImage() {
        return binding.paintView.getCanvas();
    }

    public void endGame() {
        Intent intent = new Intent(mainActivity, GameMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainActivity.startActivity(intent);
        mainActivity.finish();
    }

    public void updateRecycleViewPosition() {
        if (chatLayoutManager.getItemCount() <= 0) {
            return;
        }
        chatLayoutManager.setScrollEnabled(true);
        binding.layoutChat.smoothScrollToPosition(chatLayoutAdapter.getItemCount() - 1);
        chatLayoutManager.setScrollEnabled(false);
    }

    public void setCanvas(byte[] canvas) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(canvas, 0, canvas.length);
        binding.imageCanvas.setImageBitmap(bitmap);
    }

    public Context getContext() {
        return mainActivity;
    }

    public void setWord(String word) {
        binding.textWord.setText(word);
    }

    static class ChatLayoutManager extends androidx.recyclerview.widget.LinearLayoutManager {
        private boolean scrolledEnabled;

        public ChatLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(boolean enabled) {
            scrolledEnabled = enabled;
        }

        @Override
        public boolean canScrollVertically() {
            return scrolledEnabled;
        }
    }

    public void startRoundTimer() {
        roundTime = System.currentTimeMillis();
    }

    @SuppressLint("SetTextI18n")
    public void updateTime() {
        if (roundTime == -1) {
            return;
        }

        long freeTime;
        if (isWaitingState) {
            freeTime = Application.WAITING_SECONDS_TIME - (System.currentTimeMillis() - roundTime) / 1000;
        } else {
            freeTime = Application.ROUND_SECONDS_TIME - (System.currentTimeMillis() - roundTime) / 1000;
        }

        if (freeTime <= 0) {
            binding.textTime.setText("00:00");
            if (nowMessageFragment != null) {
                nowMessageFragment.updateTime("00:00");
            }
            return;
        }

        if (freeTime >= 60) {
            long minutes = freeTime / 60;
            long seconds = freeTime % 60;
            binding.textTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            if (nowMessageFragment != null) {
                nowMessageFragment.updateTime(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }
        } else {
            binding.textTime.setText(String.format(Locale.getDefault(), "00:%02d", freeTime));
            if (nowMessageFragment != null) {
                nowMessageFragment.updateTime(String.format(Locale.getDefault(), "00:%02d", freeTime));
            }
        }
    }

    public void setRemainingTime(int remainingTime) {
        roundTime = System.currentTimeMillis() - (Application.ROUND_SECONDS_TIME - remainingTime) * 1000L;
    }

    public boolean isMessageState() {
        return isWaitingState;
    }
}
