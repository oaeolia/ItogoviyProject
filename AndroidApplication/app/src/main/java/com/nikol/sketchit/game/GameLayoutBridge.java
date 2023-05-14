package com.nikol.sketchit.game;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.nikol.sketchit.Application;
import com.nikol.sketchit.ChatLayoutAdapter;
import com.nikol.sketchit.GameMenuActivity;
import com.nikol.sketchit.MainActivity;
import com.nikol.sketchit.databinding.ActivityMainBinding;

import java.util.List;

public class GameLayoutBridge {
    private final ActivityMainBinding binding;
    private final MainActivity mainActivity;

    private final ChatLayoutAdapter chatLayoutAdapter;

    public GameLayoutBridge(ActivityMainBinding binding, MainActivity mainActivity) {
        this.binding = binding;
        this.mainActivity = mainActivity;

        chatLayoutAdapter = new ChatLayoutAdapter();
        binding.layoutChat.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(mainActivity));
        binding.layoutChat.setAdapter(chatLayoutAdapter);
        binding.layoutChat.setPaintView(binding.paintView);
    }

    public void setLoadState() {
        binding.progressBarLayout.setVisibility(View.VISIBLE);
        binding.layoutTools.setVisibility(View.INVISIBLE);
        binding.layoutPaintColors.setVisibility(View.INVISIBLE);
        binding.buttonEnterVariant.setVisibility(View.INVISIBLE);
        binding.paintView.clear();
        mainActivity.setEnableDraw(false);
    }

    public void setPaintState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.layoutTools.setVisibility(View.VISIBLE);
        binding.layoutPaintColors.setVisibility(View.VISIBLE);
        binding.buttonEnterVariant.setVisibility(View.INVISIBLE);
        binding.inputVariant.setVisibility(View.INVISIBLE);
        binding.paintView.clear();
        mainActivity.setEnableDraw(true);
    }

    public void setWatchState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.layoutTools.setVisibility(View.INVISIBLE);
        binding.layoutPaintColors.setVisibility(View.INVISIBLE);
        binding.buttonEnterVariant.setVisibility(View.VISIBLE);
        binding.inputVariant.setVisibility(View.VISIBLE);
        binding.paintView.clear();
        mainActivity.setEnableDraw(false);
    }

    public void updateChat(List<String> message) {
        chatLayoutAdapter.getUpdates(message);
    }

    public void setEnterButtonOnClickListener(View.OnClickListener listener) {
        binding.buttonEnterVariant.setOnClickListener(listener);
    }

    public boolean isVariantEmpty() {
        return binding.inputVariant.getText().toString().isEmpty();
    }

    public String getVariant() {
        return binding.inputVariant.getText().toString();
    }

    public Drawable getCanvasImage() {
        return null;
    }

    public Application getApplication() {
        return (Application) mainActivity.getApplication();
    }

    public void endGame() {
        Intent intent = new Intent(mainActivity, GameMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainActivity.startActivity(intent);
        mainActivity.finish();
    }

    public Context getContext() {
        return binding.getRoot().getContext();
    }
}
