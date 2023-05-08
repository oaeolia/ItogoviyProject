package com.nikol.sketchit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatRecycleView extends RecyclerView {
    private PaintView paintView;


    public ChatRecycleView(@NonNull Context context) {
        super(context);
    }


    public ChatRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPaintView(PaintView paintView) {
        this.paintView = paintView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (paintView != null) {
            if (paintView.draw(e.getX() + getX(), e.getY() + getY(), e.getAction())) {
                return false;
            }
            paintView.invalidate();
        }

        return super.onTouchEvent(e);
    }
}
