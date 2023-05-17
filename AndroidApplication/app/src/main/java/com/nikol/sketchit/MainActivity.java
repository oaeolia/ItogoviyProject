package com.nikol.sketchit;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.nikol.sketchit.databinding.ActivityMainBinding;
import com.nikol.sketchit.game.GameController;
import com.nikol.sketchit.game.GameLayoutBridge;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ROOM_ID_INTENT_KEY = "roomId";

    private ImageButton currentPaint, drawBtn, eraseBtn;
    private PaintView paintView;
    private int smallBrush;
    private float mediumBrush;
    private float largeBrush;
    private ActivityMainBinding binding;
    private Timer serverUpdateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // For game system
//        TODO: Remake (this is only for test)
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        if (intent.hasExtra("roomId")) {
            createGameController(binding, intent.getIntExtra("roomId", -1));
        }


        paintView = findViewById(R.id.paint_view);
        LinearLayout paintLayout = findViewById(R.id.layout_paint_colors);
        currentPaint = (ImageButton) paintLayout.getChildAt(0);
        currentPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint_pressed));

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        drawBtn = findViewById(R.id.button_draw);
        eraseBtn = findViewById(R.id.button_erase);
        drawBtn.setOnClickListener(this);
        eraseBtn.setOnClickListener(this);
        paintView.setBrushSize(mediumBrush);
    }

    public void paintClicked(View view) {
        if (view != currentPaint) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            paintView.setColor(color);
            imgView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint_pressed));
            currentPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.paint));
            currentPaint = (ImageButton) view;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_draw) {
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setContentView(R.layout.dialog_brushchooser);
            ImageButton smallBtn = brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(v -> {
                paintView.setBrushSize(smallBrush);
                paintView.setLastBrushSize(smallBrush);
                brushDialog.dismiss();
            });
            ImageButton mediumBtn = brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(v -> {
                paintView.setBrushSize(mediumBrush);
                paintView.setLastBrushSize(mediumBrush);
                brushDialog.dismiss();
            });
            ImageButton largeBtn = brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(v -> {
                paintView.setBrushSize(largeBrush);
                paintView.setLastBrushSize(largeBrush);
                brushDialog.dismiss();
            });
            brushDialog.show();
        } else if (view.getId() == R.id.button_erase) {
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setContentView(R.layout.dialog_brushchooser);
            ImageButton smallBtn = brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(v -> {
                paintView.setErase(true);
                paintView.setBrushSize(smallBrush);
                brushDialog.dismiss();
            });
            ImageButton mediumBtn = brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(v -> {
                paintView.setErase(true);
                paintView.setBrushSize(mediumBrush);
                brushDialog.dismiss();
            });
            ImageButton largeBtn = brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(v -> {
                paintView.setErase(true);
                paintView.setBrushSize(largeBrush);
                brushDialog.dismiss();
            });
            paintView.setErase(false);
            paintView.setBrushSize(paintView.getLastBrushSize());
            brushDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        serverUpdateTimer.cancel();
        serverUpdateTimer.purge();
        super.onDestroy();
    }

    // For game system
    private void createGameController(ActivityMainBinding binding, int roomId) {
        GameLayoutBridge bridge = new GameLayoutBridge(binding, this);
        GameController controller = new GameController(roomId, bridge, this);
        controller.startGame();
        serverUpdateTimer = new Timer();
        serverUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                controller.update();
            }
        }, 0, 3000);
    }

    public void setEnableDraw(boolean enable) {
        binding.paintView.setEnabledDraw(enable);
    }
}
