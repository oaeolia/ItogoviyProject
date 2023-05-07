package com.example.itogoviyproject.game;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.example.itogoviyproject.MainActivity;
import com.example.itogoviyproject.databinding.ActivityMainBinding;

public class GameLayoutBridge {
    private final ActivityMainBinding binding;
    private final MainActivity mainActivity;

    public GameLayoutBridge(ActivityMainBinding binding, MainActivity mainActivity) {
        this.binding = binding;
        this.mainActivity = mainActivity;
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

    public Drawable getCanvasImage() {
        return null;
    }
}
