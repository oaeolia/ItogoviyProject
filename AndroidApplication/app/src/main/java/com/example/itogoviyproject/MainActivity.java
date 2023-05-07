package com.example.itogoviyproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.app.Dialog;
import android.view.View.OnClickListener;

import com.example.itogoviyproject.databinding.ActivityMainBinding;
import com.example.itogoviyproject.game.GameController;
import com.example.itogoviyproject.game.GameLayoutBridge;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton currentPaint, drawBtn, eraseBtn;
    private PaintView paintView;
    private int smallBrush;
    private float mediumBrush;
    private float largeBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // For game system
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        if (intent.hasExtra("roomId")) {
            createGameController(binding, intent.getIntExtra("roomId", 0));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.test_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.test_application_logout) {
            ((Application) getApplication()).getServer().logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_draw) {
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setContentView(R.layout.dialog_brushchooser);
            ImageButton smallBtn = brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintView.setBrushSize(smallBrush);
                    paintView.setLastBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintView.setBrushSize(mediumBrush);
                    paintView.setLastBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintView.setBrushSize(largeBrush);
                    paintView.setLastBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        } else if (view.getId() == R.id.button_erase) {
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setContentView(R.layout.dialog_brushchooser);
            ImageButton smallBtn = brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintView.setErase(true);
                    paintView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintView.setErase(true);
                    paintView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintView.setErase(true);
                    paintView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            paintView.setErase(false);
            paintView.setBrushSize(paintView.getLastBrushSize());
            brushDialog.show();
        }
    }

    // For game system
    private void createGameController(ActivityMainBinding binding, int roomId) {
        GameLayoutBridge bridge = new GameLayoutBridge(binding);
        GameController controller = new GameController(roomId, bridge, this);
        controller.startGame();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                controller.update();
            }
        }, 0, 1000);
    }
}
