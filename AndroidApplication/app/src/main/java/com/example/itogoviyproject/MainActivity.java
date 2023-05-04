package com.example.itogoviyproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private ImageButton currentPaint;
    private PaintView paintView;

    @SuppressLint({"UseCompatLoadingForDrawables", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        paintView = findViewById(R.id.paint_view);
        LinearLayout paintLayout = findViewById(R.id.layout_paint_colors);
        currentPaint = (ImageButton)paintLayout.getChildAt(0);
        currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void paintClicked(View view){
        if(view!= currentPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            paintView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currentPaint =(ImageButton)view;
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
}

//        switch(item.getItemId()) {
//            case R.id.normal:
//                paintView.normal();
//                return true;
//            case R.id.emboss:
//                paintView.emboss();
//                return true;
//            case R.id.blur:
//                paintView.blur();
//                return true;
//            case R.id.clear:
//                paintView.clear();
//                return true;
//            case R.id.size_small:
//                paintView.size_small();
//                return true;
//            case R.id.size_normal:
//                paintView.size_normal();
//                return true;
//            case R.id.size_big:
//                paintView.size_big();
//                return true;
//            case R.id.color_green:
//                paintView.color_green();
//                return true;
//            case R.id.color_red:
//                paintView.color_red();
//                return true;
//            case R.id.color_black:
//                paintView.color_black();
//                return true;
//            case R.id.color_yellow:
//                paintView.color_yellow();
//                return true;
//            case R.id.color_blue:
//                paintView.color_blue();
//                return true;
//            case R.id.color_pink:
//                paintView.color_pink();
//                return true;
//
//        }
