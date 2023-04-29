package com.example.itogoviyproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private PaintView paintView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        paintView = findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
    }

    // TODO: Remove (only for test!!!)
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.normal:
                paintView.normal();
                return true;
            case R.id.emboss:
                paintView.emboss();
                return true;
            case R.id.blur:
                paintView.blur();
                return true;
            case R.id.clear:
                paintView.clear();
                return true;
            case R.id.size_small:
                paintView.size_small();
                return true;
            case R.id.size_normal:
                paintView.size_normal();
                return true;
            case R.id.size_big:
                paintView.size_big();
                return true;
            case R.id.color_green:
                paintView.color_green();
                return true;
            case R.id.color_red:
                paintView.color_red();
                return true;
            case R.id.color_black:
                paintView.color_black();
                return true;
            case R.id.color_yellow:
                paintView.color_yellow();
                return true;
            case R.id.color_blue:
                paintView.color_blue();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}