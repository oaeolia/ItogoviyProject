package com.nikol.sketchit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View {

    private static Paint mPaint;
    private int paintColor = 0xFF660000;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private float mX, mY;
    private Path mPath;
    private Paint canvasPaint;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private float brushSize, lastBrushSize;
    private boolean erase = false;

    //    For game system
    private boolean isDrawEnabled = true;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(paintColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(brushSize);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
    }

    public void setBrushSize(float newSize) {
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        mPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize = lastSize;
    }

    public float getLastBrushSize() {
        return lastBrushSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public void setColor(String newColor) {
        invalidate();
        paintColor = Color.parseColor(newColor);
        mPaint.setColor(paintColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, canvasPaint);
        canvas.drawPath(mPath, mPaint);
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else mPaint.setXfermode(null);
    }

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
        mX = x;
        mY = y;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(mX, mY);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(mX, mY);
                break;
            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                break;
            default:
                return true;
        }
        return false;
    }

    //    For game system
    public void setEnabledDraw(boolean isEnabled) {
        isDrawEnabled = isEnabled;
    }

    public void clear() {
        if (mCanvas != null) {
            backgroundColor = DEFAULT_BG_COLOR;
            mCanvas.drawColor(backgroundColor, PorterDuff.Mode.CLEAR);
            invalidate();
        }
    }

    public Bitmap getCanvas() {
        return mBitmap;
    }
}
