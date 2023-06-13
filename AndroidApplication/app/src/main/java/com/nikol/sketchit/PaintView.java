package com.nikol.sketchit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * View, на котором происходит рисование. Отвечает за обработку пользователского ввода и отображение результата.
 */
public class PaintView extends View {

    private static Paint paint;
    private int paintColor = 0xFF660000;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private Path path;
    private Paint canvasPaint;
    private Bitmap bitmap;
    private Canvas canvas;
    private float brushSize, lastBrushSize;

    // For game system
    private boolean isDrawEnabled = true;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        path = new Path();
        paint = new Paint();
        paint.setColor(paintColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(brushSize);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
    }

    public void setBrushSize(float newSize) {
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        paint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize = lastSize;
    }

    public float getLastBrushSize() {
        return lastBrushSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    public void setColor(String newColor) {
        invalidate();
        paintColor = Color.parseColor(newColor);
        paint.setColor(paintColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, canvasPaint);
        canvas.drawPath(path, paint);
    }

    public void setErase(boolean isErase) {
        if (isErase) paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else paint.setXfermode(null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDrawEnabled) {
            if (draw(event.getX(), event.getY(), event.getAction())) {
                return false;
            }
        }
        invalidate();
        return true;
    }

    public boolean draw(float x, float y, int action) {
        if (!isDrawEnabled) {
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path, paint);
                path.reset();
                break;
            default:
                return true;
        }
        return false;
    }

    // For game system
    public void setEnabledDraw(boolean isEnabled) {
        isDrawEnabled = isEnabled;
    }

    public void clear() {
        if (canvas != null) {
            canvas.drawColor(DEFAULT_BG_COLOR, PorterDuff.Mode.CLEAR);
            invalidate();
        }
    }

    public Bitmap getCanvas() {
        return bitmap;
    }
}
