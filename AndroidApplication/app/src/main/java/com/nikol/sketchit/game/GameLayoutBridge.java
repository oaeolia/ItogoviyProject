package com.nikol.sketchit.game;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.nikol.sketchit.ChatLayoutAdapter;
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
        mainActivity.setEnableDraw(false);
    }

    public void setPaintState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.layoutTools.setVisibility(View.VISIBLE);
        binding.layoutPaintColors.setVisibility(View.VISIBLE);
        mainActivity.setEnableDraw(true);
    }

    public void setWatchState() {
        binding.progressBarLayout.setVisibility(View.INVISIBLE);
        binding.layoutTools.setVisibility(View.INVISIBLE);
        binding.layoutPaintColors.setVisibility(View.INVISIBLE);
        mainActivity.setEnableDraw(false);
    }

    public void updateChat(List<String> message) {
        chatLayoutAdapter.getUpdates(message);
    }

    public Drawable getCanvasImage() {
        return null;
    }
}
